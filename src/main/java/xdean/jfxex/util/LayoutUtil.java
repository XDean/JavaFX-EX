package xdean.jfxex.util;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

public class LayoutUtil {

  public static <T extends Node> T margin(T t, Insets inset) {
    HBox.setMargin(t, inset);
    return t;
  }

  public static <T extends Node> T margin(T t, double left, double right) {
    return margin(t, new Insets(0, right, 0, left));
  } 

  public static <T extends Region> T prefWidth(T t, double width) {
    t.setPrefWidth(width);
    return t;
  }

  public static <T extends Region> T minWidth(T t, double width) {
    t.setMinWidth(width);
    return t;
  }

  public static <T extends Region> T border(T t, Paint p, double width) {
    t.setBorder(getSimpleBorder(p, width));
    return t;
  }

  public static Border getSimpleBorder(Paint p, double width) {
    return new Border(new BorderStroke(p, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(width)));
  }

  public static void setAnchor(Node child, double top, double left, double bottom, double right) {
    AnchorPane.setTopAnchor(child, top);
    AnchorPane.setBottomAnchor(child, bottom);
    AnchorPane.setLeftAnchor(child, left);
    AnchorPane.setRightAnchor(child, right);
  }

  public static void setAnchorZero(Node child) {
    setAnchor(child, 0, 0, 0, 0);
  }
}
