package xdean.jfx.ex.bean.property;

import static xdean.jfx.ex.bean.ListenerUtil.weak;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.reactivex.Scheduler;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import xdean.jex.extra.LazyValue;
import xdean.jfx.ex.bean.BeanUtil;
import xdean.jfx.ex.bean.ListenerUtil;
import xdean.jfx.ex.bean.ListenerUtil.ChangeListenerEX;

public class ObjectPropertyEX<T> extends SimpleObjectProperty<T> {

  /**
   * Transform the new value to a newer value. If return the old value, this set
   * will be rejected. Transformers will handle the value as insert order. <br>
   * NOTE that transformer must never transform a non-null value to null, or NPE
   * occurs.
   */
  private final List<BinaryOperator<T>> transformers = new LinkedList<>();
  /**
   * Verify the new value. If return false, this set will be rejected. Verifiers
   * will handle the value as insert order.
   */
  private final List<BiPredicate<T, T>> verifiers = new LinkedList<>();
  private final LazyValue<Map<ObservableValue<? extends T>, ChangeListenerEX<? extends T>>> softBindings = LazyValue.create(
      () -> new IdentityHashMap<>(1));

  public ObjectPropertyEX() {
    this(null);
  }

  public ObjectPropertyEX(T initialValue) {
    this(null, "", initialValue);
  }

  public ObjectPropertyEX(Object bean, String name) {
    this(bean, name, null);
  }

  public ObjectPropertyEX(Object bean, String name, T initialValue) {
    super(bean, name, initialValue);
  }

  @Override
  public void set(T newValue) {
    checkBound();
    T oldValue = get();
    if (oldValue != newValue) {
      if ((newValue = transform(oldValue, newValue)) == oldValue) {
        return;
      } else if (!verify(oldValue, newValue)) {
        return;
      } else {
        super.set(newValue);
      }
    }
  }

  private T transform(T oldValue, T newValue) {
    boolean canNull = newValue == null;
    for (BinaryOperator<T> transformer : transformers) {
      newValue = transformer.apply(oldValue, newValue);
      if (!canNull && (newValue == null)) {
        throw new NullPointerException("A transformer never let a not-null value to null");
      } else if (newValue == oldValue) {
        return oldValue;
      }
    }
    return newValue;
  }

  private boolean verify(T oldValue, T newValue) {
    return verifiers.stream().allMatch(v -> v.test(oldValue, newValue));
  }

  protected void checkBound() {
    if (isBound()) {
      throw new java.lang.RuntimeException((getBean() != null && getName() != null ?
          getBean().getClass().getSimpleName() + "." + getName() + " : " : "") + "A bound value cannot be set.");
    }
  }

  @Override
  public final void setValue(T t) {
    super.setValue(t);
  }

  public ObjectPropertyEX<T> on(T value, Runnable r) {
    addListener(ListenerUtil.on(value, r));
    return this;
  }

  public ObjectPropertyEX<T> addVerifier(BiPredicate<T, T> verifier) {
    this.verifiers.add(verifier);
    return this;
  }

  public ObjectPropertyEX<T> addVerifier(BiPredicate<T, T> verifier, BiConsumer<T, T> onVerifyFail) {
    return addVerifier((o, n) -> {
      if (verifier.test(o, n)) {
        return true;
      } else {
        onVerifyFail.accept(o, n);
        return false;
      }
    });
  }

  public ObjectPropertyEX<T> addTransformer(BinaryOperator<T> transformer) {
    this.transformers.add(transformer);
    return this;
  }

  public ObjectPropertyEX<T> defaultForNull(T defaultValue) {
    return defaultForNull(() -> defaultValue);
  }

  /**
   * Set a default value when set to null value. If the property value is null
   * now, set to the defautlValue immediately.
   * 
   * @param defaultValueFactory
   * @return
   */
  public ObjectPropertyEX<T> defaultForNull(Supplier<T> defaultValueFactory) {
    if (get() == null) {
      set(defaultValueFactory.get());
    }
    return addTransformer((o, n) -> n == null ? defaultValueFactory.get() : n);
  }

  /**
   * Reject null value.
   * 
   * @return
   */
  public ObjectPropertyEX<T> nonNull() {
    return addVerifier((o, n) -> n != null);
  }

  public ObjectPropertyEX<T> bindBy(Property<T> p) {
    p.bind(this);
    return this;
  }

  public ObjectPropertyEX<T> bindBidirectionalBy(Property<T> p) {
    p.bindBidirectional(this);
    return this;
  }

  public void setWhile(T value, long mills) {
    BeanUtil.setWhile(this, value, mills);
  }

  public void setWhile(T value, long mills, Scheduler scheudler) {
    BeanUtil.setWhile(this, value, mills, scheudler);
  }

  public BooleanBinding is(Predicate<T> p) {
    Objects.requireNonNull(p, "Operater can't be null");
    return Bindings.createBooleanBinding(() -> p.test(get()), this);
  }

  public <K extends T> void softBind(ObservableValue<K> newObservable) {
    Objects.requireNonNull(newObservable, "Cannot bind to null");
    K current = newObservable.getValue();
    if (!Objects.equals(get(), current)) {
      set(current);
    }
    ChangeListenerEX<K> listener = weak(this, (t, o, n) -> t.set(n));
    newObservable.addListener(listener);
    softBindings.get().put(newObservable, listener);
  }

  @SuppressWarnings("unchecked")
  public <K extends T> void softUnbind(ObservableValue<K> newObservable) {
    ChangeListenerEX<K> listener = (ChangeListenerEX<K>) softBindings.get().remove(newObservable);
    if (listener != null) {
      newObservable.removeListener(listener);
    }
  }

  public Optional<T> getSafe() {
    return Optional.ofNullable(get());
  }

  public T orElse(T t) {
    T value = get();
    return value == null ? t : value;
  }
  
  public void addListenerAndInvoke(ChangeListener<? super T> l) {
    super.addListener(l);
    T value = get();
    l.changed(this, value, value);
  }
}