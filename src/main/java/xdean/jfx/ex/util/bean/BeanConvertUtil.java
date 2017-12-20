package xdean.jfx.ex.util.bean;

import static xdean.jex.util.task.TaskUtil.andFinal;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.sun.javafx.binding.BidirectionalBinding;

public interface BeanConvertUtil {

  public static BooleanBinding toBooleanBinding(final ObservableValue<Boolean> ov) {
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

  public static BooleanProperty toBoolean(Property<Boolean> p) {
    return andFinal(() -> new SimpleBooleanProperty(), np -> BidirectionalBinding.bind(np, p));
  }

  public static IntegerBinding toIntegerBinding(ObservableValue<? extends Number> ov) {
    return new IntegerBinding() {
      {
        bind(ov);
      }

      @Override
      protected int computeValue() {
        return ov.getValue() == null ? 0 : ov.getValue().intValue();
      }
    };
  }

  public static IntegerProperty toInteger(Property<Integer> p) {
    return andFinal(() -> new SimpleIntegerProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  public static DoubleBinding toDoubleBinding(ObservableValue<? extends Number> ov) {
    return new DoubleBinding() {
      {
        bind(ov);
      }

      @Override
      protected double computeValue() {
        return ov.getValue() == null ? 0 : ov.getValue().doubleValue();
      }
    };
  }

  public static DoubleProperty toDouble(Property<Double> p) {
    return andFinal(() -> new SimpleDoubleProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  public static LongBinding toLongBinding(ObservableValue<? extends Number> ov) {
    return new LongBinding() {
      {
        bind(ov);
      }

      @Override
      protected long computeValue() {
        return ov.getValue() == null ? 0 : ov.getValue().longValue();
      }
    };
  }

  public static LongProperty toLong(Property<Long> p) {
    return andFinal(() -> new SimpleLongProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  public static FloatBinding toFloatBinding(ObservableValue<? extends Number> ov) {
    return new FloatBinding() {
      {
        bind(ov);
      }

      @Override
      protected float computeValue() {
        return ov.getValue() == null ? 0 : ov.getValue().floatValue();
      }
    };
  }

  public static FloatProperty toFloat(Property<Float> p) {
    return andFinal(() -> new SimpleFloatProperty(), np -> BidirectionalBinding.bindNumber(np, p));
  }

  public static StringBinding toStringBinding(ObservableValue<String> ov) {
    return new StringBinding() {
      {
        bind(ov);
      }

      @Override
      protected String computeValue() {
        return ov.getValue();
      }
    };
  }

  public static StringProperty toString(Property<String> p) {
    return andFinal(() -> new SimpleStringProperty(), np -> np.bindBidirectional(p));
  }

  public static <T> ObjectBinding<T> toObjectBinding(ObservableValue<T> ov) {
    return new ObjectBinding<T>() {
      {
        bind(ov);
      }

      @Override
      protected T computeValue() {
        return ov.getValue();
      }
    };
  }

  public static <T> ObjectProperty<T> toObject(Property<T> p) {
    return andFinal(() -> new SimpleObjectProperty<>(), np -> np.bindBidirectional(p));
  }

  @SuppressWarnings("unchecked")
  static Object normalize(Property<?> prop, Class<?> clz) {
    if (clz.isAssignableFrom(IntegerProperty.class)) {
      return toInteger((Property<Integer>) prop);
    } else if (clz.isAssignableFrom(LongProperty.class)) {
      return toLong((Property<Long>) prop);
    } else if (clz.isAssignableFrom(FloatProperty.class)) {
      return toFloat((Property<Float>) prop);
    } else if (clz.isAssignableFrom(DoubleProperty.class)) {
      return toDouble((Property<Double>) prop);
    } else if (clz.isAssignableFrom(StringProperty.class)) {
      return toString((Property<String>) prop);
    } else if (clz.isAssignableFrom(ObjectProperty.class)) {
      return toObject((Property<Object>) prop);
    }
    return prop;
  }
}
