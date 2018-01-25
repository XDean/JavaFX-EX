package xdean.jfx.ex.bean.property;

import java.util.LinkedHashMap;
import java.util.Optional;

import javax.annotation.CheckForNull;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class MapPropertyEX<K, V> extends SimpleMapProperty<K, V> {
  public enum Bijection {
    /**
     * Not bijection
     */
    NOT,
    /**
     * Remove old entry first when value conflict
     */
    REPLACE,
    /**
     * Reject the entry when value conflict
     */
    REJECT,
    /**
     * Throw Exception when value conflict
     */
    ERROR
  }

  @SuppressWarnings("unchecked")
  private final InvalidationListener keyInListener = ob -> this.keySet().removeIf(k -> !((ObservableList<K>) ob).contains(k));
  @SuppressWarnings("unchecked")
  private final InvalidationListener valueInListener = ob -> this.values().removeIf(v -> !((ObservableList<V>) ob).contains(v));
  private @CheckForNull ObservableList<K> keyIn;
  private @CheckForNull ObservableList<V> valueIn;
  private Bijection bijection = Bijection.NOT;

  public MapPropertyEX() {
    super(defatulMap());
  }

  public MapPropertyEX(Object bean, String name, ObservableMap<K, V> value) {
    super(bean, name, value);
  }

  public MapPropertyEX(Object bean, String name) {
    super(bean, name, defatulMap());
  }

  public MapPropertyEX(ObservableMap<K, V> value) {
    super(value);
  }

  @Override
  public V put(K k, V v) {
    if (get(k) == v) {
      return v;
    } else if (keyIn != null && !keyIn.contains(k)) {
      return null;
    } else if (valueIn != null && !valueIn.contains(v)) {
      return null;
    } else if (bijection != Bijection.NOT && this.values().contains(v)) {
      if (bijection == Bijection.REJECT) {
        return null;
      } else if (bijection == Bijection.ERROR) {
        throw new IllegalArgumentException("Bijection Map already has the value " + v);
      } else {
        this.values().remove(v);
      }
    }
    return super.put(k, v);
  }
  
  public Optional<V> getSafe(K k){
    return Optional.ofNullable(get(k));
  }

  public ObjectPropertyEX<V> propertyAt(K key) {
    ObjectPropertyEX<V> p = new ObjectPropertyEX<>(this, key.toString());
    p.softBind(valueAt(key));
    p.addListener((ob, o, n) -> this.put(key, n));
    return p;
  }

  public MapPropertyEX<K, V> keyIn(@CheckForNull ObservableList<K> keys) {
    if (keyIn != null) {
      keyIn.removeListener(keyInListener);
      keyIn = null;
    }
    if (keys != null) {
      keyIn = keys;
      keys.addListener(keyInListener);
    }
    return this;
  }

  public MapPropertyEX<K, V> valueIn(@CheckForNull ObservableList<V> values) {
    if (valueIn != null) {
      valueIn.removeListener(valueInListener);
      valueIn = null;
    }
    if (values != null) {
      valueIn = values;
      values.addListener(valueInListener);
    }
    return this;
  }

  /**
   * Set the map bijection.
   */
  public MapPropertyEX<K, V> bijection(Bijection b) {
    this.bijection = b;
    return this;
  }

  private static <K, V> ObservableMap<K, V> defatulMap() {
    return FXCollections.observableMap(new LinkedHashMap<>());
  }
}
