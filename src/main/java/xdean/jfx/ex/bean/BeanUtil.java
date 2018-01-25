package xdean.jfx.ex.bean;

import static io.reactivex.annotations.SchedulerSupport.COMPUTATION;
import static xdean.jfx.ex.bean.ListenerUtil.on;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.reactivex.Scheduler;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.schedulers.Schedulers;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.jex.util.cache.CacheUtil;
import xdean.jfx.ex.bean.property.BooleanPropertyEX;
import xdean.jfx.ex.bean.property.DoublePropertyEX;
import xdean.jfx.ex.bean.property.IntegerPropertyEX;
import xdean.jfx.ex.bean.property.ListPropertyEX;
import xdean.jfx.ex.bean.property.ObjectPropertyEX;
import xdean.jfx.ex.bean.property.StringPropertyEX;

public class BeanUtil {

  private static <F, T, P extends Property<T>, Q extends P> Q nestProp(ObservableValue<F> pf, Function<F, P> func,
      Q newProp) {
    F value = pf.getValue();
    if (value != null) {
      Property<T> current = func.apply(value);
      CacheUtil.set(BeanUtil.class, newProp, current);
      newProp.bindBidirectional(current);
    }
    pf.addListener((ob, o, n) -> {
      CacheUtil.<Property<T>> remove(BeanUtil.class, newProp).ifPresent(p -> newProp.unbindBidirectional(p));
      if (n == null) {
        newProp.setValue(null);
      } else {
        Property<T> pt = func.apply(n);
        CacheUtil.set(BeanUtil.class, newProp, pt);
        newProp.bindBidirectional(pt);
      }
    });
    return newProp;
  }

  /**
   * Select a {@link Property} from a {@link ObservableValue}'s value. Any change of the returned
   * property will appear to the origin property. Vice versa. e.g.
   * 
   * 
   * @param owner the owner property
   * @param selector function from owner to the target property
   * @return nested property
   */
  public static <F, T> ObjectPropertyEX<T> nestProp(ObservableValue<F> owner, Function<F, Property<T>> selector) {
    return nestProp(owner, selector, new ObjectPropertyEX<>(owner, selector.toString()));
  }

  /**
   * Select an {@link ObservableValue} from a {@link ObservableValue}'s value. Any change of the origin
   * value will appear to the result value. 
   * 
   * @param owner the owner property
   * @param selector function from owner to the target property
   * @return nested property
   */
  public static <F, T> ObjectBinding<T> nestValue(ObservableValue<F> pf, Function<F, ? extends ObservableValue<T>> func) {
    return new ObjectBinding<T>() {
      ObservableValue<T> current;
      {
        bind(pf);
        F value = pf.getValue();
        if (value != null) {
          this.bind(current = func.apply(value));
        }
      }

      @Override
      protected T computeValue() {
        if (current != null) {
          this.unbind(current);
          current = null;
        }
        F value = pf.getValue();
        if (value != null) {
          current = func.apply(value);
          this.bind(current);
          return current.getValue();
        } else {
          return null;
        }
      }
    };
  }

  public static <F> BooleanPropertyEX nestBooleanProp(ObservableValue<F> pf, Function<F, Property<Boolean>> func) {
    return nestProp(pf, func, new BooleanPropertyEX(pf, func.toString()));
  }

  public static <F> BooleanBinding nestBooleanValue(ObservableValue<F> pf, Function<F, ObservableBooleanValue> func) {
    return BeanConvertUtil.toBooleanBinding(nestValue(pf, func));
  }

  public static <F> IntegerPropertyEX nestIntegerProp(ObservableValue<F> pf, Function<F, Property<Integer>> func) {
    return nestProp(pf, func, new IntegerPropertyEX(pf, func.toString()));
  }

  public static <F> IntegerBinding nestIntegerValue(ObservableValue<F> pf, Function<F, ObservableIntegerValue> func) {
    return BeanConvertUtil.toIntegerBinding(nestValue(pf, func));
  }

  public static <F> DoublePropertyEX nestDoubleProp(ObservableValue<F> pf, Function<F, Property<Double>> func) {
    return nestProp(pf, func, new DoublePropertyEX(pf, func.toString()));
  }

