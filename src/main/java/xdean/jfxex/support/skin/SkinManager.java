package xdean.jfxex.support.skin;

import static xdean.jfxex.bean.ListenerUtil.addListenerAndInvoke;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;

public class SkinManager {
  private final ObservableList<SkinStyle> skinList = FXCollections.observableArrayList();
  private final ObjectProperty<SkinStyle> skin = new SimpleObjectProperty<>(this, "skin", SkinStyle.EMPTY);
  private final Map<Object, ChangeListener<? super SkinStyle>> map = new WeakHashMap<>();

  public ObjectProperty<SkinStyle> skinProperty() {
    return skin;
  }

  public ObservableList<SkinStyle> getSkinList() {
    return skinList;
  }

  public Scene bind(Scene scene) {
    if (map.containsKey(scene)) {
      return scene;
    }
    ChangeListener<? super SkinStyle> listener = (ob, o, n) -> {
      scene.getStylesheets().remove(o.getURL());
      scene.getStylesheets().add(n.getURL());
    };
    addListenerAndInvoke(skin, listener);
    map.put(scene, listener);
    return scene;
  }

  public void unbind(Scene scene) {
    ChangeListener<? super SkinStyle> remove = map.remove(scene);
    if (remove != null) {
      skin.removeListener(remove);
    }
  }

  public <T> Dialog<T> bind(Dialog<T> dialog) {
    if (map.containsKey(dialog)) {
      return dialog;
    }
    ChangeListener<? super SkinStyle> listener = (ob, o, n) -> {
      dialog.getDialogPane().getStylesheets().remove(o.getURL());
      dialog.getDialogPane().getStylesheets().add(n.getURL());
    };
    addListenerAndInvoke(skin, listener);
    map.put(dialog, listener);
    dialog.setOnHidden(e -> {
      skin.removeListener(listener);
      map.remove(dialog);
    });
    return dialog;
  }

  public void unbind(Dialog<?> dialog) {
    ChangeListener<? super SkinStyle> remove = map.remove(dialog);
    if (remove != null) {
      skin.removeListener(remove);
    }
  }
}
