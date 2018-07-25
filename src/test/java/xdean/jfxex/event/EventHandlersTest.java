package xdean.jfxex.event;

import static org.junit.Assert.*;

import org.junit.Test;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

public class EventHandlersTest {
  int count = 0;

  @Test
  public void testConsume() throws Exception {
    EventHandler<Event> handler = EventHandlers.consume(e -> count++);
    Event e = new Event(EventType.ROOT);
    handler.handle(e);
    assertTrue(e.isConsumed());
    assertEquals(1, count);
  }

  @Test
  public void testConsumeIf() throws Exception {
    EventHandler<Event> handler = EventHandlers.consumeIf(e -> count == 0);
    Event e = new Event(EventType.ROOT);
    handler.handle(e);
    assertTrue(e.isConsumed());
    count = 1;
    e = new Event(EventType.ROOT);
    handler.handle(e);
    assertFalse(e.isConsumed());
  }
}
