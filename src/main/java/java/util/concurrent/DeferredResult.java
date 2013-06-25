package java.util.concurrent;

import java.util.Optional;
import java.util.function.*;

/**
 * A DeferredResult is similar to {@link Future} in that it represents the result of an asynchronous
 * computation. Unlike {@link Future} DeferredResults can be observed and actions are provided
 * to execute when the results become available.
 * </p>
 * Actions are performed in the {@link java.util.concurrent.ForkJoinPool#commonPool()} unless an
 * Executor is supplied. All operations for DeferredResult are designed to be non-blocking.
 * <p/>
 *
 * @param <T> to type of result to produce.
 * @author The Typesafe Team
 * @since 1.8
 */
public interface DeferredResult<T> {

    /* Creational methods */

    /**
     * Produces a deferred result.
     */
    static <T> DeferredResult<T> of(Supplier<T> supplier) {
        return of(ForkJoinPool.commonPool(), supplier);
    }

    static <T> DeferredResult<T> of(Executor executor, Supplier<T> supplier) {
        //FIXME: provide implementation - would return a CompletableFuture to obtain a default DeferredResult
        return null;
    }

    /**
     * Produces a deferred result of a sequence of actions.
     */
    static <T> DeferredResult<T> of(Supplier<T>... suppliers) {
        return of(ForkJoinPool.commonPool(), suppliers);
    }

    static <T> DeferredResult<T> of(Executor executor, Supplier<T>... suppliers) {
        //FIXME: provide implementation - would return a CompletableFuture to obtain a default DeferredResult
        return null;
    }

    /**
     * Creates an already completed result with the specified exception.
     */
    static <T> DeferredResult<T> failed(Throwable exception) {
        //FIXME: provide implementation - would return a CompletableFuture to obtain a default DeferredResult
        return null;
    }

    /**
     * Creates an already completed result with the specified exception.
     */
    static <T> DeferredResult<T> successful(T t) {
        //FIXME: provide implementation - would return a CompletableFuture to obtain a default DeferredResult
        return null;
    }

    /* Traversing methods */

    /**
     * Returns a result to the value of the first result in the list that is completed.
     */
    static <T> DeferredResult<T> firstCompletedOf(DeferredResult<T>... results) {
        return firstCompletedOf(ForkJoinPool.commonPool(), results);
    }

    static <T> DeferredResult<T> firstCompletedOf(Executor executor, DeferredResult<T>... results) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * Returns a result that will hold the optional value of the first result with a value that matches the predicate.
     */
    static <T> DeferredResult<Optional<T>> find(
            Predicate<? super T> predicate,
            DeferredResult<T>... results) {
        return find(ForkJoinPool.commonPool(), predicate, results);
    }

    static <T> DeferredResult<Optional<T>> find(
            Executor executor,
            Predicate<? super T> predicate,
            DeferredResult<T>... results) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * A non-blocking fold over the specified results, with the start value supplied by the `zero` function.
     * The fold is performed on the thread where the last result is completed.
     * The result will be the first failure of any of the results, or any failure in the actual fold,
     * or the product of the fold.
     */
    static <T, R> DeferredResult<T> fold(Function<? super T, ? extends R> zero,
                                         BiFunction<? super R, ? super T, ? extends R> folder,
                                         DeferredResult<T>... results) {
        return fold(ForkJoinPool.commonPool(), zero, folder, results);
    }

    static <T, R> DeferredResult<T> fold(Executor executor,
                                         Function<? super T, ? extends R> zero,
                                         BiFunction<? super R, ? super T, ? extends R> folder,
                                         DeferredResult<T>... results) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * Initiates a fold over the supplied results where the fold-zero is the  value of the result
     * that is completed first.
     */
    static <T, R> DeferredResult<T> reduce(BiFunction<? super R, ? super T, ? extends R> folder,
                                           DeferredResult<T>... results) {
        return reduce(ForkJoinPool.commonPool(), folder, results);
    }

    static <T, R> DeferredResult<T> reduce(Executor executor,
                                           BiFunction<? super R, ? super T, ? extends R> folder,
                                           DeferredResult<T>... results) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * Transforms a list of values into a list of results produced using that value.
     * This is useful for performing a parallel map.
     */
    static <T, A> Iterable<DeferredResult<T>> traverse(Iterable<A> collection,
                                                       Function<? super A, DeferredResult<? extends T>>... suppliers) {
        return traverse(ForkJoinPool.commonPool(), collection, suppliers);
    }

    static <T, A> Iterable<DeferredResult<T>> traverse(Executor executor,
                                                       Iterable<A> collection,
                                                       Function<? super A, DeferredResult<? extends T>>... suppliers) {
        //FIXME: provide implementation
        return null;
    }

    /* Instance methods */

    /**
     * When this result is completed successfully (i.e. with a value),
     * apply the action function to the value.
     * <p/>
     * If the result has already been completed with a value,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    default void onSuccess(Consumer<? super T> action) {
        onSuccess(action, ForkJoinPool.commonPool());
    }

    void onSuccess(Consumer<? super T> action, Executor executor);

    /**
     * When this result is completed with a failure (i.e. with a throwable),
     * apply the provided callback to the throwable.
     * <p/>
     * If the result has already been completed with a failure,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * Will not be called in case that the result is completed with a value.
     */
    default void onFailure(Consumer<? super Throwable> failure) {
        onFailure(failure, ForkJoinPool.commonPool());
    }

