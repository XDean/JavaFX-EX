package xdean.jfx.ex.support;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.VerticalDirection;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class ResizeSupport {

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

  private static final double OFFSET = 3;

  private static class SizeParam {
    WeakReference<Node> nodeRef;
    DoubleProperty width;
    DoubleProperty height;

    Cursor lastCursor;
    Corner pressedCorner;

    double startX;
    double startY;
    double startWidth;
    double startHeight;
    double startPosX;
    double startPosY;

    public SizeParam(Node node, DoubleProperty width, DoubleProperty height) {
      super();
      this.nodeRef = new WeakReference<>(node);
      this.width = width;
      this.height = height;
    }

    private void move(MouseEvent e) {
      setCursor(calcCorner(e));
    }

    private void press(MouseEvent e) {
      Node node = nodeRef.get();
      if (e.isConsumed() == false && node != null) {
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
      Node node = nodeRef.get();
      if (pressedCorner != null && e.isConsumed() == false && node != null) {
        double dx = e.getScreenX() - startX;
        if (pressedCorner.horizontal == HorizontalDirection.RIGHT) {
          width.set(startWidth + dx);
        } else if (pressedCorner.horizontal == HorizontalDirection.LEFT) {
          if (startWidth - dx > node.minWidth(height.get())) {
            width.set(startWidth - dx);
            node.setLayoutX(startPosX + dx);
          }
        }

        double dy = e.getScreenY() - startY;
        if (pressedCorner.vertical == VerticalDirection.DOWN) {
          height.set(startHeight + dy);
        } else if (pressedCorner.vertical == VerticalDirection.UP) {
          if (startHeight - dy > node.minHeight(width.get())) {
            height.set(startHeight - dy);
            node.setLayoutY(startPosY + dy);
          }
        }
        e.consume();
      }
    }

    private void release(MouseEvent e) {
      pressedCorner = null;
    }

    private void setCursor(Corner corner) {
      Node node = nodeRef.get();
      if (node == null) {
        return;
      }
      if (corner == Corner.CENTER) {
        if (lastCursor != null) {
          node.setCursor(lastCursor);
        } else {
          node.setCursor(corner.cursor);
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

      if (left < OFFSET) {
        if (up < OFFSET) {
          return Corner.LEFT_UP;
        }
        if (down < OFFSET) {
          return Corner.LEFT_DOWN;
        }
        return Corner.LEFT;
      }
      if (right < OFFSET) {
        if (up < OFFSET) {
          return Corner.RIGHT_UP;
        }
        if (down < OFFSET) {
          return Corner.RIGHT_DOWN;
        }
        return Corner.RIGHT;
      }

      if (up < OFFSET) {
        return Corner.UP;
      }
      if (down < OFFSET) {
        return Corner.DOWN;
      }
      return Corner.CENTER;
    }
  }

  private static Map<EventTarget, SizeParam> map = new WeakHashMap<>();

  public static void bind(Region node) {
    bind(node, node.prefWidthProperty(), node.prefHeightProperty());
  }

  public static void bind(Node node, DoubleProperty width, DoubleProperty height) {
    node.addEventHandler(MouseEvent.MOUSE_MOVED, move);
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    node.addEventHandler(MouseEvent.MOUSE_RELEASED, release);
    map.put(node, new SizeParam(node, width, height));
  }

  public static void unbind(Node node) {
    node.removeEventHandler(MouseEvent.MOUSE_MOVED, move);
    node.removeEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    node.addEventHandler(MouseEvent.MOUSE_RELEASED, release);
    map.remove(node);
  }

  private static final EventHandler<MouseEvent> move = e ->
      Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.move(e));

  private static final EventHandler<MouseEvent> press = e ->
      Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.press(e));

  private static final EventHandler<MouseEvent> drag = e ->
      Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.drag(e));

  private static final EventHandler<MouseEvent> release = e ->
      Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.release(e));
}
