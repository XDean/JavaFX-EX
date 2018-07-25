package xdean.jfxex.support;

import static javafx.scene.input.MouseButton.PRIMARY;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import xdean.jfxex.support.DragSupport;
import xdean.jfxex.support.DragSupport.DragConfig;
import xdean.jfxex.test.FxMatchers.Windows;

public class DragSupportWindowTest extends ApplicationTest {
  private Stage stage;
  private DragConfig config;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    Scene scene = new Scene(new Group(new Rectangle(100, 100)), 300, 300);
    stage.setScene(scene);
    stage.setAlwaysOnTop(true);
    stage.setX(0);
    stage.setY(0);
    stage.show();
    config = DragSupport.bind(stage);
  }

  @Override
  public void stop() throws Exception {
    DragSupport.unbind(stage);
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
    config.borderWidthProperty().set(10.0);
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
    config.enableProperty().set(false);
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
  }

  @Test
  public void testMax() throws Exception {
    config.maxXProperty().set(50);
    config.maxYProperty().set(50);
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
    config.unbind();
    moveTo(stage);
    drag(100, 100);
    assertThat(stage, Windows.atScreen(100, 100));
  }

  private void drag(int dx, int dy) {
    press(PRIMARY).moveBy(dx, dy).release(PRIMARY);
  }
}
