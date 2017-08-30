package xdean.jfx.ex.util.bean;

import static xdean.jex.util.cache.CacheUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import rx.Observable;
import rx.subscriptions.Subscriptions;
import xdean.jex.util.collection.ListUtil;

import com.sun.javafx.binding.BidirectionalContentBinding;

public class CollectionUtil {

  public static <F, T> void bind(ObservableList<F> list1, ObservableList<T> list2, Function<F, T> forward,
      Function<T, F> backward) {
    ObservableList<T> newList1 = CollectionUtil.map(list1, forward, backward);
    set(BeanUtil.class, list1, newList1);
    BidirectionalContentBinding.bind(newList1, list2);
  }

  public static <F, T> ObservableList<T> map(ObservableList<F> list, Function<F, T> forward, Function<T, F> backward) {
    ObservableList<T> newList = FXCollections.observableArrayList();
    newList.setAll(ListUtil.map(list, forward));

    MapToTargetListener<F, T> forwardListener = new MapToTargetListener<>(list, newList, forward);
    MapToTargetListener<T, F> backwardListener = new MapToTargetListener<>(newList, list, backward);
    forwardListener.updating.bindBidirectional(backwardListener.updating);

    list.addListener(forwardListener);
    newList.addListener(backwardListener);
    return newList;
  }

  /**
   * To make the element always appeared and only appeared on the specified position of the list. And add it to the list
   * immediately.<br>
   *
   * TODO: release position<br>
   * XXX: don't always whole list scan
   *
   * @deprecated  Serious bug, should be redesign
   * @param list
   * @param element
   * @param pos Index to fix onto. If the position has been fixed by another element, it will be replaced.
   * @throws IllegalArgumentException If the position is not available. For example, if you want to fix an element onto
   *           index 2, there must have an element fixed on the index 0 and 1.
   */
  @Deprecated
  static <T> void fixPosition(ObservableList<T> list, T element, int pos) throws IllegalArgumentException {
    // Because collection can't be key
    int identityHashCode = System.identityHashCode(list);
    List<T> forward = cache(identityHashCode, "CollectionUtil.fixPosition.forward", () -> new ArrayList<>());
    List<T> backward = cache(identityHashCode, "CollectionUtil.fixPosition.backward", () -> new ArrayList<>());
    cache(identityHashCode, "CollectionUtil.fixPosition.subscription", () -> {
      AtomicBoolean handling = new AtomicBoolean(false);
      return Observable.create(subscriber -> {
        ListChangeListener<T> listener = c -> subscriber.onNext(list);
        list.addListener(listener);
        subscriber.add(Subscriptions.create(() -> list.removeListener(listener)));
      })
          .filter(l -> handling.get() == false)
          .doOnNext(s -> handling.set(true))
          // XXX If don't add an element first, an exception will be throw
          // Caused by: java.lang.UnsupportedOperationException
          // at java.util.Collections$UnmodifiableCollection.add(Collections.java:1055)
          // at javafx.collections.ListChangeBuilder.nextRemove(ListChangeBuilder.java:204)
          // at javafx.collections.ObservableListBase.nextRemove(ObservableListBase.java:150)
          // at javafx.collections.ModifiableObservableListBase.remove(ModifiableObservableListBase.java:181)
          // at java.util.AbstractList$Itr.remove(AbstractList.java:374)
          // at java.util.Collection.removeIf(Collection.java:415)
          // at xdean.jfx.ex.util.bean.CollectionUtil.lambda$5(CollectionUtil.java:61)
          .doOnNext(l -> {
            Stream.concat(forward.stream(), backward.stream())
                .findFirst()
                .ifPresent(t -> {
                  list.add(0, t);
                  list.remove(0);
                });
          })
          .doOnNext(l -> {
            list.removeIf(forward::contains);
            list.addAll(0, forward);
          })
          .doOnNext(l -> {
            list.removeIf(backward::contains);
            list.addAll(backward);
          })
          .doOnNext(s -> handling.set(false))
          .subscribe();
    });
    if (pos >= 0) {
      if (forward.size() < pos) {
        throw new IllegalArgumentException("Fix previous positions first. Expect at most: " + forward.size());
      } else if (forward.size() == pos) {
        forward.add(element);
      } else {
        forward.set(pos, element);
        list.remove(pos);
      }
      list.add(pos, element);
    }
    if (pos < 0) {
      if (backward.size() < -pos - 1) {
        throw new IllegalArgumentException("Fix previous positions first. Expect at least: " + (-backward.size() - 1));
      } else if (backward.size() == -pos - 1) {
        backward.add(0, element);
      } else {
        backward.set(backward.size() + pos + 1, element);
        list.remove(backward.size() + pos + 1);
      }
      list.add(list.size() + pos + 1, element);
    }
  }
}
