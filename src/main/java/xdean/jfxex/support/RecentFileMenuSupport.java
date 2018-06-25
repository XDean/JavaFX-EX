package xdean.jfxex.support;

import static xdean.jex.util.function.Predicates.isEquals;
import static xdean.jfxex.bean.BeanUtil.mapList;
import static xdean.jfxex.bean.ListenerUtil.list;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import xdean.jex.log.Logable;

public abstract class RecentFileMenuSupport implements Logable {

  private final ObservableList<Path> recentFiles = FXCollections.observableArrayList();
  private final List<Consumer<Path>> handlers = new ArrayList<>();

  public RecentFileMenuSupport() {
    recentFiles.addListener(list(b -> b.onChange(c -> save())));
  }

  public ObservableList<MenuItem> toMenuItems() {
    return mapList(recentFiles, this::pathToMenuItem);
  }

  public void addHandler(Consumer<Path> handler) {
    handlers.add(handler);
  }

  public void removeHandler(Consumer<Path> handler) {
    handlers.remove(handler);
  }

  protected MenuItem pathToMenuItem(Path path) {
    MenuItem item = new MenuItem();
    item.setText(path.toString());
    item.setOnAction(e -> handlers.forEach(h -> h.accept(path)));
    return item;
  }

  protected abstract List<String> doLoad();

  protected abstract void doSave(List<String> s);

  public void load() {
    try {
      Observable.fromIterable(doLoad())
          .map(String::trim)
          .filter(s -> s.length() != 0)
          .map(Paths::get)
          .forEach(recentFiles::add);
    } catch (Exception e) {
      error().log("Error to load recent location", e);
    }
  }

  public void save() {
    doSave(recentFiles.stream()
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
