package xdean.jfxex.bean;

import static xdean.jex.util.cache.CacheUtil.set;

import java.util.function.Function;

import com.google.common.collect.Lists;
import com.sun.javafx.binding.BidirectionalContentBinding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CollectionUtil {

  public static <F, T> void bind(ObservableList<F> list1, ObservableList<T> list2, Function<F, T> forward,
      Function<T, F> backward) {
    ObservableList<T> newList1 = CollectionUtil.map(list1, forward, backward);
    set(BeanUtil.class, list1, newList1);
    BidirectionalContentBinding.bind(newList1, list2);
  }

  public static <F, T> ObservableList<T> map(ObservableList<F> list, Function<F, T> forward, Function<T, F> backward) {
    ObservableList<T> newList = FXCollections.observableArrayList();
    newList.setAll(Lists.transform(list, forward::apply));

    MapToTargetListener<F, T> forwardListener = new MapToTargetListener<>(list, newList, forward);
    MapToTargetListener<T, F> backwardListener = new MapToTargetListener<>(newList, list, backward);
    forwardListener.updating.bindBidirectional(backwardListener.updating);

    list.addListener(forwardListener);
    newList.addListener(backwardListener);
    return newList;
  }
}
