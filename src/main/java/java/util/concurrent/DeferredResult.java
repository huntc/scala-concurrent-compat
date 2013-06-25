package java.util.concurrent;

import java.util.Optional;
import java.util.function.*;

/**
 * A DeferredResult is a Future that can be observed. Actions are provided
 * to execute when the results of operations are available.
 * </p>
 * DeferredResults are similar to Futures except their results cannot
 * be blocked for.
 * </p>
 * Actions are performed at whatever time and in whatever thread the library chooses unless an
 * Executor is supplied.
 * <p/>
 *
 * @param <T> to type of result to produce.
 */
public interface DeferredResult<T> {

    /* Creational methods */

    /**
     * Produces a future result.
     */
    static <T> DeferredResult<T> of(Supplier<T> supplier) {
        return of(ForkJoinPool.commonPool(), supplier);
    }

    static <T> DeferredResult<T> of(Executor executor, Supplier<T> supplier) {
        //FIXME: provide implementation - would return a CompletableFuture to obtain a default DeferredResult
        return null;
    }

    /**
     * Produces a future result of a sequence of actions.
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
     * Returns a `Future` to the result of the first future in the list that is completed.
     */
    static <T> DeferredResult<T> firstCompletedOf(DeferredResult<T>... futures) {
        return firstCompletedOf(ForkJoinPool.commonPool(), futures);
    }

    static <T> DeferredResult<T> firstCompletedOf(Executor executor, DeferredResult<T>... futures) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * Returns a `Future` that will hold the optional result of the first `Future` with a result that matches the predicate.
     */
    static <T> DeferredResult<Optional<T>> find(
            Predicate<? super T> predicate,
            DeferredResult<T>... futures) {
        return find(ForkJoinPool.commonPool(), predicate, futures);
    }

    static <T> DeferredResult<Optional<T>> find(
            Executor executor,
            Predicate<? super T> predicate,
            DeferredResult<T>... futures) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * A non-blocking fold over the specified futures, with the start value of the given zero.
     * The fold is performed on the thread where the last future is completed,
     * the result will be the first failure of any of the futures, or any failure in the actual fold,
     * or the result of the fold.
     */
    static <T, R> DeferredResult<T> fold(Function<? super T, ? extends R> zero,
                                         BiFunction<? super R, ? super T, ? extends R> folder,
                                         DeferredResult<T>... futures) {
        return fold(ForkJoinPool.commonPool(), zero, folder, futures);
    }

    static <T, R> DeferredResult<T> fold(Executor executor,
                                         Function<? super T, ? extends R> zero,
                                         BiFunction<? super R, ? super T, ? extends R> folder,
                                         DeferredResult<T>... futures) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * Initiates a fold over the supplied futures where the fold-zero is the result value of the `Future`
     * that's completed first.
     */
    static <T, R> DeferredResult<T> reduce(BiFunction<? super R, ? super T, ? extends R> folder,
                                           DeferredResult<T>... futures) {
        return reduce(ForkJoinPool.commonPool(), folder, futures);
    }

    static <T, R> DeferredResult<T> reduce(Executor executor,
                                           BiFunction<? super R, ? super T, ? extends R> folder,
                                           DeferredResult<T>... futures) {
        //FIXME: provide implementation
        return null;
    }

    /**
     * Transforms a list of values into a list of futures produced using that value.
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
     * When this future is completed successfully (i.e. with a value),
     * apply the action function to the value.
     * <p/>
     * If the future has already been completed with a value,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    default void onSuccess(Consumer<? super T> action) {
        onSuccess(action, ForkJoinPool.commonPool());
    }

    void onSuccess(Consumer<? super T> action, Executor executor);

    /**
     * When this future is completed with a failure (i.e. with a throwable),
     * apply the provided callback to the throwable.
     * <p/>
     * If the future has already been completed with a failure,
     * this will either be applied immediately or be scheduled asynchronously.
     * <p/>
     * Will not be called in case that the future is completed with a value.
     */
    default void onFailure(Consumer<? super Throwable> failure) {
        onFailure(failure, ForkJoinPool.commonPool());
    }

