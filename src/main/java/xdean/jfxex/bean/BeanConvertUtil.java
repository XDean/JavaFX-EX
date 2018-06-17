package xdean.jfxex.bean;

import static xdean.jfxex.bean.BindingUtil.createListBinding;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.sun.javafx.binding.BidirectionalBinding;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.jfxex.bean.annotation.NotRef;
import xdean.jfxex.bean.property.ObjectPropertyEX;

/**
 * Utility class to
 * <ol>
 * <li>Convert {@link ObservableValue} to {@link Binding}. (Note the result
 * Binding will adapt null value to default value. )</li>
 * <li>Convert {@code Property<T>} to {@code TProperty}. (Note that the result
 * Property DON'T hold the reference of the origin property)</li>
 * </ol>
 *
 * @author XDean
 *
 */
public enum BeanConvertUtil {
  ;

  public static final ObservableBooleanValue TRUE = createBooleanValue(true), FALSE = createBooleanValue(false);

  public static ObservableBooleanValue toBooleanValue(boolean b) {
    return b ? TRUE : FALSE;
  }

  /**
   * Convert {@code ObservableValue<Boolean>} to {@link BooleanBinding}. Default
   * value is false.
   */
  public static BooleanBinding toBooleanBinding(final ObservableValue<Boolean> ov) {
    return Bindings.createBooleanBinding(() -> get(ov, false), ov);
  }

  /**
   * Convert {@code Property<Boolean>} to {@link BooleanProperty}
   */
  public static BooleanProperty toBoolean(Property<Boolean> p) {
    return let(new SimpleBooleanProperty(), np -> BidirectionalBinding.bind(np, p));
  }

  /**
   * Convert {@code Property<? extends Number>} to {@link IntegerBinding}.
   * Default value is 0.
   */
  public static IntegerBinding toIntegerBinding(ObservableValue<? extends Number> ov) {
    return Bindings.createIntegerBinding(() -> get(ov, 0).intValue(), ov);
  }

