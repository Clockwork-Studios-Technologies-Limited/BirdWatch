// Jibril contributed this code: 

package com.team.telemetry.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock tests can advance by hand, so every timestamp in a test is exact. */
public class MutableClock extends Clock {
    public static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");
    private Instant now;

    public MutableClock() { this(T0); }
    public MutableClock(Instant start) { this.now = start; }

    public void advance(Duration d) { now = now.plus(d); }
    public void advanceSeconds(long s) { advance(Duration.ofSeconds(s)); }

    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return now; }
}
