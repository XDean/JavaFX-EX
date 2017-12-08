package xdean.jfx.ex.util.support;

import static javafx.scene.input.MouseButton.PRIMARY;
import static org.junit.Assert.*;
import static xdean.jfx.ex.test.FxMatchers.sizeOf;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import xdean.jfx.ex.support.ResizeSupport;
import xdean.jfx.ex.support.ResizeSupport.ResizeConfig;

public class ResizeSupportTest extends ApplicationTest {
  private Rectangle rectangle;
  private ResizeConfig config;

  @Override
  public void start(Stage stage) throws Exception {
    Scene scene = new Scene(new Group(rectangle = new Rectangle(100, 100)), 300, 300);
    rectangle.setLayoutX(100);
    rectangle.setLayoutY(100);
    stage.setScene(scene);
    stage.setAlwaysOnTop(true);
    stage.show();
    config = ResizeSupport.bind(rectangle);
  }

  @Test
  public void testRightLarge() throws Exception {
    assertThat(rectangle, sizeOf(100, 100));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMaxX() - 1, screen.getMaxY() - 1);
    drag(50, 50);
    assertThat(rectangle, sizeOf(150, 150));
  }

  @Test
  public void testRightSmall() throws Exception {
    assertThat(rectangle, sizeOf(100, 100));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMaxX() - 1, screen.getMaxY() - 1);
    drag(-50, -50);
    assertThat(rectangle, sizeOf(50, 50));
  }

  @Test
  public void testLeftLarge() throws Exception {
    assertThat(rectangle, sizeOf(100, 100));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMinX() + 1, screen.getMinY() + 1);
    drag(50, 50);
    assertThat(rectangle, sizeOf(50, 50));
  }

  @Test
  public void testLeftSmall() throws Exception {
    assertThat(rectangle, sizeOf(100, 100));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMinX() + 1, screen.getMinY() + 1);
    drag(-50, -50);
    assertThat(rectangle, sizeOf(150, 150));
  }

  @Test
  public void testBorder() throws Exception {
    config.borderWidthProperty().set(10);
    assertThat(rectangle, sizeOf(100, 100));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMaxX() - 8, screen.getMaxY() - 8);
    drag(50, 50);
    assertThat(rectangle, sizeOf(150, 150));
  }

  @Test
  public void testEnable() throws Exception {
    assertThat(rectangle, sizeOf(100, 100));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMaxX() - 1, screen.getMaxY() - 1);
    drag(50, 50);
    assertThat(rectangle, sizeOf(150, 150));
    config.enableProperty().set(false);
    drag(-50, -50);
    assertThat(rectangle, sizeOf(150, 150));
    config.enableProperty().set(true);
    drag(50, 50);
    drag(50, 50);
    assertThat(rectangle, sizeOf(200, 200));
  }

  @Test
  public void testUnbind() throws Exception {
    assertThat(rectangle, sizeOf(100, 100));
    Bounds screen = rectangle.localToScreen(rectangle.getBoundsInLocal());
    moveTo(screen.getMaxX() - 1, screen.getMaxY() - 1);
    drag(50, 50);
    assertThat(rectangle, sizeOf(150, 150));
    config.unbind();
    drag(-50, -50);
    assertThat(rectangle, sizeOf(150, 150));
  }

  private void drag(int dx, int dy) {
    press(PRIMARY).moveBy(dx, dy).release(PRIMARY);
  }
}
