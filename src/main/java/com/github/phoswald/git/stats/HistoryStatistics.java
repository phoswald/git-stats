package com.github.phoswald.git.stats;

import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record HistoryStatistics( //
        int commitCount, //
        SortedMap<User, Long> commitCountByAuthor, //
        SortedMap<LocalDate, Long> commitCountByDate, //
        List<CommitInfo> commits //
) {
}
