package xdean.jfx.ex.bean.property;

import static xdean.jfx.ex.bean.ListenerUtil.weak;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.CheckForNull;

import io.reactivex.Scheduler;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import xdean.jex.extra.LazyValue;
import xdean.jex.util.task.If;
import xdean.jfx.ex.bean.BeanConvertUtil;
import xdean.jfx.ex.bean.BeanUtil;
import xdean.jfx.ex.bean.ListenerUtil;
import xdean.jfx.ex.bean.ListenerUtil.ChangeListenerEX;
import xdean.jfx.ex.bean.annotation.CheckNull;
import xdean.jfx.ex.bean.annotation.NotRef;

/**
 * Extension of {@link SimpleObjectProperty}.
 * 
 * XXX Dean reduce the class size if possible, currently 368 bytes for new instance.
 * 
 * @author XDean
 *
 * @param <T> type of the wrapped {@code Object}
 */
public class ObjectPropertyEX<T> extends SimpleObjectProperty<T> {
  /**
   * Transform the new value to a newer value. If return the old value, this set will be rejected. Transformers will
   * handle the value as insert order. <br>
   * NOTE that transformer must never transform a non-null value to null, or NPE occurs.
   */
  private final List<BinaryOperator<T>> transformers = new LinkedList<>();
  /**
   * Verify the new value. If return false, this set will be rejected. Verifiers will handle the value as insert order.
   */
  private final List<BiPredicate<T, T>> verifiers = new LinkedList<>();
  /**
   * If not null, the property value is constrained in this list.
   */
  private @CheckForNull ObservableList<T> valueList;
  /**
   * Only work with valueList. If true, the first element will be set when the current value not in the valueList any
   * more, or will be set to null.
   */
  private boolean selectFirst;
  private final InvalidationListener inListListener = ob -> If.that(valueList.contains(get()))
      .ordo(() -> set(selectFirst ? valueList.stream().findFirst().orElse(null) : null));
  /**
   * Hold the references of soft bindings.
   */
  private final LazyValue<List<ObservableValue<? extends T>>> softBindings = LazyValue.create(() -> new LinkedList<>());
  private final ChangeListenerEX<T> bindListener = weak(this, (t, o, n) -> t.set(n));

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
    if (valueList != null && !valueList.contains(newValue)) {
      return false;
    }
    return verifiers.stream().allMatch(v -> v.test(oldValue, newValue));
  }

  protected void checkBound() {
    if (isBound()) {
      throw new java.lang.RuntimeException(
          (getBean() != null && getName() != null ? getBean().getClass().getSimpleName() + "." + getName() + " : " : "")
              + "A bound value cannot be set.");
    }
  }

  @Override
  public final void setValue(T t) {
    super.setValue(t);
  }

  /**
   * Run the action when this property's value change to the value.
   */
  public ObjectPropertyEX<T> on(T value, Runnable action) {
    addListener(ListenerUtil.on(value, action));
    return this;
  }

  /**
   * Add a verifier
   * 
   * @see #verifiers
   */
  public ObjectPropertyEX<T> addVerifier(BiPredicate<T, T> verifier) {
    this.verifiers.add(verifier);
    return this;
  }

  /**
   * Add a verifier and do the fail action when the verification failed.
   * 
   * @see #addVerifier(BiPredicate)
   */
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

  /**
   * Add transformer
   * 
   * @see #transformers
   */
  public ObjectPropertyEX<T> addTransformer(BinaryOperator<T> transformer) {
    this.transformers.add(transformer);
    return this;
  }

  /**
   * @see #defaultForNull(Supplier)
   */
  public ObjectPropertyEX<T> defaultForNull(T defaultValue) {
    return defaultForNull(() -> defaultValue);
  }

  /**
   * When set to null, use default value instead of null. If the property value is null now, set to the defautlValue
   * immediately.
   */
  public ObjectPropertyEX<T> defaultForNull(Supplier<T> defaultValueFactory) {
    if (get() == null) {
      set(defaultValueFactory.get());
    }
    return addTransformer((o, n) -> n == null ? defaultValueFactory.get() : n);
  }

  /**
   * Reject null value.
   */
  public ObjectPropertyEX<T> nonNull() {
    return addVerifier((o, n) -> n != null);
  }

  /**
   * Reversed operator of {@link #bind(ObservableValue)}
   */
  public ObjectPropertyEX<T> bindBy(Property<T> p) {
    p.bind(this);
    return this;
  }

  /**
   * Reversed operator of {@link #bindBidirectional(Property)}
   */
  public ObjectPropertyEX<T> bindBidirectionalBy(Property<T> p) {
    p.bindBidirectional(this);
    return this;
  }

  /**
   * Set the value only given mills
   * 
   * @see BeanUtil#setWhile(Property, Object, long)
   */
  public void setWhile(T value, long mills) {
    BeanUtil.setWhile(this, value, mills);
  }

  /**
   * Set the value only given mills and recovered in the scheduler.
   * 
   * @see BeanUtil#setWhile(Property, Object, long, Scheduler)
   */
  public void setWhile(T value, long mills, Scheduler scheudler) {
    BeanUtil.setWhile(this, value, mills, scheudler);
  }

  /**
   * Creates a new {@code BooleanBinding} that holds {@code true} if the test passed.
   */
  public BooleanBinding is(Predicate<T> p) {
    Objects.requireNonNull(p, "Operater can't be null");
    return Bindings.createBooleanBinding(() -> p.test(get()), this);
  }

  /**
   * Bind the property to the observable value without any limit. The reference of observable value will be holden like
   * strong bind(bind from {@link Property}). The property value will be set immediately in binding.
   */
  public <K extends T> void softBind(ObservableValue<K> newObservable) {
    weakBind(newObservable);
    set(newObservable.getValue());
    softBindings.get().add(newObservable);
  }

  /**
   * Remove the soft bind to the specific observable value.
   */
  public <K extends T> void softUnbind(ObservableValue<K> newObservable) {
    weakUnbind(newObservable);
    softBindings.get().remove(newObservable);
  }

  /**
   * Bind the property to the observable value without any limit. The reference of observable value will not be holden.
   * The property value will not be set in binding.
   */
  public <K extends T> void weakBind(@NotRef ObservableValue<K> newObservable) {
    Objects.requireNonNull(newObservable, "Cannot bind to null");
    newObservable.removeListener(bindListener);
    newObservable.addListener(bindListener);
  }

  /**
   * Remove the weak bind to the specific observable value.
   */
  public <K extends T> void weakUnbind(ObservableValue<K> newObservable) {
    newObservable.removeListener(bindListener);
  }

  /**
   * Get value as {@link Optional}.
   */
  public Optional<T> getSafe() {
    return Optional.ofNullable(get());
  }

  /**
   * Get value or default value if null.
   */
  public T orElse(T defaultValue) {
    T value = get();
    return value == null ? defaultValue : value;
  }

  /**
   * Add a {@link ChangeListener} and invoke it immediately with both current value.
   */
  public void addListenerAndInvoke(ChangeListener<? super T> l) {
    super.addListener(l);
    T value = get();
    l.changed(this, value, value);
  }

  /**
   * Convert to V type with forward and backward converter
   * 
   * @see BeanConvertUtil#convert(Property, Property, Function, Function)
   */
  public <V> ObjectPropertyEX<V> convert(Function<T, V> fromTo, Function<V, T> toFrom) {
    return BeanConvertUtil.convert(this, fromTo, toFrom);
  }

  /**
   * Convert to {@link StringPropertyEX} with a {@link StringConverter}
   */
  public StringPropertyEX convertToString(StringConverter<T> converter) {
    return convertToString(converter::toString, converter::fromString);
  }

  /**
   * @see #convert(Function, Function)
   */
  public StringPropertyEX convertToString(Function<T, String> fromTo, Function<String, T> toFrom) {
    return BeanConvertUtil.convert(this, new StringPropertyEX(this, "convertToString"), fromTo, toFrom);
  }

  /**
   * Restrict the property's value in the specific list. <br>
   * When new value comes, all transformer will performed normally, but verifier will not invoked if the new value not
   * in the value list.<br>
   * Note this operation will lead this property be nullable.
   * 
   * @param list the value list, null to release the restriction.
   * @param selectFirst if the list updated, select the first element or be null.
   * @return self
   */
  public ObjectPropertyEX<@CheckNull T> in(ObservableList<T> list, boolean selectFirst) {
    if (valueList != null) {
      valueList.removeListener(inListListener);
      valueList = null;
    }
    this.selectFirst = selectFirst;
    if (list != null) {
      valueList = list;
      list.addListener(inListListener);
    }
    return this;
  }
}