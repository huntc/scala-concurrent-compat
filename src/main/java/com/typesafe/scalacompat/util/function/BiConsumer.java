package com.typesafe.scalacompat.util.function;

/**
 * An operation which accepts two input arguments and returns no result. This is the two-arity specialization
 * of Consumer. Unlike most other functional interfaces, BiConsumer is expected to operate via side-effects.
 */
public abstract class BiConsumer<T, U> {
    /**
     * Performs operations upon the provided objects which may modify those objects and/or external state
     */
    abstract void accept(T t, U u);

    // TODO: Implement the remaining methods.
}
