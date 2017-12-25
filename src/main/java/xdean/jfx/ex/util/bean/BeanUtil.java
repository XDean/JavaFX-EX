package xdean.jfx.ex.util.bean;

import java.util.function.Function;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import xdean.jex.util.cache.CacheUtil;

public class BeanUtil {
  public static <F, T> Property<T> nestProp(ObservableValue<F> pf, Function<F, Property<T>> func) {
    Property<T> current = func.apply(pf.getValue());
    Property<T> nestProp = new SimpleObjectProperty<>();
    CacheUtil.set(BeanUtil.class, nestProp, current);
    nestProp.bindBidirectional(current);
    pf.addListener((ob, o, n) -> {
      CacheUtil.<Property<T>> remove(BeanUtil.class, nestProp).ifPresent(p -> nestProp.unbindBidirectional(p));
      Property<T> pt = func.apply(n);
      CacheUtil.set(BeanUtil.class, nestProp, pt);
      nestProp.bindBidirectional(pt);
    });
    return nestProp;
  }

  public static <F, T> ObservableValue<T> nestValue(ObservableValue<F> pf, Function<F, ObservableValue<T>> func) {
    ObservableValue<T> current = func.apply(pf.getValue());
    Property<T> nestProp = new SimpleObjectProperty<>();
    nestProp.bind(current);
    pf.addListener((ob, o, n) -> {
      ObservableValue<T> pt = func.apply(n);
      nestProp.unbind();
      nestProp.bind(pt);
    });
    return nestProp;
  }

  public static <F, T> ListProperty<T> nestListProp(ObservableValue<F> pf, Function<F, ListProperty<T>> func) {
    ListProperty<T> current = func.apply(pf.getValue());
    ListProperty<T> nestProp = new SimpleListProperty<>();
    CacheUtil.set(BeanUtil.class, nestProp, current);
    nestProp.bindBidirectional(current);
    pf.addListener((ob, o, n) -> {
      CacheUtil.<ListProperty<T>> remove(BeanUtil.class, nestProp).ifPresent(p -> nestProp.unbindContentBidirectional(p));
      ListProperty<T> pt = func.apply(n);
      CacheUtil.set(BeanUtil.class, nestProp, pt);
      nestProp.bindContentBidirectional(pt);
    });
    return nestProp;
  }

  public static <F, T> ObservableList<T> nestListValue(ObservableValue<F> pf, Function<F, ObservableList<T>> func) {
    ObservableList<T> current = func.apply(pf.getValue());
    ListProperty<T> nestProp = new SimpleListProperty<>();
    CacheUtil.set(BeanUtil.class, nestProp, current);
    nestProp.bindContent(current);
    pf.addListener((ob, o, n) -> {
      CacheUtil.<ListProperty<T>> remove(BeanUtil.class, nestProp).ifPresent(p -> nestProp.unbindContent(p));
      ObservableList<T> pt = func.apply(n);
      CacheUtil.set(BeanUtil.class, nestProp, pt);
      nestProp.bindContent(pt);
    });
    return nestProp;
  }

  public static <F, T> MapableValue<T> map(ObservableValue<F> ov, Function<F, T> func) {
    return new SimpleMapableValue<T>() {
      {
        bind(ov);
      }

      @Override
      protected T computeValue() {
        return func.apply(ov.getValue());
      };
    };
  }

  public static interface MapableValue<T> extends ObservableValue<T> {
    default <P> MapableValue<P> map(Function<T, P> func) {
      return BeanUtil.map(this, func);
    };
  }

  private static abstract class SimpleMapableValue<T> extends ObjectBinding<T> implements MapableValue<T> {
  }
}
