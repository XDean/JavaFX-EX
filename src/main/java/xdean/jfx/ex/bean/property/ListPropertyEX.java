package xdean.jfx.ex.bean.property;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListPropertyEX<T> extends SimpleListProperty<T> {

  public ListPropertyEX() {
    super(FXCollections.observableArrayList());
  }

  public ListPropertyEX(Object bean, String name, ObservableList<T> value) {
    super(bean, name, value);
  }

  public ListPropertyEX(Object bean, String name) {
    super(bean, name, FXCollections.observableArrayList());
  }

  public ListPropertyEX(ObservableList<T> value) {
    super(value);
  }
}
