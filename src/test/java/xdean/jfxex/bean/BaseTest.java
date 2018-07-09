package xdean.jfxex.bean;

import static org.junit.Assert.assertTrue;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import javafx.collections.FXCollections;
import xdean.jex.extra.function.ActionE0;
import xdean.jfxex.bean.property.BooleanPropertyEX;
import xdean.jfxex.bean.property.DoublePropertyEX;
import xdean.jfxex.bean.property.IntegerPropertyEX;
import xdean.jfxex.bean.property.ListPropertyEX;
import xdean.jfxex.bean.property.MapPropertyEX;
import xdean.jfxex.bean.property.ObjectPropertyEX;
import xdean.jfxex.bean.property.StringPropertyEX;

public class BaseTest {

  protected final IntegerPropertyEX ip = new IntegerPropertyEX(this, "ip", 1);
  protected final StringPropertyEX sp = new StringPropertyEX("1");
  protected final DoublePropertyEX dp = new DoublePropertyEX(1.0);
  protected final BooleanPropertyEX bp = new BooleanPropertyEX(this, "bp");
  protected final ListPropertyEX<Integer> list = new ListPropertyEX<>(FXCollections.observableArrayList(1));
  protected final MapPropertyEX<Integer, Integer> map = new MapPropertyEX<>();
  protected final ObjectPropertyEX<Owner<Integer>> owner = new ObjectPropertyEX<>(new Owner<>(1));

  private int here = 0;

  public void assertReach(ActionE0<?> a) {
    assertReach(1, a);
  }

  public void assertReach(int count, ActionE0<?> a) {
    here = 0;
    uncheck(() -> a.call());
    assertTrue("Not reach there for " + count + " times.", here == count);
  }

  public void assertNotReach(ActionE0<?> a) {
    here = 0;
    uncheck(() -> a.call());
    assertTrue("Indeed reach there.", here == 0);
  }

  public <T> void here() {
    here++;
  }

  public <T> T here(T t) {
    here++;
    return t;
  }
}
