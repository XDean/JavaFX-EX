package xdean.jfx.ex.support;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.VerticalDirection;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import xdean.jex.util.calc.MathUtil;

public class ResizeSupport {

  public interface ResizeConfig {
    BooleanProperty enableProperty();

    DoubleProperty borderWidthProperty();

    DoubleProperty minWidthProperty();

    DoubleProperty minHeightProperty();

    DoubleProperty maxWidthProperty();

    DoubleProperty maxHeightProperty();

    ObjectProperty<Cursor> defaultCursorProperty();

    void unbind();
  }

  enum Corner {
    UP(Cursor.N_RESIZE, null, VerticalDirection.UP),
    LEFT_UP(Cursor.NW_RESIZE, HorizontalDirection.LEFT, VerticalDirection.UP),
    LEFT(Cursor.W_RESIZE, HorizontalDirection.LEFT, null),
    LEFT_DOWN(Cursor.SW_RESIZE, HorizontalDirection.LEFT, VerticalDirection.DOWN),
    DOWN(Cursor.S_RESIZE, null, VerticalDirection.DOWN),
    RIGHT_DOWN(Cursor.SE_RESIZE, HorizontalDirection.RIGHT, VerticalDirection.DOWN),
    RIGHT(Cursor.E_RESIZE, HorizontalDirection.RIGHT, null),
    RIGHT_UP(Cursor.NE_RESIZE, HorizontalDirection.RIGHT, VerticalDirection.UP),
    CENTER(Cursor.DEFAULT, null, null);

    final Cursor cursor;
    final HorizontalDirection horizontal;
    final VerticalDirection vertical;

    private Corner(Cursor cursor, HorizontalDirection horizontal, VerticalDirection vertical) {
      this.cursor = cursor;
      this.horizontal = horizontal;
      this.vertical = vertical;
    }
  }

  private static class BaseResize extends WeakReference<Node> implements ResizeConfig {
    DoubleProperty width;
    DoubleProperty height;
    DoubleProperty borderWidth = new SimpleDoubleProperty(3);
    DoubleProperty minWidth = new SimpleDoubleProperty(10);
    DoubleProperty minHeight = new SimpleDoubleProperty(10);
    DoubleProperty maxWidth = new SimpleDoubleProperty(Double.MAX_VALUE);
    DoubleProperty maxHeight = new SimpleDoubleProperty(Double.MAX_VALUE);
    BooleanProperty enable = new SimpleBooleanProperty(true);
    ObjectProperty<Cursor> defaultCursor = new SimpleObjectProperty<>(Cursor.DEFAULT);

    Cursor lastCursor;
    Corner pressedCorner;

    double startX;
    double startY;
    double startWidth;
    double startHeight;
    double startPosX;
    double startPosY;

    public BaseResize(Node node, DoubleProperty width, DoubleProperty height) {
      super(node);
      this.width = width;
      this.height = height;
      enable.addListener((ob, o, n) -> {
        if (n == false) {
          setCursor(Corner.CENTER);
        }
      });
    }

    private void move(MouseEvent e) {
      if (isEnable()) {
        setCursor(calcCorner(e));
      }
    }

    private void press(MouseEvent e) {
      Node node = get();
      if (isEnable() && e.isConsumed() == false && node != null) {
        Corner corner = calcCorner(e);
        if (corner != Corner.CENTER) {
          pressedCorner = corner;
          startX = e.getScreenX();
          startY = e.getScreenY();
          startWidth = width.get() == -1 ? node.prefWidth(-1) : width.get();
          startHeight = height.get() == -1 ? node.prefHeight(-1) : height.get();
          startPosX = node.getLayoutX();
          startPosY = node.getLayoutY();
          e.consume();
        }
      }
    }

