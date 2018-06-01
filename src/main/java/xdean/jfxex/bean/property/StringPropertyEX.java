package xdean.jfxex.bean.property;

import javafx.beans.property.StringProperty;
import xdean.jex.extra.LazyValue;
import xdean.jfxex.bean.BeanConvertUtil;

public class StringPropertyEX extends ObjectPropertyEX<String> {

  private LazyValue<StringProperty> normal = LazyValue.create(() -> BeanConvertUtil.toString(this));

  public StringPropertyEX() {
    super();
  }

  public StringPropertyEX(Object bean, String name, String initialValue) {
    super(bean, name, initialValue);
  }

  public StringPropertyEX(Object bean, String name) {
    super(bean, name);
  }

  public StringPropertyEX(String initialValue) {
    super(initialValue);
  }

  public StringProperty normalize() {
    return normal.get();
  }

  public StringPropertyEX emptyForNull() {
    defaultForNull("");
    return this;
  }
}
