package xdean.jfxex.test;

import org.hamcrest.Matcher;
import org.testfx.matcher.base.GeneralMatchers;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.stage.Window;

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

  static Matcher<Node> sizeOf(double x, double y) {
    return GeneralMatchers.typeSafeMatcher(Node.class, "has size (" + x + ", " + y + ")", n -> {
      Bounds bound = n.getBoundsInParent();
      return bound.getWidth() == x && bound.getHeight() == y;
    });
  }

  interface Windows {
    static Matcher<Window> atScreen(double x, double y) {
      return GeneralMatchers.typeSafeMatcher(Window.class, "at screen (" + x + ", " + y + ")",
          window -> window.getX() == x && window.getY() == y);
    }

    static Matcher<Window> sizeOf(double x, double y) {
      return GeneralMatchers.typeSafeMatcher(Window.class, "has size (" + x + ", " + y + ")",
          window -> window.getWidth() == x && window.getHeight() == y);
    }
  }
}