    void onFailure(Consumer<? super Throwable> failure, Executor executor);

    /**
     * When this future result is completed, either through an exception, or a value,
     * apply the provided action.
     * <p/>
     * If the future result has already been completed,
     * this will either be applied immediately or be scheduled asynchronously.
     */
    default void onComplete(Runnable action) {
        onComplete(action, ForkJoinPool.commonPool());
    }

    void onComplete(Runnable action, Executor executor);

    /* Miscellaneous */

    /**
     * Returns whether the future has already been completed with
     * a value or an exception.
     *
     * @return `true` if the future is already completed, `false` otherwise
     */
    boolean isCompleted();

    /**
     * The value of this current `Future`.
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
    default void forEach(Consumer<? super T> action) {
        forEach(action, ForkJoinPool.commonPool());
    }

    void forEach(Consumer<? super T> action, Executor executor);

    /**
     * Creates a new future by applying the transformer to the successful result of
     * this future, or the failure to the failed result. If there is any non-fatal
     * exception thrown when transformer or failure is applied, that exception will be propagated
     * to the resulting future.
     */
    default <R> DeferredResult<R> transform(Function<? super T, ? extends R> transformer,
                                            Function<? super Throwable, ? super Throwable> failure) {
        return transform(transformer, failure, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> transform(Function<? super T, ? extends R> transformer,
                                    Function<? super Throwable, ? super Throwable> failure,
                                    Executor executor);

    /**
     * Creates a new future by applying a function to the successful result of
     * this future. If this future is completed with an exception then the new
     * future will also contain this exception.
     */
    default <R> DeferredResult<R> map(Function<? super T, ? extends R> mapper) {
        return map(mapper, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> map(Function<? super T, ? extends R> mapper, Executor executor);

    /**
     * Creates a new future by applying a function to the successful result of
     * this future, and returns the result of the function as the new future.
     * If this future is completed with an exception then the new future will
     * also contain this exception.
     */
    default <R> DeferredResult<R> flatMap(Function<? super T, ? extends DeferredResult<? extends R>> mapper) {
        return flatMap(mapper, ForkJoinPool.commonPool());
    }

    <R> DeferredResult<R> flatMap(Function<? super T, ? extends DeferredResult<? extends R>> mapper, Executor executor);

    /**
     * Creates a new future by filtering the value of the current future with a predicate.
     * <p/>
     * If the current future contains a value which satisfies the predicate, the new future will also hold that value.
     * Otherwise, the resulting future will fail with a `NoSuchElementException`.
     * <p/>
     * If the current future fails, then the resulting future also fails.
     */
    default DeferredResult<T> filter(Predicate<? super T> predicate) {
        return filter(predicate, ForkJoinPool.commonPool());
    }

    DeferredResult<T> filter(Predicate<? super T> predicate, Executor executor);

    /**
     * Performs a mutable reduction operation on the future. A mutable reduction is one in which
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
     * Creates a new future that will handle any matching throwable that this
     * future might contain. If there is no match, or if this future contains
     * a valid result then the new future will contain the same.
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
     * Creates a new future which holds the result of this future if it was completed successfully, or, if not,
     * the result of the `that` future if `that` is completed successfully.
     * If both futures are failed, the resulting future holds the throwable object of the first future.
     * <p/>
     * Using this method will not cause concurrent programs to become nondeterministic.
     */
    <R extends T> DeferredResult<R> fallbackTo(Function<? super T, ? extends R> fallback);

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
    default DeferredResult<T> andThen(Consumer<? super T> action) {
        return andThen(action, ForkJoinPool.commonPool());
    }

    DeferredResult<T> andThen(Consumer<? super T> action, Executor executor);

}

