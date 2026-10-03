package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostgresDialectTest {

  private final PostgresDialect dialect = new PostgresDialect();

  @Test
  @DisplayName("Имена оборачиваются в двойные кавычки")
  void quotesIdentifiers() {
    assertEquals("\"users\"", dialect.quoteIdentifier("users"));
    assertEquals("\"order\"", dialect.quoteIdentifier("order"));
  }

  @Test
  @DisplayName("Плейсхолдер — вопросительный знак")
  void usesQuestionMarkPlaceholder() {
    assertEquals("?", dialect.placeholder(1));
    assertEquals("?", dialect.placeholder(2));
  }

  @Test
  @DisplayName("Пустой список колонок печатается звездочкой")
  void printsStarForEmptyColumns() {
    var query = dialect.formatSelect("posts", List.of());

    assertEquals("SELECT * FROM \"posts\"", query.sql());
    assertEquals(List.of(), query.params());
  }

  @Test
  @DisplayName("Диалект печатает, но схему не проверяет")
  void dialectDoesNotCheckSchema() {
    var query = dialect.formatSelect("unknown", List.of("whatever"));

    assertEquals("SELECT \"whatever\" FROM \"unknown\"", query.sql());
  }
}
