package hexlet.code;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Неизменяемое обновление. Каждый метод, продолжающий запрос, возвращает новое обновление.
 *
 * <p>Присваивания печатаются в порядке вызовов и раньше условия, поэтому нумерация их параметров
 * идёт первой.
 */
public final class Update {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final Map<String, Object> values;
  private final Where where;

  Update(Dialect dialect, Schema schema, String table) {
    this(dialect, schema, table, Map.of(), Where.empty());
  }

  private Update(
      Dialect dialect, Schema schema, String table, Map<String, Object> values, Where where) {
    this.dialect = dialect;
    this.schema = schema;
    this.table = table;
    this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    this.where = where;
  }

  /**
   * Присваивает значение колонке. Повторный вызов для той же колонки заменяет значение.
   *
   * <p>null уходит параметром: записать NULL — обычная операция.
   */
  public Update set(String column, Object value) {
    var added = new LinkedHashMap<>(values);
    added.put(Objects.requireNonNull(column), value);
    return new Update(dialect, schema, table, added, where);
  }

  /** Добавляет условие. Несколько вызовов соединяются через AND. */
  public Update where(Condition condition) {
    return new Update(dialect, schema, table, values, where.and(condition));
  }

  /** Явно разрешает обновить всю таблицу. */
  public Update all() {
    return new Update(dialect, schema, table, values, where.allRows());
  }

  /** Печатает обновление. */
  public CompiledQuery toSql() {
    if (values.isEmpty()) {
      throw new QueryException("В UPDATE нечего записывать, позовите set()");
    }
    if (where.isEmpty()) {
      throw new QueryWithoutConditionException(
          "UPDATE без условия меняет всю таблицу, позовите all(), если это нужно");
    }
    var writer = new SqlWriter(dialect, schema, table);
    dialect.formatUpdate(writer, table, values);
    dialect.formatWhere(writer, where.conditions());
    return writer.compiled();
  }
}
