package com.github.phoswald.git.stats;

import java.time.Instant;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CommitInfo( //
        String hash, //
        Instant timestamp, //
        User author, //
        String message //
) {
}
