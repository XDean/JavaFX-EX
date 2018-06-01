package xdean.jfxex.bean;

import static xdean.jfxex.bean.ListenerUtil.on;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.reactivex.Scheduler;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.schedulers.Schedulers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.MapBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import xdean.jex.util.cache.CacheUtil;
import xdean.jfxex.bean.property.BooleanPropertyEX;
import xdean.jfxex.bean.property.DoublePropertyEX;
import xdean.jfxex.bean.property.IntegerPropertyEX;
import xdean.jfxex.bean.property.ListPropertyEX;
import xdean.jfxex.bean.property.MapPropertyEX;
import xdean.jfxex.bean.property.ObjectPropertyEX;
import xdean.jfxex.bean.property.StringPropertyEX;

/**
 * Utility class to create bean(observable value) with powerful function.
 * 
 * @author XDean
 *
 */
public class BeanUtil {

  private static <F, T, P extends Property<T>, Q extends P> Q nestProp(ObservableValue<F> owner, Function<F, P> selector,
      Q newProp) {
    F value = owner.getValue();
    if (value != null) {
      Property<T> current = selector.apply(value);
      CacheUtil.set(BeanUtil.class, newProp, current);
      newProp.bindBidirectional(current);
    }
    owner.addListener((ob, o, n) -> {
      CacheUtil.<Property<T>> remove(BeanUtil.class, newProp).ifPresent(p -> newProp.unbindBidirectional(p));
      if (n == null) {
        newProp.setValue(null);
      } else {
        Property<T> pt = selector.apply(n);
        CacheUtil.set(BeanUtil.class, newProp, pt);
        newProp.bindBidirectional(pt);
      }
    });
    return newProp;
  }

  /**
   * Select a {@link Property} from a {@link ObservableValue}'s value. Any change of the returned property will appear
   * to the origin property. Vice versa. e.g.
   * 
   * <pre>
   * class Owner {
   *   Property&#60;T&#62; name = new SimpleStringProperty();
   * }
   * Owner a = new Owner();
   * a.name.setValue("a1");
   * Owner b = new Owner();
   * b.name.setValue("b1");
   * Property&#60;Owner&#62; owner = new SimpleObjectProperty&#60;&#62;(a);
   * ObjectPropertyEX&#60;String&#62; currentName = nestProp(owner, o -> o.name); // "a1"
   * a.name.setValue("a2"); // "a2"
   * b.name.setValue("b2"); // "a2"
   * owner.setValue(b); // "b2"
   * a.name.setValue("a3"); // "b2"
   * b.name.setValue("b3"); // "b3"
   * </pre>
   * 
   * @param owner the owner value
   * @param selector function from owner to the target property
   * @return nested property
   */
  public static <F, T> ObjectPropertyEX<T> nestProp(ObservableValue<F> owner, Function<F, Property<T>> selector) {
    return nestProp(owner, selector, new ObjectPropertyEX<>(owner, selector.toString()));
  }

  /**
   * Select an {@link ObservableValue} from a {@link ObservableValue}'s value. Any change of the origin value will
   * appear to the result value.
   * 
   * @param owner the owner value
   * @param selector function from owner to the target value
   * @return nested value
   * @see #nestProp(ObservableValue, Function)
   */
  public static <F, T> ObjectBinding<T> nestValue(ObservableValue<F> owner, Function<F, ? extends ObservableValue<T>> selector) {
    return new ObjectBinding<T>() {
      ObservableValue<T> current;
      {
        bind(owner);
        F value = owner.getValue();
        if (value != null) {
          this.bind(current = selector.apply(value));
        }
      }

      @Override
      protected T computeValue() {
        if (current != null) {
          this.unbind(current);
          current = null;
        }
        F value = owner.getValue();
        if (value != null) {
          current = selector.apply(value);
          this.bind(current);
          return current.getValue();
        } else {
          return null;
        }
      }
    };
  }

  /**
   * Select a nested boolean property
   * 
   * @see #nestProp(ObservableValue, Function)
   */
  public static <F> BooleanPropertyEX nestBooleanProp(ObservableValue<F> owner, Function<F, Property<Boolean>> selector) {
    return nestProp(owner, selector, new BooleanPropertyEX(owner, selector.toString()));
  }

