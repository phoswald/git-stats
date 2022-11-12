package com.github.phoswald.git.reports;

public record Point(String x, double y) {

    public static Point of(String x, double y) {
        return new Point(x, y);
    }

    public static Point of(double x, double y) {
        return new Point(Double.toString(x), y);
    }

    public String ys() {
        return Double.toString(y);
    }
}
