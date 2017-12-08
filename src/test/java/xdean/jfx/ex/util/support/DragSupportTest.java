package xdean.jfx.ex.util.support;

import static javafx.scene.input.MouseButton.PRIMARY;
import static org.junit.Assert.assertThat;
import static xdean.jfx.ex.test.FxMatchers.atParent;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.geometry.Bounds;
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
    Scene scene = new Scene(new Group(rectangle = new Rectangle(100, 100)), 300, 300);
    stage.setScene(scene);
    stage.setAlwaysOnTop(true);
    stage.show();
    dragConfig = DragSupport.bind(rectangle);
  }

  @Test
  public void testNormal() throws Exception {
    assertThat(rectangle, atParent(0, 0));
    moveTo(rectangle);
    drag(100, 100);
    assertThat(rectangle, atParent(100, 100));
  }

  @Test
  public void testBoard() throws Exception {
    dragConfig.borderWidthProperty().set(10.0);

    assertThat(rectangle, atParent(0, 0));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMinX(), screen.getMinY());
    drag(100, 100);
    assertThat(rectangle, atParent(0, 0));
    moveTo(rectangle);
    drag(100, 100);
    assertThat(rectangle, atParent(100, 100));
  }

  @Test
  public void testEnable() throws Exception {
    assertThat(rectangle, atParent(0, 0));
    moveTo(rectangle);
    drag(100, 100);
    assertThat(rectangle, atParent(100, 100));
    dragConfig.enableProperty().set(false);
    moveTo(rectangle);
    drag(100, 100);
    assertThat(rectangle, atParent(100, 100));
  }

  private void drag(int dx, int dy) {
    press(PRIMARY).moveBy(dx, dy).release(PRIMARY);
  }
}
