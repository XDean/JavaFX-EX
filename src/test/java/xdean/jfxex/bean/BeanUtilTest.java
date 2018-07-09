package xdean.jfxex.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.mapToBoolean;
import static xdean.jfxex.bean.BeanUtil.nestBooleanProp;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestDoubleProp;
import static xdean.jfxex.bean.BeanUtil.nestDoubleValue;
import static xdean.jfxex.bean.BeanUtil.nestIntegerProp;
import static xdean.jfxex.bean.BeanUtil.nestIntegerValue;
import static xdean.jfxex.bean.BeanUtil.nestListProp;
import static xdean.jfxex.bean.BeanUtil.nestListValue;
import static xdean.jfxex.bean.BeanUtil.nestMapProp;
import static xdean.jfxex.bean.BeanUtil.nestMapValue;
import static xdean.jfxex.bean.BeanUtil.nestProp;
import static xdean.jfxex.bean.BeanUtil.nestStringProp;
import static xdean.jfxex.bean.BeanUtil.nestStringValue;
import static xdean.jfxex.bean.BeanUtil.nestValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import io.reactivex.schedulers.Schedulers;
import javafx.beans.binding.Binding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import xdean.jfxex.bean.property.BooleanPropertyEX;
import xdean.jfxex.bean.property.IntegerPropertyEX;
import xdean.jfxex.bean.property.ObjectPropertyEX;

public class BeanUtilTest {
  Random r = new Random();

  @Test
  public void testNestProp() throws Exception {
    testNestProp(new SimpleObjectProperty<>(new Owner<>(new Object())), o -> nestProp(o, t -> t.prop), () -> new Object());
    testNestProp(new SimpleObjectProperty<>(), o -> nestIntegerProp(o, t -> t.prop), () -> new Integer(r.nextInt()));
    testNestProp(new SimpleObjectProperty<>(), o -> nestDoubleProp(o, t -> t.prop), () -> new Double(r.nextDouble()));
    testNestProp(new SimpleObjectProperty<>(), o -> nestBooleanProp(o, t -> t.prop), () -> new Boolean(r.nextBoolean()));
    testNestProp(new SimpleObjectProperty<>(), o -> nestStringProp(o, t -> t.prop),
        () -> new String(Integer.toString(r.nextInt())));
    testNestProp(new SimpleObjectProperty<>(new Owner<>(randomList())), o -> nestListProp(o, t -> t.prop), () -> randomList());
    testNestProp(new SimpleObjectProperty<>(), o -> nestListProp(o, t -> t.prop), () -> randomList());
    testNestProp(new SimpleObjectProperty<>(), o -> nestMapProp(o, t -> t.prop), () -> randomMap());
  }

  @Test
  public void testNestValue() throws Exception {
    testNestValue(new SimpleObjectProperty<>(new Owner<>(new Object())), o -> nestValue(o, t -> t.ob), () -> new Object());
    testNestValue(new SimpleObjectProperty<>(), o -> nestIntegerValue(o, t -> t.ob), () -> new Integer(r.nextInt()));
    testNestValue(new SimpleObjectProperty<>(), o -> nestDoubleValue(o, t -> t.ob), () -> new Double(r.nextDouble()));
    testNestValue(new SimpleObjectProperty<>(), o -> nestBooleanValue(o, t -> t.ob), () -> new Boolean(r.nextBoolean()));
    testNestValue(new SimpleObjectProperty<>(), o -> nestStringValue(o, t -> t.ob),
        () -> new String(Integer.toString(r.nextInt())));
    testNestList(new SimpleObjectProperty<>(new Owner<>(randomList())), o -> nestListValue(o, t -> t.ob.getValue()),
        () -> randomList());
    testNestList(new SimpleObjectProperty<>(), o -> nestListValue(o, t -> t.ob.getValue()), () -> randomList());
    testNestMap(new SimpleObjectProperty<>(), o -> nestMapValue(o, t -> t.ob.getValue()), () -> randomMap());
    testNestMap(new SimpleObjectProperty<>(new Owner<>(randomMap())), o -> nestMapValue(o, t -> t.ob.getValue()),
        () -> randomMap());
  }

  @Test
  public void testMap() throws Exception {
    testMap(new SimpleObjectProperty<>(), o -> map(o, t -> t.prop.getValue()), () -> new Object());
    testMap(new SimpleObjectProperty<>(), o -> mapToBoolean(o, t -> t.prop.getValue()), () -> new Boolean(r.nextBoolean()));
  }

  @Test
  public void testSetWhile() throws Exception {
    IntegerPropertyEX p = new IntegerPropertyEX();
    p.setWhile(1, 100);
    assertEquals(1, p.get().intValue());
    p.setWhile(2, 200, Schedulers.io());
    assertEquals(2, p.get().intValue());
    Thread.sleep(500);
    assertEquals(1, p.get().intValue());
  }

