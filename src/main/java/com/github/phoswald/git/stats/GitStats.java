package com.github.phoswald.git.stats;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameGenerator;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.git.reports.ReportGenerator;

public class GitStats implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Git git;
    private final Repository repo;
    private final Path repoPath;
    private final ReportGenerator reportGenerator;

    public GitStats(Path repoPath, Path outputPath) throws IOException {
        this.git = Git.open(repoPath.toFile());
        this.repo = git.getRepository();
        this.repoPath = repoPath;
        this.reportGenerator = new ReportGenerator(outputPath);
    }

    @Override
    public void close() {
        git.close();
    }

    public CommitStatistics calculateCommitStatistics(String revision) throws IOException {
        ObjectId objId = repo.resolve(revision);
        try (RevWalk walk = new RevWalk(repo)) {
            RevCommit commit = walk.parseCommit(objId);
            return new CommitStatisticsBuilder() //
                    .repo(repoPath.toString()) //
                    .revision(revision) //
                    .commit(createCommitInfo(commit)) //
                    .fileCount(getFiles(repo, commit).size()) //
                    .build();
        }
    }

    private List<String> getFiles(Repository repo, RevCommit commit) throws IOException {
        List<String> fileNames = new ArrayList<>();
        RevTree tree = commit.getTree();
        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                logger.debug("TreeWalk: hash={}, file={}", treeWalk.getObjectId(0).getName(), treeWalk.getPathString());
                fileNames.add(treeWalk.getPathString());
            }
        }
        return fileNames;
    }

    public BlameStatistics calculateBlameStatistics(String revision, Path file) throws IOException {
        ObjectId objId = repo.resolve(revision);
        try (BlameGenerator blameGenerator = new BlameGenerator(repo, file.toString())) {
            BlameResult blameResult = blameGenerator.push(null, objId).computeBlameResult();
            int lineCount = blameResult.getResultContents().size();

            // collect lines sharing the same commit
            Map<String, CommitLines> commits = new LinkedHashMap<>();
            for (int line = 0; line < lineCount; line++) {
                RevCommit commit = blameResult.getSourceCommit(line);
                commits.computeIfAbsent(commit.getName(), k -> new CommitLines(commit)).lineCount++;
            }

            // aggregate line counts by users and timestamps
            SortedMap<User, Long> lineCountByAuthor = new TreeMap<>(comparing(User::toString));
            SortedMap<LocalDate, Long> lineCountByDate = new TreeMap<>();
            for (var entry : commits.values()) {
                logger.debug("CommitLines: hash={}, lineCount={}", entry.commit.hash(), entry.lineCount);
                lineCountByAuthor.compute(entry.commit.author(), (k, v) -> entry.addLineCount(v));
                lineCountByDate.compute(entry.commit.date(), (k, v) -> entry.addLineCount(v));
            }
            return new BlameStatisticsBuilder() //
                    .repo(repoPath.toString()) //
                    .revision(revision) //
                    .file(file.toString()) //
                    .lineCount(lineCount) //
                    .lineCountByAuthor(lineCountByAuthor) //
                    .lineCountByDate(lineCountByDate) //
                    .build();
        }
    }

    private static CommitInfo createCommitInfo(RevCommit commit) {
        return new CommitInfoBuilder() //
                .hash(commit.getName()) //
                .timestamp(Instant.ofEpochSecond(commit.getCommitTime())) //
                .author(new UserBuilder() //
                        .name(commit.getAuthorIdent().getName()) //
                        .email(commit.getAuthorIdent().getEmailAddress()) //
                        .build()) //
                .message(commit.getShortMessage()) //
                .build();
    }

    public HistoryStatistics calculateHistoryStatistics(String revision) throws IOException {
        ObjectId objId = repo.resolve(revision);
        try (RevWalk walk = new RevWalk(repo)) {
            walk.markStart(walk.parseCommit(objId));
            List<CommitInfo> commits = stream(walk.spliterator(), false) //
                    .map(commit -> createCommitInfo(commit)) //
                    .sorted(comparing(CommitInfo::timestamp)) //
                    .toList();
            return new HistoryStatisticsBuilder() //
                    .commitCount(commits.size()) //
                    .commitCountByAuthor(sortMap(commits.stream() //
                            .collect(groupingBy(CommitInfo::author, counting())), comparing(User::toString))) //
                    .commitCountByDate(sortMap(commits.stream() //
                            .collect(groupingBy(CommitInfo::date, counting())))) //
                    .commits(commits) //
                    .build();
        }
    }

    public List<Path> generateHistoryReport(HistoryStatistics stats) throws IOException {
        return reportGenerator.generateHistoryReport(stats.commits());
    }

    private <K, V> SortedMap<K, V> sortMap(Map<K, V> map) {
        return sortMap(map, null);
    }

    private <K, V> SortedMap<K, V> sortMap(Map<K, V> map, Comparator<? super K> comparator) {
        var tree = new TreeMap<K, V>(comparator);
        tree.putAll(map);
        return tree;
    }

    private static class CommitLines {
        private final CommitInfo commit;
        private int lineCount;

        CommitLines(RevCommit commit) {
            this.commit = createCommitInfo(commit);
        }

        Long addLineCount(Long v) {
            return Long.valueOf((v == null ? 0 : v.intValue()) + lineCount);
        }
    }
}
