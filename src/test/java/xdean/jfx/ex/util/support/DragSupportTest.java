package xdean.jfx.ex.util.support;

import static javafx.scene.input.MouseButton.PRIMARY;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import xdean.jfx.ex.support.DragSupport;
import xdean.jfx.ex.support.DragSupport.DragConfig;

public class DragSupportTest extends ApplicationTest {
  private Rectangle rectangle;
  private DragConfig dragConfig;

  @Override
  public void start(Stage stage) {
    Scene scene = new Scene(new Group(rectangle = new Rectangle(100, 100)), 800, 600);
    rectangle.setLayoutX(100);
    rectangle.setLayoutY(100);
    stage.setScene(scene);
    stage.show();
    dragConfig = DragSupport.bind(rectangle);
  }

  @Test
  public void test() throws Exception {
    moveTo(rectangle).press(PRIMARY).moveBy(100, 100).release(PRIMARY);

  }
}
