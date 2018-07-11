package xdean.jfxex.support.undo;

import java.util.List;
import java.util.function.Function;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.behavior.TextFieldBehavior;

public class UndoRedoKey {

  enum UndoRedo {
    UNDO,
    REDO,
    NOT;
  }

  private static final KeyCodeCombination DEFAULT_UNDO_KEY = new KeyCodeCombination(KeyCode.Z,
      KeyCombination.SHORTCUT_DOWN);
  private static final KeyCodeCombination DEFAULT_REDO_KEY = new KeyCodeCombination(KeyCode.Y,
      KeyCombination.SHORTCUT_DOWN);
  public static final Function<KeyEvent, UndoRedo> DEFAULT = e -> {
    if (DEFAULT_UNDO_KEY.match(e)) {
      return UndoRedo.UNDO;
    }
    if (DEFAULT_REDO_KEY.match(e)) {
      return UndoRedo.REDO;
    }
    return UndoRedo.NOT;
  };

  public static final Function<KeyEvent, UndoRedo> TEXT_AREA = e -> convertFromString(matchActionForEvent(TA.BINDINGS, e));

  public static final Function<KeyEvent, UndoRedo> TEXT_FIELD = e -> convertFromString(matchActionForEvent(TF.BINDINGS, e));

  private static UndoRedo convertFromString(String text) {
    if ("Undo".equals(text)) {
      return UndoRedo.UNDO;
    } else if ("Redo".equals(text)) {
      return UndoRedo.REDO;
    } else {
      return UndoRedo.NOT;
    }
  }

  /**
   * From {@code BehaviorBase}
   * 
   * @param keyBindings
   * @param e
   * @return
   */
  private static String matchActionForEvent(List<KeyBinding> keyBindings, KeyEvent e) {
    if (e == null) {
      throw new NullPointerException("KeyEvent must not be null");
    }
    KeyBinding match = null;
    int specificity = 0;
    int maxBindings = keyBindings.size();
    for (int i = 0; i < maxBindings; i++) {
      KeyBinding binding = keyBindings.get(i);
      int s = binding.getSpecificity(null, e);
      if (s > specificity) {
        specificity = s;
        match = binding;
      }
    }
    String action = null;
    if (match != null) {
      action = match.getAction();
    }
    return action;
  }

  private static class TA extends TextAreaBehavior {
    private static final List<KeyBinding> BINDINGS = TEXT_AREA_BINDINGS;

    private TA(TextArea textArea) {
      super(textArea);
    }
  }

  private static class TF extends TextFieldBehavior {
    private static final List<KeyBinding> BINDINGS = TEXT_INPUT_BINDINGS;

    private TF(TextField textField) {
      super(textField);
    }
  }
}