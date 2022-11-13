package com.github.phoswald.git.reports;

public record Sample(String label, double value) {

    public static Sample sample(String label, double value) {
        return new Sample(label, value);
    }

    public static Sample sample(double label, double value) {
        return new Sample(Double.toString(label), value);
    }

    public String valueAsStr() {
        return Double.toString(value);
    }
}
