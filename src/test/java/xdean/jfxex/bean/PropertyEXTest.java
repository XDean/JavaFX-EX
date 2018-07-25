package xdean.jfxex.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.reactivex.Observable;
import javafx.beans.binding.BooleanBinding;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.BooleanPropertyEX;
import xdean.jfxex.bean.property.IntegerPropertyEX;
import xdean.jfxex.bean.property.ListPropertyEX;
import xdean.jfxex.bean.property.MapPropertyEX.Bijection;
import xdean.jfxex.bean.property.ObjectPropertyEX;
import xdean.jfxex.bean.property.StringPropertyEX;

public class PropertyEXTest extends BaseTest {

  @Test
  public void testAddListenerAndInvoke() throws Exception {
    assertReach(() -> sp.addListenerAndInvoke(ListenerUtil.on("1", () -> here())));
    sp.on("2", () -> here());
    assertReach(() -> sp.set("2"));
    assertNotReach(() -> sp.set("2"));
    assertNotReach(() -> sp.set("3"));
  }

  @Test
  public void testVerifierAndTransformer() throws Exception {
    ip.addVerifier((o, n) -> n > o, (o, n) -> here());
    assertReach(() -> ip.set(0));
    assertNotReach(() -> ip.set(2));

    sp.addTransformer((o, n) -> o + n);
    sp.set("2");
    assertEquals("12", sp.get());
    sp.set("");
    assertEquals("12", sp.get());
  }

  @Test
  public void testForNull() throws Exception {
    sp.set(null);
    sp.emptyForNull();
    sp.set(null);
    assertEquals("", sp.get());
    sp.nonNull();
    sp.set("1");
    assertEquals("1", sp.get());
    sp.set(null);
    assertEquals("", sp.get());

    ip.defaultForNull(100);
    ip.set(null);
    assertEquals(100, ip.get().intValue());
    ip.set(5);
    assertEquals(5, ip.get().intValue());
  }

  @Test
  public void testBindBy() throws Exception {
    sp.bindBy(new StringPropertyEX());
    ip.bindBidirectionalBy(new IntegerPropertyEX());
  }

  @Test
  public void testIs() throws Exception {
    BooleanBinding even = ip.is(i -> i % 2 == 0);
    assertFalse(even.get());
    ip.set(100);
    assertTrue(even.get());
  }

  @Test
  public void testSoftBind() throws Exception {
    StringPropertyEX other = new StringPropertyEX("2");
    sp.softBind(other);
    assertEquals("2", sp.get());
    other.set("3");
    assertEquals("3", sp.get());
    sp.set("4");
    assertEquals("4", sp.get());
    sp.softUnbind(other);
    other.set("5");
    assertEquals("4", sp.get());
  }

  @Test
  public void testIn() throws Exception {
    ListPropertyEX<Integer> list = new ListPropertyEX<>();
    list.addAll(1, 2, 3);
    ip.in(list, true);
    assertEquals(1, ip.get().intValue());
    list.remove(0);
    assertEquals(2, ip.get().intValue());
    list.add(5);
    ip.set(5);
    assertEquals(5, ip.get().intValue());
    ip.set(1);
    assertEquals(5, ip.get().intValue());
    ip.in(null, true);
    ip.set(1);
    assertEquals(1, ip.get().intValue());
  }

  @Test
  public void testBooleanAnd() throws Exception {
    BooleanPropertyEX other = new BooleanPropertyEX();
    bp.set(true);
    assertTrue(bp.get());
    bp.and(other.normalize());
    assertFalse(bp.get());
    other.set(true);
    assertFalse(bp.get());
    bp.set(true);
    assertTrue(bp.get());
  }

  @Test
  public void testBooleanOr() throws Exception {
    BooleanPropertyEX other = new BooleanPropertyEX();
    bp.or(other.normalize());
    assertFalse(bp.get());
    other.set(true);
    assertTrue(bp.get());
    bp.set(false);
    assertTrue(bp.get());
    other.set(false);
    bp.set(false);
    assertFalse(bp.get());
  }

  @Test
  public void testKeyIn() throws Exception {
    map.keyIn(list);
    map.valueIn(list);
    list.setAll(1, 2, 3, 4);
    map.put(2, 1);
    map.put(3, 1);
    assertEquals(2, map.size());
    list.remove(1);
    assertEquals(1, map.size());
    list.remove(0);
    assertEquals(0, map.size());
    map.put(1, 2);
    assertEquals(0, map.size());
    map.keyIn(null);
    map.put(1, 3);
    assertEquals(1, map.size());
    map.valueIn(null);
    map.put(2, 3);
    assertEquals(2, map.size());
  }

  @Test
  public void testBijection() throws Exception {
    map.bijection(Bijection.REJECT);
    map.put(1, 2);
    map.put(2, 2);
    assertEquals(1, map.size());
    map.bijection(Bijection.REPLACE);
    map.put(2, 3);
    map.put(3, 3);
    assertEquals(Integer.valueOf(3), map.get(3));
    assertEquals(2, map.size());
    map.bijection(Bijection.ERROR);
    Observable.fromCallable(() -> map.put(5, 3))
        .test()
        .assertError(IllegalArgumentException.class);
  }

  @Test
  public void testPropertyAt() throws Exception {
    ObjectPropertyEX<@CheckNull Integer> p = map.propertyAt(1);
    assertEquals(null, p.get());
    map.put(1, 2);
    assertEquals(2, p.get().intValue());
  }

  @Test
  public void testOther() throws Exception {
    assertTrue(ip.getSafe().isPresent());
    assertEquals("1", sp.orElse("123"));
    sp.set(null);
    assertEquals("123", sp.orElse("123"));
    assertFalse(map.getSafe(0).isPresent());
  }
}
