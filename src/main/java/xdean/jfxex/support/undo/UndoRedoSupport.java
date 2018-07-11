package xdean.jfxex.support.undo;

import java.util.EmptyStackException;
import java.util.List;
import java.util.WeakHashMap;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextInputControl;
import xdean.jex.log.Logable;

public class UndoRedoSupport implements Undoable, Logable {

  private static WeakHashMap<Object, UndoRedoSupport> map = new WeakHashMap<>();

  public static UndoRedoSupport get(Object key) {
    return map.computeIfAbsent(key, k -> new UndoRedoSupport());
  }

  public static void set(Object key, UndoRedoSupport urs) {
    map.put(key, urs);
  }

  public static UndoRedoSupport remove(Object key) {
    return map.remove(key);
  }

  public static UndoRedoSupport getContext() {
    return get(Thread.currentThread());
  }

  public static void setContext(UndoRedoSupport urs) {
    set(Thread.currentThread(), urs);
  }

  public static UndoRedoSupport removeContext() {
    return remove(Thread.currentThread());
  }

  ObservableList<Undoable> undoList;
  ObservableList<Undoable> redoList;
  boolean handling;
  boolean addable = true;

  private UndoRedoSupport() {
    undoList = FXCollections.observableArrayList();
    redoList = FXCollections.observableArrayList();
  }

  public ObservableList<Undoable> undoListProperty() {
    return undoList;
  }

  public ObservableList<Undoable> redoListProperty() {
    return redoList;
  }

  public boolean isUndoable() {
    return !undoList.isEmpty();
  }

  public boolean isRedoable() {
    return !redoList.isEmpty();
  }

  @Override
  public Response undo() {
    debug().log("To undo, size = " + undoList.size() + ", " + this);
    handling = true;
    try {
      Undoable u;
      while (true) {
        if (isUndoable()) {
          u = pop(undoList);
        } else {
          return Response.DONE;
        }
        switch (u.undo()) {
        case CANCEL:
          push(undoList, u);
          return Response.CANCEL;
        case DONE:
          push(redoList, u);
          return Response.DONE;
        case SKIP:
          continue;
        }
      }
    } finally {
      handling = false;
    }
  }

  @Override
  public Response redo() {
    debug().log("To redo, size = " + redoList.size() + ", " + this);
    handling = true;
    try {
      Undoable u;
      while (true) {
        if (isRedoable()) {
          u = pop(redoList);
        } else {
          return Response.DONE;
        }
        switch (u.redo()) {
        case CANCEL:
          push(redoList, u);
          return Response.CANCEL;
        case DONE:
          push(undoList, u);
          return Response.DONE;
        case SKIP:
          continue;
        }
      }
    } finally {
      handling = false;
    }
  }

  /**
   * An action has been done and add it into uedoList
   *
   * @param u the action
   * @return whether the action add to undo list
   */
  public boolean add(Undoable u) {
    if (!addable || handling) {
      return false;
    }
    debug().log("Add new in " + this);
    push(undoList, u);
    redoList.clear();
    return true;
  }

  public void setAddable(boolean b) {
    addable = b;
  }

  public boolean isAddable() {
    return addable;
  }

  private <E> E pop(List<E> list) {
    E obj;
    int len = list.size();

    obj = peek(list);
    list.remove(len - 1);

    return obj;
  }

  private <E> E peek(List<E> list) {
    int len = list.size();
    if (len == 0) {
      throw new EmptyStackException();
    }
    return list.get(len - 1);
  }

  private <E> E push(List<E> list, E e) {
    list.add(e);
    return e;
  }

  /************************** bind *************************/
  public void bind(CheckBox box) {
    box.selectedProperty()
        .addListener((ob, o, n) -> add(Undoable.create(UndoUtil.weakConsumer(box, CheckBox::setSelected), n, o)));
  }

  public <T> void bind(ComboBox<T> box) {
    box.getSelectionModel()
        .selectedItemProperty()
        .addListener((ob, o, n) -> add(Undoable.create(
            UndoUtil.<SingleSelectionModel<T>, T> weakConsumer(box.getSelectionModel(), SingleSelectionModel::select),
            n, o)));
  }

  public void bind(TextInputControl text) {
    class TextUndoRedoSupport {
      TextInputControl textControl;
      String oldText;

      public TextUndoRedoSupport(TextInputControl textControl) {
        this.textControl = textControl;
        textControl.focusedProperty().addListener(this::focusChanged);
        textControl.textProperty().addListener(this::textChanged);
      }

      private void textChanged(Observable ob, String o, String n) {
        if (!textControl.isFocused()) {
          addAction(o, n);
        }
      }

      private void focusChanged(Observable ob, boolean o, boolean n) {
        if (n) {
          oldText = textControl.getText();
        } else {
          if (oldText != null) {
            String newText = textControl.getText();
            if (!newText.equals(oldText)) {
              addAction(oldText, newText);
            }
          }
        }
      }

      private void addAction(String oldText, String newText) {
        UndoRedoSupport.this.add(Undoable.create(UndoUtil.weakConsumer(textControl, TextInputControl::setText),
            newText, oldText));
      }
    }
    new TextUndoRedoSupport(text);
  }
}