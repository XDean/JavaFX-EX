package xdean.jfx.ex.util.bean;

import static xdean.jfx.ex.util.bean.BeanUtil.nestWrap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.Test;

public class TestBean {
  @Test
  public void testNestWrap() {
    NestClass area1 = new NestClass();
    NestClass area2 = new NestClass();
    ObjectProperty<NestClass> areaProp = new SimpleObjectProperty<>();
    NestClass wrapArea = nestWrap(areaProp, NestClass.class);
    wrapArea.textProperty().addListener((ob, o, n) -> System.out.println(n));
    areaProp.set(area1);
    wrapArea.setText("123");
    System.out.println("area1: "+area1.text.get());
    area1.setText("0");
    area1.setText("1");
    area2.setText("2");
    areaProp.set(area2);
    area1.setText("3");
    area2.setText("4");
  }
}

class NestClass {
  StringProperty text = new SimpleStringProperty();

  public NestClass() {
  }
  
  public StringProperty textProperty() {
    return text;
  }

  public void setText(String st) {
    textProperty().setValue(st);
  }
}