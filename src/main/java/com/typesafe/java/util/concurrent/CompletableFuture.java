package com.typesafe.java.util.concurrent;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;

/**
 * /**
 * A {@link Future} that may be explicitly completed (setting its
 * value and status), and may include dependent functions and actions
 * that trigger upon its completion.
 * <p/>
 * <p>When two or more threads attempt to
 * {@link #complete complete},
 * {@link #completeExceptionally completeExceptionally}, or
 * {@link #cancel cancel}
 * a CompletableFuture, only one of them succeeds.
 * <p/>
 * <p>Methods are available for adding dependents based on
 * user-provided Functions, Consumers, or Runnables. The appropriate
 * form to use depends on whether actions require arguments and/or
 * produce results.  Completion of a dependent action will trigger the
 * completion of another CompletableFuture.  Actions may also be
 * triggered after either or both the current and another
 * CompletableFuture complete.  Multiple CompletableFutures may also
 * be grouped as one using {@link #anyOf(CompletableFuture...)} and
 * {@link #allOf(CompletableFuture...)}.
 * <p/>
 * <p>CompletableFutures themselves do not execute asynchronously.
 * However, actions supplied for dependent completions of another
 * CompletableFuture will do. The methods provide a way to commence
 * asynchronous processing of an action using either a given {@link Executor}
 * or by default the {@link ForkJoinPool#commonPool()}. To simplify monitoring,
 * debugging, and tracking, all generated asynchronous tasks are
 * instances of the marker interface {@link AsynchronousCompletionTask}.
 * <p/>
 * <p>Since (unlike {@link FutureTask}) this class has no direct
 * control over the computation that causes it to be completed,
 * cancellation is treated as just another form of exceptional completion.
 * Method {@link #cancel cancel} has the same effect as
 * {@code completeExceptionally(new CancellationException())}.
 * <p/>
 * <p>In case of exceptional completion with a CompletionException,
 * methods {@link #get()} and {@link #get(long, TimeUnit)} throw an
 * {@link ExecutionException} with the same cause as held in the
 * corresponding CompletionException.  However, in these cases,
 * methods {@link #join()} and {@link #value} throw the
 * CompletionException, which simplifies usage.
 * <p/>
 * <p>Arguments used to pass a completion result (that is, for parameters
 * of type {@code T}) may be null, but passing a null value for any other
 * parameter will result in a {@link NullPointerException} being thrown.
 *
 * @author Doug Lea
 * @since 1.8
 */
public class CompletableFuture<T> implements Future<T>, DeferredResult<T> {

    /*
     * Overview:
     *
     * 1. Non-nullness of field result (set via CAS) indicates done.
     * An AltResult is used to box null as a result, as well as to
     * hold exceptions.  Using a single field makes completion fast
     * and simple to detect and trigger, at the expense of a lot of
     * encoding and decoding that infiltrates many methods. One minor
     * simplification relies on the (static) NIL (to box null results)
     * being the only AltResult with a null exception field, so we
     * don't usually need explicit comparisons with NIL. The CF
     * exception propagation mechanics surrounding decoding rely on
     * unchecked casts of decoded results really being unchecked,
     * where user type errors are caught at point of use, as is
     * currently the case in Java. These are highlighted by using
     * SuppressWarnings-annotated temporaries.
     *
     * 2. Waiters are held in a Treiber stack similar to the one used
     * in FutureTask, Phaser, and SynchronousQueue. See their
     * internal documentation for algorithmic details.
     *
     * 3. Completions are also kept in a list/stack, and pulled off
     * and run when completion is triggered. (We could even use the
     * same stack as for waiters, but would give up the potential
     * parallelism obtained because woken waiters help release/run
     * others -- see method postComplete).  Because post-processing
     * may race with direct calls, class Completion opportunistically
     * extends AtomicInteger so callers can claim the action via
     * compareAndSet(0, 1).  The Completion.run methods are all
     * written a boringly similar uniform way (that sometimes includes
     * unnecessary-looking checks, kept to maintain uniformity).
     * There are enough dimensions upon which they differ that
     * attempts to factor commonalities while maintaining efficiency
     * require more lines of code than they would save.
     *
     * 4. The exported then/and/or methods do support a bit of
     * factoring (see doThenApply etc). They must cope with the
     * intrinsic races surrounding addition of a dependent action
     * versus performing the action directly because the task is
     * already complete.  For example, a CF may not be complete upon
     * entry, so a dependent completion is added, but by the time it
     * is added, the target CF is complete, so must be directly
     * executed. This is all done while avoiding unnecessary object
     * construction in safe-bypass cases.
     */

