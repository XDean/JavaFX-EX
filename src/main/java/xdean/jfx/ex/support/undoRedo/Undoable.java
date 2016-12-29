package xdean.jfx.ex.support.undoRedo;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Undoable {

  public enum Response {
    DONE,
    CANCEL,
    SKIP;
  }

  Response redo();

  Response undo();

  public static Undoable create(Supplier<Response> todo, Supplier<Response> undo) {
    return new Undoable() {
      @Override
      public Response undo() {
        return undo.get();
      }

      @Override
      public Response redo() {
        return todo.get();
      }
    };
  }

  public static <T> Undoable create(Function<T, Response> call, T todo, T undo) {
    return create(() -> call.apply(todo), () -> call.apply(undo));
  }

  public static <T> Undoable create(Function<T, Response> todo, Function<T, Response> undo, T obj) {
    return create(() -> todo.apply(obj), () -> undo.apply(obj));
  }

  public static <T, P> Undoable create(Function<T, Response> todo, Supplier<T> todoObj, Function<P, Response> undo,
      Supplier<P> uodoObj) {
    return create(() -> todo.apply(todoObj.get()), () -> undo.apply(uodoObj.get()));
  }
}
