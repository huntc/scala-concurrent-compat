package com.typesafe.scalacompat.concurrent;

import scala.concurrent.Promise$class;
import scala.util.Try;

public class Promise<T> implements scala.concurrent.Promise<T> {

    public Promise() {
        Promise$class.$init$(this);
    }

    /**
     * Future containing the value of this promise.
     */
    @Override
    public scala.concurrent.Future<T> future() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns whether the promise has already been completed with
     * a value or an exception.
     * <p/>
     * $nonDeterministic
     *
     * @return `true` if the promise is already completed, `false` otherwise
     */
    @Override
    public boolean isCompleted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Completes the promise with either an exception or a value.
     *
     * @param result Either the value or the exception to complete the promise with.
     *               <p/>
     *               $promiseCompletion
     */
    @Override
    public scala.concurrent.Promise<T> complete(Try<T> result) {
        return Promise$class.complete(this, result);
    }

    /**
     * Tries to complete the promise with either a value or the exception.
     * <p/>
     * $nonDeterministic
     *
     * @return If the promise has already been completed returns `false`, or `true` otherwise.
     */
    @Override
    public boolean tryComplete(Try<T> result) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Completes the promise with a value.
     *
     * @param v The value to complete the promise with.
     *          <p/>
     *          $promiseCompletion
     */
    @Override
    public scala.concurrent.Promise<T> success(T v) {
        return Promise$class.success(this, v);
    }

    /**
     * Tries to complete the promise with a value.
     * <p/>
     * $nonDeterministic
     *
     * @return If the promise has already been completed returns `false`, or `true` otherwise.
     */
    @Override
    public boolean trySuccess(T value) {
        return Promise$class.trySuccess(this, value);
    }

    /**
     * Completes the promise with an exception.
     *
     * @param t The throwable to complete the promise with.
     *          <p/>
     *          $allowedThrowables
     *          <p/>
     *          $promiseCompletion
     */
    @Override
    public scala.concurrent.Promise<T> failure(Throwable t) {
        return Promise$class.failure(this, t);
    }

    /**
     * Tries to complete the promise with an exception.
     * <p/>
     * $nonDeterministic
     *
     * @return If the promise has already been completed returns `false`, or `true` otherwise.
     */
    @Override
    public boolean tryFailure(Throwable t) {
        return Promise$class.tryFailure(this, t);
    }

    /** Completes this promise with the specified future, once that future is completed.
     *
     *  @return   This promise
     */
    @Override
    public scala.concurrent.Promise<T> completeWith(scala.concurrent.Future<T> other) {
        return Promise$class.completeWith(this, other);
    }

    /** Attempts to complete this promise with the specified future, once that future is completed.
     *
     *  @return   This promise
     */
    @Override
    public scala.concurrent.Promise<T> tryCompleteWith(scala.concurrent.Future<T> other) {
        return Promise$class.tryCompleteWith(this, other);
    }
}
