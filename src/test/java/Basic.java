/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.*;

public class Basic {

    static void checkCompletedNormally(CompletableFuture<?> cf, Object value) {
        checkCompletedNormally(cf, value == null ? null : new Object[]{value});
    }

    static void checkCompletedNormally(CompletableFuture<?> cf, Object[] values) {
        try {
            equalAnyOf(cf.join(), values);
        } catch (Throwable x) {
            unexpected(x);
        }
        try {
            equalAnyOf(cf.value().orElse(null), values);
        } catch (Throwable x) {
            unexpected(x);
        }
        try {
            equalAnyOf(cf.get(), values);
        } catch (Throwable x) {
            unexpected(x);
        }
        try {
            equalAnyOf(cf.get(0L, SECONDS), values);
        } catch (Throwable x) {
            unexpected(x);
        }
        check(cf.isDone(), "Expected isDone to be true, got:" + cf);
        check(!cf.isCancelled(), "Expected isCancelled to be false");
        check(!cf.cancel(true), "Expected cancel to return false");
        check(cf.toString().contains("[Completed normally]"));
        check(!cf.complete(null), "Expected complete() to fail");
        check(!cf.completeExceptionally(new Throwable()),
                "Expected completeExceptionally() to fail");
    }

    static <T> void checkCompletedExceptionally(CompletableFuture<T> cf)
            throws Exception {
        checkCompletedExceptionally(cf, false);
    }

    @SuppressWarnings("unchecked")
    static <T> void checkCompletedExceptionally(CompletableFuture<T> cf, boolean cancelled)
            throws Exception {
        try {
            cf.join();
            fail("Excepted exception to be thrown");
        } catch (CompletionException x) {
            if (cancelled) fail();
            else pass();
        } catch (CancellationException x) {
            if (cancelled) pass();
            else fail();
        }
        try {
            cf.value();
            fail("Excepted exception to be thrown");
        } catch (CompletionException x) {
            if (cancelled) fail();
            else pass();
        } catch (CancellationException x) {
            if (cancelled) pass();
            else fail();
        }
        try {
            cf.get();
            fail("Excepted exception to be thrown");
        } catch (CancellationException x) {
            if (cancelled) pass();
            else fail();
        } catch (ExecutionException x) {
            if (cancelled) check(x.getCause() instanceof CancellationException);
            else pass();
        }
        try {
            cf.get(0L, SECONDS);
            fail("Excepted exception to be thrown");
        } catch (CancellationException x) {
            if (cancelled) pass();
            else fail();
        } catch (ExecutionException x) {
            if (cancelled) check(x.getCause() instanceof CancellationException);
            else pass();
        }
        check(cf.isDone(), "Expected isDone to be true, got:" + cf);
        check(cf.isCancelled() == cancelled, "Expected isCancelled: " + cancelled + ", got:" + cf.isCancelled());
        check(cf.cancel(true) == cancelled, "Expected cancel: " + cancelled + ", got:" + cf.cancel(true));
        check(cf.toString().contains("[Completed exceptionally]"));  // ## TODO: 'E'xceptionally
        check(!cf.complete((T) new Object()), "Expected complete() to fail");
        check(!cf.completeExceptionally(new Throwable()),
                "Expected completeExceptionally() to fail, already completed");
    }

    private static void test() throws Throwable {

        Thread.currentThread().setName("mainThread");

        //----------------------------------------------------------------
        // supplier tests
        //----------------------------------------------------------------
        try {
            CompletableFuture<String> cf = CompletableFuture.of(() -> "a test string");
            checkCompletedNormally(cf, cf.join());
            cf = CompletableFuture.of(() -> {
                if (true) throw new RuntimeException();
                else return "";
            });
            checkCompletedExceptionally(cf);
        } catch (Throwable t) {
            unexpected(t);
        }

    }

    //--------------------- Infrastructure ---------------------------
    static volatile int passed = 0, failed = 0;

    static void pass() {
        passed++;
    }

    static void fail() {
        failed++;
        Thread.dumpStack();
    }

    static void fail(String msg) {
        System.out.println(msg);
        fail();
    }

    static void unexpected(Throwable t) {
        failed++;
        t.printStackTrace();
    }

    static void check(boolean cond) {
        if (cond) pass();
        else fail();
    }

    static void check(boolean cond, String msg) {
        if (cond) pass();
        else fail(msg);
    }

    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);
    }

    static void equalAnyOf(Object x, Object[] y) {
        if (x == null && y == null) {
            pass();
            return;
        }
        for (Object z : y) {
            if (x.equals(z)) {
                pass();
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : y)
            sb.append(o).append(" ");
        fail(x + " not equal to one of [" + sb + "]");
    }

    public static void main(String[] args) throws Throwable {
        try {
            test();
        } catch (Throwable t) {
            unexpected(t);
        }
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");
    }
}
