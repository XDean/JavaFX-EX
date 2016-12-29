package xdean.jfx.ex.util.bean;

import static xdean.jfx.ex.util.bean.BeanUtil.nestWrap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

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
    wrapArea.setText("0-0");
    System.out.println("area1: " + area1.text.get());
    area1.setText("1-0");
    area1.setText("1-1");
    area2.setText("2-0");
    area2.setText("2-1");
    areaProp.set(area2);
    area1.setText("1-2");
    area2.setText("2-2");
    area1.setText("1-3");
    area2.setText("2-3");
  }
}

class NestClass {
  CustomProp text = new CustomProp();

  public NestClass() {
  }

  public CustomProp textProperty() {
    return text;
  }

  public void setText(String st) {
    textProperty().setValue(st);
  }
}

class CustomProp extends SimpleStringProperty {

}