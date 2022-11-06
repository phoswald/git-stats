package com.github.phoswald.git.stats;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CommitStatistics( //
        String repo, //
        String revision, //
        CommitInfo commit, //
        int fileCount, //
        int lineCount //
) {
}
