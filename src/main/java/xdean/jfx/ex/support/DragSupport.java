package xdean.jfx.ex.support;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import xdean.jex.util.calc.MathUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

public class DragSupport {

  public interface DragConfig {
    BooleanProperty enableProperty();

    DoubleProperty maxXProperty();

    DoubleProperty maxYProperty();
  }

  private interface DragHandle extends DragConfig {
    void press(MouseEvent e);

    void drag(MouseEvent e);
  }

  private static class NodeDrag implements DragHandle {
    final WeakReference<Node> nodeRef;
    final DoubleProperty maxX;
    final DoubleProperty maxY;
    final BooleanProperty enable;
    double startX;
    double startY;

    public NodeDrag(Node node) {
      this.nodeRef = new WeakReference<>(node);
      this.maxX = new SimpleDoubleProperty(Double.MAX_VALUE);
      this.maxY = new SimpleDoubleProperty(Double.MAX_VALUE);
      this.enable = new SimpleBooleanProperty(true);
    }

    @Override
    public void press(MouseEvent e) {
      Node node = nodeRef.get();
      if (isEnable() && e.isConsumed() == false && node != null) {
        startX = e.getScreenX() - node.getLayoutX();
        startY = e.getScreenY() - node.getLayoutY();
        e.consume();
      }
    }

    @Override
    public void drag(MouseEvent e) {
      Node node = nodeRef.get();
      if (isEnable() && e.isConsumed() == false && node != null) {
        node.setLayoutX(MathUtil.toRange(e.getScreenX() - startX, 0, maxX.get()));
        node.setLayoutY(MathUtil.toRange(e.getScreenY() - startY, 0, maxY.get()));
        e.consume();
      }
    }

    public boolean isEnable() {
      return enable.get();
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
  }

  private static class WindowDrag implements DragHandle {

    final WeakReference<Window> nodeRef;
    final DoubleProperty maxX;
    final DoubleProperty maxY;
    final BooleanProperty enable;
    double startX;
    double startY;

    public WindowDrag(Window node) {
      this.nodeRef = new WeakReference<>(node);
      this.maxX = new SimpleDoubleProperty(Double.MAX_VALUE);
      this.maxY = new SimpleDoubleProperty(Double.MAX_VALUE);
      this.enable = new SimpleBooleanProperty(true);
    }

    @Override
    public void press(MouseEvent e) {
      Window node = nodeRef.get();
      if (isEnable() && e.isConsumed() == false && node != null) {
        startX = e.getScreenX() - node.getX();
        startY = e.getScreenY() - node.getY();
        e.consume();
      }
    }

    @Override
    public void drag(MouseEvent e) {
      Window node = nodeRef.get();
      if (isEnable() && e.isConsumed() == false && node != null) {
        node.setX(MathUtil.toRange(e.getScreenX() - startX, 0, maxX.get()));
        node.setY(MathUtil.toRange(e.getScreenY() - startY, 0, maxY.get()));
        e.consume();
      }
    }

    public boolean isEnable() {
      return enable.get();
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

  }

  private static Map<EventTarget, DragHandle> map = new WeakHashMap<>();

  public static DragConfig bind(Node node) {
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.addEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    DragHandle dragConfig = new NodeDrag(node);
    map.put(node, dragConfig);
    return dragConfig;
  }

  public static DragConfig bind(Window window) {
    window.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
    window.addEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    DragHandle dragConfig = new WindowDrag(window);
    map.put(window, dragConfig);
    return dragConfig;
  }

  public static void unbind(Node node) {
    node.removeEventHandler(MouseEvent.MOUSE_PRESSED, press);
    node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    map.remove(node);
  }

  public static void unbind(Window window) {
    window.removeEventHandler(MouseEvent.MOUSE_PRESSED, press);
    window.removeEventHandler(MouseEvent.MOUSE_DRAGGED, drag);
    map.remove(window);
  }

  private static final EventHandler<MouseEvent> press = e ->
      Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.press(e));

  private static final EventHandler<MouseEvent> drag = e ->
      Optional.ofNullable(map.get(e.getSource())).ifPresent(d -> d.drag(e));
}
