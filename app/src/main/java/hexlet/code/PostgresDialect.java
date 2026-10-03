package hexlet.code;

import java.util.List;

/** Диалект PostgreSQL. */
public class PostgresDialect implements Dialect {

  @Override
  public void formatSelect(SqlWriter writer, String table, List<String> columns) {
    writer.sql("SELECT ");
    if (columns.isEmpty()) {
      writer.sql("*");
    }
    for (var i = 0; i < columns.size(); i++) {
      if (i > 0) {
        writer.sql(", ");
      }
      writer.column(columns.get(i));
    }
    writer.sql(" FROM ").table(table);
  }

  @Override
  public String quoteIdentifier(String name) {
    return "\"" + name + "\"";
  }

  /** PostgreSQL нумерует плейсхолдеры по порядку: {@code $1}, {@code $2} и так далее. */
  @Override
  public String placeholder(int index) {
    return "$" + index;
  }
}
