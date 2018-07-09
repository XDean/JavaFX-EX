package xdean.jfxex.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static xdean.jfxex.bean.BeanConvertUtil.convert;
import static xdean.jfxex.bean.BeanConvertUtil.convertList;
import static xdean.jfxex.bean.BeanConvertUtil.toBooleanValue;
import static xdean.jfxex.bean.BindingUtil.autoValid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import javafx.beans.binding.Binding;
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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.jfxex.bean.property.ObjectPropertyEX;

public class BeanConvertUtilTest {

  @Test
  public void testToBooleanValue() throws Exception {
    ObservableBooleanValue t = toBooleanValue(true);
    assertTrue(t.get());
    ObservableBooleanValue f = toBooleanValue(false);
    assertFalse(f.getValue());
    t.addListener(o -> getClass());
    t.addListener((ob, o, n) -> getClass());
    f.removeListener(o -> getClass());
    f.removeListener((ob, o, n) -> getClass());
  }

  @Test
  public void testNormalize() throws Exception {
    this.testNormalize(new SimpleObjectProperty<>(1), IntegerProperty.class, 2, BeanConvertUtil::toInteger);
    this.testNormalize(new SimpleObjectProperty<>(true), BooleanProperty.class, false, BeanConvertUtil::toBoolean);
    this.testNormalize(new SimpleObjectProperty<>(1d), DoubleProperty.class, 2d, BeanConvertUtil::toDouble);
    this.testNormalize(new SimpleObjectProperty<>(1f), FloatProperty.class, 2f, BeanConvertUtil::toFloat);
    this.testNormalize(new SimpleObjectProperty<>(1L), LongProperty.class, 2L, BeanConvertUtil::toLong);
    this.testNormalize(new SimpleObjectProperty<>("1"), StringProperty.class, "2", BeanConvertUtil::toString);
    this.testNormalize(new SimpleIntegerProperty(1), ObjectProperty.class, 2, BeanConvertUtil::toObject);
    this.testNormalize(new SimpleObjectProperty<>(FXCollections.<Integer> observableArrayList()), ListProperty.class,
        FXCollections.observableArrayList(1), BeanConvertUtil::toList);
  }

  @Test
  public void testToBinding() throws Exception {
    this.testToBinding(new SimpleObjectProperty<>(1), IntegerBinding.class, 2, BeanConvertUtil::toIntegerBinding);
    this.testToBinding(new SimpleObjectProperty<>(true), BooleanBinding.class, false, BeanConvertUtil::toBooleanBinding);
    this.testToBinding(new SimpleObjectProperty<>(1d), DoubleBinding.class, 2d, BeanConvertUtil::toDoubleBinding);
    this.testToBinding(new SimpleObjectProperty<>(1f), FloatBinding.class, 2f, BeanConvertUtil::toFloatBinding);
    this.testToBinding(new SimpleObjectProperty<>(1L), LongBinding.class, 2L, BeanConvertUtil::toLongBinding);
    this.testToBinding(new SimpleObjectProperty<>("1"), StringBinding.class, "2", BeanConvertUtil::toStringBinding);
    this.testToBinding(new SimpleObjectProperty<>(null), ObjectBinding.class, 2, BeanConvertUtil::toObjectBinding);
    this.<List<Integer>, List<Integer>, ListBinding<Integer>, Property<List<Integer>>> testToBinding(
        new SimpleObjectProperty<>(new ArrayList<>()),
        ListBinding.class, FXCollections.observableArrayList(1), BeanConvertUtil::toListBinding)
        .setValue(null);
  }

  @Test
  public void testConvert() throws Exception {
    IntegerProperty ip = new SimpleIntegerProperty(1);
    ObjectPropertyEX<Integer> twice = convert(ip, i -> i.intValue() * 2, i -> i / 2);
    assertEquals(2, twice.get().intValue());
    twice.set(10);
    assertEquals(5, ip.get());
    ip.set(8);
    assertEquals(16, twice.get().intValue());
  }

  @Test
  public void testConvertList() throws Exception {
    ObservableList<Integer> list = FXCollections.observableArrayList(1, 2);
    ObservableList<Integer> twice = convertList(list, i -> i * 2, i -> i / 2);
    assertEquals(2, twice.size());
    assertEquals(2, twice.get(0).intValue());
    assertEquals(4, twice.get(1).intValue());
    twice.set(1, 10);
    assertEquals(5, list.get(1).intValue());
  }

  private <N, A extends N, P extends Property<N>, O extends Property<A>> O testNormalize(O origin, Class<? super P> target,
      A value, Function<? super Property<A>, P> func) {
    P p = func.apply(origin);
    target.isAssignableFrom(p.getClass());
    assertEquals(origin.getValue(), p.getValue());
    A oldA = origin.getValue();
    N oldN = p.getValue();
    p.setValue(value);
    assertEquals(origin.getValue(), p.getValue());
    assertEquals(value, origin.getValue());
    origin.setValue(oldA);
    assertEquals(origin.getValue(), p.getValue());
    assertEquals(oldN, p.getValue());
    return origin;
  }

  private <N, A extends N, P extends Binding<? extends N>, O extends Property<A>> O testToBinding(O origin,
      Class<? super P> target,
      A value, Function<? super ObservableValue<A>, P> func) {
    P p = func.apply(origin);
    target.isAssignableFrom(p.getClass());
    assertEquals(origin.getValue(), p.getValue());
    A oldA = origin.getValue();
    N oldN = p.getValue();
    origin.setValue(value);
    assertEquals(origin.getValue(), p.getValue());
    assertEquals(value, p.getValue());
    origin.setValue(oldA);
    assertEquals(origin.getValue(), p.getValue());
    assertEquals(oldN, p.getValue());
    autoValid((Binding<?>) p);
    return origin;
  }
}
