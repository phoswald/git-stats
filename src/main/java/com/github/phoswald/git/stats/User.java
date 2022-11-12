package com.github.phoswald.git.stats;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record User(String name, String email) {
}
