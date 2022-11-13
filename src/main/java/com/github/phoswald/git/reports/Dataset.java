package com.github.phoswald.git.reports;

import static com.github.phoswald.git.reports.Sequence.sequence;
import static java.util.Comparator.comparing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record Dataset(String label, List<Sequence> sequences) {

    public static Dataset dataset(String label, Sequence... sequences) {
        return new Dataset(label, Arrays.asList(sequences));
    }

    public static Dataset dataset(String label, Map<String, Map<String, Long>> map, Set<String> filter) {
        // TODO: add sequence() for others (not part of filter), required refactoring
        return new Dataset(label, map.entrySet().stream() //
                .filter(e -> filter.contains(e.getKey())) //
                .map(e -> sequence(e.getKey(), e.getValue())) //
                .sorted(comparing(Sequence::label)) //
                .toList());
    }
}
