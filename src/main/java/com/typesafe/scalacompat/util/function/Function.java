package com.typesafe.scalacompat.util.function;

/**
 * @param <T> the type of the input to the apply operation
 * @param <R> the type of the result of the apply operation
 */
public interface Function<T, R> {
    /**
     * Compute the result of applying the function to the input argument
     */
    R apply(T t);

    // TODO: Provide the remaining methods.
}