    void onFailure(Consumer<? super Throwable> failure, Executor executor);

    /**
     * When this deferred result is completed, either through an exception, or a value,
     * apply the provided action.
     * <p/>
     * If the deferred result has already been completed,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    default void onComplete(Runnable action) {
        onComplete(action, ForkJoinPool.commonPool());
    }

    void onComplete(Runnable action, Executor executor);

    /* Miscellaneous */

    /**
     * Returns whether the result has already been completed with
     * a value or an exception.
     *
     * @return `true` if the result is already completed, `false` otherwise
     */
    boolean isCompleted();

    /**
     * The value of this current result.
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
     * It is failed with a {@link java.util.NoSuchElementException} if the original result is completed successfully.
     */
    DeferredResult<Throwable> failed();

    /* Monadic operations */

    /**
     * Asynchronously processes the value in the result once the value becomes available.
     * <p/>
     * Will not be called if the result fails.
     */
    default void forEach(Consumer<? super T> action) {
        forEach(action, ForkJoinPool.commonPool());
    }

    void forEach(Consumer<? super T> action, Executor executor);

    /**
     * Creates a new result by applying the transformer to the successful value of
     * this result, or the failure to the failed value. If there is any non-fatal
     * exception thrown when transformer or failure is applied, that exception will be propagated
     * to the result returned.
     */
    default <R> DeferredResult<R> transform(Function<? super T, ? extends R> transformer,
                                            Function<? super Throwable, ? super Throwable> failure) {
        return transform(transformer, failure, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> transform(Function<? super T, ? extends R> transformer,
                                    Function<? super Throwable, ? super Throwable> failure,
                                    Executor executor);

    /**
     * Creates a new result by applying a function to the successful value of
     * this result. If this result is completed with an exception then the new
     * result will also contain this exception.
     */
    default <R> DeferredResult<R> map(Function<? super T, ? extends R> mapper) {
        return map(mapper, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> map(Function<? super T, ? extends R> mapper, Executor executor);

    /**
     * Creates a new result by applying a function to the successful value of
     * this result, and returns the result of the function as the new result.
     * If this result is completed with an exception then the new result will
     * also contain this exception.
     */
    default <R> DeferredResult<R> flatMap(Function<? super T, ? extends DeferredResult<? extends R>> mapper) {
        return flatMap(mapper, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> flatMap(Function<? super T, ? extends DeferredResult<? extends R>> mapper, Executor executor);

    /**
     * Creates a new result by filtering the value of the current result with a predicate.
     * <p/>
     * If the current result contains a value which satisfies the predicate, the new result will also hold that value.
     * Otherwise, the returned result will fail with a {@link java.util.NoSuchElementException}.
     * <p/>
     * If the current result fails, then the returned result also fails.
     */
    default DeferredResult<T> filter(Predicate<? super T> predicate) {
        return filter(predicate, ForkJoinPool.commonPool());
    }

    DeferredResult<T> filter(Predicate<? super T> predicate, Executor executor);

    /**
     * Performs a mutable reduction operation on the result. A mutable reduction is one in which
     * the reduced value is a mutable value holder, such as an ArrayList, and elements are incorporated by
     * updating the state of the result, rather than by replacing the result.
     */
    default <R> R collect(Supplier<R> resultFactory,
                          BiConsumer<R, ? super T> accumulator,
                          BiConsumer<R, R> combiner) {
        return collect(resultFactory, accumulator, combiner, ForkJoinPool.commonPool());
    }

    <R> R collect(Supplier<R> resultFactory,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner,
                  Executor executor);

    /**
     * Creates a new result that will handle any matching throwable that this
     * result might contain. If there is no match, or if this result contains
     * a valid result then the new result will contain the same.
     */
    default <R> DeferredResult<R> recover(Function<? super Throwable, ? extends R> recovery) {
        return recover(recovery, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> recover(Function<? super Throwable, ? extends R> recovery, Executor executor);

    default <R> DeferredResult<R> recoverWith(Function<? super Throwable, DeferredResult<? extends R>> recovery) {
        return recoverWith(recovery, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> recoverWith(Function<? super Throwable, DeferredResult<? extends R>> recovery, Executor executor);

    /**
     * Creates a new result which holds the value of this result if it was completed successfully, or, if not,
     * the value of the `fallback` result if `fallback` is completed successfully.
     * If both results are failed, the returned result holds the throwable object of the first result.
     * <p/>
     * Using this method will not cause concurrent programs to become nondeterministic.
     */
    <R extends T> DeferredResult<R> fallbackTo(Function<? super T, ? extends R> fallback);

    /**
     * Applies the side-effecting function to the value of this result, and returns
     * a new result with the value of this result.
     * <p/>
     * This method allows one to enforce that the callbacks are executed in a
     * specified order.
     * <p/>
     * Note that if one of the chained `andThen` callbacks throws
     * an exception, that exception is not propagated to the subsequent `andThen`
     * callbacks. Instead, the subsequent `andThen` callbacks are given the original
     * value of this result.
     */
    default DeferredResult<T> andThen(Consumer<? super T> action) {
        return andThen(action, ForkJoinPool.commonPool());
    }

    DeferredResult<T> andThen(Consumer<? super T> action, Executor executor);

}

