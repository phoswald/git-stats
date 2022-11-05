package com.github.phoswald.git.stats;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CommitStatistics( //
        CommitInfo info, //
        int fileCount, //
        int lineCount) {
}
