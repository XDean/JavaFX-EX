package xdean.jfxex.bean.property;

import javafx.beans.property.BooleanProperty;
import xdean.jex.extra.LazyValue;
import xdean.jfxex.bean.BeanConvertUtil;

public class BooleanPropertyEX extends ObjectPropertyEX<Boolean> {

  private LazyValue<BooleanProperty> normal = LazyValue.create(() -> BeanConvertUtil.toBoolean(this));

  public BooleanPropertyEX() {
    super(false);
  }

  public BooleanPropertyEX(Boolean initialValue) {
    super(initialValue);
  }

  public BooleanPropertyEX(Object bean, String name) {
    super(bean, name, false);
  }

  public BooleanPropertyEX(Object bean, String name, Boolean initialValue) {
    super(bean, name, initialValue);
  }

  public BooleanPropertyEX onTrue(Runnable r) {
    on(true, r);
    return this;
  }

  public BooleanPropertyEX onFalse(Runnable r) {
    on(false, r);
    return this;
  }

  public BooleanProperty normalize() {
    return normal.get();
  }
}
