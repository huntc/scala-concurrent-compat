package com.typesafe.scalacompat.concurrent;

import org.junit.Test;

public class FutureTest {

    @Test
    public void shouldCompleteFuture() {
        Future.of(() -> 1);
    }
}
