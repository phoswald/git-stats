package com.github.phoswald.git.stats.charts;

import static com.github.phoswald.git.reports.Dataset.dataset;
import static com.github.phoswald.git.reports.Sample.sample;
import static com.github.phoswald.git.reports.Sequence.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class ChartGeneratorTest {

    private final ChartGenerator testee = new ChartGenerator(Paths.get("target/charts"));

    @Test
    void generatePieChart() throws IOException {
        var seq = sequence(null, sample("Value A", 10), sample("Value B", 20));
        Path file = testee.generatePieChart("sample-pie-chart", seq);
        assertValidChart(file);
    }

    @Test
    void generateBarChart() throws IOException {
        var seq = sequence(null, sample("Value A", 10), sample("Value B", 20));
        Path file = testee.generateBarChart("sample-bar-chart", seq);
        assertValidChart(file);
    }

    @Test
    void generateBarsChart() throws IOException {
        var seq1 = sequence("Series 1", sample("Value A", 10), sample("Value B", 20));
        var seq2 = sequence("Series 2", sample("Value A", 12), sample("Value B", 17));
        Path file = testee.generateBarsChart("sample-bars-chart", dataset(null, seq1, seq2));
        assertValidChart(file);
    }

    @Test
    void generateLineChart() throws IOException {
        var seq = sequence(null, sample(1, 1), sample(2, 2), sample(3, 4), sample(4, 8), sample(5, 16));
        Path file = testee.generateLineChart("sample-line-chart", seq);
        assertValidChart(file);
    }

    @Test
    void generateLinesChart() throws IOException {
        var seq1 = sequence("Series A", sample(1, 1), sample(2, 2), sample(3, 4), sample(4, 8), sample(5, 16));
        var seq2 = sequence("Series B", sample(1, 5), sample(2, 6), sample(3, 4), sample(4, 3), sample(5, 1));
        Path file = testee.generateLinesChart("sample-lines-chart", dataset(null, seq1, seq2));
        assertValidChart(file);
    }

    private void assertValidChart(Path file) throws IOException {
        String fileContent = Files.readString(file); // TODO: verify chart content, PNG download
        assertThat(fileContent, startsWith("<!doctype html>"));
    }
}
