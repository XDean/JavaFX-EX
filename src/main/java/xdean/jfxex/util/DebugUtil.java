package xdean.jfxex.util;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;

public class DebugUtil {

  public static void traceBean(ObservableValue<?> ov) {
    ov.addListener((ob, o, n) -> System.out
        .println(String.format("Observable: %s changed at %s, from %s to %s", ob, Thread.currentThread(), o, n)));
  }

  public static void printAllWithId(Node n, int space) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < space; i++) {
      sb.append("-");
    }
    sb.append(String.format("id:%s  %s", n.getId(), n));
    System.out.println(sb.toString());
    if (n instanceof Parent) {
      ((Parent) n).getChildrenUnmodifiable().forEach(node -> {
        printAllWithId(node, space + 1);
      });
    }
  }
}
