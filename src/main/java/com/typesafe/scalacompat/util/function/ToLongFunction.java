package com.typesafe.scalacompat.util.function;

/**
 * Apply a function to the input argument, yielding an appropriate result. This is the long-bearing specialization
 * for Function.
 */
public interface ToLongFunction<T> {
    /**
     * Compute the result of applying the function to the input argument
     */
    long applyAsLong(T t);
}
