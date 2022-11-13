package com.github.phoswald.git.stats.charts;

import static com.github.phoswald.git.reports.Dataset.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.github.phoswald.git.reports.Dataset;
import com.github.phoswald.git.reports.Sample;
import com.github.phoswald.git.reports.Sequence;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

public class ChartGenerator {

    private static final String plotlyUrl = "https://cdn.plot.ly/plotly-2.16.1.min.js";
    private static final Jsonb json = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    private final Path targetDir;

    public ChartGenerator(Path targetDir) {
        this.targetDir = targetDir;
    }

    public Path generatePieChart(String name, Sequence sequence) throws IOException {
        PlotlyTrace trace = new PlotlyTraceBuilder() //
                .type("pie") //
                .labels(sequence.samples().stream().map(Sample::label).toList()) //
                .values(sequence.samples().stream().map(Sample::valueAsStr).toList()) //
                .build();
        PlotlyLayout layout = new PlotlyLayoutBuilder() //
                .title(name) //
                .width(800) //
                .height(600) //
                .build();
        return generatePlotlyChart(name, Arrays.asList(trace), layout);
    }

    public Path generateBarChart(String name, Sequence sequence) throws IOException {
        return generateBarsChart(name, dataset(null, sequence));
    }

    public Path generateBarsChart(String name, Dataset dataset) throws IOException {
        List<PlotlyTrace> traces = dataset.sequences().stream() //
                .map(sequence -> new PlotlyTraceBuilder() //
                        .type("bar") //
                        .name(sequence.label()) //
                        .x(sequence.samples().stream().map(Sample::label).toList()) //
                        .y(sequence.samples().stream().map(Sample::valueAsStr).toList()) //
                        .build())
                .toList();
        PlotlyLayout layout = new PlotlyLayoutBuilder() //
                .title(name) //
                .width(800) //
                .height(600) //
                .build();
        return generatePlotlyChart(name, traces, layout);
    }

    public Path generateLineChart(String name, Sequence sequence) throws IOException {
        return generateLinesChart(name, dataset(null, sequence));
    }

    public Path generateLinesChart(String name, Dataset dataset) throws IOException {
        List<PlotlyTrace> traces = dataset.sequences().stream() //
                .map(sequence -> new PlotlyTraceBuilder() //
                        .type("scatter") //
                        .name(sequence.label()) //
                        .x(sequence.samples().stream().map(Sample::label).toList()) //
                        .y(sequence.samples().stream().map(Sample::valueAsStr).toList()) //
                        .build())
                .toList();
        PlotlyLayout layout = new PlotlyLayoutBuilder() //
                .title(name) //
                .width(800) //
                .height(600) //
                .build();
        return generatePlotlyChart(name, traces, layout);
    }

    private Path generatePlotlyChart(String name, List<PlotlyTrace> data, PlotlyLayout layout) throws IOException {
        Files.createDirectories(targetDir);
        Path filePath = targetDir.resolve(name + ".html");
        String fileContent = """
                <!doctype html>
                <html lang="en">
                <head>
                  <title>${name}</title>
                  <script src="${plotlyUrl}"></script>
                </head>
                <body>
                  <div id="placeholder"></div>
                  <script>
                    var data = ${data};
                    var layout = ${layout};
                    Plotly.newPlot(document.getElementById("placeholder"), data, layout);
                  </script>
                </body>
                </html>"""; //
        fileContent = fileContent //
                .replace("${plotlyUrl}", plotlyUrl) //
                .replace("${name}", name) //
                .replace("${data}", json.toJson(data).replace("\n", "\n    ")) //
                .replace("${layout}", json.toJson(layout).replace("\n", "\n    "));
        Files.writeString(filePath, fileContent);
        return filePath;
    }
}