    // preliminaries

    static final class AltResult {
        final Throwable ex; // null only for NIL

        AltResult(Throwable ex) {
            this.ex = ex;
        }
    }

    static final AltResult NIL = new AltResult(null);

    // Fields

    volatile Object result;              // Either the result or boxed AltResult
    volatile WaitNode waiters;           // Treiber stack of threads blocked on get()
    volatile CompletionNode completions; // list (Treiber stack) of completions

    // Basic utilities for triggering and processing completions

    /**
     * Removes and signals all waiting threads and runs all completions.
     */
    final void postComplete() {
        WaitNode q;
        Thread t;
        while ((q = waiters) != null) {
            if (UNSAFE.compareAndSwapObject(this, WAITERS, q, q.next) &&
                    (t = q.thread) != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }

        CompletionNode h;
        Completion c;
        while ((h = completions) != null) {
            if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, h, h.next) &&
                    (c = h.completion) != null)
                c.run();
        }
    }

    /**
     * Triggers completion with the encoding of the given arguments:
     * if the exception is non-null, encodes it as a wrapped
     * CompletionException unless it is one already.  Otherwise uses
     * the given result, boxed as NIL if null.
     */
    final void internalComplete(T v, Throwable ex) {
        if (result == null)
            UNSAFE.compareAndSwapObject
                    (this, RESULT, null,
                            (ex == null) ? (v == null) ? NIL : v :
                                    new AltResult((ex instanceof CompletionException) ? ex :
                                            new CompletionException(ex)));
        postComplete(); // help out even if not triggered
    }

    /**
     * If triggered, helps release and/or process completions.
     */
    final void helpPostComplete() {
        if (result != null)
            postComplete();
    }

    /* ------------- waiting for completions -------------- */

    /**
     * Number of processors, for spin control
     */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * Heuristic spin value for waitingGet() before blocking on
     * multiprocessors
     */
    static final int SPINS = (NCPU > 1) ? 1 << 8 : 0;

    /**
     * Linked nodes to record waiting threads in a Treiber stack.  See
     * other classes such as Phaser and SynchronousQueue for more
     * detailed explanation. This class implements ManagedBlocker to
     * avoid starvation when blocking actions pile up in
     * ForkJoinPools.
     */
    static final class WaitNode implements ForkJoinPool.ManagedBlocker {
        long nanos;          // wait time if timed
        final long deadline; // non-zero if timed
        volatile int interruptControl; // > 0: interruptible, < 0: interrupted
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode(boolean interruptible, long nanos, long deadline) {
            this.thread = Thread.currentThread();
            this.interruptControl = interruptible ? 1 : 0;
            this.nanos = nanos;
            this.deadline = deadline;
        }

        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (Thread.interrupted()) {
                int i = interruptControl;
                interruptControl = -1;
                if (i > 0)
                    return true;
            }
            if (deadline != 0L &&
                    (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                thread = null;
                return true;
            }
            return false;
        }

        public boolean block() {
            if (isReleasable())
                return true;
            else if (deadline == 0L)
                LockSupport.park(this);
            else if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
    }

    /**
     * Returns raw result after waiting, or null if interruptible and
     * interrupted.
     */
    private Object waitingGet(boolean interruptible) {
        WaitNode q = null;
        boolean queued = false;
        int spins = SPINS;
        for (Object r; ; ) {
            if ((r = result) != null) {
                if (q != null) { // suppress unpark
                    q.thread = null;
                    if (q.interruptControl < 0) {
                        if (interruptible) {
                            removeWaiter(q);
                            return null;
                        }
                        Thread.currentThread().interrupt();
                    }
                }
                postComplete(); // help release others
                return r;
            } else if (spins > 0) {
                int rnd = ThreadLocalRandom.nextSecondarySeed();
                if (rnd == 0)
                    rnd = ThreadLocalRandom.current().nextInt();
                if (rnd >= 0)
                    --spins;
            } else if (q == null)
                q = new WaitNode(interruptible, 0L, 0L);
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, WAITERS,
                        q.next = waiters, q);
            else if (interruptible && q.interruptControl < 0) {
                removeWaiter(q);
                return null;
            } else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q);
                } catch (InterruptedException ex) {
                    q.interruptControl = -1;
                }
            }
        }
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param nanos time to wait
     * @return raw result
     */
    private Object timedAwaitDone(long nanos)
            throws InterruptedException, TimeoutException {
        WaitNode q = null;
        boolean queued = false;
        for (Object r; ; ) {
            if ((r = result) != null) {
                if (q != null) {
                    q.thread = null;
                    if (q.interruptControl < 0) {
                        removeWaiter(q);
                        throw new InterruptedException();
                    }
                }
                postComplete();
                return r;
            } else if (q == null) {
                if (nanos <= 0L)
                    throw new TimeoutException();
                long d = System.nanoTime() + nanos;
                q = new WaitNode(true, nanos, d == 0L ? 1L : d); // avoid 0
            } else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, WAITERS,
                        q.next = waiters, q);
            else if (q.interruptControl < 0) {
                removeWaiter(q);
                throw new InterruptedException();
            } else if (q.nanos <= 0L) {
                if (result == null) {
                    removeWaiter(q);
                    throw new TimeoutException();
                }
            } else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q);
                } catch (InterruptedException ex) {
                    q.interruptControl = -1;
                }
            }
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid
     * accumulating garbage.  Internal nodes are simply unspliced
     * without CAS since it is harmless if they are traversed anyway
     * by releasers.  To avoid effects of unsplicing from already
     * removed nodes, the list is retraversed in case of an apparent
     * race.  This is slow when there are a lot of nodes, but we don't
     * expect lists to be long enough to outweigh higher-overhead
     * schemes.
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (; ; ) {          // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    } else if (!UNSAFE.compareAndSwapObject(this, WAITERS, q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    /* ------------- Async tasks -------------- */

    /**
     * A marker interface identifying asynchronous tasks produced by
     * these methods. This may be useful for monitoring,
     * debugging, and tracking asynchronous activities.
     *
     * @since 1.8
     */
    public static interface AsynchronousCompletionTask {
    }

    /**
     * Base class can act as either FJ or plain Runnable
     */
    abstract static class Async extends ForkJoinTask<Void>
            implements Runnable, AsynchronousCompletionTask {
        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void v) {
        }

        public final void run() {
            exec();
        }
    }

    static final class AsyncRun extends Async {
        final Runnable fn;
        final CompletableFuture<Void> dst;

        AsyncRun(Runnable fn, CompletableFuture<Void> dst) {
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<Void> d;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    fn.run();
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                }
                d.internalComplete(null, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncSupply<U> extends Async {
        final Supplier<U> fn;
        final CompletableFuture<U> dst;

        AsyncSupply(Supplier<U> fn, CompletableFuture<U> dst) {
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<U> d;
            U u;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    u = fn.get();
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                    u = null;
                }
                d.internalComplete(u, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncAccept<T> extends Async {
        final T arg;
        final Consumer<? super T> fn;
        final CompletableFuture<Void> dst;

        AsyncAccept(T arg, Consumer<? super T> fn,
                    CompletableFuture<Void> dst) {
            this.arg = arg;
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<Void> d;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    fn.accept(arg);
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                }
                d.internalComplete(null, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    /* ------------- Completions -------------- */

    /**
     * Simple linked list nodes to record completions, used in
     * basically the same way as WaitNodes. (We separate nodes from
     * the Completions themselves mainly because for the And and Or
     * methods, the same Completion object resides in two lists.)
     */
    static final class CompletionNode {
        final Completion completion;
        volatile CompletionNode next;

        CompletionNode(Completion completion) {
            this.completion = completion;
        }
    }

    // Opportunistically subclass AtomicInteger to use compareAndSet to claim.
    abstract static class Completion extends AtomicInteger implements Runnable {
    }

    static final class HandleCompletion<T, U> extends Completion {
        final CompletableFuture<? extends T> src;
        final BiFunction<? super T, Throwable, ? extends U> fn;
        final CompletableFuture<U> dst;

        HandleCompletion(CompletableFuture<? extends T> src,
                         BiFunction<? super T, Throwable, ? extends U> fn,
                         CompletableFuture<U> dst) {
            this.src = src;
            this.fn = fn;
            this.dst = dst;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final BiFunction<? super T, Throwable, ? extends U> fn;
            final CompletableFuture<U> dst;
            Object r;
            T t;
            Throwable ex;
            if ((dst = this.dst) != null &&
                    (fn = this.fn) != null &&
                    (a = this.src) != null &&
                    (r = a.result) != null &&
                    compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked") T tr = (T) r;
                    t = tr;
                }
                U u = null;
                Throwable dx = null;
                try {
                    u = fn.apply(t, ex);
                } catch (Throwable rex) {
                    dx = rex;
                }
                dst.internalComplete(u, dx);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    private <U> CompletableFuture<U> doHandle
            (BiFunction<? super T, Throwable, ? extends U> fn) {
        if (fn == null) throw new NullPointerException();
        CompletableFuture<U> dst = new CompletableFuture<U>();
        HandleCompletion<T, U> d = null;
        Object r;
        if ((r = result) == null) {
            CompletionNode p =
                    new CompletionNode(d = new HandleCompletion<T, U>(this, fn, dst));
            while ((r = result) == null) {
                if (UNSAFE.compareAndSwapObject(this, COMPLETIONS,
                        p.next = completions, p))
                    break;
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked") T tr = (T) r;
                t = tr;
            }
            U u;
            Throwable dx;
            try {
                u = fn.apply(t, ex);
                dx = null;
            } catch (Throwable rex) {
                dx = rex;
                u = null;
            }
            dst.internalComplete(u, dx);
        }
        helpPostComplete();
        return dst;
    }

    // public methods

    /**
     * Creates a new incomplete CompletableFuture.
     */
    public CompletableFuture() {
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the given executor with the value obtained
     * by calling the given Supplier.
     *
     * @param supplier a function returning the value to be used
     *                 to complete the returned CompletableFuture
     * @return the new DeferredResult
     */
    public static <U> DeferredResult<U> of(Supplier<U> supplier) {
        return of(supplier, ForkJoinPool.commonPool());
    }

    public static <U> DeferredResult<U> of(Supplier<U> supplier,
                                           Executor executor) {
        if (executor == null || supplier == null)
            throw new NullPointerException();
        CompletableFuture<U> f = new CompletableFuture<U>();
        executor.execute(new AsyncSupply<U>(supplier, f));
        return f;
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the given executor after it runs the given
     * action.
     *
     * @param runnable the action to run before completing the
     *                 returned CompletableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new DeferredResult
     */
    public static DeferredResult<Void> of(Runnable runnable) {
        return of(runnable, ForkJoinPool.commonPool());
    }

    public static DeferredResult<Void> of(Runnable runnable,
                                          Executor executor) {
        if (executor == null || runnable == null)
            throw new NullPointerException();
        CompletableFuture<Void> f = new CompletableFuture<Void>();
        executor.execute(new AsyncRun(runnable, f));
        return f;
    }

    /**
     * Returns a new CompletableFuture that is already completed with
     * the given value.
     *
     * @param value the value
     * @return the completed DeferredResult
     */
    public static <U> DeferredResult<U> successful(U value) {
        CompletableFuture<U> f = new CompletableFuture<U>();
        f.result = (value == null) ? NIL : value;
        return f;
    }

    /**
     * Returns a new CompletableFuture that is already completed with
     * a throwable.
     *
     * @param value the throwable
     * @return the completed DeferredResult
     */
    public static DeferredResult<Throwable> failed(Throwable value) {
        CompletableFuture<Throwable> f = new CompletableFuture<>();
        f.result = (value == null) ? NIL : new AltResult(value);
        return f;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        Object r;
        Throwable ex, cause;
        if ((r = result) == null && (r = waitingGet(true)) == null)
            throw new InterruptedException();
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked") T tr = (T) r;
            return tr;
        }
        if ((ex = ((AltResult) r).ex) == null)
            return null;
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if ((ex instanceof CompletionException) &&
                (cause = ex.getCause()) != null)
            ex = cause;
        throw new ExecutionException(ex);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Object r;
        Throwable ex, cause;
        long nanos = unit.toNanos(timeout);
        if (Thread.interrupted())
            throw new InterruptedException();
        if ((r = result) == null)
            r = timedAwaitDone(nanos);
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked") T tr = (T) r;
            return tr;
        }
        if ((ex = ((AltResult) r).ex) == null)
            return null;
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if ((ex instanceof CompletionException) &&
                (cause = ex.getCause()) != null)
            ex = cause;
        throw new ExecutionException(ex);
    }

    /**
     * Returns the result value when complete, or throws an
     * (unchecked) exception if completed exceptionally. To better
     * conform with the use of common functional forms, if a
     * computation involved in the completion of this
     * CompletableFuture threw an exception, this method throws an
     * (unchecked) {@link CompletionException} with the underlying
     * exception as its cause.
     *
     * @return the result value
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException   if this future completed
     *                               exceptionally or a completion computation threw an exception
     */
    public T join() {
        Object r;
        Throwable ex;
        if ((r = result) == null)
            r = waitingGet(false);
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked") T tr = (T) r;
            return tr;
        }
        if ((ex = ((AltResult) r).ex) == null)
            return null;
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if (ex instanceof CompletionException)
            throw (CompletionException) ex;
        throw new CompletionException(ex);
    }

    /**
     * If not already completed, sets the value returned by {@link
     * #get()} and related methods to the given value.
     *
     * @param value the result value
     * @return {@code true} if this invocation caused this CompletableFuture
     *         to transition to a completed state, else {@code false}
     */
    public boolean complete(T value) {
        boolean triggered = result == null &&
                UNSAFE.compareAndSwapObject(this, RESULT, null,
                        value == null ? NIL : value);
        postComplete();
        return triggered;
    }

    /**
     * If not already completed, causes invocations of {@link #get()}
     * and related methods to throw the given exception.
     *
     * @param ex the exception
     * @return {@code true} if this invocation caused this CompletableFuture
     *         to transition to a completed state, else {@code false}
     */
    public boolean completeExceptionally(Throwable ex) {
        if (ex == null) throw new NullPointerException();
        boolean triggered = result == null &&
                UNSAFE.compareAndSwapObject(this, RESULT, null, new AltResult(ex));
        postComplete();
        return triggered;
    }

    /* ------------- Completion Handlers -------------- */

    @Override
    public void onSuccess(Consumer<? super T> action, Executor executor) {
        if (executor == null) throw new NullPointerException();
        doHandle((value, ex) -> {
            if (ex == null) {
                executor.execute(new AsyncAccept<>(value, action, new CompletableFuture<>()));
            }
            return null;
        });
    }

    @Override
    public void onFailure(Consumer<? super Throwable> failure, Executor executor) {
        if (executor == null) throw new NullPointerException();
        doHandle((value, ex) -> {
            if (ex != null) {
                executor.execute(new AsyncAccept<>(ex, failure, new CompletableFuture<>()));
            }
            return null;
        });
    }

    @Override
    public void onComplete(Runnable action, Executor executor) {
        if (executor == null) throw new NullPointerException();
        doHandle((value, ex) -> {
            executor.execute(new AsyncRun(action, new CompletableFuture<>()));
            return null;
        });
    }

    @Override
    public boolean isCompleted() {
        return isDone();
    }

    @Override
    public Optional<T> value() {
        Object r;
        Throwable ex;
        if ((r = result) == null)
            return Optional.empty();
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked") T tr = (T) r;
            return Optional.of(tr);
        }
        if ((ex = ((AltResult) r).ex) == null)
            return Optional.empty();
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if (ex instanceof CompletionException)
            throw (CompletionException) ex;
        throw new CompletionException(ex);
    }

    @Override
    public DeferredResult<Throwable> failed() {
        final CompletableFuture<Throwable> dst = new CompletableFuture<>();
        onComplete(() -> {
            Object r;
            if (((r = result) instanceof AltResult)) {
                dst.complete(((AltResult) r).ex);
            } else {
                dst.completeExceptionally(new NoSuchElementException(
                        "CompletableFuture.failed not completed with a throwable"));
            }
        });
        return dst;
    }

    @Override
    public void forEach(Consumer<? super T> action, Executor executor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <R> DeferredResult<R> transform(Function<? super T, ? extends R> transformer, Function<? super Throwable, ? super Throwable> failure, Executor executor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <R> DeferredResult<R> map(Function<? super T, ? extends R> mapper, Executor executor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <R> DeferredResult<R> flatMap(Function<? super T, ? extends DeferredResult<? extends R>> mapper, Executor executor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DeferredResult<T> filter(Predicate<? super T> predicate, Executor executor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <R> DeferredResult<R> collect(Function<? super T, Optional<? extends R>> collector, Executor executor) {
        return null;
    }

    @Override
    public <R> DeferredResult<R> recover(Function<? super Throwable, Optional<? extends R>> recovery, Executor executor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <R> DeferredResult<R> recoverWith(Function<? super Throwable, Optional<DeferredResult<? extends R>>> recovery, Executor executor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <R extends T> DeferredResult<R> fallbackTo(Function<? super T, ? extends R> fallback) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DeferredResult<T> andThen(Consumer<? super T> action, Executor executor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* ------------- Traversing methods -------------- */

    /**
     * Returns a result to the value of the first result in the list that is completed.
     */
    public static <U> CompletableFuture<U> firstCompletedOf(Executor executor, CompletableFuture<U>... cfs) {
        final CompletableFuture<U> dst = new CompletableFuture<>();
        for (CompletableFuture<U> cf : cfs) {
            cf.onSuccess(value -> dst.complete(value), executor);
            cf.onFailure(ex -> dst.completeExceptionally(ex), executor);
        }
        return dst;
    }

    /**
     * Returns a result that will hold the optional value of the first result with a value that matches the predicate.
     */
    public static <U> CompletableFuture<Optional<U>> find(
            Executor executor,
            Predicate<? super U> predicate,
            CompletableFuture<U>... results) {
        // FIXME: Needs implementing.
        return null;
    }

    /**
     * A non-blocking fold over the specified results, with the start value supplied by the `zero` function.
     * The fold is performed on the thread where the last result is completed.
     * The result will be the first failure of any of the results, or any failure in the actual fold,
     * or the product of the fold.
     */
    public static <T, R> CompletableFuture<T> fold(Executor executor,
                                                   Function<? super T, ? extends R> zero,
                                                   BiFunction<? super R, ? super T, ? extends R> folder,
                                                   CompletableFuture<T>... results) {
        // FIXME: Needs implementing.
        return null;
    }

    /**
     * Initiates a fold over the supplied results where the fold-zero is the  value of the result
     * that is completed first.
     */
    public static <T, R> CompletableFuture<T> reduce(Executor executor,
                                                     BiFunction<? super R, ? super T, ? extends R> folder,
                                                     CompletableFuture<T>... results) {
        // FIXME: Needs implementing.
        return null;
    }

    /* ------------- Control and status methods -------------- */

    /**
     * If not already completed, completes this CompletableFuture with
     * a {@link CancellationException}. Dependent CompletableFutures
     * that have not already completed will also complete
     * exceptionally, with a {@link CompletionException} caused by
     * this {@code CancellationException}.
     *
     * @param mayInterruptIfRunning this value has no effect in this
     *                              implementation because interrupts are not used to control
     *                              processing.
     * @return {@code true} if this task is now cancelled
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = (result == null) &&
                UNSAFE.compareAndSwapObject
                        (this, RESULT, null, new AltResult(new CancellationException()));
        postComplete();
        return cancelled || isCancelled();
    }

    /**
     * Returns {@code true} if this CompletableFuture was cancelled
     * before it completed normally.
     *
     * @return {@code true} if this CompletableFuture was cancelled
     *         before it completed normally
     */
    @Override
    public boolean isCancelled() {
        Object r;
        return ((r = result) instanceof AltResult) &&
                (((AltResult) r).ex instanceof CancellationException);
    }

    /**
     * Returns the estimated number of CompletableFutures whose
     * completions are awaiting completion of this CompletableFuture.
     * This method is designed for use in monitoring system state, not
     * for synchronization control.
     *
     * @return the number of dependent CompletableFutures
     */
    public int getNumberOfDependents() {
        int count = 0;
        for (CompletionNode p = completions; p != null; p = p.next)
            ++count;
        return count;
    }

    /**
     * Returns a string identifying this CompletableFuture, as well as
     * its completion state.  The state, in brackets, contains the
     * String {@code "Completed Normally"} or the String {@code
     * "Completed Exceptionally"}, or the String {@code "Not
     * completed"} followed by the number of CompletableFutures
     * dependent upon its completion, if any.
     *
     * @return a string identifying this CompletableFuture, as well as its state
     */
    public String toString() {
        Object r = result;
        int count;
        return super.toString() +
                ((r == null) ?
                        (((count = getNumberOfDependents()) == 0) ?
                                "[Not completed]" :
                                "[Not completed, " + count + " dependents]") :
                        (((r instanceof AltResult) && ((AltResult) r).ex != null) ?
                                "[Completed exceptionally]" :
                                "[Completed normally]"));
    }

    /* ------------- Coercion -------------- */

    /**
     * Return a CompletableFuture representing the {@link DeferredResult}. Note that completing the returned
     * CompletableFuture will not complete a DeferredResult, as the latter is not completable, unless the
     * DeferredResult passed in is a CompletableFuture in the first instance.
     *
     * @param dr  the {@link DeferredResult}
     * @param <U> The value type of the {@link DeferredResult}
     * @return a CompletableFuture for the {@link DeferredResult}
     */
    public static <U> CompletableFuture<U> completableFuture(DeferredResult<U> dr) {
        if (dr instanceof CompletableFuture) return (CompletableFuture) dr;
        final CompletableFuture<U> cf = new CompletableFuture<>();
        dr.onComplete(() -> {
            try {
                Optional<U> value = dr.value();
                cf.complete(value.get());
            } catch (Throwable ex) {
                cf.completeExceptionally(ex);
            }
        });
        return cf;
    }

    /**
     * Return a CompletableFuture representing the {@link DeferredResult}. Note that completing the returned
     * CompletableFuture will not complete a DeferredResult, as the latter is not completable, unless the
     * DeferredResult passed in is a CompletableFuture in the first instance.
     *
     * @param drs the {@link DeferredResult}s
     * @param <U> The value type of the {@link DeferredResult}
     * @return an array of CompletableFutures for the {@link DeferredResult}s
     */
    public static <U> CompletableFuture<U>[] completableFutures(DeferredResult<U>... drs) {
        CompletableFuture<U>[] cfs = new CompletableFuture[drs.length];
        for (int i = 0; i < drs.length; ++i) {
            cfs[i] = completableFuture(drs[i]);
        }
        return cfs;
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long RESULT;
    private static final long WAITERS;
    private static final long COMPLETIONS;

    static {
        try {
            //UNSAFE = sun.misc.Unsafe.getUnsafe(); // FIXME: Uncomment this one when incorporating into the JDK
            UNSAFE = Unsafe.instance;
            Class<?> k = CompletableFuture.class;
            RESULT = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("result"));
            WAITERS = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiters"));
            COMPLETIONS = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("completions"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
