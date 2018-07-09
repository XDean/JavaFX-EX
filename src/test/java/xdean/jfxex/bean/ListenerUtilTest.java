package xdean.jfxex.bean;

import static org.junit.Assert.assertEquals;
import static xdean.jfxex.bean.ListenerUtil.weak;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;

public class ListenerUtilTest extends BaseTest {
  @Test
  public void testWeak() throws Exception {
    Object obj = new Object();
    ip.addListener(weak(obj, (ob, o) -> here()));
    ip.addListener(weak(obj, (ob, o, n) -> here()));
    assertReach(2, () -> ip.set(2));
    obj = null;
    System.gc();
    Thread.sleep(10);
    System.gc();
    assertNotReach(() -> ip.set(3));
  }

  @Test
  public void testList() throws Exception {
    AtomicInteger event = new AtomicInteger(0);
    list.addListener(ListenerUtil.list(b -> b.onAdd(o -> event.set(1))
        .onRemoved(o -> event.set(2))
        .onChange(o -> event.get())
        .onPermutated((ob, o, n) -> event.set(4))
        .onUpdated(o -> event.set(5))));
    list.addAll(4, 3, 2);
    assertEquals(1, event.get());
    list.remove(1);
    assertEquals(2, event.get());
  }

  @Test
  public void testSet() throws Exception {
    SetProperty<Integer> set = new SimpleSetProperty<>(FXCollections.observableSet(1));
    AtomicInteger event = new AtomicInteger(0);
    set.addListener(ListenerUtil.set(b -> b.onAdd(o -> event.set(1))
        .onRemoved(o -> event.set(2))));
    set.add(4);
    assertEquals(1, event.get());
    set.remove(1);
    assertEquals(2, event.get());
  }
}
