package com.typesafe.scalacompat.util.function;

/**
 * Apply a function to the input argument, yielding an appropriate result. This is the int-bearing specialization
 * for Function.
 */
public interface ToIntFunction<T> {
    /**
     * Compute the result of applying the function to the input argument
     */
    int applyAsInt(T t);
}
