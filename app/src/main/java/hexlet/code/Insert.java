package hexlet.code;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Неизменяемая вставка. Каждый метод, продолжающий запрос, возвращает новую вставку.
 *
 * <p>Колонки печатаются в порядке вызовов, значения уходят параметрами.
 */
public final class Insert {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final Map<String, Object> values;

  Insert(Dialect dialect, Schema schema, String table) {
    this(dialect, schema, table, Map.of());
  }

  private Insert(Dialect dialect, Schema schema, String table, Map<String, Object> values) {
    this.dialect = dialect;
    this.schema = schema;
    this.table = table;
    // LinkedHashMap сохраняет порядок вызовов, а Map.copyOf не принимает null значений.
    this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
  }

  /**
   * Записывает значение в колонку. Повторный вызов для той же колонки заменяет значение.
   *
   * <p>Здесь, в отличие от условий, null уходит параметром: записать NULL в колонку означает
   * «значение неизвестно», и это обычная операция.
   */
  public Insert value(String column, Object value) {
    var added = new LinkedHashMap<>(values);
    added.put(Objects.requireNonNull(column), value);
    return new Insert(dialect, schema, table, added);
  }

  public Insert onConflict(String... columns) {
    throw new UnsupportedOperationException();
  }

  public Insert doUpdate(String... columns) {
    throw new UnsupportedOperationException();
  }

  /** Печатает вставку. */
  public CompiledQuery toSql() {
    if (values.isEmpty()) {
      throw new QueryException("В INSERT нечего записывать, позовите value()");
    }
    var writer = new SqlWriter(dialect, schema, table);
    dialect.formatInsert(writer, table, values);
    return writer.compiled();
  }
}
