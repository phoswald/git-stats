package com.github.phoswald.git.stats;

import java.time.Instant;
import java.util.SortedMap;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record BlameStatistics( //
        String repo, //
        String revision, //
        String file, //
        int lineCount, //
        SortedMap<User, Integer> lineCountByAuthor, //
        SortedMap<Instant, Integer> lineCountByTimestamp //
) {
}
