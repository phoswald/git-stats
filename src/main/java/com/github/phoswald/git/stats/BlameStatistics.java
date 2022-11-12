package com.github.phoswald.git.stats;

import java.time.LocalDate;
import java.util.SortedMap;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record BlameStatistics( //
        String repo, //
        String revision, //
        String file, //
        int lineCount, //
        SortedMap<User, Long> lineCountByAuthor, //
        SortedMap<LocalDate, Long> lineCountByDate //
) {
}
