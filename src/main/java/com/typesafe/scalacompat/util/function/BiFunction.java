package com.typesafe.scalacompat.util.function;

/**
 * @param <T> the type of the input to the apply operation
 * @param <U> the type of the result of the apply operation
 * @param <R> the type of the result of the apply operation
 */
public interface BiFunction<T, U, R> {
    /**
     * Apply a function to the input arguments, yielding an appropriate result.
     * This is the two-arity specialization of Function. A function may variously provide a mapping between types,
     * object instances or keys and values or any other form of transformation upon the input.
     */
    R apply(T t, U u);

    // TODO: Provide the remaining methods.
}