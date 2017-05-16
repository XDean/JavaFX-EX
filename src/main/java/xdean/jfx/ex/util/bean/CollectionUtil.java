package xdean.jfx.ex.util.bean;

import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.collection.ListUtil;

import com.sun.javafx.binding.BidirectionalContentBinding;

public class CollectionUtil {

  public static <F, T> void bind(ObservableList<F> list1, ObservableList<T> list2, Function<F, T> forward,
      Function<T, F> backward) {
    ObservableList<T> newList1 = CollectionUtil.map(list1, forward, backward);
    CacheUtil.set(BeanUtil.class, list1, newList1);
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
}
