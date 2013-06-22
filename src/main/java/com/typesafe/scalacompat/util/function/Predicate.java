package com.typesafe.scalacompat.util.function;

/**
 * Determines if the input object matches some criteria.
 */
public interface Predicate<T> {
    /**
     * Returns true if the input object matches some criteria.
     */
    boolean test(T t);

    // TODO: Implement the remaining methods.
}