  @Test
  public void testWhen() throws Exception {
    BooleanPropertyEX p = new BooleanPropertyEX();
    ObjectPropertyEX<Integer> when = BeanUtil.when(p, 1, 2);
    assertEquals(2, when.get().intValue());
    p.set(true);
    assertEquals(1, when.get().intValue());
    when.set(2);
    assertFalse(p.get());
    when.set(6);
    assertFalse(p.get());
  }

  private <T> void testNestProp(Property<Owner<T>> owner, Function<Property<Owner<T>>, Property<T>> func, Supplier<T> tFactory) {
    Property<T> nest = func.apply(owner);
    owner.setValue(new Owner<>(tFactory.get()));
    assertEquals(owner.getValue().prop.getValue(), nest.getValue());
    T otherT = tFactory.get();
    nest.setValue(otherT);
    assertEquals(owner.getValue().prop.getValue(), nest.getValue());
    assertEquals(owner.getValue().prop.getValue(), otherT);
    Owner<T> otherOwner = new Owner<>(tFactory.get());
    owner.setValue(otherOwner);
    assertEquals(owner.getValue().prop.getValue(), nest.getValue());
    assertEquals(otherOwner.prop.getValue(), nest.getValue());
    T old = nest.getValue();
    owner.setValue(null);
    assertNotEquals(nest.getValue(), old);
  }

  private <T> void testNestValue(Property<Owner<T>> owner, Function<ObservableValue<Owner<T>>, Binding<? super T>> func,
      Supplier<T> tFactory) {
    Binding<? super T> nest = func.apply(owner);
    owner.setValue(new Owner<>(tFactory.get()));
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    T otherT = tFactory.get();
    owner.getValue().writableOb.setValue(otherT);
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    assertEquals(owner.getValue().writableOb.getValue(), otherT);
    Owner<T> otherOwner = new Owner<>(tFactory.get());
    owner.setValue(otherOwner);
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    assertEquals(otherOwner.writableOb.getValue(), nest.getValue());
    owner.setValue(null);
  }

  private <E, T extends ObservableList<E>> void testNestList(Property<Owner<T>> owner,
      Function<ObservableValue<Owner<T>>, Binding<T>> func, Supplier<T> tFactory) {
    Binding<T> nest = func.apply(owner);
    owner.setValue(new Owner<>(tFactory.get()));
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    T otherT = tFactory.get();
    owner.getValue().writableOb.getValue().setAll(otherT);
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    assertEquals(owner.getValue().writableOb.getValue(), otherT);
    Owner<T> otherOwner = new Owner<>(tFactory.get());
    owner.setValue(otherOwner);
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    assertEquals(otherOwner.writableOb.getValue(), nest.getValue());
    owner.setValue(null);
    assertTrue(nest.getValue().isEmpty());
  }

  private <K, V, T extends ObservableMap<K, V>> void testNestMap(Property<Owner<T>> owner,
      Function<ObservableValue<Owner<T>>, Binding<T>> func, Supplier<T> tFactory) {
    Binding<T> nest = func.apply(owner);
    owner.setValue(new Owner<>(tFactory.get()));
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    T otherT = tFactory.get();
    owner.getValue().writableOb.getValue().clear();
    owner.getValue().writableOb.getValue().putAll(otherT);
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    assertEquals(owner.getValue().writableOb.getValue(), otherT);
    Owner<T> otherOwner = new Owner<>(tFactory.get());
    owner.setValue(otherOwner);
    assertEquals(owner.getValue().writableOb.getValue(), nest.getValue());
    assertEquals(otherOwner.writableOb.getValue(), nest.getValue());
    owner.setValue(null);
    assertTrue(nest.getValue().isEmpty());
  }

  private <T> void testMap(Property<Owner<T>> p, Function<ObservableValue<Owner<T>>, Binding<T>> func, Supplier<T> tFactory) {
    T t1 = tFactory.get();
    p.setValue(new Owner<>(t1));
    Binding<T> map = func.apply(p);
    assertEquals(t1, map.getValue());
    p.getValue().prop.setValue(tFactory.get());
    assertEquals(t1, map.getValue());
    T t2 = tFactory.get();
    p.setValue(new Owner<>(t2));
    assertEquals(t2, map.getValue());
  }

  public ObservableList<Integer> randomList() {
    return FXCollections.observableArrayList(r.nextInt());
  }

  public ObservableMap<Integer, Integer> randomMap() {
    return FXCollections.observableMap(new HashMap<>(Collections.singletonMap(r.nextInt(), r.nextInt())));
  }
}
