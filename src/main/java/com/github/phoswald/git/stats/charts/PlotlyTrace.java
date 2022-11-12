package com.github.phoswald.git.stats.charts;

import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record PlotlyTrace( // must be public for JSON-B?
        String name, //
        String type, //
        List<String> labels, //
        List<String> values, //
        List<String> x, //
        List<String> y //
) {
}
