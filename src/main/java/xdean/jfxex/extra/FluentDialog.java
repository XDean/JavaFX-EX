package xdean.jfxex.extra;

import java.util.Optional;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

public class FluentDialog<T> {

  public static <T> FluentDialog<T> create(Dialog<T> dialog) {
    return new FluentDialog<>(dialog);
  }

  private final Dialog<T> dialog;

  public FluentDialog(Dialog<T> dialog) {
    this.dialog = dialog;
  }

  public Dialog<T> getDialog() {
    return dialog;
  }

  public FluentDialog<T> modality(Modality modality) {
    dialog.initModality(modality);
    return this;
  }

  public FluentDialog<T> owner(Window owner) {
    dialog.initOwner(owner);
    return this;
  }

  public FluentDialog<T> style(StageStyle style) {
    dialog.initStyle(style);
    return this;
  }

  public FluentDialog<T> title(String title) {
    dialog.setTitle(title);
    return this;
  }

  public FluentDialog<T> header(String header) {
    dialog.setHeaderText(header);
    return this;
  }

  public FluentDialog<T> content(String content) {
    dialog.setContentText(content);
    return this;
  }

  public FluentDialog<T> graphic(Node graphic) {
    dialog.setGraphic(graphic);
    return this;
  }

  public FluentDialog<T> size(double width, double height) {
    dialog.setWidth(width);
    dialog.setHeight(height);
    return this;
  }

  public FluentDialog<T> position(double x, double y) {
    dialog.setX(x);
    dialog.setY(y);
    return this;
  }

  public FluentDialog<T> resizable(boolean resizable) {
    dialog.setResizable(resizable);
    return this;
  }

  public FluentDialog<T> converter(Callback<ButtonType, T> converter) {
    dialog.setResultConverter(converter);
    return this;
  }

  public FluentDialog<T> dialogPane(DialogPane value) {
    dialog.setDialogPane(value);
    return this;
  }

  public FluentDialog<T> onShowing(EventHandler<DialogEvent> value) {
    dialog.setOnShowing(value);
    return this;
  }

  public FluentDialog<T> onShown(EventHandler<DialogEvent> value) {
    dialog.setOnShown(value);
    return this;
  }

  public FluentDialog<T> onHiding(EventHandler<DialogEvent> value) {
    dialog.setOnHiding(value);
    return this;
  }

  public FluentDialog<T> onHidden(EventHandler<DialogEvent> value) {
    dialog.setOnHidden(value);
    return this;
  }

  public FluentDialog<T> onCloseRequest(EventHandler<DialogEvent> value) {
    dialog.setOnCloseRequest(value);
    return this;
  }

  public void show() {
    dialog.show();
  }

  public Optional<T> showAndWait() {
    return dialog.showAndWait();
  }
}
