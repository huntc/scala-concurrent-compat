package com.typesafe.java.util.concurrent;

import java.util.concurrent.Executor;
import java.util.function.*;

/**
 * Similar to a {@link java.util.concurrent.Future} in that it has a result determined at some point in the future.
 * Unlike {@link java.util.concurrent.Future}, DeferredResult includes dependent functions and actions that trigger
 * upon its completion.
 *
 * @author Doug Lea
 * @since 1.8
 */
public interface DeferredResult<T> {

    /**
     * Returns a new DeferredResult that is already completed with
     * the given value.
     *
     * @param value the value
     * @return the completed DeferredResult
     */
    static <U> DeferredResult<U> completedFuture(U value) {
        return CompletableFuture.completedFuture(value);
    }

    /**
     * Returns a new DeferredResult that is completed when all of
     * the given DeferredResults complete.  If any of the given
     * DeferredResults complete exceptionally, then the returned
     * DeferredResult also does so, with a DeferredResult
     * holding this exception as its cause.  Otherwise, the results,
     * if any, of the given DeferredResult are not reflected in
     * the returned DeferredResult, but may be obtained by
     * inspecting them individually. If no DeferredResults are
     * provided, returns a DeferredResult completed with the value
     * {@code null}.
     *
     * @param drs the DeferredResults
     * @return a new DeferredResult that is completed when all of the
     * given DeferredResults complete
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    static DeferredResult<Void> allOf(DeferredResult<?>... drs) {
        return CompletableFuture.allOf(CompletableFuture.completableFutures(drs));
    }

    /**
     * Returns a new DeferredResult that is completed when any of
     * the given DeferredResults complete, with the same result.
     * Otherwise, if it completed exceptionally, the returned
     * DeferredResult also does so, with a DeferredResult
     * holding this exception as its cause.  If no DeferredResults
     * are provided, returns an incomplete CompletableFuture.
     *
     * @param drs the DeferredResults
     * @return a new DeferredResult that is completed with the
     * result or exception of any of the given DeferredResults when
     * one completes
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    static DeferredResult<Object> anyOf(DeferredResult<?>... drs) {
        return CompletableFuture.anyOf(CompletableFuture.completableFutures(drs));
    }

    /**
     * Returns the result value (or throws any encountered exception)
     * if completed, else returns the given valueIfAbsent.
     *
     * @param valueIfAbsent the value to return if not completed
     * @return the result value, if completed, else the given valueIfAbsent
     * @throws java.util.concurrent.CancellationException
     *          if the computation was cancelled
     * @throws java.util.concurrent.CompletionException
     *          if this future completed
     *          exceptionally or a completion computation threw an exception
     */
    T getNow(T valueIfAbsent);

