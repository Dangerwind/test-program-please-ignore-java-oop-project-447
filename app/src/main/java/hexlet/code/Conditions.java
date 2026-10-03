package hexlet.code;

/**
 * Готовые условия запроса.
 *
 * <pre>{@code
 * import static hexlet.code.Conditions.eq;
 *
 * var adults = and(eq("active", true), gt("age", 18));
 * }</pre>
 */
public final class Conditions {

  private Conditions() {}

  public static Condition eq(String column, Object value) {
    throw new UnsupportedOperationException();
  }

  public static Condition ne(String column, Object value) {
    throw new UnsupportedOperationException();
  }

  public static Condition gt(String column, Object value) {
    throw new UnsupportedOperationException();
  }

  public static Condition lt(String column, Object value) {
    throw new UnsupportedOperationException();
  }

  public static Condition in(String column, Object... values) {
    throw new UnsupportedOperationException();
  }

  public static Condition isNull(String column) {
    throw new UnsupportedOperationException();
  }

  public static Condition and(Condition... conditions) {
    throw new UnsupportedOperationException();
  }

  public static Condition or(Condition... conditions) {
    throw new UnsupportedOperationException();
  }

  public static Condition not(Condition condition) {
    throw new UnsupportedOperationException();
  }
}
