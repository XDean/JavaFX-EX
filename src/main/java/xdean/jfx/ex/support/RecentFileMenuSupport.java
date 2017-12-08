package xdean.jfx.ex.support;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.util.Pair;
import xdean.jex.util.log.Logable;

public abstract class RecentFileMenuSupport implements Logable{

  private ObservableList<Pair<File, MenuItem>> recents;
  private Consumer<File> onAction = f -> getClass();
  @SuppressWarnings("unused")
  private Menu menu;

  public RecentFileMenuSupport(Menu menu) {
    this.menu = menu;
    recents = FXCollections.observableArrayList();
    recents.addListener((ListChangeListener.Change<? extends Pair<File, MenuItem>> change) -> {
      while (change.next()) {
        if (change.wasRemoved()) {
          change.getRemoved().stream()
              .map(Pair::getValue)
              .forEach(menu.getItems()::remove);
        }
        else if (change.wasAdded()) {
          change.getAddedSubList().stream()
              .map(Pair::getValue)
              .forEach(item -> menu.getItems().add(0, item));
        }
        _save();
      }
    });
    _load();
  }

  public abstract List<String> load();

  public abstract void save(List<String> s);

  private void _load() {
    try {
      Observable.fromIterable(load())
          .map(String::trim)
          .filter(s -> s.length() != 0)
          .map(File::new)
          .map(this::getRecentMenuItemPair)
          .forEach(recents::add);
    } catch (Exception e) {
      log().error("Error to load recent location", e);
    }
  }

  private void _save() {
    save(recents.stream()
        .map(Pair::getKey)
        .map(File::toString)
        .collect(Collectors.toList()));
  }

  public File getLastFile() {
    if (recents.size() == 0) {
      return new File(".");
    } else {
      Pair<File, MenuItem> pair = recents.remove(recents.size() - 1);
      File file = pair.getKey();
      if (Files.exists(file.toPath())) {
        return file;
      } else {
        recents.remove(pair);
        return getLastFile();
      }
    }
  }

  public void setLastFile(File file) {
    if (file == null) {
      return;
    }
    List<Pair<File, MenuItem>> collect = recents.stream().filter(p -> p.getKey().equals(file))
        .collect(Collectors.toList());
    recents.removeAll(collect);
    recents.add(getRecentMenuItemPair(file));
  }

  public void clear() {
    recents.clear();
  }

  public void setOnAction(Consumer<File> onAction) {
    this.onAction = onAction;
  }

  private Pair<File, MenuItem> getRecentMenuItemPair(File file) {
    MenuItem item = new MenuItem();
    item.setText(file.toString());
    item.setOnAction(e -> onAction.accept(file));
    return new Pair<>(file, item);
  }

}