    /**
     * Returns a new DeferredResult that is completed
     * when this DeferredResult completes, with the result of the
     * given function of this DeferredResult's result.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied function throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param fn the function to use to compute the value of
     *           the returned DeferredResult
     * @return the new DeferredResult
     */
    <U> DeferredResult<U> thenApply(Function<? super T, ? extends U> fn);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when this DeferredResult completes, with the result of the
     * given function of this DeferredResult's result from a
     * task running in the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied function throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param fn the function to use to compute the value of
     *           the returned DeferredResult
     * @return the new DeferredResult
     */
    <U> DeferredResult<U> thenApplyAsync
    (Function<? super T, ? extends U> fn);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when this DeferredResult completes, with the result of the
     * given function of this DeferredResult's result from a
     * task running in the given executor.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied function throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param fn       the function to use to compute the value of
     *                 the returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    <U> DeferredResult<U> thenApplyAsync
    (Function<? super T, ? extends U> fn,
     Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when this DeferredResult completes, after performing the given
     * action with this DeferredResult's result.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param block the action to perform before completing the
     *              returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> thenAccept(Consumer<? super T> block);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when this DeferredResult completes, after performing the given
     * action with this DeferredResult's result from a task running
     * in the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param block the action to perform before completing the
     *              returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> thenAcceptAsync(Consumer<? super T> block);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when this DeferredResult completes, after performing the given
     * action with this DeferredResult's result from a task running
     * in the given executor.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param block    the action to perform before completing the
     *                 returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    DeferredResult<Void> thenAcceptAsync(Consumer<? super T> block,
                                         Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when this DeferredResult completes, after performing the given
     * action.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param action the action to perform before completing the
     *               returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> thenRun(Runnable action);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when this DeferredResult completes, after performing the given
     * action from a task running in the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param action the action to perform before completing the
     *               returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> thenRunAsync(Runnable action);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when this DeferredResult completes, after performing the given
     * action from a task running in the given executor.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * DeferredResult completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param action   the action to perform before completing the
     *                 returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    DeferredResult<Void> thenRunAsync(Runnable action,
                                      Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when both this and the other given DeferredResult complete,
     * with the result of the given function of the results of the two
     * DeferredResults.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied function throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other DeferredResult
     * @param fn    the function to use to compute the value of
     *              the returned DeferredResult
     * @return the new DeferredResult
     */
    <U, V> DeferredResult<V> thenCombine
    (DeferredResult<? extends U> other,
     BiFunction<? super T, ? super U, ? extends V> fn);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when both this and the other given DeferredResult complete,
     * with the result of the given function of the results of the two
     * DeferredResults from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied function throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other DeferredResult
     * @param fn    the function to use to compute the value of
     *              the returned DeferredResult
     * @return the new DeferredResult
     */
    <U, V> DeferredResult<V> thenCombineAsync
    (DeferredResult<? extends U> other,
     BiFunction<? super T, ? super U, ? extends V> fn);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when both this and the other given DeferredResult complete,
     * with the result of the given function of the results of the two
     * DeferredResults from a task running in the given executor.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied function throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other    the other DeferredResult
     * @param fn       the function to use to compute the value of
     *                 the returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    <U, V> DeferredResult<V> thenCombineAsync
    (DeferredResult<? extends U> other,
     BiFunction<? super T, ? super U, ? extends V> fn,
     Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when both this and the other given DeferredResult complete,
     * after performing the given action with the results of the two
     * DeferredResults.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other DeferredResult
     * @param block the action to perform before completing the
     *              returned DeferredResult
     * @return the new DeferredResult
     */
    <U> DeferredResult<Void> thenAcceptBoth
    (DeferredResult<? extends U> other,
     BiConsumer<? super T, ? super U> block);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when both this and the other given DeferredResult complete,
     * after performing the given action with the results of the two
     * DeferredResults from a task running in the {@link
     * java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other DeferredResult
     * @param block the action to perform before completing the
     *              returned DeferredResult
     * @return the new DeferredResult
     */
    <U> DeferredResult<Void> thenAcceptBothAsync
    (DeferredResult<? extends U> other,
     BiConsumer<? super T, ? super U> block);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when both this and the other given DeferredResult complete,
     * after performing the given action with the results of the two
     * DeferredResults from a task running in the given executor.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other    the other DeferredResult
     * @param block    the action to perform before completing the
     *                 returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    <U> DeferredResult<Void> thenAcceptBothAsync
    (DeferredResult<? extends U> other,
     BiConsumer<? super T, ? super U> block,
     Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when both this and the other given DeferredResult complete,
     * after performing the given action.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other  the other DeferredResult
     * @param action the action to perform before completing the
     *               returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> runAfterBoth(DeferredResult<?> other,
                                      Runnable action);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when both this and the other given DeferredResult complete,
     * after performing the given action from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other  the other DeferredResult
     * @param action the action to perform before completing the
     *               returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> runAfterBothAsync(DeferredResult<?> other,
                                           Runnable action);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when both this and the other given DeferredResult complete,
     * after performing the given action from a task running in the
     * given executor.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned DeferredResult completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other    the other DeferredResult
     * @param action   the action to perform before completing the
     *                 returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    DeferredResult<Void> runAfterBothAsync(DeferredResult<?> other,
                                           Runnable action,
                                           Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when either this or the other given DeferredResult completes,
     * with the result of the given function of either this or the other
     * DeferredResult's result.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied function
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other DeferredResult
     * @param fn    the function to use to compute the value of
     *              the returned DeferredResult
     * @return the new DeferredResult
     */
    <U> DeferredResult<U> applyToEither
    (DeferredResult<? extends T> other,
     Function<? super T, U> fn);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when either this or the other given DeferredResult completes,
     * with the result of the given function of either this or the other
     * DeferredResult's result from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied function
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other DeferredResult
     * @param fn    the function to use to compute the value of
     *              the returned DeferredResult
     * @return the new DeferredResult
     */
    <U> DeferredResult<U> applyToEitherAsync
    (DeferredResult<? extends T> other,
     Function<? super T, U> fn);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when either this or the other given DeferredResult completes,
     * with the result of the given function of either this or the other
     * DeferredResult's result from a task running in the
     * given executor.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied function
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other    the other DeferredResult
     * @param fn       the function to use to compute the value of
     *                 the returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    <U> DeferredResult<U> applyToEitherAsync
    (DeferredResult<? extends T> other,
     Function<? super T, U> fn,
     Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when either this or the other given DeferredResult completes,
     * after performing the given action with the result of either this
     * or the other DeferredResult's result.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied action
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other DeferredResult
     * @param block the action to perform before completing the
     *              returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> acceptEither
    (DeferredResult<? extends T> other,
     Consumer<? super T> block);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when either this or the other given DeferredResult completes,
     * after performing the given action with the result of either this
     * or the other DeferredResult's result from a task running in
     * the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied action
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other DeferredResult
     * @param block the action to perform before completing the
     *              returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> acceptEitherAsync
    (DeferredResult<? extends T> other,
     Consumer<? super T> block);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when either this or the other given DeferredResult completes,
     * after performing the given action with the result of either this
     * or the other DeferredResult's result from a task running in
     * the given executor.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied action
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other    the other DeferredResult
     * @param block    the action to perform before completing the
     *                 returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    DeferredResult<Void> acceptEitherAsync
    (DeferredResult<? extends T> other,
     Consumer<? super T> block,
     Executor executor);

    /**
     * Returns a new DeferredResult that is completed
     * when either this or the other given DeferredResult completes,
     * after performing the given action.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied action
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other  the other DeferredResult
     * @param action the action to perform before completing the
     *               returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> runAfterEither(DeferredResult<?> other,
                                        Runnable action);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when either this or the other given DeferredResult completes,
     * after performing the given action from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied action
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other  the other DeferredResult
     * @param action the action to perform before completing the
     *               returned DeferredResult
     * @return the new DeferredResult
     */
    DeferredResult<Void> runAfterEitherAsync
    (DeferredResult<?> other,
     Runnable action);

    /**
     * Returns a new DeferredResult that is asynchronously completed
     * when either this or the other given DeferredResult completes,
     * after performing the given action from a task running in the
     * given executor.
     * <p/>
     * <p>If this and/or the other DeferredResult complete
     * exceptionally, then the returned DeferredResult may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned DeferredResult.  If the supplied action
     * throws an exception, then the returned DeferredResult completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other    the other DeferredResult
     * @param action   the action to perform before completing the
     *                 returned DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    DeferredResult<Void> runAfterEitherAsync
    (DeferredResult<?> other,
     Runnable action,
     Executor executor);

    /**
     * Returns a DeferredResult that upon completion, has the same
     * value as produced by the given function of the result of this
     * DeferredResult.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, then the
     * returned DeferredResult also does so, with a
     * CompletionException holding this exception as its cause.
     * Similarly, if the computed DeferredResult completes
     * exceptionally, then so does the returned DeferredResult.
     *
     * @param fn the function returning a new DeferredResult
     * @return the DeferredResult
     */
    <U> DeferredResult<U> thenCompose
    (Function<? super T, DeferredResult<U>> fn);

    /**
     * Returns a DeferredResult that upon completion, has the same
     * value as that produced asynchronously using the {@link
     * java.util.concurrent.ForkJoinPool#commonPool()} by the given function of the result
     * of this DeferredResult.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, then the
     * returned DeferredResult also does so, with a
     * CompletionException holding this exception as its cause.
     * Similarly, if the computed DeferredResult completes
     * exceptionally, then so does the returned DeferredResult.
     *
     * @param fn the function returning a new DeferredResult
     * @return the DeferredResult
     */
    <U> DeferredResult<U> thenComposeAsync
    (Function<? super T, DeferredResult<U>> fn);

    /**
     * Returns a DeferredResult that upon completion, has the same
     * value as that produced asynchronously using the given executor
     * by the given function of this DeferredResult.
     * <p/>
     * <p>If this DeferredResult completes exceptionally, then the
     * returned DeferredResult also does so, with a
     * CompletionException holding this exception as its cause.
     * Similarly, if the computed DeferredResult completes
     * exceptionally, then so does the returned DeferredResult.
     *
     * @param fn       the function returning a new DeferredResult
     * @param executor the executor to use for asynchronous execution
     * @return the DeferredResult
     */
    <U> DeferredResult<U> thenComposeAsync
    (Function<? super T, DeferredResult<U>> fn,
     Executor executor);

    /**
     * Returns a new DeferredResult that is completed when this
     * DeferredResult completes, with the result of the given
     * function of the exception triggering this DeferredResult's
     * completion when it completes exceptionally; otherwise, if this
     * DeferredResult completes normally, then the returned
     * DeferredResult also completes normally with the same value.
     *
     * @param fn the function to use to compute the value of the
     *           returned DeferredResult if this DeferredResult completed
     *           exceptionally
     * @return the new DeferredResult
     */
    DeferredResult<T> exceptionally
    (Function<Throwable, ? extends T> fn);

    /**
     * Returns a new DeferredResult that is completed when this
     * DeferredResult completes, with the result of the given
     * function of the result and exception of this DeferredResult's
     * completion.  The given function is invoked with the result (or
     * {@code null} if none) and the exception (or {@code null} if none)
     * of this DeferredResult when complete.
     *
     * @param fn the function to use to compute the value of the
     *           returned DeferredResult
     * @return the new DeferredResult
     */
    <U> DeferredResult<U> handle
    (BiFunction<? super T, Throwable, ? extends U> fn);

}
