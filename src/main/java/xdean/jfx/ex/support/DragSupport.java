package xdean.jfx.ex.support;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import xdean.jex.util.calc.MathUtil;

public class DragSupport {

  public interface DragConfig {
    BooleanProperty enableProperty();

    DoubleProperty maxXProperty();

    DoubleProperty maxYProperty();

    DoubleProperty borderWidthProperty();

    void unbind();
  }

  private interface DragHandle extends DragConfig {
    void press(MouseEvent e);

    void drag(MouseEvent e);

    void release(MouseEvent e);
  }

  private static abstract class BaseDrag<T> extends WeakReference<T> implements DragHandle {
    final DoubleProperty maxX;
    final DoubleProperty maxY;
    final DoubleProperty borderWidth;
    final BooleanProperty enable;
    double startX;
    double startY;
    boolean pressed;

    public BaseDrag(T t) {
      super(t);
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
      return pressLocal.getX() > bw &&
          pressLocal.getY() > bw &&
          maxX - pressLocal.getX() > bw &&
          maxY - pressLocal.getY() > bw;
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
        System.out.print(node.screenToLocal(e.getScreenX(), e.getScreenY()));
        System.out.println("\t" + boundsInLocal);
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
        node.setLayoutX(MathUtil.toRange(e.getScreenX() - startX, 0, maxX.get()));
        node.setLayoutY(MathUtil.toRange(e.getScreenY() - startY, 0, maxY.get()));
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
    }

    @Override
    public void press(MouseEvent e) {
      Window window = get();
      if (isEnable() && e.isConsumed() == false && window != null) {
        if (canDrag(new Point2D(e.getSceneX(), e.getSceneY()), window.getX() + window.getWidth(), window.getY() + window.getHeight())) {
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
        window.setX(MathUtil.toRange(e.getScreenX() - startX, 0, maxX.get()));
        window.setY(MathUtil.toRange(e.getScreenY() - startY, 0, maxY.get()));
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

  private static Map<EventTarget, DragHandle> map = new WeakHashMap<>();

  public static DragConfig bind(Node node) {
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    node.addEventHandler(MouseEvent.MOUSE_RELEASED, release);
    DragHandle dragConfig = new NodeDrag(node);
    map.put(node, dragConfig);
    return dragConfig;
  }

  public static DragConfig bind(Window window) {
    window.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
    window.addEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    window.addEventHandler(MouseEvent.MOUSE_RELEASED, release);
    DragHandle dragConfig = new WindowDrag(window);
    map.put(window, dragConfig);
    return dragConfig;
  }

  public static void unbind(Node node) {
    node.removeEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    node.removeEventHandler(MouseEvent.MOUSE_RELEASED, release);
    map.remove(node);
  }

  public static void unbind(Window window) {
    window.removeEventHandler(MouseEvent.MOUSE_PRESSED, press);
    window.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    window.removeEventHandler(MouseEvent.MOUSE_RELEASED, release);
    map.remove(window);
  }

  private static final EventHandler<MouseEvent> press = e -> Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.press(e));
  private static final EventHandler<MouseEvent> drag = e -> Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.drag(e));
  private static final EventHandler<MouseEvent> release = e -> Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.release(e));
}
