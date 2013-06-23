package com.typesafe.scalacompat.concurrent;

import com.typesafe.scalacompat.util.function.BiConsumer;
import com.typesafe.scalacompat.util.function.BiFunction;
import com.typesafe.scalacompat.util.function.Consumer;
import com.typesafe.scalacompat.util.function.Function;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * A {@link Future} that may include dependent functions and actions
 * that trigger upon its completion.
 * <p/>
 * <p>Methods are available for adding dependents based on
 * user-provided Functions, Consumers, or Runnables. The appropriate
 * form to use depends on whether actions require arguments and/or
 * produce results.  Completion of a dependent action will trigger the
 * completion of another DeferredResult.  Actions may also be
 * triggered after either or both the current and another
 * DeferredResult complete.  Multiple DeferredResults may also
 * be grouped as one using {@link #anyOf(DeferredResult...)} and
 * {@link #allOf(DeferredResult...)}.
 * <p/>
 * <p>DeferredResults themselves do not execute asynchronously.
 * However, actions supplied for dependent completions of another
 * DeferredResult may do so, depending on whether they are provided
 * via one of the <em>async</em> methods (that is, methods with names
 * of the form <tt><var>xxx</var>Async</tt>).  The <em>async</em>
 * methods provide a way to commence asynchronous processing of an
 * action using either a given {@link Executor} or by default the
 * {@link ForkJoinPool#commonPool()}. To simplify monitoring,
 * debugging, and tracking, all generated asynchronous tasks are
 * instances of the marker interface {@link AsynchronousCompletionTask}.
 * <p/>
 * <p>Actions supplied for dependent completions of <em>non-async</em>
 * methods may be performed by the thread that completes the current
 * DeferredResult, or by any other caller of these methods.  There
 * are no guarantees about the order of processing completions unless
 * constrained by these methods.
 * <p/>
 * <p>Since (unlike {@link FutureTask}) this class has no direct
 * control over the computation that causes it to be completed,
 * cancellation is treated as just another form of exceptional completion.
 * Method {@link #cancel cancel} has the same effect as
 * {@code completeExceptionally(new CancellationException())}.
 * <p/>
 * <p>Upon exceptional completion (including cancellation), or when a
 * completion entails an additional computation which terminates
 * abruptly with an (unchecked) exception or error, then all of their
 * dependent completions (and their dependents in turn) generally act
 * as {@code completeExceptionally} with a {@link CompletionException}
 * holding that exception as its cause.  However, the {@link
 * #exceptionally exceptionally} and {@link #handle handle}
 * completions <em>are</em> able to handle exceptional completions of
 * the DeferredResults they depend on.
 * <p/>
 * <p>In case of exceptional completion with a CompletionException,
 * methods {@link #get()} and {@link #get(long, TimeUnit)} throw an
 * {@link ExecutionException} with the same cause as held in the
 * corresponding CompletionException.  However, in these cases,
 * methods {@link #join()} and {@link #getNow} throw the
 * CompletionException, which simplifies usage.
 * <p/>
 * <p>Arguments used to pass a completion result (that is, for parameters
 * of type {@code T}) may be null, but passing a null value for any other
 * parameter will result in a {@link NullPointerException} being thrown.
 *
 * @author Doug Lea
 * @since 1.8
 */
public interface DeferredResult<T> {
    /**
     * Returns the result value when complete, or throws an
     * (unchecked) exception if completed exceptionally. To better
     * conform with the use of common functional forms, if a
     * computation involved in the completion of this
     * DeferredResult threw an exception, this method throws an
     * (unchecked) {@link java.util.concurrent.CompletionException} with the underlying
     * exception as its cause.
     *
     * @return the result value
     * @throws java.util.concurrent.CancellationException
     *          if the computation was cancelled
     * @throws java.util.concurrent.CompletionException
     *          if this future completed
     *          exceptionally or a completion computation threw an exception
     */
    T join();

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

    /**
     * Returns a new DeferredResult that is completed when all of
     * the given DeferredResults complete.  If any of the given
     * DeferredResults complete exceptionally, then the returned
     * DeferredResult also does so, with a CompletionException
     * holding this exception as its cause.  Otherwise, the results,
     * if any, of the given DeferredResults are not reflected in
     * the returned DeferredResult, but may be obtained by
     * inspecting them individually. If no DeferredResults are
     * provided, returns a DeferredResult completed with the value
     * {@code null}.
     * <p/>
     * <p>Among the applications of this method is to await completion
     * of a set of independent DeferredResults before continuing a
     * program, as in: {@code DeferredResult.allOf(c1, c2,
     *c3).join();}.
     *
     * @param cfs the DeferredResults
     * @return a new DeferredResult that is completed when all of the
     *         given DeferredResults complete
     * @throws NullPointerException if the array or any of its elements are
     *                              {@code null}
     */
    public static DeferredResult<Void> allOf(DeferredResult<?>... cfs) {
        return null;
    }

    /**
     * Returns a new DeferredResult that is completed when any of
     * the given DeferredResults complete, with the same result.
     * Otherwise, if it completed exceptionally, the returned
     * DeferredResult also does so, with a CompletionException
     * holding this exception as its cause.  If no DeferredResults
     * are provided, returns an incomplete DeferredResult.
     *
     * @param cfs the DeferredResults
     * @return a new DeferredResult that is completed with the
     *         result or exception of any of the given DeferredResults when
     *         one completes
     * @throws NullPointerException if the array or any of its elements are
     *                              {@code null}
     */
    public static DeferredResult<Object> anyOf(DeferredResult<?>... cfs) {
        return null;
    }
}
