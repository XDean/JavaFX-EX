package xdean.jfxex.bean;

import static xdean.jex.util.function.Predicates.isEquals;
import static xdean.jex.util.lang.ExceptionUtil.throwIt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;

public class When<T, R> {

  private final ObservableValue<T> target;
  private final List<Observable> dependencies = new ArrayList<>();
  private final Map<Predicate<T>, Supplier<? extends R>> conditionResultMap = new LinkedHashMap<>();

  public When(ObservableValue<T> target) {
    this.target = target;
    addDependency(target);
  }

  public Then when(Predicate<T> condition) {
    return new Then(condition);
  }

  public Then when(T value) {
    return when(isEquals(value));
  }

  public Then when(ObservableValue<T> ob) {
    addDependency(ob);
    return when(e -> Objects.equals(e, ob.getValue()));
  }

  public Then when(ObservableBooleanValue condition) {
    addDependency(condition);
    return when(e -> condition.get());
  }

  public When<T, R> addDependency(Observable... dependencies) {
    this.dependencies.addAll(Arrays.asList(dependencies));
    return this;
  }

  public ObjectBinding<R> orElse(Supplier<R> defaultValue) {
    conditionResultMap.put(t -> true, defaultValue);
    return new ResultBinding<>(this);
  }

  public ObjectBinding<R> orElse(R defaultValue) {
    return orElse(() -> defaultValue);
  }

  public ObjectBinding<R> orElse(ObservableValue<R> defaultValue) {
    addDependency(defaultValue);
    return orElse(() -> defaultValue.getValue());
  }

  public ObjectBinding<R> orElse() {
    return orElse(() -> throwIt(new IllegalStateException("Not covered situation.")));
  }

  public class Then {
    private final Predicate<T> condition;

    public Then(Predicate<T> condition) {
      this.condition = condition;
    }

    public When<T, R> then(Supplier<? extends R> value) {
      conditionResultMap.put(condition, value);
      return When.this;
    }

    public When<T, R> then(R value) {
      return then(() -> value);
    }

    public When<T, R> then(ObservableValue<? extends R> value) {
      addDependency(value);
      return then(() -> value.getValue());
    }
  }

  private static class ResultBinding<T, R> extends ObjectBinding<R> {
    private final ObservableValue<T> target;
    private final Map<Predicate<T>, Supplier<? extends R>> conditionResultMap;

    public ResultBinding(When<T, R> when) {
      this.target = when.target;
      this.conditionResultMap = new LinkedHashMap<>(when.conditionResultMap);
      bind(when.dependencies.stream().toArray(Observable[]::new));
    }

    @Override
    protected R computeValue() {
      T value = target.getValue();
      return conditionResultMap.entrySet().stream()
          .filter(e -> e.getKey().test(value))
          .findFirst()
          .map(e -> e.getValue().get())
          .orElseThrow(() -> new IllegalStateException("Must have default value."));
    }
  }
}
