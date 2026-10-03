package hexlet.code;

import java.util.List;

/**
 * Неизменяемая выборка. Каждый метод, продолжающий запрос, возвращает новую выборку.
 *
 * <p>Запрос хранит, что выбрать и откуда. Текст собирает диалект, но только после проверки имён по
 * схеме.
 */
public final class Select {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final List<String> columns;

  Select(Dialect dialect, Schema schema, String table, List<String> columns) {
    this.dialect = dialect;
    this.schema = schema;
    this.table = table;
    this.columns = List.copyOf(columns);
  }

  public Select where(Condition condition) {
    throw new UnsupportedOperationException();
  }

  public Select orderBy(String column) {
    throw new UnsupportedOperationException();
  }

  public Select limit(int count) {
    throw new UnsupportedOperationException();
  }

  public Select offset(int count) {
    throw new UnsupportedOperationException();
  }

  /** Проверяет имена по схеме и печатает запрос диалектом. */
  public CompiledQuery toSql() {
    schema.requireTable(table);
    for (var column : columns) {
      schema.requireColumn(table, column);
    }
    return dialect.formatSelect(table, columns);
  }
}