  /**
   * Select a nested boolean value
   * 
   * @see #nestValue(ObservableValue, Function)
   */
  public static <F> BooleanBinding nestBooleanValue(ObservableValue<F> owner, Function<F, ObservableBooleanValue> selector) {
    return BeanConvertUtil.toBooleanBinding(nestValue(owner, selector));
  }

  /**
   * Select a nested int property
   * 
   * @see #nestProp(ObservableValue, Function)
   */
  public static <F> IntegerPropertyEX nestIntegerProp(ObservableValue<F> owner, Function<F, Property<Integer>> selector) {
    return nestProp(owner, selector, new IntegerPropertyEX(owner, selector.toString()));
  }

  /**
   * Select a nested int value
   * 
   * @see #nestValue(ObservableValue, Function)
   */
  public static <F> IntegerBinding nestIntegerValue(ObservableValue<F> owner, Function<F, ObservableIntegerValue> selector) {
    return BeanConvertUtil.toIntegerBinding(nestValue(owner, selector));
  }

  /**
   * Select a nested double property
   * 
   * @see #nestProp(ObservableValue, Function)
   */
  public static <F> DoublePropertyEX nestDoubleProp(ObservableValue<F> owner, Function<F, Property<Double>> selector) {
    return nestProp(owner, selector, new DoublePropertyEX(owner, selector.toString()));
  }

  /**
   * Select a nested double value
   * 
   * @see #nestValue(ObservableValue, Function)
   */
  public static <F> DoubleBinding nestDoubleValue(ObservableValue<F> owner, Function<F, ObservableDoubleValue> selector) {
    return BeanConvertUtil.toDoubleBinding(nestValue(owner, selector));
  }

  /**
   * Select a nested string property
   * 
   * @see #nestProp(ObservableValue, Function)
   */
  public static <F> StringPropertyEX nestStringProp(ObservableValue<F> owner, Function<F, Property<String>> selector) {
    return nestProp(owner, selector, new StringPropertyEX(owner, selector.toString()));
  }

  /**
   * Select a nested string value
   * 
   * @see #nestValue(ObservableValue, Function)
   */
  public static <F> StringBinding nestStringValue(ObservableValue<F> owner, Function<F, ObservableStringValue> selector) {
    return BeanConvertUtil.toStringBinding(nestValue(owner, selector));
  }

  /**
   * Select a nested list property
   * 
   * @see #nestProp(ObservableValue, Function)
   */
  public static <F, T> ListPropertyEX<T> nestListProp(ObservableValue<F> owner, Function<F, ListProperty<T>> selector) {
    ListPropertyEX<T> nestProp = new ListPropertyEX<>();
    F value = owner.getValue();
    if (value != null) {
      ListProperty<T> current = selector.apply(value);
      CacheUtil.set(BeanUtil.class, nestProp, current);
      nestProp.bindBidirectional(current);
    }
    owner.addListener((ob, o, n) -> {
      CacheUtil.<ListProperty<T>> remove(BeanUtil.class, nestProp).ifPresent(p -> nestProp.unbindBidirectional(p));
      if (n != null) {
        ListProperty<T> pt = selector.apply(n);
        CacheUtil.set(BeanUtil.class, nestProp, pt);
        nestProp.bindBidirectional(pt);
      }
    });
    return nestProp;
  }

  /**
   * Select a nested list value
   * 
   * @see #nestValue(ObservableValue, Function)
   */
  public static <F, T> ListBinding<T> nestListValue(ObservableValue<F> owner, Function<F, ObservableList<T>> selector) {
    return new ListBinding<T>() {
      ObservableList<T> current;
      {
        bind(owner);
        F value = owner.getValue();
        if (value != null) {
          this.bind(current = selector.apply(value));
        }
      }

      @Override
      protected ObservableList<T> computeValue() {
        if (current != null) {
          this.unbind(current);
          current = null;
        }
        F value = owner.getValue();
        if (value != null) {
          current = selector.apply(value);
          this.bind(current);
          return current;
        } else {
          return FXCollections.emptyObservableList();
        }
      }
    };
  }

