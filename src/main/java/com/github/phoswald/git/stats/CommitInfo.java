package com.github.phoswald.git.stats;

import java.time.Instant;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CommitInfo( //
        String commitHash, //
        Instant commitTimestamp, //
        String commitAuthorName, //
        String commitAuthorEmail, //
        String commitMessage) {
}
