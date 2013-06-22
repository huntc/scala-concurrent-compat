package com.typesafe.scalacompat.util.function;

/**
 * Apply a function to the input argument, yielding an appropriate result. This is the double-bearing specialization
 * for Function.
 */
public interface ToDoubleFunction<T> {
    /**
     * Compute the result of applying the function to the input argument
     */
    double applyAsDouble(T t);
}
