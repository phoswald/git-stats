package com.github.phoswald.git.stats;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class GitStatsTest {

    private final GitStats testee = new GitStats(Paths.get("src/test/resources/it/guava.git"));

    @Test
    void calculateCommitStatistics_commitHash_success() throws IOException {
        var statistics = testee.calculateCommitStatistics("822125f9ee7a71c830f1383e9e5a8663414d8f48");

        assertThat(statistics.info().commitHash(), equalTo("822125f9ee7a71c830f1383e9e5a8663414d8f48"));
        assertThat(statistics.info().commitTimestamp().atOffset(UTC), equalTo(OffsetDateTime.of(2022, 10, 27, 17, 33, 37, 0, UTC)));
        assertThat(statistics.info().commitAuthorName(), equalTo("cpovirk"));
        assertThat(statistics.info().commitAuthorEmail(), equalTo("cpovirk@google.com"));
        assertThat(statistics.info().commitMessage(), equalTo("Bump deps."));
        assertThat(statistics.fileCount(), equalTo(3239));
    }

    @Test
    void calculateCommitStatistics_releaseTag_success() throws IOException {
        var statistics = testee.calculateCommitStatistics("v31.1");

        assertThat(statistics.info().commitHash(), equalTo("0a17f4a429323589396c38d8ce75ca058faa6c64"));
        assertThat(statistics.info().commitTimestamp().atOffset(UTC), equalTo(OffsetDateTime.of(2022, 2, 28, 21, 06, 35, 0, UTC)));
        assertThat(statistics.info().commitAuthorName(), equalTo("Chris Povirk"));
        assertThat(statistics.info().commitAuthorEmail(), equalTo("cpovirk@google.com"));
        assertThat(statistics.info().commitMessage(), equalTo("Set version number for guava-parent to 31.1."));
        assertThat(statistics.fileCount(), equalTo(3107));
    }
}
