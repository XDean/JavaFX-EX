package xdean.jfx.ex.support.undoRedo;

import java.lang.ref.WeakReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import xdean.jfx.ex.support.undoRedo.Undoable.Response;

//TODO Exception?
public class UndoUtil {
  public static <O, T> Function<T, Response> weakConsumer(O weakObject, BiConsumer<O, T> func) {
    Weak<O> weak = new Weak<O>(weakObject);
    return t -> weak.doIfPresentConsumer(o -> func.accept(o, t));
  }

  public static <O, T> Function<T, Response> weakFunction(O weakObject, BiFunction<O, T, Response> func) {
    Weak<O> weak = new Weak<O>(weakObject);
    return t -> weak.doIfPresentFunction(o -> func.apply(o, t));
  }

  /**
   * The function return true means DONE, and false means CANCEL
   * 
   * @param weakObject
   * @param func
   * @return
   */
  public static <O, T> Function<T, Response> weakPredicate(O weakObject, BiFunction<O, T, Boolean> func) {
    Weak<O> weak = new Weak<O>(weakObject);
    return t -> weak.doIfPresentPredicate(o -> func.apply(o, t));
  }

  private static class Weak<T> extends WeakReference<T> {
    Weak(T referent) {
      super(referent);
    }

    Response doIfPresentConsumer(Consumer<T> consumer) {
      T t = get();
      if (t != null) {
        consumer.accept(t);
        return Response.DONE;
      }
      return Response.SKIP;
    }

    Response doIfPresentFunction(Function<T, Response> consumer) {
      T t = get();
      if (t != null) {
        return consumer.apply(t);
      }
      return Response.SKIP;
    }

    Response doIfPresentPredicate(Predicate<T> consumer) {
      T t = get();
      if (t != null) {
        return consumer.test(t) ? Response.DONE : Response.CANCEL;
      }
      return Response.SKIP;
    }
  }
}
