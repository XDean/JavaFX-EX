package xdean.jfxex.support;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Window;
import xdean.jex.util.calc.MathUtil;

public class DragSupport {

  public interface DragConfig {
    BooleanProperty enableProperty();

    DoubleProperty minXProperty();

    DoubleProperty minYProperty();

    DoubleProperty maxXProperty();

    DoubleProperty maxYProperty();

    DoubleProperty borderWidthProperty();

    void unbind();
  }

  private interface DragHandler extends DragConfig {
    void press(MouseEvent e);

    void drag(MouseEvent e);

    void release(MouseEvent e);
  }

  private static class DragHandlerWrapper {
    final DragHandler dragHandler;
    final EventHandler<MouseEvent> press;
    final EventHandler<MouseEvent> drag;
    final EventHandler<MouseEvent> release;

    public DragHandlerWrapper(DragHandler dragHandler) {
      this.dragHandler = dragHandler;
      this.press = dragHandler::press;
      this.drag = dragHandler::drag;
      this.release = dragHandler::release;
    }
  }

  private static abstract class BaseDrag<T> extends WeakReference<T> implements DragHandler {
    final DoubleProperty minX, maxX;
    final DoubleProperty minY, maxY;
    final DoubleProperty borderWidth;
    final BooleanProperty enable;
    double startX;
    double startY;
    boolean pressed;

    public BaseDrag(T t) {
      super(t);
      this.minX = new SimpleDoubleProperty(0);
      this.minY = new SimpleDoubleProperty(0);
      this.maxX = new SimpleDoubleProperty(Double.MAX_VALUE);
      this.maxY = new SimpleDoubleProperty(Double.MAX_VALUE);
      this.borderWidth = new SimpleDoubleProperty(3);
      this.enable = new SimpleBooleanProperty(true);
    }

    @Override
    public void release(MouseEvent e) {
      pressed = false;
    }

    protected boolean isEnable() {
      return enable.get();
    }

    protected boolean canDrag(Point2D pressLocal, double maxX, double maxY) {
      double bw = borderWidth.get();
      return pressLocal.getX() > bw && pressLocal.getY() > bw &&
          maxX - pressLocal.getX() > bw && maxY - pressLocal.getY() > bw;
    }

    @Override
    public BooleanProperty enableProperty() {
      return enable;
    }

    @Override
    public DoubleProperty maxXProperty() {
      return maxX;
    }

    @Override
    public DoubleProperty maxYProperty() {
      return maxY;
    }

    @Override
    public DoubleProperty minXProperty() {
      return minX;
    }

    @Override
    public DoubleProperty minYProperty() {
      return minY;
    }

    @Override
    public DoubleProperty borderWidthProperty() {
      return borderWidth;
    }
  }

  private static class NodeDrag extends BaseDrag<Node> {

    public NodeDrag(Node node) {
      super(node);
    }

    @Override
    public void press(MouseEvent e) {
      Node node = get();
      if (isEnable() && e.isConsumed() == false && node != null) {
        Bounds boundsInLocal = node.getBoundsInLocal();
        if (canDrag(node.screenToLocal(e.getScreenX(), e.getScreenY()), boundsInLocal.getMaxX(), boundsInLocal.getMaxY())) {
          startX = e.getScreenX() - node.getLayoutX();
          startY = e.getScreenY() - node.getLayoutY();
          e.consume();
          pressed = true;
        }
      }
    }

    @Override
    public void drag(MouseEvent e) {
      Node node = get();
      if (pressed && isEnable() && e.isConsumed() == false && node != null) {
        node.setLayoutX(MathUtil.toRange(e.getScreenX() - startX, minX.get(), maxX.get()));
        node.setLayoutY(MathUtil.toRange(e.getScreenY() - startY, minY.get(), maxY.get()));
        e.consume();
      }
    }

    @Override
    public void unbind() {
      Node node = get();
      if (node != null) {
        DragSupport.unbind(node);
      }
    }
  }

  private static class WindowDrag extends BaseDrag<Window> {

    public WindowDrag(Window window) {
      super(window);
      Rectangle2D bound = Screen.getPrimary().getVisualBounds();
      maxX.set(bound.getMaxX() - window.getWidth());
      maxY.set(bound.getMaxY() - window.getHeight());
    }

    @Override
    public void press(MouseEvent e) {
      Window window = get();
      if (isEnable() && e.isConsumed() == false && window != null) {
        if (canDrag(new Point2D(e.getSceneX(), e.getSceneY()), window.getX() + window.getWidth(),
            window.getY() + window.getHeight())) {
          startX = e.getScreenX() - window.getX();
          startY = e.getScreenY() - window.getY();
          e.consume();
          pressed = true;
        }
      }
    }

    @Override
    public void drag(MouseEvent e) {
      Window window = get();
      if (pressed && isEnable() && e.isConsumed() == false && window != null) {
        window.setX(MathUtil.toRange(e.getScreenX() - startX, minX.get(), maxX.get()));
        window.setY(MathUtil.toRange(e.getScreenY() - startY, minY.get(), maxY.get()));
        e.consume();
      }
    }

    @Override
    public void unbind() {
      Window window = get();
      if (window != null) {
        DragSupport.unbind(window);
      }
    }
  }

  private static final Map<EventTarget, DragHandlerWrapper> HANDLER_MAP = new WeakHashMap<>();

  public static DragConfig bind(Node node) {
    DragHandlerWrapper handler = new DragHandlerWrapper(new NodeDrag(node));
    HANDLER_MAP.put(node, handler);
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, handler.press);
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, handler.drag);
    node.addEventHandler(MouseEvent.MOUSE_RELEASED, handler.release);
    return handler.dragHandler;
  }

  public static DragConfig bind(Window window) {
    DragHandlerWrapper handler = new DragHandlerWrapper(new WindowDrag(window));
    HANDLER_MAP.put(window, handler);
    window.addEventHandler(MouseEvent.MOUSE_PRESSED, handler.press);
    window.addEventHandler(MouseEvent.MOUSE_DRAGGED, handler.drag);
    window.addEventHandler(MouseEvent.MOUSE_RELEASED, handler.release);
    return handler.dragHandler;
  }

  public static void unbind(Node node) {
    DragHandlerWrapper handler = HANDLER_MAP.remove(node);
    if (handler != null) {
      node.removeEventHandler(MouseEvent.MOUSE_PRESSED, handler.press);
      node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, handler.drag);
      node.removeEventHandler(MouseEvent.MOUSE_RELEASED, handler.release);
    }
  }

  public static void unbind(Window window) {
    DragHandlerWrapper handler = HANDLER_MAP.remove(window);
    if (handler != null) {
      window.removeEventHandler(MouseEvent.MOUSE_PRESSED, handler.press);
      window.removeEventHandler(MouseEvent.MOUSE_DRAGGED, handler.drag);
      window.removeEventHandler(MouseEvent.MOUSE_RELEASED, handler.release);
    }
  }
}
