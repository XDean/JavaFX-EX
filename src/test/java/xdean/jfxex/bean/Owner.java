package xdean.jfxex.bean;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

public final class Owner<T> {
  final Property<T> prop = new SimpleObjectProperty<>();
  final Property<T> writableOb = new SimpleObjectProperty<>();
  final ObservableValue<T> ob = writableOb;

  public Owner(T value) {
    prop.setValue(value);
    writableOb.setValue(value);
  }
}