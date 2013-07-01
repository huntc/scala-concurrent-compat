package com.typesafe.scalacompat.concurrent;

import scala.*;
import scala.Function0$class;
import scala.concurrent.*;
import scala.concurrent.ExecutionContext$class;
import scala.concurrent.Future$class;
import scala.concurrent.duration.Duration;
import scala.concurrent.forkjoin.ForkJoinPool;
import scala.reflect.ClassTag;
import scala.util.Try;

import java.util.function.Supplier;

public class Future<T> implements scala.concurrent.Future<T> {

    private final static scala.concurrent.ExecutionContext defaultEc = ExecutionContext.fromExecutor(ForkJoinPool.commonPool());

    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the given executor with the value obtained
     * by calling the given Supplier.
     *
     * @param supplier a function returning the value to be used
     *                 to complete the returned CompletableFuture
     * @return the new CompletableFuture
     */
    public static <U> Future<U> of(Supplier<U> supplier) {
        return of(supplier, defaultEc);
    }

    public static <U> Future<U> of(Supplier<U> supplier,
                                   scala.concurrent.ExecutionContext executor) {
        if (executor == null || supplier == null)
            throw new NullPointerException();
        PromiseCompletingRunnable<U> runnable = new PromiseCompletingRunnable<>(supplier);
        executor.prepare().execute(runnable);
        return runnable.getPromise().future();
    }

    Future() {
        Future$class.$init$(this);
    }

    /**
     * When this future is completed successfully (i.e. with a value),
     * apply the provided partial function to the value if the partial function
     * is defined at that value.
     * <p/>
     * If the future has already been completed with a value,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * $multipleCallbacks
     * $callbackInContext
     */
    @Override
    public <U> void onSuccess(PartialFunction<T, U> pf, scala.concurrent.ExecutionContext executor) {
        Future$class.onSuccess(this, pf, executor);
    }

    /**
     * When this future is completed with a failure (i.e. with a throwable),
     * apply the provided callback to the throwable.
     * <p/>
     * $caughtThrowables
     * <p/>
     * If the future has already been completed with a failure,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * Will not be called in case that the future is completed with a value.
     * <p/>
     * $multipleCallbacks
     * $callbackInContext
     */
    @Override
    public <U> void onFailure(PartialFunction<Throwable, U> callback, scala.concurrent.ExecutionContext executor) {
        Future$class.onFailure(this, callback, executor);
    }