  /**
   * Select a nested map property
   * 
   * @see #nestProp(ObservableValue, Function)
   */
  public static <F, K, V> MapPropertyEX<K, V> nestMapProp(ObservableValue<F> owner, Function<F, MapProperty<K, V>> selector) {
    return nestProp(owner, selector, new MapPropertyEX<>());
  }

  /**
   * Select a nested map value
   * 
   * @see #nestValue(ObservableValue, Function)
   */
  public static <F, K, V> MapBinding<K, V> nestMapValue(ObservableValue<F> owner, Function<F, ObservableMap<K, V>> selector) {
    return new MapBinding<K, V>() {
      ObservableMap<K, V> current;
      {
        bind(owner);
        F value = owner.getValue();
        if (value != null) {
          this.bind(current = selector.apply(value));
        }
      }

      @Override
      protected ObservableMap<K, V> computeValue() {
        if (current != null) {
          this.unbind(current);
          current = null;
        }
        F value = owner.getValue();
        if (value != null) {
          current = selector.apply(value);
          this.bind(current);
          return current;
        } else {
          return FXCollections.emptyObservableMap();
        }
      }
    };
  }

  /**
   * Convenient method to map value of an {@link ObservableValue}
   */
  public static <F, T> ObjectBinding<T> map(ObservableValue<F> ov, Function<F, T> selector) {
    return Bindings.createObjectBinding(() -> selector.apply(ov.getValue()), ov);
  }

  /**
   * Convenient method to map value of an {@link ObservableValue} to boolean
   */
  public static <F> BooleanBinding mapToBoolean(ObservableValue<F> ov, Predicate<F> selector) {
    return Bindings.createBooleanBinding(() -> selector.test(ov.getValue()), ov);
  }

  /**
   * Convert {@link ObservableList<F>} to {@link ObservableList<T>} with function.<br>
   * Note this map is unidirectional.
   * 
   * @see BeanConvertUtil#convert(ObservableList, Function, Function)
   */
  public static <F, T> ObservableList<T> mapList(ObservableList<F> list, Function<F, T> func) {
    ObservableList<T> newList = FXCollections.observableArrayList();
    newList.setAll(Lists.transform(list, func::apply));
    list.addListener(new MapToTargetListener<>(list, newList, func));
    return newList;
  }

  /**
   * Create a {@link ObjectProperty} has one to one correspondence to the given boolean Property. If the object property
   * is set to another value, the boolean property will not change.
   */
  public static <T> ObjectPropertyEX<T> when(Property<Boolean> p, Supplier<T> trueValue, Supplier<T> falseValue) {
    ObjectPropertyEX<T> np = new ObjectPropertyEX<>();
    np.set(p.getValue() ? trueValue.get() : falseValue.get());
    np.addListener(on(falseValue, () -> p.setValue(false)).on(trueValue, () -> p.setValue(true)));
    p.addListener(on(false, () -> np.setValue(falseValue.get())).on(true, () -> np.setValue(trueValue.get())));
    return np;
  }

  /**
   * @see #when(Property, Supplier, Supplier)
   */
  public static <T> ObjectPropertyEX<T> when(Property<Boolean> p, T trueValue, T falseValue) {
    return when(p, () -> trueValue, () -> falseValue);
  }

  /**
   * Set the property to the value and restore it after the given time.
   * 
   * @param p the property
   * @param value the value
   * @param mills time mills
   * @param scheduler timing thread
   */
  public static <T> void setWhile(Property<T> p, T value, long mills, Scheduler scheduler) {
    T old = p.getValue();
    p.setValue(value);
    scheduler.createWorker().schedule(() -> {
      if (Objects.equals(p.getValue(), value)) {
        p.setValue(old);
      }
    }, mills, TimeUnit.MILLISECONDS);
  }

  /**
   * @see #setWhile(Property, Object, long, Scheduler)
   */
  @SchedulerSupport(SchedulerSupport.COMPUTATION)
  public static <T> void setWhile(Property<T> p, T value, long mills) {
    BeanUtil.setWhile(p, value, mills, Schedulers.computation());
  }
}