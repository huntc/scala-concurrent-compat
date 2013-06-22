package com.typesafe.scalacompat.concurrent;

import com.typesafe.scalacompat.util.Optional;
import com.typesafe.scalacompat.util.function.*;

/**
 * A DeferredResult is a Future that can be observed. Actions are provided
 * to execute when the results of operations are available.
 * </p>
 * DeferredResults are similar to Futures except their results cannot
 * be blocked for.
 * </p>
 * Actions are performed at whatever time and in whatever thread the library chooses.
 *
 * @param <T> to type of result to produce.
 */
public interface DeferredResult<T> {

    /* Creational methods */

    /**
     * Produces a future result.
     */
    <T> DeferredResult<T> of(Supplier<T> supplier);

    /**
     * Produces a future result of a sequence of actions.
     */
    <T> DeferredResult<T> of(Supplier<T>... suppliers);

    /**
     * Creates an already completed result with the specified exception.
     */
    DeferredResult<T> failed(Throwable exception);

    /**
     * Creates an already completed result with the specified exception.
     */
    DeferredResult<T> successful(T t);

    /* Instance methods */

    /**
     * When this future is completed successfully (i.e. with a value),
     * apply the action function to the value.
     * <p/>
     * If the future has already been completed with a value,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    void onSuccess(Consumer<? super T> action);

    /**
     * When this future is completed with a failure (i.e. with a throwable),
     * apply the provided callback to the throwable.
     * <p/>
     * If the future has already been completed with a failure,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * Will not be called in case that the future is completed with a value.
     */
    void onFailure(Consumer<? super Throwable> failure);

    /**
     * When this future result is completed, either through an exception, or a value,
     * apply the provided action.
     * <p/>
     * If the future result has already been completed,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    void onComplete(Runnable action);

    /* Miscellaneous */

    /**
     * Returns whether the future has already been completed with
     * a value or an exception.
     *
     * @return `true` if the future is already completed, `false` otherwise
     */
    boolean isCompleted();

    /**
     * The value of this `Future`.
     */
    Optional<T> value();

    /* Projections */

    /**
     * Returns a failed projection of this future.
     * <p/>
     * The failed projection is a future holding a value of type `Throwable`.
     * <p/>
     * It is completed with a value which is the throwable of the original future
     * in case the original future is failed.
     * <p/>
     * It is failed with a `NoSuchElementException` if the original future is completed successfully.
     */
    DeferredResult<Throwable> failed();

    /* Monadic operations */

    /**
     * Asynchronously processes the value in the future once the value becomes available.
     * <p/>
     * Will not be called if the future fails.
     */
    void forEach(Consumer<? super T> action);

    /**
     * Creates a new future by applying the transformer to the successful result of
     * this future, or the failure to the failed result. If there is any non-fatal
     * exception thrown when transformer or failure is applied, that exception will be propagated
     * to the resulting future.
     */
    <R> DeferredResult<R> transform(Function<? super T, ? extends R> transformer,
                                    Function<? super Throwable, ? super Throwable> failure);

    /**
     * Creates a new future by applying a function to the successful result of
     * this future. If this future is completed with an exception then the new
     * future will also contain this exception.
     */
    <R> DeferredResult<R> map(Function<? super T, ? extends R> mapper);

    DeferredResult<Double> mapToDouble(ToDoubleFunction<? super T> mapper);

    DeferredResult<Integer> mapToInt(ToIntFunction<? super T> mapper);

    DeferredResult<Long> mapToLong(ToLongFunction<? super T> mapper);

    /**
     * Creates a new future by applying a function to the successful result of
     * this future, and returns the result of the function as the new future.
     * If this future is completed with an exception then the new future will
     * also contain this exception.
     */
    <R> DeferredResult<R> flatMap(Function<? super T, ? extends DeferredResult<? extends R>> mapper);

    DeferredResult<Double> flatMapToDouble(Function<? super T, ? extends DeferredResult<Double>> mapper);

    DeferredResult<Integer> flatMapToInt(Function<? super T, ? extends DeferredResult<Integer>> mapper);

    DeferredResult<Long> flatMapToLong(Function<? super T, ? extends DeferredResult<Long>> mapper);

    /**
     * Creates a new future by filtering the value of the current future with a predicate.
     * <p/>
     * If the current future contains a value which satisfies the predicate, the new future will also hold that value.
     * Otherwise, the resulting future will fail with a `NoSuchElementException`.
     * <p/>
     * If the current future fails, then the resulting future also fails.
     */
    DeferredResult<T> filter(Predicate<? super T> predicate);

    /**
     * Performs a mutable reduction operation on the future. A mutable reduction is one in which
     * the reduced value is a mutable value holder, such as an ArrayList, and elements are incorporated by
     * updating the state of the result, rather than by replacing the result.
     */
    <R> R collect(Supplier<R> resultFactory,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * Creates a new future that will handle any matching throwable that this
     * future might contain. If there is no match, or if this future contains
     * a valid result then the new future will contain the same.
     */
    <R> DeferredResult<R> recover(Function<? super Throwable, ? extends R> recovery);

    <R> DeferredResult<R> recoverWith(Function<? super Throwable, DeferredResult<? extends R>> recovery);

    // No zip method as there are no tuples in Java.

    /**
     * Creates a new future which holds the result of this future if it was completed successfully, or, if not,
     * the result of the `that` future if `that` is completed successfully.
     * If both futures are failed, the resulting future holds the throwable object of the first future.
     * <p/>
     * Using this method will not cause concurrent programs to become nondeterministic.
     */
    <R extends T> DeferredResult<R> fallbackTo(Function<? super T, ? extends R> fallback);

    // No mapTo method as ClassTags are not available in Java.

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
     */
    DeferredResult<T> andThen(Consumer<? super T> action);

}
// TODO: Add:
// static methods: firstCompletedOf, find, fold, reduce, traverse
