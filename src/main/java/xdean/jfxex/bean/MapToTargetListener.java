package xdean.jfxex.bean;

import java.lang.ref.WeakReference;
import java.util.function.Function;

import com.google.common.collect.Lists;

import javafx.beans.WeakListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import xdean.codecov.CodecovIgnore;

@CodecovIgnore
class MapToTargetListener<F, T> implements ListChangeListener<F>, WeakListener {
  WeakReference<ObservableList<F>> sourceListRef;
  WeakReference<ObservableList<T>> targetListRef;
  Function<F, T> function;
  BooleanProperty updating = new SimpleBooleanProperty(false);

  public MapToTargetListener(ObservableList<F> sourceList, ObservableList<T> targetList, Function<F, T> function) {
    this.targetListRef = new WeakReference<>(targetList);
    this.sourceListRef = new WeakReference<>(sourceList);
    this.function = function;
  }

  @Override
  public void onChanged(Change<? extends F> change) {
    if (updating.get()) {
      return;
    }
    ObservableList<F> sourceList = this.sourceListRef.get();
    ObservableList<T> targetList = this.targetListRef.get();
    if (sourceList == null || targetList == null) {
      if (sourceList != null) {
        sourceList.removeListener(this);
      }
      return;
    }
    if (updating.get()) {
      return;
    }
    updating.set(true);
    while (change.next()) {
      if (change.wasPermutated()) {
        targetList.remove(change.getFrom(), change.getTo());
        targetList.addAll(change.getFrom(),
            Lists.transform(change.getList().subList(change.getFrom(), change.getTo()), function::apply));
      } else if (change.wasUpdated()) {
        for (int i = change.getFrom(); i < change.getTo(); ++i) {
          targetList.set(i, function.apply(change.getList().get(i)));
        }
      } else {
        if (change.wasRemoved()) {
          targetList.remove(change.getFrom(), change.getFrom() + change.getRemovedSize());
        }
        if (change.wasAdded()) {
          targetList.addAll(change.getFrom(), Lists.transform(change.getAddedSubList(), function::apply));
        }
      }
    }
    updating.set(false);
  }

  @Override
  public boolean wasGarbageCollected() {
    return (sourceListRef.get() == null) || (targetListRef.get() == null);
  }
}