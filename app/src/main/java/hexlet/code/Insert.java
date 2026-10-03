package hexlet.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Неизменяемая вставка. Каждый метод, продолжающий запрос, возвращает новую вставку.
 *
 * <p>Колонки печатаются в порядке вызовов, значения уходят параметрами.
 *
 * <p>Вставка умеет разрешать конфликт по уникальному ключу, то есть upsert: {@link #onConflict}
 * называет колонки ключа, а {@link #doUpdate} — колонки, которым достаются значения из вставляемой
 * строки.
 */
public final class Insert {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final Map<String, Object> values;
  private final List<String> conflictColumns;
  private final List<String> updateColumns;

  Insert(Dialect dialect, Schema schema, String table) {
    this(dialect, schema, table, Map.of(), List.of(), List.of());
  }

  private Insert(
      Dialect dialect,
      Schema schema,
      String table,
      Map<String, Object> values,
      List<String> conflictColumns,
      List<String> updateColumns) {
    this.dialect = dialect;
    this.schema = schema;
    this.table = table;
    // LinkedHashMap сохраняет порядок вызовов, а Map.copyOf не принимает null значений.
    this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    this.conflictColumns = List.copyOf(conflictColumns);
    this.updateColumns = List.copyOf(updateColumns);
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
    return new Insert(dialect, schema, table, added, conflictColumns, updateColumns);
  }

  /**
   * Называет колонки уникального ключа, по которому база ищет конфликт.
   *
   * <p>Работает в паре с {@link #doUpdate}: конфликт без указания, что обновлять, бессмыслен.
   */
  public Insert onConflict(String... columns) {
    return new Insert(
        dialect, schema, table, values, appended(conflictColumns, columns), updateColumns);
  }

  /**
   * Называет колонки, которым достаются значения из вставляемой строки.
   *
   * <p>Работает в паре с {@link #onConflict}: обновлять нечего, если не сказано, при каком
   * конфликте.
   */
  public Insert doUpdate(String... columns) {
    return new Insert(
        dialect, schema, table, values, conflictColumns, appended(updateColumns, columns));
  }

  /** Печатает вставку. */
  public CompiledQuery toSql() {
    if (values.isEmpty()) {
      throw new QueryException("В INSERT нечего записывать, позовите value()");
    }
    if (conflictColumns.isEmpty() != updateColumns.isEmpty()) {
      throw new QueryException(
          "onConflict() и doUpdate() ходят парой: одно называет ключ, другое — что обновить");
    }
    // MySQL колонки конфликта не печатает, поэтому проверяем их сами: опечатка должна ловиться
    // одинаково во всех диалектах.
    conflictColumns.forEach(column -> schema.requireColumn(table, column));
    updateColumns.forEach(column -> schema.requireColumn(table, column));

    var writer = new SqlWriter(dialect, schema, table);
    dialect.formatInsert(writer, table, values);
    if (!conflictColumns.isEmpty()) {
      dialect.formatUpsert(writer, conflictColumns, updateColumns);
    }
    return writer.compiled();
  }

  /** Дописывает колонки к списку, сохраняя порядок вызовов. */
  private static List<String> appended(List<String> list, String[] columns) {
    Objects.requireNonNull(columns, "Колонки не должны быть null");
    var result = new ArrayList<String>(list);
    Collections.addAll(result, columns);
    return List.copyOf(result);
  }
}
