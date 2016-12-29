package xdean.jfx.ex.extra;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ModifiableObject {

  private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper(this, "modified", false);
  private Boolean snapshot;// if not null, means disabled

  public ReadOnlyBooleanProperty modifiedProperty() {
    return modified.getReadOnlyProperty();
  }

  public boolean isModified() {
    return modifiedProperty().get();
  }

  protected void modified() {
    if (snapshot == null) {
      modified.set(true);
    }
  }

  public void saved() {
    if (snapshot == null) {
      modified.set(false);
    }
  }

  public void disableModified() {
    snapshot = modified.getValue();
  }

  public void enableModified() {
    if (snapshot != null && snapshot.booleanValue() == false) {
      saved();
    }
    snapshot = null;
  }

  public void bindModified(ObservableValue<?>... ovs) {
    for (ObservableValue<?> ov : ovs) {
      ov.addListener((observable, o, n) -> modified());
    }
  }

  public <T> void bindModified(ObservableList<T> list) {
    list.addListener(new ListChangeListener<T>() {
      @Override
      public void onChanged(Change<? extends T> c) {
        modified();
      }
    });
  }

  public void bindModified(ModifiableObject... mcs) {
    for (ModifiableObject mc : mcs) {
      mc.modifiedProperty().addListener((observable, o, n) -> {
        if (n) {
          modified();
        }
      });
      modified.addListener((observable, o, n) -> {
        if (!n) {
          mc.saved();
        }
      });
    }
  }
}