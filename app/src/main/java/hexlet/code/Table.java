package hexlet.code;

import java.util.List;

/**
 * Таблица базы. Открывается через {@link Sql#table(String)} и служит точкой старта для всех видов
 * запросов.
 */
public final class Table {

  private final Dialect dialect;
  private final Schema schema;
  private final String name;

  Table(Dialect dialect, Schema schema, String name) {
    this.dialect = dialect;
    this.schema = schema;
    this.name = name;
  }

  /** Выбирает колонки. Без аргументов выбирает все: {@code SELECT *}. */
  public Select select(String... columns) {
    return new Select(dialect, schema, name, List.of(columns));
  }

  public Insert insert() {
    return new Insert(dialect, schema, name);
  }

  public Update update() {
    return new Update(dialect, schema, name);
  }

  public Delete delete() {
    return new Delete(dialect, schema, name);
  }
}
