package xdean.jfx.ex.support.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;

public class SkinManager {
  private final List<SkinStyle> skinList = new ArrayList<>();

  private ReadOnlyObjectWrapper<SkinStyle> skin = new ReadOnlyObjectWrapper<>();

  private Map<Object, ChangeListener<? super SkinStyle>> map = new HashMap<>();

  public SkinManager() {
  }

  public ReadOnlyObjectProperty<SkinStyle> skinProperty() {
    return skin.getReadOnlyProperty();
  }

  public List<SkinStyle> getSkinList() {
    return Collections.unmodifiableList(skinList);
  }

  public SkinStyle currentSkin() {
    return skin.get();
  }

  public void changeSkin(SkinStyle style) {
    skin.set(style);
  }

  public void addSkin(SkinStyle skin) {
    skinList.add(skin);
  }

  public void bind(Scene scene) {
    if (map.containsKey(scene)) {
      return;
    }
    ChangeListener<? super SkinStyle> listener = (ob, o, n) -> {
      scene.getStylesheets().remove(o.getURL());
      scene.getStylesheets().add(n.getURL());
    };
    if (skin.get() != null) {
      scene.getStylesheets().add(skin.get().getURL());
    }
    skin.addListener(listener);
    map.put(scene, listener);
  }

  public void unbind(Scene scene) {
    ChangeListener<? super SkinStyle> remove = map.remove(scene);
    if (remove != null) {
      skin.removeListener(remove);
    }
  }

  public void bind(Dialog<?> dialog) {
    if (map.containsKey(dialog)) {
      return;
    }
    ChangeListener<? super SkinStyle> listener = (ob, o, n) -> {
      dialog.getDialogPane().getStylesheets().remove(o.getURL());
      dialog.getDialogPane().getStylesheets().add(n.getURL());
    };
    if (skin.get() != null) {
      dialog.getDialogPane().getStylesheets().add(skin.get().getURL());
    }
    skin.addListener(listener);
    map.put(dialog, listener);
    dialog.setOnHidden(e -> {
      skin.removeListener(listener);
      map.remove(dialog);
    });
  }
}
