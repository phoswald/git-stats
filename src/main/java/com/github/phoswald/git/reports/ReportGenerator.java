package com.github.phoswald.git.reports;

import static com.github.phoswald.git.reports.Dataset.dataset;
import static com.github.phoswald.git.reports.Sequence.sequence;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.github.phoswald.git.stats.CommitInfo;
import com.github.phoswald.git.stats.charts.ChartGenerator;

public class ReportGenerator {

    private final ChartGenerator charts;

    public ReportGenerator(Path targetDir) {
        this.charts = new ChartGenerator(targetDir);
    }

    public List<Path> generateHistoryReport(List<CommitInfo> commits) throws IOException {
        var countByName = commits.stream().collect(groupingBy(c -> c.author().name(), counting()));
        var countByEmail = commits.stream().collect(groupingBy(c -> c.author().email(), counting()));
        var countByYear = commits.stream().collect(groupingBy(c -> c.year(), counting()));
        var countByMonth = commits.stream().collect(groupingBy(c -> c.month(), counting()));
        var countByEmailYear = commits.stream().collect(groupingBy(c -> c.author().email(), groupingBy(c -> c.year(), counting())));
        var countByEmailMonth = commits.stream().collect(groupingBy(c -> c.author().email(), groupingBy(c -> c.month(), counting())));

        var topEmails = sequence(null, countByEmail).topValues(10);

        return Arrays.asList( //
                charts.generatePieChart("Commits by Author (Name)", sequence(null, countByName).topSamples(10)), //
                charts.generateBarChart("Commits by Author (Email)", sequence(null, countByEmail).topSamples(20)), //
                charts.generateLineChart("Commits by Year", sequence(null, countByYear)), //
                charts.generateLineChart("Commits by Month", sequence(null, countByMonth)), //
                charts.generateLinesChart("Commits by Year for Author (Email)", dataset(null, countByEmailYear, topEmails)), //
                charts.generateLinesChart("Commits by Month for Author (Email)", dataset(null, countByEmailMonth, topEmails)));
    }
}
