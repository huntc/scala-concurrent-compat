import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.japi.Procedure;
import org.junit.Test;
import scala.concurrent.Future;
import scala.concurrent.Promise;

public class SomeTest {
    @Test
    public void testSomeFutures() {
        ActorSystem system = ActorSystem.create("MySystem");

        final Promise<Integer> p = Futures.promise();
        final Future<Integer> f = p.future();

        f.onSuccess(OnSuccess.of((Integer v) -> {
            System.out.println(v);
        }), system.dispatcher());

        p.success(1);
    }

    public final static class OnSuccess<T> extends akka.dispatch.OnSuccess<T> {

        public static <U> OnSuccess of(final Procedure<U> body) {
            return new OnSuccess(body);
        }

        final Procedure<T> body;

        protected OnSuccess(final Procedure<T> body) {
            this.body = body;
        }

        @Override
        public final void onSuccess(T t) throws Exception {
            body.apply(t);
        }
    }

}
