package com.github.phoswald.git.stats;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
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

public class GitStats implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Git git;
    private final Repository repo;
    private final Path repoPath;

    public GitStats(Path repoPath) throws IOException {
        this.git = Git.open(repoPath.toFile());
        this.repo = git.getRepository();
        this.repoPath = repoPath;
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
            SortedMap<User, Integer> lineCountByAuthor = new TreeMap<>(Comparator.comparing(User::toString));
            SortedMap<Instant, Integer> lineCountByTimestamp = new TreeMap<>();
            for (var entry : commits.values()) {
                logger.debug("CommitLines: hash={}, lineCount={}", entry.commit.hash(), entry.lineCount);
                lineCountByAuthor.compute(entry.commit.author(), (k, v) -> entry.addLineCount(v));
                lineCountByTimestamp.compute(entry.commit.timestamp(), (k, v) -> entry.addLineCount(v));
            }
            return new BlameStatisticsBuilder() //
                    .repo(repoPath.toString()) //
                    .revision(revision) //
                    .file(file.toString()) //
                    .lineCount(lineCount) //
                    .lineCountByAuthor(lineCountByAuthor) //
                    .lineCountByTimestamp(lineCountByTimestamp) //
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

    private static class CommitLines {
        private final CommitInfo commit;
        private int lineCount;

        CommitLines(RevCommit commit) {
            this.commit = createCommitInfo(commit);
        }

        Integer addLineCount(Integer v) {
            return Integer.valueOf((v == null ? 0 : v.intValue()) + lineCount);
        }
    }
}