    /**
     * When this future is completed, either through an exception, or a value,
     * apply the provided function.
     * <p/>
     * If the future has already been completed,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * $multipleCallbacks
     * $callbackInContext
     */
    @Override
    public <U> void onComplete(Function1<Try<T>, U> func, scala.concurrent.ExecutionContext executor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns whether the future has already been completed with
     * a value or an exception.
     * <p/>
     * $nonDeterministic
     *
     * @return `true` if the future is already completed, `false` otherwise
     */
    @Override
    public boolean isCompleted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The value of this `Future`.
     * <p/>
     * If the future is not completed the returned value will be `None`.
     * If the future is completed the value will be `Some(Success(t))`
     * if it contains a valid result, or `Some(Failure(error))` if it contains
     * an exception.
     */
    @Override
    public Option<Try<T>> value() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns a failed projection of this future.
     * <p/>
     * The failed projection is a future holding a value of type `Throwable`.
     * <p/>
     * It is completed with a value which is the throwable of the original future
     * in case the original future is failed.
     * <p/>
     * It is failed with a `NoSuchElementException` if the original future is completed successfully.
     * <p/>
     * Blocking on this future returns a value if the original future is completed with an exception
     * and throws a corresponding exception if the original future fails.
     */
    @Override
    public scala.concurrent.Future<Throwable> failed() {
        return Future$class.failed(this);
    }

    /**
     * Asynchronously processes the value in the future once the value becomes available.
     * <p/>
     * Will not be called if the future fails.
     */
    @Override
    public <U> void foreach(Function1<T, U> f, scala.concurrent.ExecutionContext executor) {
        Future$class.foreach(this, f, executor);
    }

    /**
     * Creates a new future by applying the 's' function to the successful result of
     * this future, or the 'f' function to the failed result. If there is any non-fatal
     * exception thrown when 's' or 'f' is applied, that exception will be propagated
     * to the resulting future.
     *
     * @param s function that transforms a successful result of the receiver into a
     *          successful result of the returned future
     * @param f function that transforms a failure of the receiver into a failure of
     *          the returned future
     * @return a future that will be completed with the transformed value
     */
    @Override
    public <S> scala.concurrent.Future<S> transform(Function1<T, S> s, Function1<Throwable, Throwable> f, scala.concurrent.ExecutionContext executor) {
        return Future$class.transform(this, s, f, executor);
    }

    /**
     * Creates a new future by applying a function to the successful result of
     * this future. If this future is completed with an exception then the new
     * future will also contain this exception.
     * <p/>
     * $forComprehensionExamples
     */
    @Override
    public <S> scala.concurrent.Future<S> map(Function1<T, S> f, scala.concurrent.ExecutionContext executor) {
        return Future$class.map(this, f, executor);
    }

    /**
     * Creates a new future by applying a function to the successful result of
     * this future, and returns the result of the function as the new future.
     * If this future is completed with an exception then the new future will
     * also contain this exception.
     * <p/>
     * $forComprehensionExamples
     */
    @Override
    public <S> scala.concurrent.Future<S> flatMap(Function1<T, scala.concurrent.Future<S>> f, scala.concurrent.ExecutionContext executor) {
        return Future$class.flatMap(this, f, executor);
    }

    /**
     * Creates a new future by filtering the value of the current future with a predicate.
     * <p/>
     * If the current future contains a value which satisfies the predicate, the new future will also hold that value.
     * Otherwise, the resulting future will fail with a `NoSuchElementException`.
     * <p/>
     * If the current future fails, then the resulting future also fails.
     * <p/>
     * Example:
     * {{{
     * val f = future { 5 }
     * val g = f filter { _ % 2 == 1 }
     * val h = f filter { _ % 2 == 0 }
     * Await.result(g, Duration.Zero) // evaluates to 5
     * Await.result(h, Duration.Zero) // throw a NoSuchElementException
     * }}}
     */
    @Override
    public scala.concurrent.Future<T> filter(Function1<T, Object> pred, scala.concurrent.ExecutionContext executor) {
        return Future$class.filter(this, pred, executor);
    }

    @Override
    public scala.concurrent.Future<T> withFilter(Function1<T, Object> pred, scala.concurrent.ExecutionContext executor) {
        return Future$class.withFilter(this, pred, executor);
    }

    /**
     * Creates a new future by mapping the value of the current future, if the given partial function is defined at that value.
     * <p/>
     * If the current future contains a value for which the partial function is defined, the new future will also hold that value.
     * Otherwise, the resulting future will fail with a `NoSuchElementException`.
     * <p/>
     * If the current future fails, then the resulting future also fails.
     * <p/>
     * Example:
     * {{{
     * val f = future { -5 }
     * val g = f collect {
     * case x if x < 0 => -x
     * }
     * val h = f collect {
     * case x if x > 0 => x * 2
     * }
     * Await.result(g, Duration.Zero) // evaluates to 5
     * Await.result(h, Duration.Zero) // throw a NoSuchElementException
     * }}}
     */
    @Override
    public <S> scala.concurrent.Future<S> collect(PartialFunction<T, S> pf, scala.concurrent.ExecutionContext executor) {
        return Future$class.collect(this, pf, executor);
    }

    /**
     * Creates a new future that will handle any matching throwable that this
     * future might contain. If there is no match, or if this future contains
     * a valid result then the new future will contain the same.
     * <p/>
     * Example:
     * <p/>
     * {{{
     * future (6 / 0) recover { case e: ArithmeticException => 0 } // result: 0
     * future (6 / 0) recover { case e: NotFoundException   => 0 } // result: exception
     * future (6 / 2) recover { case e: ArithmeticException => 0 } // result: 3
     * }}}
     */
    @Override
    public <U> scala.concurrent.Future<U> recover(PartialFunction<Throwable, U> pf, scala.concurrent.ExecutionContext executor) {
        return Future$class.recover(this, pf, executor);
    }

    /**
     * Creates a new future that will handle any matching throwable that this
     * future might contain by assigning it a value of another future.
     * <p/>
     * If there is no match, or if this future contains
     * a valid result then the new future will contain the same result.
     * <p/>
     * Example:
     * <p/>
     * {{{
     * val f = future { Int.MaxValue }
     * future (6 / 0) recoverWith { case e: ArithmeticException => f } // result: Int.MaxValue
     * }}}
     */
    @Override
    public <U> scala.concurrent.Future<U> recoverWith(PartialFunction<Throwable, scala.concurrent.Future<U>> pf, scala.concurrent.ExecutionContext executor) {
        return Future$class.recoverWith(this, pf, executor);
    }

    /**
     * Zips the values of `this` and `that` future, and creates
     * a new future holding the tuple of their results.
     * <p/>
     * If `this` future fails, the resulting future is failed
     * with the throwable stored in `this`.
     * Otherwise, if `that` future fails, the resulting future is failed
     * with the throwable stored in `that`.
     */
    @Override
    public <U> scala.concurrent.Future<Tuple2<T, U>> zip(scala.concurrent.Future<U> that) {
        return Future$class.zip(this, that);
    }

    /**
     * Creates a new future which holds the result of this future if it was completed successfully, or, if not,
     * the result of the `that` future if `that` is completed successfully.
     * If both futures are failed, the resulting future holds the throwable object of the first future.
     * <p/>
     * Using this method will not cause concurrent programs to become nondeterministic.
     * <p/>
     * Example:
     * {{{
     * val f = future { sys.error("failed") }
     * val g = future { 5 }
     * val h = f fallbackTo g
     * Await.result(h, Duration.Zero) // evaluates to 5
     * }}}
     */
    @Override
    public <U> scala.concurrent.Future<U> fallbackTo(scala.concurrent.Future<U> that) {
        return Future$class.fallbackTo(this, that);
    }

    /**
     * Creates a new `Future[S]` which is completed with this `Future`'s result if
     * that conforms to `S`'s erased type or a `ClassCastException` otherwise.
     */
    @Override
    public <S> scala.concurrent.Future<S> mapTo(ClassTag<S> tag) {
        return Future$class.mapTo(this, tag);
    }

    /**
     * Applies the side-effecting function to the result of this future, and returns
     * a new future with the result of this future.
     * <p/>
     * This method allows one to enforce that the callbacks are executed in a
     * specified order.
     * <p/>
     * Note that if one of the chained `andThen` callbacks throws
     * an exception, that exception is not propagated to the subsequent `andThen`
     * callbacks. Instead, the subsequent `andThen` callbacks are given the original
     * value of this future.
     * <p/>
     * The following example prints out `5`:
     * <p/>
     * {{{
     * val f = future { 5 }
     * f andThen {
     * case r => sys.error("runtime exception")
     * } andThen {
     * case Failure(t) => println(t)
     * case Success(v) => println(v)
     * }
     * }}}
     */
    @Override
    public <U> scala.concurrent.Future<T> andThen(PartialFunction<Try<T>, U> pf, scala.concurrent.ExecutionContext executor) {
        return Future$class.andThen(this, pf, executor);
    }

    /**
     * Await the "completed" state of this `Awaitable`.
     * <p/>
     * '''''This method should not be called directly; use [[Await.ready]] instead.'''''
     *
     * @param atMost maximum wait time, which may be negative (no waiting is done),
     *               [[scala.concurrent.duration.Duration.Inf Duration.Inf]] for unbounded waiting, or a finite positive
     *               duration
     * @return this `Awaitable`
     * @throws InterruptedException     if the current thread is interrupted while waiting
     * @throws java.util.concurrent.TimeoutException
     *                                  if after waiting for the specified time this `Awaitable` is still not ready
     * @throws IllegalArgumentException if `atMost` is [[scala.concurrent.duration.Duration.Undefined Duration.Undefined]]
     */
    @Override
    public Awaitable<T> ready(Duration atMost, CanAwait permit) throws InterruptedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Await and return the result (of type `T`) of this `Awaitable`.
     * <p/>
     * '''''This method should not be called directly; use [[Await.result]] instead.'''''
     *
     * @param atMost maximum wait time, which may be negative (no waiting is done),
     *               [[scala.concurrent.duration.Duration.Inf Duration.Inf]] for unbounded waiting, or a finite positive
     *               duration
     * @return the result value if the `Awaitable` is completed within the specific maximum wait time
     * @throws InterruptedException     if the current thread is interrupted while waiting
     * @throws java.util.concurrent.TimeoutException
     *                                  if after waiting for the specified time this `Awaitable` is still not ready
     * @throws IllegalArgumentException if `atMost` is [[scala.concurrent.duration.Duration.Undefined Duration.Undefined]]
     */
    @Override
    public T result(Duration atMost, CanAwait permit) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class PromiseCompletingRunnable<T> implements Runnable {
        private final Supplier<T> supplier;

        private final Promise<T> promise = new Promise<>();

        public PromiseCompletingRunnable(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void run() {

        }

        public Promise<T> getPromise() {
            return promise;
        }
    }

}
