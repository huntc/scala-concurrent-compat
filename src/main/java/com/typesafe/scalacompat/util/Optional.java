package com.typesafe.scalacompat.util;

import scala.Option;

/**
 * A container object which may or may not contain a non-null value. If a value is present, isPresent() will return
 * true and get() will return the value.
 * <p/>
 * Additional methods that depend on the presence or absence of a contained value are provided, such as orElse()
 * (return a default value if value not present) and ifPresent() (execute a block of code if the value is present).
 * <p/>
 * FIXME: Move into its own library.
 *
 * @param <T>
 */
public class Optional<T> {

    // Wraps a Scala Option.
    private final Option<T> option;

    protected Optional(final T value) {
        this.option = Option.apply(value);
    }

    /**
     * Returns an empty Optional instance.
     */
    public static <T> Optional<T> empty() {
        return (Optional<T>) of(Option.empty().get());
    }

    /**
     * Return an Optional with the specified present value
     */
    public static <T> Optional<T> of(T value) {
        return new Optional(value);
    }

    // FIXME: More methods required from the JDK Optional type.
}
