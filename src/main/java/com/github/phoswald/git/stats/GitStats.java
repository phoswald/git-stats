package com.github.phoswald.git.stats;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitStats {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Path repoPath;

    public GitStats(Path repoPath) {
        this.repoPath = repoPath;
    }

    public CommitStatistics calculateCommitStatistics(String revision) throws IOException {
        try (Git git = Git.open(repoPath.toFile())) {
            Repository repo = git.getRepository();
            ObjectId objId = repo.resolve(revision);
            try (RevWalk walk = new RevWalk(repo)) {
                RevCommit commit = walk.parseCommit(objId);
                return new CommitStatisticsBuilder() //
                        .info(new CommitInfoBuilder() //
                                .commitHash(objId.getName()) //
                                .commitTimestamp(Instant.ofEpochSecond(commit.getCommitTime())) //
                                .commitAuthorName(commit.getAuthorIdent().getName()) //
                                .commitAuthorEmail(commit.getAuthorIdent().getEmailAddress()) //
                                .commitMessage(commit.getShortMessage()) //
                                .build()) //
                        .fileCount(getFiles(repo, commit).size()) //
                        .build();
            }

        }
    }

    private List<String> getFiles(Repository repo, RevCommit commit) throws IOException {
        List<String> fileNames = new ArrayList<>();
        RevTree tree = commit.getTree();
        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            while(treeWalk.next()) {
                 logger.debug("treeWalk: {} {} {}", treeWalk.getObjectId(0).getName(), treeWalk.getFileMode(), treeWalk.getPathString());
                 fileNames.add(treeWalk.getPathString());
            }
        }
        return fileNames;
    }
}
