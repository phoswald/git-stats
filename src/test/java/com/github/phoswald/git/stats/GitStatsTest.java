package com.github.phoswald.git.stats;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GitStatsTest {

    private GitStats testee;

    @BeforeEach
    void open() throws IOException {
        testee = new GitStats(Paths.get("src/test/resources/it/guava.git"));
    }

    @AfterEach
    void close() {
        testee.close();;
    }

    @Test
    void calculateCommitStatistics_commitHash_success() throws IOException {
        CommitStatistics stats = testee.calculateCommitStatistics("822125f9ee7a71c830f1383e9e5a8663414d8f48");

        assertThat(stats.repo(), equalTo("src/test/resources/it/guava.git"));
        assertThat(stats.revision(), equalTo("822125f9ee7a71c830f1383e9e5a8663414d8f48"));
        assertThat(stats.commit().hash(), equalTo("822125f9ee7a71c830f1383e9e5a8663414d8f48"));
        assertThat(stats.commit().timestamp().atOffset(UTC), equalTo(OffsetDateTime.of(2022, 10, 27, 17, 33, 37, 0, UTC)));
        assertThat(stats.commit().author().name(), equalTo("cpovirk"));
        assertThat(stats.commit().author().email(), equalTo("cpovirk@google.com"));
        assertThat(stats.commit().message(), equalTo("Bump deps."));
        assertThat(stats.fileCount(), equalTo(3239));
        assertThat(stats.lineCount(), equalTo(0)); // TODO line count
    }

    @Test
    void calculateCommitStatistics_releaseTag_success() throws IOException {
        CommitStatistics stats = testee.calculateCommitStatistics("v31.1");

        assertThat(stats.repo(), equalTo("src/test/resources/it/guava.git"));
        assertThat(stats.revision(), equalTo("v31.1"));
        assertThat(stats.commit().hash(), equalTo("0a17f4a429323589396c38d8ce75ca058faa6c64"));
        assertThat(stats.commit().timestamp().atOffset(UTC), equalTo(OffsetDateTime.of(2022, 2, 28, 21, 06, 35, 0, UTC)));
        assertThat(stats.commit().author().name(), equalTo("Chris Povirk"));
        assertThat(stats.commit().author().email(), equalTo("cpovirk@google.com"));
        assertThat(stats.commit().message(), equalTo("Set version number for guava-parent to 31.1."));
        assertThat(stats.fileCount(), equalTo(3107));
        assertThat(stats.lineCount(), equalTo(0)); // TODO line count
    }

    @Test
    void calculateBlameStatistics_commitHash_success() throws IOException {
        BlameStatistics stats = testee.calculateBlameStatistics( //
                "822125f9ee7a71c830f1383e9e5a8663414d8f48", //
                Path.of("guava/src/com/google/common/annotations/Beta.java"));

        assertThat(stats.repo(), equalTo("src/test/resources/it/guava.git"));
        assertThat(stats.revision(), equalTo("822125f9ee7a71c830f1383e9e5a8663414d8f48"));
        assertThat(stats.file(), equalTo("guava/src/com/google/common/annotations/Beta.java"));
        assertThat(stats.lineCount(), equalTo(46));
        assertThat(stats.lineCountByAuthor().size(), equalTo(4));
        assertThat(stats.lineCountByTimestamp().size(), equalTo(5));
        assertThat(stats.lineCountByAuthor().values().stream().collect(toSum()), equalTo(46));
        assertThat(stats.lineCountByTimestamp().values().stream().collect(toSum()), equalTo(46));
    }

    private static Collector<Integer, ?, Integer> toSum() {
        return Collectors.summingInt(Integer::intValue);
    }
}
