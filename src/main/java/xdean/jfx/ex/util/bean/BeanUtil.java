package xdean.jfx.ex.util.bean;

import java.util.function.Function;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.lang.ExceptionUtil;

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
          Property<T> pt = ExceptionUtil.uncatch(() -> func.apply(n));
          if (pt == null) {
            return;
          }
          CacheUtil.<Property<T>> remove(BeanUtil.class, this).ifPresent(p -> this.unbindBidirectional(p));
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
          ObservableValue<T> pt = ExceptionUtil.uncatch(() -> func.apply(n));
          if (pt == null) {
            return;
          }
          CacheUtil.<ObservableValue<T>> remove(BeanUtil.class, this).ifPresent(p -> this.unbind());
          CacheUtil.set(BeanUtil.class, this, pt);
          this.bind(pt);
        });
      }
    };
  }

  public static interface MapableValue<T> extends ObservableValue<T> {
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
        return ExceptionUtil.uncatch(() -> func.apply(ov.getValue()));
      };
    };
  }
}
