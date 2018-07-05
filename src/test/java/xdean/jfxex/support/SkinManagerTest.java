package xdean.jfxex.support;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import xdean.jfxex.support.skin.SkinManager;
import xdean.jfxex.support.skin.SkinStyle;

public class SkinManagerTest extends ApplicationTest {
  private SkinManager skinManager = new SkinManager();
  private Scene scene;
  private HBox root;
  private Alert alert;

  @Override
  public void start(Stage stage) {
    scene = new Scene(root = new HBox(), 300, 300);
    stage.setScene(scene);
    stage.show();
    skinManager.bind(scene);
    skinManager.bind(scene);

    alert = new Alert(AlertType.ERROR);
  }

  public enum Skin implements SkinStyle {
    RED("/skin/red.css"),
    BLUE("/skin/blue.css");

    private final String url;

    private Skin(String url) {
      this.url = url;
    }

    @Override
    public String getURL() {
      return url;
    }

    @Override
    public String getName() {
      return name();
    }
  }

  @Test
  public void test() throws Exception {
    skinManager.skinProperty().set(Skin.BLUE);
    capture(root).getImage().getPixelReader().getColor(1, 1).equals(Color.BLUE);
    skinManager.skinProperty().set(Skin.RED);
    capture(root).getImage().getPixelReader().getColor(1, 1).equals(Color.RED);
    skinManager.unbind(scene);
    skinManager.skinProperty().set(Skin.BLUE);
    capture(root).getImage().getPixelReader().getColor(1, 1).equals(Color.RED);
    skinManager.unbind(scene);
  }

  @Test
  public void testDialog() throws Exception {
    DialogPane root = alert.getDialogPane();
    skinManager.bind(alert);
    skinManager.bind(alert);
    skinManager.skinProperty().set(Skin.BLUE);
    capture(root).getImage().getPixelReader().getColor(1, 1).equals(Color.BLUE);
    skinManager.skinProperty().set(Skin.RED);
    capture(root).getImage().getPixelReader().getColor(1, 1).equals(Color.RED);
    skinManager.unbind(alert);
    skinManager.skinProperty().set(Skin.BLUE);
    capture(root).getImage().getPixelReader().getColor(1, 1).equals(Color.RED);
    alert.hide();
    skinManager.unbind(alert);
  }
}
