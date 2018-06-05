package xdean.jfxex.bean;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import xdean.jex.extra.function.Action2;
import xdean.jex.extra.function.Action3;

/**
 * Utility class for JavaFX listener
 * 
 * @author Dean Xu (XDean@github.com)
 *
 */
public enum ListenerUtil {
  ;

  public static <T, O extends ObservableValue<T>> void addListenerAndInvoke(O ob, ChangeListener<? super T> l) {
    ob.addListener(l);
    T value = ob.getValue();
    l.changed(ob, value, value);
  }

  /**
   * Create a ChangeListener who hold a WeakReference of the object. If the object is collected,
   * remove the listener.
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

  /**
   * Create a InvalidationListener who hold a WeakReference of the object. If the object is
   * collected, remove the listener.
   * 
   * @param obj the object
   * @param listener the actual listener. (observable, obj) -&gt; {}
   * @return the listener
   */
  public static <O> InvalidationListener weak(O obj, Action2<Observable, O> listener) {
    return new InvalidationListener() {
      private final WeakReference<O> weak = new WeakReference<>(obj);

      @Override
      public void invalidated(Observable ob) {
        O object = weak.get();
        if (object == null) {
          ob.removeListener(this);
        } else {
          listener.call(ob, object);
        }
      }
    };
  }

  /**
   * Listener to do the action when becomes to the value
   */
  public static <T> ChangeListenerEX<T> on(T value, Runnable action) {
    return on(() -> value, action);
  }

  /**
   * Listener to do the action when becomes to the value from the supplier
   */
  public static <T> ChangeListenerEX<T> on(Supplier<T> s, Runnable action) {
    return on(n -> Objects.equals(n, s.get()), n -> action.run());
  }

  /**
   * Listener to do the action when the test passed
   */
  public static <T> ChangeListenerEX<T> on(Predicate<T> p, Consumer<T> action) {
    return (ob, o, n) -> {
      if (p.test(n)) {
        action.accept(n);
      }
    };
  }

  /**
   * Create a {@link ListChangeListenerEX} from
   * {@link com.asml.jex.beans.ListenerUtil.ListChangeListenerEX.Builder}
   */
  public static <T> ListChangeListenerEX<T> list(UnaryOperator<ListChangeListenerEX.Builder<T>> build) {
    return build.apply(new ListChangeListenerEX.Builder<>()).build();
  }

  /**
   * Create a {@link SetChangeListenerEX} from
   * {@link com.asml.jex.beans.ListenerUtil.SetChangeListenerEX.Builder}
   */
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

      public Builder<T> onChange(Consumer<javafx.collections.ListChangeListener.Change<? extends T>> c) {
        l.onChange.add(c);
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
    private final List<Consumer<javafx.collections.ListChangeListener.Change<? extends T>>> onChange = new LinkedList<>();

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
        onChange.forEach(t -> t.accept(c));
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
