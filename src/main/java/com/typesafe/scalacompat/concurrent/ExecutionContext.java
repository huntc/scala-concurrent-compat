package com.typesafe.scalacompat.concurrent;

import scala.Function1;
import scala.concurrent.ExecutionContext$;
import scala.concurrent.ExecutionContext$class;
import scala.runtime.BoxedUnit;

import java.util.concurrent.Executor;

public class ExecutionContext implements scala.concurrent.ExecutionContext {
    /**
     * Creates an `ExecutionContext` from the given `Executor`.
     */
    public static scala.concurrent.ExecutionContext fromExecutor(final Executor e, final Function1<Throwable, BoxedUnit> reporter) {
        return ExecutionContext$.MODULE$.fromExecutor(e, reporter);
    }

    /**
     * Creates an `ExecutionContext` from the given `Executor` with the default Reporter.
     */
    public static scala.concurrent.ExecutionContext fromExecutor(final Executor e) {
        return new ExecutionContext(e, ExecutionContext$.MODULE$.defaultReporter());
    }

    private final Executor executor;

    private final Function1<Throwable, BoxedUnit> reporter;

    ExecutionContext(final Executor executor, final Function1<Throwable, BoxedUnit> reporter) {
        this.executor = executor;
        this.reporter = reporter;
        ExecutionContext$class.$init$(this);
    }

    /**
     * Runs a block of code on this execution context.
     */
    @Override
    public void execute(final Runnable runnable) {
        try {
            executor.execute(runnable);
        } catch (Throwable t) {
            reportFailure(t);
        }
    }

    /**
     * Reports that an asynchronous computation failed.
     */
    @Override
    public void reportFailure(final Throwable t) {
        reporter.apply(t);
    }

    /**
     * Prepares for the execution of a task. Returns the prepared
     * execution context. A valid implementation of `prepare` is one
     * that simply returns `this`.
     */
    @Override
    public scala.concurrent.ExecutionContext prepare() {
        return ExecutionContext$class.prepare(this);
    }
}
