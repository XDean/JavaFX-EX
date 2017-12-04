package xdean.jfx.ex.util;

import org.hamcrest.Matcher;
import org.testfx.matcher.base.GeneralMatchers;

import javafx.geometry.Bounds;
import javafx.scene.Node;

public interface Mathers {
  static Matcher<Node> onScreen(double x, double y) {
    return GeneralMatchers.typeSafeMatcher(Node.class, "", n -> {
      Bounds screenBounds = n.localToScreen(n.getBoundsInLocal());
      return screenBounds.getMinX() == x && screenBounds.getMinY() == y;
    });
  }
}
