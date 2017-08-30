package xdean.jfx.ex.util.bean;

import static xdean.jex.util.task.TaskUtil.firstSuccess;
import static xdean.jfx.ex.util.bean.BeanConvertUtil.normalize;

import java.lang.reflect.Method;
import java.util.function.Function;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.experimental.UtilityClass;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.lang.ExceptionUtil;

import com.google.common.annotations.Beta;

@UtilityClass
public class BeanUtil {

  public BooleanBinding isNull(final ObservableValue<?> op) {
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

  public BooleanBinding isNotNull(final ObservableValue<?> op) {
    return not(isNull(op));
  }

  public BooleanBinding yep(final ObservableValue<Boolean> ov) {
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

  public BooleanBinding not(final ObservableValue<Boolean> ov) {
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

  public <F, T> Property<T> nestProp(ObservableValue<F> pf, Function<F, Property<T>> func) {
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

  public <F, T> ObservableValue<T> nestValue(ObservableValue<F> pf, Function<F, ObservableValue<T>> func) {
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

  @Beta
  @SuppressWarnings("unchecked")
  <T> T nestWrap(ObservableValue<T> p, Class<T> clz) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(clz);
    enhancer.setCallback(new MethodInterceptor() {
      @Override
      public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (method.getName().endsWith("Property")) {
          Object result = firstSuccess(
              () -> CacheUtil.cache(
                  p,
                  method.toString(),
                  () -> normalize(nestProp(p, t -> ExceptionUtil.uncheck(() -> (Property<Object>) method.invoke(t, args))),
                      method.getReturnType())),
              () -> CacheUtil.cache(p, method.toString(),
                  () -> nestValue(p, t -> ExceptionUtil.uncheck(() -> (ObservableValue<Object>) method.invoke(t, args)))));
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

  private abstract class SimpleMapableValue<T> extends ObjectBinding<T> implements MapableValue<T> {
    @Override
    public <P> MapableValue<P> map(Function<T, P> func) {
      return BeanUtil.map(this, func);
    }
  }

  public <F, T> MapableValue<T> map(ObservableValue<F> ov, Function<F, T> func) {
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

  /**
   * @deprecated Use {@link CollectionUtil#map(ObservableList<F>,Function<F, T>,Function<T, F>)} instead
   */
  @Deprecated
  public <F, T> ObservableList<T> map(ObservableList<F> list, Function<F, T> forward, Function<T, F> backward) {
    return CollectionUtil.map(list, forward, backward);
  }

  /**
   * @deprecated Use {@link CollectionUtil#bind(ObservableList<F>,ObservableList<T>,Function<F, T>,Function<T, F>)}
   *             instead
   */
  @Deprecated
  public <F, T> void bind(ObservableList<F> list1, ObservableList<T> list2, Function<F, T> forward,
      Function<T, F> backward) {
    CollectionUtil.bind(list1, list2, forward, backward);
  }
}
