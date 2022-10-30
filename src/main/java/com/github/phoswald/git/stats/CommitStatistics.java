package com.github.phoswald.git.stats;

import lombok.Builder;

@Builder
public record CommitStatistics( //
        CommitInfo info, //
        int fileCount, //
        int lineCount) {
}