  /**
   * Convert {@code Property<Integer>} to {@link IntegerProperty}
   */
  public static IntegerProperty toInteger(Property<Integer> p) {
    return let(new SimpleIntegerProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  /**
   * Convert {@code ObservableValue<? extends Number>} to {@link DoubleBinding}.
   * Default value is 0d.
   */
  public static DoubleBinding toDoubleBinding(ObservableValue<? extends Number> ov) {
    return Bindings.createDoubleBinding(() -> get(ov, 0d).doubleValue(), ov);
  }

  /**
   * Convert {@code Property<Double>} to {@link DoubleProperty}
   */
  public static DoubleProperty toDouble(Property<Double> p) {
    return let(new SimpleDoubleProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  /**
   * Convert {@code ObservableValue<? extends Number>} to {@link LongBinding}.
   * Default value is 0L
   */
  public static LongBinding toLongBinding(ObservableValue<? extends Number> ov) {
    return Bindings.createLongBinding(() -> get(ov, 0L).longValue(), ov);
  }

  /**
   * Convert {@code Property<Long>} to {@link LongProperty}
   */
  public static LongProperty toLong(Property<Long> p) {
    return let(new SimpleLongProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  /**
   * Convert {@code ObservableValue<? extends Number>} to {@link FloatBinding}.
   * Default value is 0f
   */
  public static FloatBinding toFloatBinding(ObservableValue<? extends Number> ov) {
    return Bindings.createFloatBinding(() -> get(ov, 0f).floatValue(), ov);
  }

  /**
   * Convert {@code Property<Float>} to {@link FloatProperty}
   */
  public static FloatProperty toFloat(Property<Float> p) {
    return let(new SimpleFloatProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  /**
   * Convert {@code ObservableValue<String>} to {@link StringBinding}. Default
   * value is "".
   */
  public static StringBinding toStringBinding(ObservableValue<String> ov) {
    return Bindings.createStringBinding(() -> get(ov, ""), ov);
  }

  /**
   * Convert {@code Property<String>} to {@link StringProperty}
   */
  public static StringProperty toString(Property<String> p) {
    return let(new SimpleStringProperty(), np -> np.bindBidirectional(p));
  }

  /**
   * Convert {@code ObservableValue<T>} to {@code ObjectBinding<T>}
   */
  public static <T> ObjectBinding<T> toObjectBinding(ObservableValue<T> ov) {
    return Bindings.createObjectBinding(() -> get(ov, null), ov);
  }

  /**
   * Convert {@code Property<T>} to {@code ObjectProperty<T>}
   */
  public static <T> ObjectProperty<T> toObject(Property<T> p) {
    return let(new SimpleObjectProperty<>(), np -> np.bindBidirectional(p));
  }

  /**
   * Convert {@code ObservableValue<List<T>>} to {@code ListBinding<T>}. Default
   * value is unmodifiable empty list.
   */
  public static <T> ListBinding<T> toListBinding(ObservableValue<List<T>> ov) {
    return createListBinding(() -> {
      List<T> value = ov.getValue();
      if (value == null) {
        return FXCollections.emptyObservableList();
      } else if (value instanceof ObservableList) {
        return value;
      } else {
        return FXCollections.observableList(value);
      }
    }, ov);
  }

  /**
   * Convert {@code Property<ObservableList<T>>} to {@code ListProperty<T>}
   */
  public static <T> ListProperty<T> toList(Property<ObservableList<T>> p) {
    return let(new SimpleListProperty<>(), np -> np.bindBidirectional(p));
  }

  /**
   * Convert a {@code Property<F>} to {@code Property<T>} by two converter. The two converter must
   * be strict reverse process, or infinite loop will happen.
   */
  public static <F, T> ObjectPropertyEX<T> convert(Property<F> from, Function<F, T> fromTo, Function<T, F> toFrom) {
    return convert(from, new ObjectPropertyEX<>(from, "convert"), fromTo, toFrom);
  }

  /**
   * @see #convert(Property, Function, Function)
   */
  public static <F, T, R extends Property<T>> R convert(Property<F> from, R to, Function<F, T> fromTo, Function<T, F> toFrom) {
    from.addListener((ob, o, n) -> to.setValue(fromTo.apply(n)));
    to.addListener((ob, o, n) -> from.setValue(toFrom.apply(n)));
    to.setValue(fromTo.apply(from.getValue()));
    return to;
  }

  /**
   * Transform {@code ObservableList<F>} to {@code ObservableList<T>} with two functions.
   *
   * @param list the from list
   * @param forward function from F to T
   * @param backward function from T to F
   * @return
   */
  public static <F, T> ObservableList<T> convertList(@NotRef ObservableList<F> list, Function<F, T> forward, Function<T, F> backward) {
    ObservableList<T> newList = FXCollections.observableArrayList();
    newList.setAll(Lists.transform(list, forward::apply));

    MapToTargetListener<F, T> forwardListener = new MapToTargetListener<>(list, newList, forward);
    MapToTargetListener<T, F> backwardListener = new MapToTargetListener<>(newList, list, backward);
    forwardListener.updating.bindBidirectional(backwardListener.updating);

    list.addListener(forwardListener);
    newList.addListener(backwardListener);
    return newList;
  }

  private static <T> T let(T t, Consumer<T> c) {
    c.accept(t);
    return t;
  }

  private static <T, S extends T> T get(ObservableValue<? extends T> ov, S value) {
    T v = ov.getValue();
    return v == null ? value : v;
  }



  private static ObservableBooleanValue createBooleanValue(boolean b) {
    return new ObservableBooleanValue() {
      @Override
      public void addListener(ChangeListener<? super Boolean> arg0) {
      }

      @Override
      public void removeListener(InvalidationListener arg0) {
      }

      @Override
      public void addListener(InvalidationListener arg0) {
      }

      @Override
      public void removeListener(ChangeListener<? super Boolean> arg0) {
      }

      @Override
      public Boolean getValue() {
        return b;
      }

      @Override
      public boolean get() {
        return b;
      }
    };
  }
}