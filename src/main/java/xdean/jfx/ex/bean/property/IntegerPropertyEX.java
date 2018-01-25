package xdean.jfx.ex.bean.property;

import javafx.beans.property.IntegerProperty;
import xdean.jex.extra.LazyValue;
import xdean.jfx.ex.bean.BeanConvertUtil;

public class IntegerPropertyEX extends ObjectPropertyEX<Integer> {

  private LazyValue<IntegerProperty> normal = LazyValue.create(() -> BeanConvertUtil.toInteger(this));

  public IntegerPropertyEX() {
    super(0);
  }

  public IntegerPropertyEX(Object bean, String name, Integer initialValue) {
    super(bean, name, initialValue);
  }

  public IntegerPropertyEX(Object bean, String name) {
    super(bean, name, 0);
  }

  public IntegerPropertyEX(Integer initialValue) {
    super(initialValue);
  }

  public IntegerProperty normalize() {
    return normal.get();
  }

}
