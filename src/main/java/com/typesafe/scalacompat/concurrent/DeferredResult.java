package com.typesafe.scalacompat.concurrent;

import com.typesafe.scalacompat.util.Optional;
import com.typesafe.scalacompat.util.function.Consumer;

/**
 * A DeferredResult is a Future that can be observed. Actions are provided
 * to execute when the results of operations are available.
 * </p>
 * DeferredResults are similar to Futures except their results cannot
 * be blocked for.
 *
 * @param <T> to type of result to produce.
 */
public interface DeferredResult<T> {

    /**
     * When this result is completed successfully (i.e. with a value),
     * apply the provided action to the value.
     * <p/>
     * If the result has already been completed with a value,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * The action may be performed at whatever time and in whatever thread the library chooses.
     */
    void onSuccess(Consumer<T> action);

    /**
     * When this result is completed with a failure (i.e. with a throwable),
     * apply the provided callback to the throwable.
     * <p/>
     * If the result has already been completed with a failure,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * Will not be called in case that the result is completed with a value.
     * <p/>
     * The action may be performed at whatever time and in whatever thread the library chooses.
     */
    void onFailure(Consumer<T> action);

    /**
     * When this result is completed, either through an exception, or a value,
     * apply the provided action.
     * <p/>
     * If the action has already been completed,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * The action may be performed at whatever time and in whatever thread the library chooses.
     */
    void onComplete(Consumer<T> action);

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
     * <p/>
     * It is failed with a `NoSuchElementException` if the original result is completed successfully.
     */
    DeferredResult<Throwable> failed();

    /* Monadic operations */

    /**
     * Asynchronously processes the value in the result once the value becomes available.
     * <p/>
     * Will not be called if the result fails.
     * <p/>
     * For any given element, the action may be performed at whatever time and in whatever thread the library chooses.
     * If the action accesses shared state, it is responsible for providing the required synchronization.
     */
    void forEach(Consumer<? super T> action);

    // TODO: The remaining monadic methods along with the static methods of the companion Future object
}
