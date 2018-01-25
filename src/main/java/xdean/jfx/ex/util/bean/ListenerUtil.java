package xdean.jfx.ex.util.bean;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import xdean.jex.extra.function.Action3;

public enum ListenerUtil {
  ;

  /**
   * Create a ChangeListener who hold a WeakReference of the object. If the
   * object is collected, remove the listener.
   * 
   * @param obj the object
   * @param listener the actual listener. (obj, old, new) -&gt; {}
   * @return the listener
   */
  public static <O, T> ChangeListenerEX<T> weak(O obj, Action3<O, T, T> listener) {
    return new ChangeListenerEX<T>() {
      private final WeakReference<O> weak = new WeakReference<>(obj);

      @Override
      public void changed(ObservableValue<? extends T> ob, T o, T n) {
        O object = weak.get();
        if (object == null) {
          ob.removeListener(this);
        } else {
          listener.call(object, o, n);
        }
      }
    };
  }

  public static <T> ChangeListenerEX<T> on(T value, Runnable r) {
    return on(() -> value, r);
  }

  public static <T> ChangeListenerEX<T> on(Supplier<T> s, Runnable r) {
    return on(n -> Objects.equals(n, s.get()), n -> r.run());
  }

  public static <T> ChangeListenerEX<T> on(Predicate<T> p, Consumer<T> r) {
    return (ob, o, n) -> {
      if (p.test(n)) {
        r.accept(n);
      }
    };
  }

  public static <T> ListChangeListenerEX<T> list(UnaryOperator<ListChangeListenerEX.Builder<T>> build) {
    return build.apply(new ListChangeListenerEX.Builder<>()).build();
  }

  public static <T> SetChangeListenerEX<T> set(UnaryOperator<SetChangeListenerEX.Builder<T>> build) {
    return build.apply(new SetChangeListenerEX.Builder<>()).build();
  }

  @FunctionalInterface
  public interface ChangeListenerEX<T> extends ChangeListener<T> {
    default ChangeListenerEX<T> then(ChangeListener<T> other) {
      return (ob, o, n) -> {
        changed(ob, o, n);
        other.changed(ob, o, n);
      };
    }

    default ChangeListenerEX<T> on(T value, Runnable r) {
      return then(ListenerUtil.on(value, r));
    }

    default ChangeListenerEX<T> on(Supplier<T> s, Runnable r) {
      return then(ListenerUtil.on(s, r));
    }

    default ChangeListenerEX<T> on(Predicate<T> p, Consumer<T> r) {
      return then(ListenerUtil.on(p, r));
    }
  }

  public static class ListChangeListenerEX<T> implements ListChangeListener<T> {
    public static class Builder<T> {
      private final ListChangeListenerEX<T> l = new ListChangeListenerEX<>();

      public Builder<T> onAdd(Consumer<T> c) {
        l.onAdded.add(c);
        return this;
      }

      public Builder<T> onRemoved(Consumer<T> c) {
        l.onRemoved.add(c);
        return this;
      }

      public Builder<T> onUpdated(Consumer<T> c) {
        l.onUpdated.add(c);
        return this;
      }

      public Builder<T> onPermutated(Action3<T, Integer, Integer> c) {
        l.onPermutated.add(c);
        return this;
      }

      public ListChangeListenerEX<T> build() {
        return l;
      }
    }

    private final List<Consumer<T>> onAdded = new LinkedList<>();
    private final List<Consumer<T>> onRemoved = new LinkedList<>();
    private final List<Consumer<T>> onUpdated = new LinkedList<>();
    private final List<Action3<T, Integer, Integer>> onPermutated = new LinkedList<>();

    @Override
    public void onChanged(javafx.collections.ListChangeListener.Change<? extends T> c) {
      while (c.next()) {
        if (c.wasPermutated()) {
          for (int i = c.getFrom(); i < c.getTo(); ++i) {
            int o = i;
            int n = c.getPermutation(i);
            T item = c.getList().get(n);
            onPermutated.forEach(fp -> fp.call(item, o, n));
          }
        } else if (c.wasUpdated()) {
          for (int i = c.getFrom(); i < c.getTo(); ++i) {
            T t = c.getList().get(i);
            onUpdated.forEach(fr -> fr.accept(t));
          }
        } else {
          for (T t : c.getRemoved()) {
            onRemoved.forEach(fr -> fr.accept(t));
          }
          for (T t : c.getAddedSubList()) {
            onAdded.forEach(fa -> fa.accept(t));
          }
        }
      }
    }
  }

  public static class SetChangeListenerEX<T> implements SetChangeListener<T> {
    public static class Builder<T> {
      private final SetChangeListenerEX<T> l = new SetChangeListenerEX<>();

      public Builder<T> onAdd(Consumer<T> c) {
        l.onAdded.add(c);
        return this;
      }

      public Builder<T> onRemoved(Consumer<T> c) {
        l.onRemoved.add(c);
        return this;
      }

      public SetChangeListenerEX<T> build() {
        return l;
      }
    }

    private final List<Consumer<T>> onAdded = new LinkedList<>();
    private final List<Consumer<T>> onRemoved = new LinkedList<>();

    @Override
    public void onChanged(javafx.collections.SetChangeListener.Change<? extends T> c) {
      if (c.wasAdded()) {
        onAdded.forEach(e -> e.accept(c.getElementAdded()));
      } else if (c.wasRemoved()) {
        onRemoved.forEach(e -> e.accept(c.getElementAdded()));
      }
    }
  }
}