  public static <F> DoubleBinding nestDoubleValue(ObservableValue<F> pf, Function<F, ObservableDoubleValue> func) {
    return BeanConvertUtil.toDoubleBinding(nestValue(pf, func));
  }

  public static <F> StringPropertyEX nestStringProp(ObservableValue<F> pf, Function<F, Property<String>> func) {
    return nestProp(pf, func, new StringPropertyEX(pf, func.toString()));
  }

  public static <F> StringBinding nestStringValue(ObservableValue<F> pf, Function<F, ObservableStringValue> func) {
    return BeanConvertUtil.toStringBinding(nestValue(pf, func));
  }

  public static <F, T> ListPropertyEX<T> nestListProp(ObservableValue<F> pf, Function<F, ListProperty<T>> func) {
    ListPropertyEX<T> nestProp = new ListPropertyEX<>();
    F value = pf.getValue();
    if (value != null) {
      ListProperty<T> current = func.apply(value);
      CacheUtil.set(BeanUtil.class, nestProp, current);
      nestProp.bindBidirectional(current);
    }
    pf.addListener((ob, o, n) -> {
      CacheUtil.<ListProperty<T>> remove(BeanUtil.class, nestProp).ifPresent(
          p -> nestProp.unbindContentBidirectional(p));
      if (n != null) {
        ListProperty<T> pt = func.apply(n);
        CacheUtil.set(BeanUtil.class, nestProp, pt);
        nestProp.bindContentBidirectional(pt);
      }
    });
    return nestProp;
  }

  public static <F, T> ListBinding<T> nestListValue(ObservableValue<F> pf, Function<F, ObservableList<T>> func) {
    return new ListBinding<T>() {
      ObservableList<T> current;
      {
        bind(pf);
        F value = pf.getValue();
        if (value != null) {
          this.bind(current = func.apply(value));
        }
      }

      @Override
      protected ObservableList<T> computeValue() {
        if (current != null) {
          this.unbind(current);
          current = null;
        }
        F value = pf.getValue();
        if (value != null) {
          current = func.apply(value);
          this.bind(current);
          return current;
        } else {
          return FXCollections.emptyObservableList();
        }
      }
    };
  }

  public static <F, T> ObjectBinding<T> map(ObservableValue<F> ov, Function<F, T> func) {
    return new ObjectBinding<T>() {
      {
        bind(ov);
      }

      @Override
      protected T computeValue() {
        return func.apply(ov.getValue());
      };
    };
  }

  public static <F> BooleanBinding mapToBoolean(ObservableValue<F> ov, Predicate<F> func) {
    return new BooleanBinding() {
      {
        bind(ov);
      }

      @Override
      protected boolean computeValue() {
        return func.test(ov.getValue());
      }
    };
  }

  public static <T> ObjectPropertyEX<T> when(Property<Boolean> p, Supplier<T> trueValue, Supplier<T> falseValue) {
    ObjectPropertyEX<T> np = new ObjectPropertyEX<>();
    np.set(p.getValue() ? trueValue.get() : falseValue.get());
    np.addListener(on(falseValue, () -> p.setValue(false)).on(trueValue, () -> p.setValue(true)));
    p.addListener(on(false, () -> np.setValue(falseValue.get())).on(true, () -> np.setValue(trueValue.get())));
    return np;
  }

  public static <T> ObjectPropertyEX<T> when(Property<Boolean> p, T trueValue, T falseValue) {
    return when(p, () -> trueValue, () -> falseValue);
  }

  @SchedulerSupport(COMPUTATION)
  public static <T> void setWhile(Property<T> p, T value, long mills) {
    BeanUtil.setWhile(p, value, mills, Schedulers.computation());
  }

  public static <T> void setWhile(Property<T> p, T value, long mills, Scheduler scheduler) {
    T old = p.getValue();
    p.setValue(value);
    scheduler.createWorker().schedule(() -> {
      if (Objects.equals(p.getValue(), value)) {
        p.setValue(old);
      }
    }, mills, TimeUnit.MILLISECONDS);
  }
}