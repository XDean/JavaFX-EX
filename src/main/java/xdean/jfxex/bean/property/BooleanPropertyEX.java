package xdean.jfxex.bean.property;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import xdean.jex.extra.LazyValue;
import xdean.jfxex.bean.BeanConvertUtil;
import xdean.jfxex.bean.BeanUtil;

/**
 * @author Dean Xu (XDean@github.com)
 */
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

  /**
   * Constraint this property always be false if the observable is false.
   */
  public BooleanPropertyEX and(ObservableValue<Boolean> ob) {
    addVerifier((o, n) -> n ? ob.getValue() : true);
    ob.addListener(o -> set(get() && ob.getValue()));
    set(get() && ob.getValue());
    return this;
  }

  /**
   * Constraint this property always be true if the observable is true.
   */
  public BooleanPropertyEX or(ObservableValue<Boolean> ob) {
    addVerifier((o, n) -> n ? true : !ob.getValue());
    ob.addListener(o -> set(get() || ob.getValue()));
    set(get() || ob.getValue());
    return this;
  }

  /**
   * Convert this {@link BooleanPropertyEX} to standard {@link BooleanProperty}
   */
  public BooleanProperty normalize() {
    return normal.get();
  }

  public BooleanProperty reverse() {
    return BeanUtil.reverse(this);
  }
}
