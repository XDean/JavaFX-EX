package xdean.jfx.ex.util.support;

import static javafx.scene.input.MouseButton.PRIMARY;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import xdean.jfx.ex.support.DragSupport;
import xdean.jfx.ex.support.DragSupport.DragConfig;
import xdean.jfx.ex.test.FxMatchers.Windows;

public class DragSupportWindowTest extends ApplicationTest {
  private Stage stage;
  private DragConfig dragConfig;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    Scene scene = new Scene(new Group(new Rectangle(100, 100)), 300, 300);
    stage.setScene(scene);
    stage.setAlwaysOnTop(true);
    stage.setX(0);
    stage.setY(0);
    stage.show();
    dragConfig = DragSupport.bind(stage);
  }

  @Test
  public void testNormal() throws Exception {
    assertThat(stage, Windows.atScreen(0, 0));
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
  }

  @Test
  public void testBoard() throws Exception {
    dragConfig.borderWidthProperty().set(10.0);

    assertThat(stage, Windows.atScreen(0, 0));
    moveTo(stage.getX(), stage.getY());
    drag(100, 100);
    assertThat(stage, Windows.atScreen(0, 0));
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
  }

  @Test
  public void testEnable() throws Exception {
    assertThat(stage, Windows.atScreen(0, 0));
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
    dragConfig.enableProperty().set(false);
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
  }

  @Test
  public void testMax() throws Exception {
    dragConfig.maxXProperty().set(50);
    dragConfig.maxYProperty().set(50);
    assertThat(stage, Windows.atScreen(0, 0));
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(50, 50));
  }

  @Test
  public void testUnbind() throws Exception {
    assertThat(stage, Windows.atScreen(0, 0));
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
    dragConfig.unbind();
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
  }

  private void drag(int dx, int dy) {
    press(PRIMARY).moveBy(dx, dy).release(PRIMARY);
  }
}
