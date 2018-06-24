package xdean.jfxex.event;

import java.util.function.Predicate;

import javafx.event.Event;
import javafx.event.EventHandler;

public interface EventHandlers {
  static <T extends Event> EventHandler<T> consumeIf(Predicate<T> test) {
    return e -> {
      if (test.test(e)) {
        e.consume();
      }
    };
  }

  static <T extends Event> EventHandler<T> consume(EventHandler<T> handler) {
    return e -> {
      handler.handle(e);
      e.consume();
    };
  }
}
