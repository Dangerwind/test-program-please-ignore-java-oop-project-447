package hexlet.code;

import java.util.List;
import java.util.Objects;

/**
 * Готовые условия запроса.
 *
 * <pre>{@code
 * import static hexlet.code.Conditions.eq;
 *
 * var adults = and(eq("active", true), gt("age", 18));
 * }</pre>
 *
 * <p>Сравнение с пустым значением библиотека не позволяет: для него есть {@link #isNull}. Вызов с
 * {@code null} бросает {@link QueryException}.
 */
public final class Conditions {

  private Conditions() {}

  /** Равенство: {@code "id" = $1}. */
  public static Condition eq(String column, Object value) {
    return new ComparisonCondition(column, "=", requireValue(value));
  }

  /** Неравенство: {@code "name" <> $1}. */
  public static Condition ne(String column, Object value) {
    return new ComparisonCondition(column, "<>", requireValue(value));
  }

  /** Больше: {@code "age" > $1}. */
  public static Condition gt(String column, Object value) {
    return new ComparisonCondition(column, ">", requireValue(value));
  }

  /** Меньше: {@code "age" < $1}. */
  public static Condition lt(String column, Object value) {
    return new ComparisonCondition(column, "<", requireValue(value));
  }

  /** Вхождение в список: {@code "id" IN ($1, $2, $3)}. */
  public static Condition in(String column, Object... values) {
    if (values == null) {
      throw new QueryException("Список значений для IN не может быть null");
    }
    if (values.length == 0) {
      throw new QueryException("Список значений для IN не может быть пустым");
    }
    for (var value : values) {
      requireValue(value);
    }
    return new InCondition(column, List.of(values));
  }

  /** Пустое значение: {@code "email" IS NULL}. */
  public static Condition isNull(String column) {
    return new IsNullCondition(column);
  }

  /** Соединяет условия через AND. Вложенный OR берётся в скобки. */
  public static Condition and(Condition... conditions) {
    return new AndCondition(requireNotEmpty(conditions, "AND"));
  }

  /** Соединяет условия через OR. Вложенный AND скобками не берётся. */
  public static Condition or(Condition... conditions) {
    return new OrCondition(requireNotEmpty(conditions, "OR"));
  }

  /** Отрицает условие, всегда беря его в скобки. */
  public static Condition not(Condition condition) {
    return new NotCondition(Objects.requireNonNull(condition));
  }

  private static Object requireValue(Object value) {
    if (value == null) {
      throw new QueryException("Значение условия не может быть null, для этого есть isNull()");
    }
    return value;
  }

  private static List<Condition> requireNotEmpty(Condition[] conditions, String operator) {
    if (conditions.length == 0) {
      throw new QueryException(operator + " не может быть без условий");
    }
    return List.of(conditions);
  }
}
