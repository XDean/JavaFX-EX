package xdean.jfxex.util;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.util.StringConverter;
import xdean.jex.extra.tryto.Try;

/**
 * Utility class of {@link StringConverter}
 * 
 * @author Dean Xu (XDean@github.com)
 *
 */
public class StringConverters {
  /**
   * Create {@link StringConverter} from format and parse {@link Function}
   * 
   * @param format from value to string
   * @param parse from string to value
   * @return the {@link StringConverter}
   */
  public static <T> StringConverter<T> create(Function<T, String> format, Function<String, T> parse) {
    return new StringConverter<T>() {
      @Override
      public String toString(T t) {
        return format.apply(t);
      }

      @Override
      public T fromString(String s) {
        return parse.apply(s);
      }
    };
  }

  /**
   * Create {@link StringConverter} from parse {@link Function}
   * 
   * @param parse from string to value
   * @return the {@link StringConverter}
   */
  public static <T> StringConverter<T> create(Function<String, T> parse) {
    return create(Object::toString, parse);
  }

  /**
   * @see #forInteger(Integer)
   */
  public static StringConverter<Integer> forInteger() {
    return forInteger(0);
  }

  /**
   * Integer String Converter with default value.
   */
  public static StringConverter<Integer> forInteger(Integer defaultValue) {
    return forInteger(() -> defaultValue);
  }

  /**
   * Integer String Converter with default value provider.
   */
  public static StringConverter<Integer> forInteger(Supplier<Integer> defaultValue) {
    return create(
        d -> d == null ? "" : Integer.toString(d),
        s -> Try.to(() -> Integer.valueOf(s)).getOrElse(defaultValue));
  }

  /**
   * @see #forDouble(Double)
   */
  public static StringConverter<Double> forDouble() {
    return forDouble(0d);
  }

  /**
   * Double String Converter with default value.
   */
  public static StringConverter<Double> forDouble(Double defaultValue) {
    return forDouble(() -> defaultValue);
  }

  /**
   * Double String Converter with default value provider.
   */
  public static StringConverter<Double> forDouble(Supplier<Double> defaultValue) {
    return create(
        d -> d == null ? "" : Double.toString(d),
        s -> Try.to(() -> Double.valueOf(s)).getOrElse(defaultValue));
  }

  /**
   * Create {@link Optional} String Converter from format and parse functions. When value is empty,
   * string will be empty.
   */
  public static <T> StringConverter<Optional<T>> optional(Function<T, String> format, Function<String, T> parse) {
    return create(
        t -> t.map(format).orElse(""),
        s -> Optional.ofNullable(parse.apply(s)));
  }

  /**
   * {@link OptionalInt} String Converter
   */
  public static StringConverter<OptionalInt> optionalInt() {
    return create(
        t -> t.isPresent() ? Integer.toString(t.getAsInt()) : "",
        s -> Try.to(() -> OptionalInt.of(Integer.parseInt(s))).getOrElse(OptionalInt.empty()));
  }

  /**
   * {@link OptionalDouble} String Converter
   */
  public static StringConverter<OptionalDouble> optionalDouble() {
    return create(
        t -> t.isPresent() ? Double.toString(t.getAsDouble()) : "",
        s -> Try.to(() -> OptionalDouble.of(Double.parseDouble(s))).getOrElse(OptionalDouble.empty()));
  }
}
