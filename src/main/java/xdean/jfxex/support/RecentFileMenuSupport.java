package xdean.jfxex.support;

import static xdean.jex.util.function.Predicates.isEquals;
import static xdean.jfxex.bean.BeanUtil.mapList;
import static xdean.jfxex.bean.ListenerUtil.list;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import xdean.jex.log.Logable;

public abstract class RecentFileMenuSupport implements Logable {

  private final ObservableList<Path> recentFiles = FXCollections.observableArrayList();

  public RecentFileMenuSupport() {
    _load();
    recentFiles.addListener(list(b -> b.onChange(c -> _save())));
  }

  public void bind(Menu menu, Consumer<Path> onAction) {
    bind(menu, o -> {
      MenuItem item = new MenuItem();
      item.setText(o.toString());
      item.setOnAction(e -> onAction.accept(o));
      return item;
    });
  }

  public void bind(Menu menu, Function<Path, MenuItem> factory) {
    ObservableList<MenuItem> items = mapList(recentFiles, factory);
    Bindings.bindContentBidirectional(items, menu.getItems());
  }

  public abstract List<String> load();

  public abstract void save(List<String> s);

  private void _load() {
    try {
      Observable.fromIterable(load())
          .map(String::trim)
          .filter(s -> s.length() != 0)
          .map(Paths::get)
          .forEach(recentFiles::add);
    } catch (Exception e) {
      error().log("Error to load recent location", e);
    }
  }

  private void _save() {
    save(recentFiles.stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
  }

  public Optional<Path> getLatestFile() {
    return recentFiles.stream().findFirst();
  }

  public void setLatestFile(Path path) {
    recentFiles.removeIf(isEquals(path));
    recentFiles.add(0, path);
  }

  public void clear() {
    recentFiles.clear();
  }
}
