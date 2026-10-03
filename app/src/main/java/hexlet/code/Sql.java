package hexlet.code;

import java.util.Objects;

/** Точка входа в библиотеку: связывает диалект со схемой и открывает таблицы. */
public final class Sql {

  private final Dialect dialect;
  private final Schema schema;

  private Sql(Dialect dialect, Schema schema) {
    this.dialect = dialect;
    this.schema = schema;
  }

  /** Создаёт запросы для схемы в диалекте. */
  public static Sql using(Dialect dialect, Schema schema) {
    return new Sql(Objects.requireNonNull(dialect), Objects.requireNonNull(schema));
  }

  /** Открывает таблицу для запросов. */
  public Table table(String name) {
    return new Table(dialect, schema, Objects.requireNonNull(name));
  }
}
