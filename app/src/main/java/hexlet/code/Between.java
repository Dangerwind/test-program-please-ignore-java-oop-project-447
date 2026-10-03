package hexlet.code;

/**
 * Значение лежит в диапазоне включительно: {@code "age" BETWEEN $1 AND $2}.
 *
 * <p>Пример условия, написанного снаружи библиотеки. Ему достаточно трёх публичных методов {@link
 * SqlWriter}: {@link SqlWriter#column}, {@link SqlWriter#param} и {@link SqlWriter#sql}. Поэтому
 * библиотеку не нужно править, чтобы добавить оператор, которого в ней нет: колонка проверяется по
 * схеме, а значения уходят параметрами в любом диалекте.
 *
 * <p>Новое условие вкладывается в {@code and()} и {@code or()} наравне со встроенными.
 *
 * <pre>{@code
 * import static hexlet.code.Conditions.and;
 *
 * var adults = and(new Between("age", 18, 65), eq("active", true));
 * }</pre>
 */
public final class Between implements Condition {

  private final String column;
  private final Object from;
  private final Object to;

  /**
   * Создаёт условие диапазона. Границы входят в диапазон.
   *
   * @param column колонка со значением
   * @param from нижняя граница
   * @param to верхняя граница
   * @throws QueryException если колонка или любая граница равна {@code null}
   */
  public Between(String column, Object from, Object to) {
    this.column = requireValue(column, "Колонка");
    this.from = requireValue(from, "Нижняя граница");
    this.to = requireValue(to, "Верхняя граница");
  }

  @Override
  public void render(SqlWriter writer) {
    writer.column(column).sql(" BETWEEN ").param(from).sql(" AND ").param(to);
  }

  /**
   * Пустое значение библиотека не позволяет: для него есть условие {@code IS NULL}. Имя колонки —
   * тоже опечатка, поэтому и то и другое сообщаем одинаково.
   *
   * @throws QueryException если значение равно {@code null}
   */
  private static String requireValue(String value, String name) {
    if (value == null) {
      throw new QueryException(name + " условия не может быть null");
    }
    return value;
  }

  /**
   * Границы диапазона — обычные значения, они уходят параметрами.
   *
   * @throws QueryException если граница равна {@code null}
   */
  private static Object requireValue(Object value, String name) {
    if (value == null) {
      throw new QueryException(name + " не может быть null, для этого есть условие IS NULL");
    }
    return value;
  }
}
