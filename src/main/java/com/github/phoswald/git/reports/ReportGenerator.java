package com.github.phoswald.git.reports;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.BiFunction;

import com.github.phoswald.git.stats.User;
import com.github.phoswald.git.stats.charts.ChartGenerator;

public class ReportGenerator {

    private final ChartGenerator charts;

    public ReportGenerator(Path targetDir) {
        this.charts = new ChartGenerator(targetDir);
    }

    public List<Path> generateCountByAuthorChart(SortedMap<User, Long> commitCountByAuthor) throws IOException {
        Map<String, Double> countByAuthorName = commitCountByAuthor.entrySet().stream() //
                .collect(groupingBy(e -> e.getKey().name(), summingDouble(e -> e.getValue().doubleValue())));
        Map<String, Double> countByAuthorEmail = commitCountByAuthor.entrySet().stream() //
                .collect(groupingBy(e -> e.getKey().email(), summingDouble(e -> e.getValue().doubleValue())));
        return Arrays.asList( //
                charts.generatePieChart("Commits by Author (Name)",
                        selectTopValues(toSortedList(countByAuthorName, Point::of), 10)), //
                charts.generateBarChart("Commits by Author (Email)",
                        selectTopValues(toSortedList(countByAuthorEmail, Point::of), 20)));
    }

    public List<Path> generateCountByDateChart(SortedMap<LocalDate, Long> commitCountByDate) throws IOException {
        Map<String, Double> countByYear = commitCountByDate.entrySet().stream() //
                .collect(groupingBy(e -> e.getKey().toString().substring(0, 4), summingDouble(e -> e.getValue())));
        Map<String, Double> countByMonth = commitCountByDate.entrySet().stream() //
                .collect(groupingBy(e -> e.getKey().toString().substring(0, 7), summingDouble(e -> e.getValue())));
        return Arrays.asList( //
                charts.generateLineChart("Commits by Year", toSortedList(countByYear, Point::of)), //
                charts.generateLineChart("Commits by Month", toSortedList(countByMonth, Point::of)));
    }

    private <K, V> List<Point> toSortedList(Map<K, V> map, BiFunction<K, V, Point> converter) {
        return map.entrySet().stream() //
                .map(e -> converter.apply(e.getKey(), e.getValue())) //
                .sorted(comparing(Point::x)) //
                .toList();
    }

    private List<Point> selectTopValues(List<Point> list, int count) {
        var sorted = new ArrayList<Point>(list);
        sorted.sort(comparing(Point::y).reversed());
        if (count < sorted.size()) {
            var selection = new ArrayList<Point>();
            double remaining = sorted.stream().skip(count).collect(summingDouble(Point::y)).doubleValue();
            selection.addAll(sorted.subList(0, count));
            selection.add(Point.of("others (" + (sorted.size() - count) + ")", remaining));
            return selection;
        } else {
            return sorted;
        }
    }
}
