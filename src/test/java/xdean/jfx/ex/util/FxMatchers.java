package xdean.jfx.ex.util;

import org.hamcrest.Matcher;
import org.testfx.matcher.base.GeneralMatchers;

import javafx.geometry.Bounds;
import javafx.scene.Node;

public interface FxMatchers {
  static Matcher<Node> atScreen(double x, double y) {
    return GeneralMatchers.typeSafeMatcher(Node.class, "at screen (" + x + ", " + y + ")", n -> {
      Bounds bound = n.localToScreen(n.getBoundsInLocal());
      return bound.getMinX() == x && bound.getMinY() == y;
    });
  }

  static Matcher<Node> atParent(double x, double y) {
    return GeneralMatchers.typeSafeMatcher(Node.class, "at parent (" + x + ", " + y + ")", n -> {
      Bounds bound = n.getBoundsInParent();
      return bound.getMinX() == x && bound.getMinY() == y;
    });
  }

}
