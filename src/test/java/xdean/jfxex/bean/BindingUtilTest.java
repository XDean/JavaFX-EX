package xdean.jfxex.bean;

import static org.junit.Assert.assertEquals;
import static xdean.jfxex.bean.BindingUtil.autoValid;
import static xdean.jfxex.bean.BindingUtil.bindContentBidrectly;
import static xdean.jfxex.bean.BindingUtil.createListBinding;
import static xdean.jfxex.bean.BindingUtil.createMapBinding;
import static xdean.jfxex.bean.BindingUtil.nestBind;
import static xdean.jfxex.bean.BindingUtil.observeProperties;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.MapBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.jfxex.bean.property.IntegerPropertyEX;

public class BindingUtilTest extends BaseTest {

  @Test
  public void testAutoValid() throws Exception {
    BooleanBinding b = Bindings.createBooleanBinding(() -> here(true), ip);
    assertNotReach(() -> ip.set(2));
    autoValid(b);
    assertReach(() -> ip.set(3));
  }

  @Test
  public void testCreateListBinding() throws Exception {
    ListBinding<Integer> lb = createListBinding(list);
    assertEquals(Arrays.asList(list), lb.getDependencies());
    list.add(2);
    assertEquals(Arrays.asList(1, 2), lb);
    lb.dispose();
  }

  @Test
  public void testCreateMapBinding() throws Exception {
    MapBinding<Integer, Integer> mb = createMapBinding(map);
    assertEquals(Arrays.asList(map), mb.getDependencies());
    map.put(1, 2);
    assertEquals(Collections.singletonMap(1, 2), mb.get());
    mb.dispose();
  }

  @Test
  public void testCase() throws Exception {
    IntegerPropertyEX other = new IntegerPropertyEX(50);
    ObjectBinding<Number> when = BindingUtil.<Integer, Number> cases(ip)
        .addDependency(bp)
        .when(1).then(1)
        .when(i -> i > 100).then(() -> 100)
        .when(other).then(other.normalize().add(10))
        .orElse(-1);
    assertEquals(1, when.get());
    ip.set(105);
    assertEquals(100, when.get());
    ip.set(50);
    assertEquals(60, when.get());
    ip.set(3);
    assertEquals(-1, when.get());
  }

  @Test
  public void testBindContentBidirectly() throws Exception {
    ObservableList<String> other = FXCollections.observableArrayList();
    bindContentBidrectly(other, list, i -> i.toString(), s -> Integer.valueOf(s));
    assertEquals(Arrays.asList("1"), other);
    other.add("123");
    assertEquals(Arrays.asList(1, 123), list);
  }

  @Test
  public void testNestBind() throws Exception {
    nestBind(owner, p -> p.prop, ip);
    assertEquals(owner.get().prop.getValue(), ip.get());
    owner.set(new Owner<>(2));
    assertEquals(owner.get().prop.getValue(), ip.get());
    ip.set(3);
    assertEquals(owner.get().prop.getValue(), ip.get());
    owner.set(null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testConcat() throws Exception {
    ObservableList<ObservableList<Integer>> ol = observeProperties(FXCollections.observableArrayList(list, list));
    ListBinding<Integer> concat = BindingUtil.concat(ol, ip);
    assertEquals(Arrays.asList(1, 1), concat);
    list.add(2);
    assertEquals(Arrays.asList(1, 2, 1, 2), concat);
    ol.remove(0);
    assertEquals(Arrays.asList(1, 2), concat);
    ol.remove(0);
    assertEquals(Arrays.asList(), concat);
  }
}