    private void drag(MouseEvent e) {
      Node node = get();
      if (isEnable() && pressedCorner != null && e.isConsumed() == false && node != null) {
        double dx = e.getScreenX() - startX;
        if (pressedCorner.horizontal == HorizontalDirection.RIGHT) {
          width.set(MathUtil.toRange(startWidth + dx, minWidth.get(), maxWidth.get()));
        } else if (pressedCorner.horizontal == HorizontalDirection.LEFT) {
          width.set(MathUtil.toRange(startWidth - dx, minWidth.get(), maxWidth.get()));
          node.setLayoutX(startWidth + startPosX - width.get());
        }

        double dy = e.getScreenY() - startY;
        if (pressedCorner.vertical == VerticalDirection.DOWN) {
          height.set(MathUtil.toRange(startHeight + dy, minHeight.get(), maxHeight.get()));
        } else if (pressedCorner.vertical == VerticalDirection.UP) {
          height.set(MathUtil.toRange(startHeight - dy, minHeight.get(), maxHeight.get()));
          node.setLayoutY(startHeight + startPosY - height.get());
        }
        e.consume();
      }
    }

    private void release(MouseEvent e) {
      pressedCorner = null;
    }

    private void setCursor(Corner corner) {
      Node node = get();
      if (node == null) {
        return;
      }
      if (corner == Corner.CENTER) {
        if (lastCursor != null) {
          node.setCursor(lastCursor);
        } else {
          node.setCursor(defaultCursor.get());
        }
        lastCursor = null;
      } else {
        if (lastCursor == null) {
          lastCursor = node.getCursor();
        }
        node.setCursor(corner.cursor);
      }
    }

    private Corner calcCorner(MouseEvent e) {
      double left = e.getX();
      double right = width.get() - e.getX();
      double up = e.getY();
      double down = height.get() - e.getY();

      double borderWidth = getBorderWidth();
      if (left < borderWidth) {
        if (up < borderWidth) {
          return Corner.LEFT_UP;
        }
        if (down < borderWidth) {
          return Corner.LEFT_DOWN;
        }
        return Corner.LEFT;
      }
      if (right < borderWidth) {
        if (up < borderWidth) {
          return Corner.RIGHT_UP;
        }
        if (down < borderWidth) {
          return Corner.RIGHT_DOWN;
        }
        return Corner.RIGHT;
      }

      if (up < borderWidth) {
        return Corner.UP;
      }
      if (down < borderWidth) {
        return Corner.DOWN;
      }
      return Corner.CENTER;
    }

    public double getBorderWidth() {
      return borderWidth.get();
    }

    public boolean isEnable() {
      return enable.get();
    }

    @Override
    public BooleanProperty enableProperty() {
      return enable;
    }

    @Override
    public DoubleProperty borderWidthProperty() {
      return borderWidth;
    }

    @Override
    public DoubleProperty minWidthProperty() {
      return minWidth;
    }

    @Override
    public DoubleProperty minHeightProperty() {
      return minHeight;
    }

    @Override
    public ObjectProperty<Cursor> defaultCursorProperty() {
      return defaultCursor;
    }

    @Override
    public DoubleProperty maxWidthProperty() {
      return maxWidth;
    }

    @Override
    public DoubleProperty maxHeightProperty() {
      return maxHeight;
    }

    @Override
    public void unbind() {
      Node node = get();
      if (node != null) {
        ResizeSupport.unbind(node);
      }
    }
  }

  private static Map<EventTarget, BaseResize> map = new WeakHashMap<>();

  public static ResizeConfig bind(Region region) {
    return bind(region, region.prefWidthProperty(), region.prefHeightProperty());
  }

  public static ResizeConfig bind(Rectangle rectangle) {
    return bind(rectangle, rectangle.widthProperty(), rectangle.heightProperty());
  }

  public static ResizeConfig bind(Node node, DoubleProperty width, DoubleProperty height) {
    node.addEventHandler(MouseEvent.MOUSE_MOVED, move);
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    node.addEventHandler(MouseEvent.MOUSE_RELEASED, release);
    BaseResize resize = new BaseResize(node, width, height);
    map.put(node, resize);
    return resize;
  }

  public static void unbind(Node node) {
    node.removeEventHandler(MouseEvent.MOUSE_MOVED, move);
    node.removeEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    node.removeEventHandler(MouseEvent.MOUSE_RELEASED, release);
    map.remove(node).setCursor(Corner.CENTER);;
  }

  private static final EventHandler<MouseEvent> move = e -> Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.move(e));

  private static final EventHandler<MouseEvent> press = e -> Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.press(e));

  private static final EventHandler<MouseEvent> drag = e -> Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.drag(e));

  private static final EventHandler<MouseEvent> release = e -> Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.release(e));
}
