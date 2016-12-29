package xdean.jfx.ex.util.bean;

import static xdean.jex.util.task.TaskUtil.*;
import static xdean.jfx.ex.util.bean.BeanConvertUtil.normalize;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.function.Function;

import javafx.beans.WeakListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.collection.ListUtil;

import com.google.common.annotations.Beta;
import com.sun.javafx.binding.BidirectionalContentBinding;

public class BeanUtil {

  public static BooleanBinding isNull(final ObservableValue<?> op) {
    if (op == null) {
      throw new NullPointerException("Operand cannot be null.");
    }

    return new BooleanBinding() {
      {
        super.bind(op);
      }

      @Override
      public void dispose() {
        super.unbind(op);
      }

      @Override
      protected boolean computeValue() {
        return op.getValue() == null;
      }

      @Override
      public ObservableList<?> getDependencies() {
        return FXCollections.singletonObservableList(op);
      }
    };
  }

  public static BooleanBinding isNotNull(final ObservableValue<?> op) {
    return not(isNull(op));
  }

  public static BooleanBinding yep(final ObservableValue<Boolean> ov) {
    if (ov == null) {
      throw new NullPointerException("Operand cannot be null.");
    }

    return new BooleanBinding() {
      {
        super.bind(ov);
      }

      @Override
      public void dispose() {
        super.unbind(ov);
      }

      @Override
      protected boolean computeValue() {
        return ov.getValue() != Boolean.FALSE;
      }

      @Override
      public ObservableList<?> getDependencies() {
        return FXCollections.singletonObservableList(ov);
      }
    };
  }

  public static BooleanBinding not(final ObservableValue<Boolean> ov) {
    if (ov == null) {
      throw new NullPointerException("Operand cannot be null.");
    }

    return new BooleanBinding() {
      {
        super.bind(ov);
      }

      @Override
      public void dispose() {
        super.unbind(ov);
      }

      @Override
      protected boolean computeValue() {
        return ov.getValue() != Boolean.TRUE;
      }

      @Override
      public ObservableList<?> getDependencies() {
        return FXCollections.singletonObservableList(ov);
      }
    };
  }

  public static <F, T> Property<T> nestProp(ObservableValue<F> pf, Function<F, Property<T>> func) {
    return new SimpleObjectProperty<T>() {
      {
        pf.addListener((ob, o, n) -> {
          Property<T> pt = uncatch(() -> func.apply(n));
          if (pt == null) {
            return;
          }
          CacheUtil.<SimpleObjectProperty<T>, Property<T>> remove(BeanUtil.class, this).ifPresent(p -> this.unbindBidirectional(p));
          CacheUtil.set(BeanUtil.class, this, pt);
          this.bindBidirectional(pt);
        });
      }
    };
  }

  public static <F, T> ObservableValue<T> nestValue(ObservableValue<F> pf, Function<F, ObservableValue<T>> func) {
    return new SimpleObjectProperty<T>() {
      {
        pf.addListener((ob, o, n) -> {
          ObservableValue<T> pt = uncatch(() -> func.apply(n));
          if (pt == null) {
            return;
          }
          CacheUtil.<SimpleObjectProperty<T>, ObservableValue<T>> remove(BeanUtil.class, this).ifPresent(p -> this.unbind());
          CacheUtil.set(BeanUtil.class, this, pt);
          this.bind(pt);
        });
      }
    };
  }

  @Beta
  @SuppressWarnings("unchecked")
  static <T> T nestWrap(ObservableValue<T> p, Class<T> clz) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(clz);
    enhancer.setCallback(new MethodInterceptor() {
      @Override
      public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (method.getName().endsWith("Property")) {
          Object result = firstSuccess(
              () -> CacheUtil.cache(p, method.toString(),
                  () -> normalize(nestProp(p, t -> uncheck(() -> (Property<Object>) method.invoke(t, args))), method.getReturnType())),
              () -> CacheUtil.cache(p, method.toString(),
                  () -> nestValue(p, t -> uncheck(() -> (ObservableValue<Object>) method.invoke(t, args)))));
          if (result != null) {
            return result;
          }
        }
        return proxy.invokeSuper(obj, args);
      }
    });
    return (T) enhancer.create();
  }

  public interface MapableValue<T> extends ObservableValue<T> {
    public <P> MapableValue<P> map(Function<T, P> func);
  }

  private static abstract class SimpleMapableValue<T> extends ObjectBinding<T> implements MapableValue<T> {
    @Override
    public <P> MapableValue<P> map(Function<T, P> func) {
      return BeanUtil.map(this, func);
    }
  }

  public static <F, T> MapableValue<T> map(ObservableValue<F> ov, Function<F, T> func) {
    return new SimpleMapableValue<T>() {
      {
        bind(ov);
      }

      @Override
      protected T computeValue() {
        return uncatch(() -> func.apply(ov.getValue()));
      };
    };
  }

  public static <F, T> ObservableList<T> map(ObservableList<F> list, Function<F, T> forward, Function<T, F> backward) {
    ObservableList<T> newList = FXCollections.observableArrayList();
    newList.setAll(ListUtil.map(list, forward));

    MapToTargetListener<F, T> forwardListener = new MapToTargetListener<>(list, newList, forward);
    MapToTargetListener<T, F> backwardListener = new MapToTargetListener<>(newList, list, backward);
    forwardListener.updating.bindBidirectional(backwardListener.updating);

    list.addListener(forwardListener);
    newList.addListener(backwardListener);
    return newList;
  }

  private static class MapToTargetListener<F, T> implements ListChangeListener<F>, WeakListener {
    WeakReference<ObservableList<F>> sourceList;
    WeakReference<ObservableList<T>> targetList;
    Function<F, T> function;
    BooleanProperty updating = new SimpleBooleanProperty(false);

    public MapToTargetListener(ObservableList<F> sourceList, ObservableList<T> targetList, Function<F, T> function) {
      this.targetList = new WeakReference<>(targetList);
      this.sourceList = new WeakReference<>(sourceList);
      this.function = function;
    }

    @Override
    public void onChanged(Change<? extends F> change) {
      if (updating.get()) {
        return;
      }
      ObservableList<F> sourceList = this.sourceList.get();
      ObservableList<T> targetList = this.targetList.get();
      if (sourceList == null || targetList == null) {
        if (sourceList != null) {
          sourceList.removeListener(this);
        }
        return;
      }
      if (updating.get()) {
        return;
      }
      updating.set(true);
      while (change.next()) {
        if (change.wasPermutated()) {
          targetList.remove(change.getFrom(), change.getTo());
          targetList.addAll(change.getFrom(), ListUtil.map(change.getList().subList(change.getFrom(), change.getTo()), function));
        } else {
          if (change.wasRemoved()) {
            targetList.remove(change.getFrom(), change.getFrom() + change.getRemovedSize());
          }
          if (change.wasAdded()) {
            targetList.addAll(change.getFrom(), ListUtil.map(change.getAddedSubList(), function));
          }
        }
      }
      updating.set(false);
    }

    @Override
    public boolean wasGarbageCollected() {
      return (sourceList.get() == null) || (targetList.get() == null);
    }
  }

  public static <F, T> void bind(ObservableList<F> list1, ObservableList<T> list2, Function<F, T> forward, Function<T, F> backward) {
    ObservableList<T> newList1 = map(list1, forward, backward);
    CacheUtil.set(BeanUtil.class, list1, newList1);
    BidirectionalContentBinding.bind(newList1, list2);
  }
}
