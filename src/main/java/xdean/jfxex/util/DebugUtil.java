package xdean.jfxex.util;

import javafx.scene.Node;
import javafx.scene.Parent;

public class DebugUtil {
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
