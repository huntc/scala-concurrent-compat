package com.typesafe.scalacompat.concurrent;

import com.typesafe.scalacompat.util.Optional;
import com.typesafe.scalacompat.util.function.*;

import java.util.concurrent.Callable;

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
     * Produces a result.
     */
    <T> DeferredResult<T> of(Callable<T> action);

    /**
     * Produces a result of a sequence of actions.
     */
    <T> DeferredResult<T> of(Callable<T>... actions);

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
     * When this result is completed successfully (i.e. with a value),
     * apply the provided action to the value.
     * <p/>
     * If the result has already been completed with a value,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    void onSuccess(Consumer<? super T> action);

    /**
     * When this result is completed with a failure (i.e. with a throwable),
     * apply the provided callback to the throwable.
     * <p/>
     * If the result has already been completed with a failure,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * Will not be called in case that the result is completed with a value.
     */
    void onFailure(Consumer<? super Throwable> action);

    /**
     * When this result is completed, either through an exception, or a value,
     * apply the provided action.
     * <p/>
     * If the action has already been completed,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    void onComplete(Runnable action);

    /**
     * Returns whether the result has already been completed with
     * a value or an exception.
     * <p/>
     *
     * @return `true` if the result is already completed, `false` otherwise
     */
    boolean isCompleted();

    /**
     * The value of this `result`.
     * <p/>
     * If the result is not completed the returned value will be empty.
     * If the result is completed the value will be assigned.
     */
    Optional<T> value();

    /* Projections */

    /**
     * Returns a failed projection of this result.
     * <p/>
     * The failed projection is a result holding a value of type `Throwable`.
     * <p/>
     * It is completed with a value which is the throwable of the original result
     * in case the original result is failed.
     */
    DeferredResult<Throwable> failed();

    /* Monadic operations */

    /**
     * Asynchronously processes the value in the result once the value becomes available.
     * <p/>
     * Will not be called if the result fails.
     */
    void forEach(Consumer<? super T> action);

    /**
     * Creates a new future by applying the 's' function to the successful result of
     * this result, or the 'f' function to the failed result. If there is any non-fatal
     * exception thrown when 's' or 'f' is applied, that exception will be propagated
     * to the resulting future.
     *
     * @param s function that transforms a successful result of the receiver into a
     *          successful result of the returned result
     * @param f function that transforms a failure of the receiver into a failure of
     *          the returned result
     * @return a result that will be completed with the transformed value
     */
    <R> DeferredResult<R> transform(Function<? super T, ? extends R> transformer,
                                    Function<? super Throwable, ? super Throwable> f);

    /**
     * Creates a new result by applying a function to the successful result of
     * this one. If this result is completed with an exception then the new
     * result will also contain this exception.
     */
    <R> DeferredResult<R> map(Function<? super T, ? extends R> mapper);

    DeferredResult<Double> mapToDouble(ToDoubleFunction<? super T> mapper);

    DeferredResult<Integer> mapToInt(ToIntFunction<? super T> mapper);

    DeferredResult<Long> mapToLong(ToLongFunction<? super T> mapper);

    /**
     * Creates a new result by applying a function to the successful result of
     * this result, and returns the result of the function as the new result.
     * If this result is completed with an exception then the new result will
     * also contain this exception.
     */
    <R> DeferredResult<R> flatMap(Function<? super T, ? extends DeferredResult<? extends R>> mapper);

    DeferredResult<Double> flatMapToDouble(Function<? super T, ? extends DeferredResult<Double>> mapper);

    DeferredResult<Integer> flatMapToInt(Function<? super T, ? extends DeferredResult<Integer>> mapper);

    DeferredResult<Long> flatMapToLong(Function<? super T, ? extends DeferredResult<Long>> mapper);

    /**
     * Creates a new result by filtering the value of the current result with a predicate.
     * <p/>
     * If the current result contains a value which satisfies the predicate, the new result will also hold that value.
     * Otherwise, the resulting result will fail with a `NoSuchElementException`.
     * <p/>
     * If the current result fails, then the resulting result also fails.
     */
    DeferredResult<T> filter(Predicate<? super T> predicate);

    /**
     * Performs a mutable reduction operation on the elements of this stream. A mutable reduction is one in which
     * the reduced value is a mutable value holder, such as an ArrayList, and elements are incorporated by
     * updating the state of the result, rather than by replacing the result.
     */
    <R> R collect(Supplier<R> resultFactory,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    // TODO: Add:
    // instance methods: recover, recoverWith, zip, fallbackTo, mapTo, andThen
    // static methods: firstCompletedOf, find, fold, reduce, traverse
}
