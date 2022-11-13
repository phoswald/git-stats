package com.github.phoswald.git.reports;

import static com.github.phoswald.git.reports.Sample.sample;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record Sequence(String label, List<Sample> samples) {

    public static Sequence sequence(String label, Sample... samples) {
        return new Sequence(label, Arrays.asList(samples));
    }

    public static Sequence sequence(String label, Map<String, Long> map) {
        return new Sequence(label, map.entrySet().stream() //
                .map(e -> sample(e.getKey(), e.getValue())) //
                .sorted(comparing(Sample::label)) //
                .toList());
    }

    public Sequence topSamples(int count) {
        var sorted = new ArrayList<Sample>(samples);
        sorted.sort(comparing(Sample::value).reversed());
        if (count < sorted.size()) {
            var selection = new ArrayList<Sample>();
            double remaining = sorted.stream().skip(count).collect(summingDouble(Sample::value));
            selection.addAll(sorted.subList(0, count));
            selection.add(sample("others (" + (sorted.size() - count) + ")", remaining));
            return new Sequence(label, selection);
        } else {
            return new Sequence(label, sorted);
        }
    }

    public Set<String> topValues(int count) {
        var sorted = new ArrayList<Sample>(samples);
        sorted.sort(comparing(Sample::value).reversed());
        return sorted.stream().map(Sample::label).limit(count).collect(toSet());
    }
}
