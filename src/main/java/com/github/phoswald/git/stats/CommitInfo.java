package com.github.phoswald.git.stats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CommitInfo( //
        String hash, //
        Instant timestamp, //
        User author, //
        String message //
) {
    public LocalDate date() {
        return timestamp.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
