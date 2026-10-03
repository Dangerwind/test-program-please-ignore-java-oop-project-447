package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostgresDialectTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name"), "posts", List.of("id", "title")));

  private final PostgresDialect dialect = new PostgresDialect();

  private CompiledQuery format(String table, List<String> columns) {
    var writer = new SqlWriter(dialect, SCHEMA, table);
    dialect.formatSelect(writer, table, columns);
    return writer.compiled();
  }

  @Test
  @DisplayName("Имена оборачиваются в двойные кавычки")
  void quotesIdentifiers() {
    assertEquals("\"users\"", dialect.quoteIdentifier("users"));
    assertEquals("\"order\"", dialect.quoteIdentifier("order"));
  }

  @Test
  @DisplayName("Плейсхолдеры нумеруются по порядку")
  void numbersPlaceholders() {
    assertEquals("$1", dialect.placeholder(1));
    assertEquals("$2", dialect.placeholder(2));
  }

  @Test
  @DisplayName("Пустой список колонок печатается звездочкой")
  void printsStarForEmptyColumns() {
    var query = format("posts", List.of());

    assertEquals("SELECT * FROM \"posts\"", query.sql());
    assertEquals(List.of(), query.params());
  }

  @Test
  @DisplayName("Колонки печатаются через запятую")
  void printsColumns() {
    var query = format("users", List.of("id", "name"));

    assertEquals("SELECT \"id\", \"name\" FROM \"users\"", query.sql());
  }

  @Test
  @DisplayName("Писатель проверяет имена по схеме, а не диалект")
  void writerChecksSchema() {
    assertThrows(QueryException.class, () -> format("unknown", List.of()));
    assertThrows(QueryException.class, () -> format("posts", List.of("name")));
  }
}
