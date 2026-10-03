package hexlet.code;

import java.util.List;
import java.util.Map;

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
  public void formatInsert(SqlWriter writer, String table, Map<String, Object> values) {
    writer.sql("INSERT INTO ").table(table).sql(" (");
    var first = true;
    for (var column : values.keySet()) {
      if (!first) {
        writer.sql(", ");
      }
      writer.column(column);
      first = false;
    }
    writer.sql(") VALUES (");
    first = true;
    for (var value : values.values()) {
      if (!first) {
        writer.sql(", ");
      }
      writer.param(value);
      first = false;
    }
    writer.sql(")");
  }

  @Override
  public void formatUpdate(SqlWriter writer, String table, Map<String, Object> values) {
    writer.sql("UPDATE ").table(table).sql(" SET ");
    var first = true;
    for (var entry : values.entrySet()) {
      if (!first) {
        writer.sql(", ");
      }
      writer.column(entry.getKey()).sql(" = ").param(entry.getValue());
      first = false;
    }
  }

  @Override
  public void formatDelete(SqlWriter writer, String table) {
    writer.sql("DELETE FROM ").table(table);
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
