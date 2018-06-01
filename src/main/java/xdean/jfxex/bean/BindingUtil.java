package xdean.jfxex.bean;

import static xdean.jex.util.cache.CacheUtil.set;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.MapBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import xdean.jex.log.Log;
import xdean.jex.log.LogFactory;
import xdean.jex.util.cache.CacheUtil;

public class BindingUtil {
  private static final Log LOGGER = LogFactory.from(BindingUtil.class);
  private static final InvalidationListener AUTO_VALID = ob -> ((Binding<?>) ob).getValue();

  /**
   * Let the {@link Binding} valid automatically when it becomes invalid.
   * <p>
   * 
   * @apiNote Sometimes we use Binding as dependency like
   *          {@code  Binding newBinding = Bindings.createBooleanBinding(()->someBoolean(), dependencyBinding())}. When
   *          the dependency Binding invalidated first time, the new Binding will be invalidated also. But the
   *          dependency binding will always be invalid because no one valid(get) it, so this binding will lost latter
   *          changes. The correct way is
   *          {@code  Binding newBinding = Bindings.createBooleanBinding(()->someBoolean(), autoValid(dependencyBinding()))}.
   * 
   */
  public static <T, B extends Binding<T>> B autoValid(B b) {
    b.removeListener(AUTO_VALID);
    b.addListener(AUTO_VALID);
    return b;
  }

  /**
   * Create a {@link ListBinding} from an {@link ObservableList}
   */
  public static <T> ListBinding<T> createListBinding(ObservableList<T> list) {
    return createListBinding(() -> list, list);
  }

  /**
   * Helper function to create a custom {@link ListBinding}.
   *
   * @param func The function that calculates the value of this binding.
   * @param dependencies The dependencies of this binding
   * @return The generated binding
   */
  public static <T> ListBinding<T> createListBinding(Callable<List<T>> func, Observable... dependencies) {
    return new ListBinding<T>() {
      {
        bind(dependencies);
      }

      @Override
      protected ObservableList<T> computeValue() {
        try {
          List<T> list = func.call();
          return list instanceof ObservableList ? (ObservableList<T>) list : FXCollections.observableList(list);
        } catch (Exception e) {
          LOGGER.warn("Exception while evaluating binding", e);
          return null;
        }
      }

      @Override
      public void dispose() {
        super.unbind(dependencies);
      }

      @Override
      public ObservableList<?> getDependencies() {
        return ((dependencies == null) || (dependencies.length == 0)) ? FXCollections.emptyObservableList()
            : (dependencies.length == 1) ? FXCollections.singletonObservableList(dependencies[0])
                : FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(dependencies));
      }
    };
  }

  /**
   * Create a {@link MapBinding} from an {@link ObservableMap}
   */
  public static <K, V> MapBinding<K, V> createMapBinding(ObservableMap<K, V> map) {
    return createMapBinding(() -> map, map);
  }

  /**
   * Helper function to create a custom {@link MapBinding}.
   *
   * @param func The function that calculates the value of this binding.
   * @param dependencies The dependencies of this binding
   * @return The generated binding
   */
  public static <K, V> MapBinding<K, V> createMapBinding(Callable<Map<K, V>> func, Observable... dependencies) {
    return new MapBinding<K, V>() {
      {
        bind(dependencies);
      }

      @Override
      protected ObservableMap<K, V> computeValue() {
        try {
          Map<K, V> map = func.call();
          return map instanceof ObservableMap ? (ObservableMap<K, V>) map : FXCollections.observableMap(map);
        } catch (Exception e) {
          LOGGER.warn("Exception while evaluating binding", e);
          return null;
        }
      }

      @Override
      public void dispose() {
        super.unbind(dependencies);
      }

      @Override
      public ObservableList<?> getDependencies() {
        return ((dependencies == null) || (dependencies.length == 0)) ? FXCollections.emptyObservableList()
            : (dependencies.length == 1) ? FXCollections.singletonObservableList(dependencies[0])
                : FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(dependencies));
      }
    };
  }

  /**
   * Concat a series of {@link ObservableList} as a new {@link ListBinding} and add new dependencies. If the list is
   * {@link ObservableList}, it will be dependency by default. Or the list will be used as immutable list.
   */
  public static <T> ListBinding<T> concat(List<? extends ObservableList<T>> list, Observable... dependencies) {
    return new ListBinding<T>() {
      {
        if (list instanceof ObservableList) {
          bind(observeProperty((ObservableList<? extends ObservableList<T>>) list, o -> o));
        } else {
          list.forEach(this::bind);
        }
        bind(dependencies);
      }

      @Override
      @SuppressWarnings("unchecked")
      protected ObservableList<T> computeValue() {
        try {
          if (list.isEmpty()) {
            return FXCollections.emptyObservableList();
          } else {
            return FXCollections.concat((ObservableList<T>[]) list.stream().toArray(ObservableList[]::new));
          }
        } catch (Exception e) {
          LOGGER.warn("Exception while evaluating binding", e);
          return null;
        }
      }
    };
  }

  /**
   * @see FXCollections#observableList(List, javafx.util.Callback)
   */
  public static <T> ObservableList<T> observeProperty(ObservableList<T> list, Function<T, Observable> selector) {
    ObservableList<T> observe = FXCollections.observableArrayList(t -> new Observable[] { selector.apply(t) });
    set(observe, "originList", list);
    Bindings.bindContent(observe, list);
    return observe;
  }

  /**
   * @see FXCollections#observableList(List, javafx.util.Callback)
   */
  @SafeVarargs
  public static <T> ObservableList<T> observeProperties(ObservableList<T> list, Function<T, Observable>... selectors) {
    ObservableList<T> observe = FXCollections
        .observableArrayList(t -> Arrays.stream(selectors).map(f -> f.apply(t)).toArray(Observable[]::new));
    Bindings.bindContent(observe, list);
    return observe;
  }

  /**
   * Bind a nest property to an observable value. That means whatever the owner property is, its target property is
   * always bind to the binding value.
   * 
   * @param pf the owner property
   * @param func function from the owner to the target property
   * @param bind the value to bind
   */
  public static <F, T> void nestBind(ObservableValue<F> pf, Function<F, Property<T>> func, ObservableValue<T> bind) {
    F value = pf.getValue();
    if (value != null) {
      Property<T> current = func.apply(value);
      CacheUtil.set(BindingUtil.class, bind, current);
      current.bind(bind);
    }
    pf.addListener((ob, o, n) -> {
      CacheUtil.<Property<T>> get(BindingUtil.class, bind).ifPresent(p -> p.unbind());
      if (n != null) {
        Property<T> pt = func.apply(n);
        CacheUtil.set(BindingUtil.class, bind, pt);
        pt.bind(bind);
      }
    });
  }
}
