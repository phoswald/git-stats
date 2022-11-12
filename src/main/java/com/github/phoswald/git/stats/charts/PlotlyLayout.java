package com.github.phoswald.git.stats.charts;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record PlotlyLayout( // must be public for JSON-B?
        String title, //
        Integer width, //
        Integer height //
) {
}
