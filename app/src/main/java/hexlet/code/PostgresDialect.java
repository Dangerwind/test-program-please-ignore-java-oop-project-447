package hexlet.code;

import static java.util.stream.Collectors.joining;

import java.util.List;

/** Диалект PostgreSQL. */
public class PostgresDialect implements Dialect {

  @Override
  public CompiledQuery formatSelect(String table, List<String> columns) {
    var list =
        columns.isEmpty()
            ? "*"
            : columns.stream().map(this::quoteIdentifier).collect(joining(", "));
    return new CompiledQuery("SELECT " + list + " FROM " + quoteIdentifier(table), List.of());
  }

  @Override
  public String quoteIdentifier(String name) {
    return "\"" + name + "\"";
  }

  @Override
  public String placeholder(int index) {
    return "?";
  }
}
