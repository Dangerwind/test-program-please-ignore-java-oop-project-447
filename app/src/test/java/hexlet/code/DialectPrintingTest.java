package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Вся печать живёт в диалекте, а не в модели запроса.
 *
 * <p>Методы диалекта проверяются напрямую: так видно, что запрос собирает диалект, а сам диалект
 * печатает одинаково во всех базах.
 */
class DialectPrintingTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  private static Sql db(Dialect dialect) {
    return Sql.using(dialect, SCHEMA);
  }

  private static String formatWhere(Dialect dialect, List<Condition> conditions) {
    var writer = new SqlWriter(dialect, SCHEMA, "users");
    dialect.formatWhere(writer, conditions);
    return writer.compiled().sql();
  }

  @Test
  @DisplayName("WHERE печатает условие, ничего не печатая без условий")
  void printsWhere() {
    var dialect = new PostgresDialect();

    assertEquals("", formatWhere(dialect, List.of()));
    assertEquals(" WHERE \"id\" = $1", formatWhere(dialect, List.of(Conditions.eq("id", 1))));
  }

  @Test
  @DisplayName("Одно условие печатается как есть, несколько соединяются через AND")
  void joinsConditionsWithAnd() {
    var dialect = new PostgresDialect();

    assertEquals(
        " WHERE \"id\" = $1 AND \"age\" > $2",
        formatWhere(dialect, List.of(Conditions.eq("id", 1), Conditions.gt("age", 18))));
  }

  @Test
  @DisplayName("Вложенный OR берётся в скобки, одиночный печатается без них")
  void parenthesisesNestedOr() {
    var dialect = new PostgresDialect();

    // Один or(...) — печатается как есть, скобки никому не нужны.
    assertEquals(
        " WHERE \"id\" = $1 OR \"id\" = $2",
        formatWhere(
            dialect, List.of(Conditions.or(Conditions.eq("id", 1), Conditions.eq("id", 2)))));

    // Вместе с другим условием or(...) уже нужно взять в скобки, чтобы не сломать AND.
    assertEquals(
        " WHERE \"age\" > $1 AND (\"id\" = $2 OR \"id\" = $3)",
        formatWhere(
            dialect,
            List.of(
                Conditions.gt("age", 18),
                Conditions.or(Conditions.eq("id", 1), Conditions.eq("id", 2)))));
  }

  @Test
  @DisplayName("ORDER BY печатает колонки через запятую и молчит без них")
  void printsOrderBy() {
    var dialect = new PostgresDialect();

    var empty = new SqlWriter(dialect, SCHEMA, "users");
    dialect.formatOrderBy(empty, List.of());
    assertEquals("", empty.compiled().sql());

    var writer = new SqlWriter(dialect, SCHEMA, "users");
    dialect.formatOrderBy(writer, List.of("name", "id"));
    assertEquals(" ORDER BY \"name\", \"id\"", writer.compiled().sql());
  }

  @Test
  @DisplayName("LIMIT и OFFSET печатаются каждый по-своему")
  void printsLimitAndOffset() {
    var dialect = new PostgresDialect();

    var none = new SqlWriter(dialect, SCHEMA, "users");
    dialect.formatLimit(none, null, null);
    assertEquals("", none.compiled().sql());

    var onlyLimit = new SqlWriter(dialect, SCHEMA, "users");
    dialect.formatLimit(onlyLimit, 10, null);
    assertEquals(" LIMIT 10", onlyLimit.compiled().sql());

    var onlyOffset = new SqlWriter(dialect, SCHEMA, "users");
    dialect.formatLimit(onlyOffset, null, 5);
    assertEquals(" OFFSET 5", onlyOffset.compiled().sql());

    var both = new SqlWriter(dialect, SCHEMA, "users");
    dialect.formatLimit(both, 10, 20);
    assertEquals(" LIMIT 10 OFFSET 20", both.compiled().sql());
  }

  @Test
  @DisplayName("Раскладка печати одинакова во всех диалектах")
  void sameLayoutInEveryDialect() {
    for (var dialect :
        List.of(
            new BaseDialect(), new PostgresDialect(), new MySqlDialect(), new SqliteDialect())) {
      var query =
          db(dialect)
              .table("users")
              .select("name")
              .where(Conditions.eq("active", true))
              .where(Conditions.gt("age", 18))
              .orderBy("name")
              .limit(10)
              .offset(20)
              .toSql();
      var quoted = dialect.quoteIdentifier("name");

      assertEquals(
          "SELECT "
              + quoted
              + " FROM "
              + dialect.quoteIdentifier("users")
              + " WHERE "
              + dialect.quoteIdentifier("active")
              + " = "
              + dialect.placeholder(1)
              + " AND "
              + dialect.quoteIdentifier("age")
              + " > "
              + dialect.placeholder(2)
              + " ORDER BY "
              + quoted
              + " LIMIT 10 OFFSET 20",
          query.sql(),
          dialect.getClass().getSimpleName());
      assertEquals(List.of(true, 18), query.params());
    }
  }

  @Test
  @DisplayName("Модель запроса не печатает SQL сама")
  void queryModelDoesNotPrint() {
    // Печать проверяется выше по её результату. Здесь важно другое: если модель начнёт печатать
    // сама, диалект перестанет быть единственным местом печати, и такой тест перестанет
    // отражать устройство библиотеки.
    var query = db(new PostgresDialect()).table("users").select("name").toSql();

    assertEquals("SELECT \"name\" FROM \"users\"", query.sql());
  }
}
