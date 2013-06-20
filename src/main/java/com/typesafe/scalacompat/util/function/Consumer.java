package com.typesafe.scalacompat.util.function;

/**
 * An operation which accepts a single input argument and returns no result. Unlike most other functional
 * interfaces, Consumer is expected to operate via side-effects
 *
 * @param <T> The type of input objects to accept
 */
public abstract class Consumer<T> {
    /**
     * Accept an input value
     */
    public abstract void accept(T t);

    /**
     * Returns a Consumer which performs, in sequence, the operation represented by this object followed by the
     * operation represented by the other Consumer.
     * <p/>
     * Any exceptions thrown by either accept method are relayed to the caller; if performing this operation
     * throws an exception, the other operation will not be performed.
     */
    public Consumer<T> chain(Consumer<? super T> other) {
        return (Consumer<T>) other; // FIXME: Don't really understand what this does yet.
    };
}
