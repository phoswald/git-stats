package com.github.phoswald.git.stats;

import java.time.Instant;

import lombok.Builder;

@Builder
public record CommitInfo( //
        String commitHash, //
        Instant commitTimestamp, //
        String commitAuthorName, //
        String commitAuthorEmail, //
        String commitMessage) {
}
