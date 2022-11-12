package com.github.phoswald.git.stats.charts;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.phoswald.git.reports.Point;
import com.github.phoswald.git.reports.Series;

class ChartGeneratorTest {

    private final ChartGenerator testee = new ChartGenerator(Paths.get("target/charts"));

    @Test
    void generatePieChart() throws IOException {
        var points = Arrays.asList(Point.of("Value A", 10), Point.of("Value B", 20));
        Path file = testee.generatePieChart("sample-pie-chart", points);
        assertValidChart(file);
    }

    @Test
    void generateBarChart() throws IOException {
        var points = Arrays.asList(Point.of("Value A", 10), Point.of("Value B", 20));
        Path file = testee.generateBarChart("sample-bar-chart", points);
        assertValidChart(file);
    }

    @Test
    void generateBarsChart() throws IOException {
        var series1 = new Series("Series 1", Arrays.asList(Point.of("Value A", 10), Point.of("Value B", 20)));
        var series2 = new Series("Series 2", Arrays.asList(Point.of("Value A", 12), Point.of("Value B", 17)));
        Path file = testee.generateBarsChart("sample-bars-chart", Arrays.asList(series1, series2));
        assertValidChart(file);
    }

    @Test
    void generateLineChart() throws IOException {
        var points = Arrays.asList( //
                Point.of(1, 1), Point.of(2, 2), Point.of(3, 4), Point.of(4, 8), Point.of(5, 16));
        Path file = testee.generateLineChart("sample-line-chart", points);
        assertValidChart(file);
    }

    @Test
    void generateLinesChart() throws IOException {
        var series1 = new Series("Series A", Arrays.asList( //
                Point.of(1, 1), Point.of(2, 2), Point.of(3, 4), Point.of(4, 8), Point.of(5, 16)));
        var series2 = new Series("Series B", Arrays.asList( //
                Point.of(1, 5), Point.of(2, 6), Point.of(3, 4), Point.of(4, 3), Point.of(5, 1)));
        Path file = testee.generateLinesChart("sample-lines-chart", Arrays.asList(series1, series2));
        assertValidChart(file);
    }

    private void assertValidChart(Path file) throws IOException {
        String fileContent = Files.readString(file); // TODO: verify chart content, PNG download
        assertThat(fileContent, startsWith("<!doctype html>"));
    }
}
