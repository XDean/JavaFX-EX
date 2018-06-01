package xdean.jfxex.bean.property;

import javafx.beans.property.DoubleProperty;
import xdean.jex.extra.LazyValue;
import xdean.jfxex.bean.BeanConvertUtil;

/**
 * For reduce duplicate code, this class extends ObjectPropertyEX&lt;Double&lt;.
 * But in fact it should be DoubleProperty, its value can't be null. You can use
 * {@link #normalize()} to convert it to DoubleProperty
 * 
 * @author Dean Xu (XDean@github.com)
 *
 */
public class DoublePropertyEX extends ObjectPropertyEX<Double> {

  private LazyValue<DoubleProperty> normal = LazyValue.create(() -> BeanConvertUtil.toDouble(this));

  public DoublePropertyEX() {
    this(0d);
  }

  public DoublePropertyEX(Object bean, String name, Double initialValue) {
    super(bean, name, initialValue);
  }

  public DoublePropertyEX(Object bean, String name) {
    this(bean, name, 0d);
  }

  public DoublePropertyEX(Double initialValue) {
    this(null, "", initialValue);
  }

  @Override
  public void set(Double t) {
    checkBound();
    t = t == null ? 0d : t;
    super.set(t);
  }

  public DoubleProperty normalize() {
    return normal.get();
  }
}
