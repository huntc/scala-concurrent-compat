package com.typesafe.scalacompat.util.function;

/**
 * A supplier of objects. The result objects are either created during the invocation of get() or by some prior action.
 */
public interface Supplier<T> {
    /**
     * Returns an object.
     */
    T get();
}
