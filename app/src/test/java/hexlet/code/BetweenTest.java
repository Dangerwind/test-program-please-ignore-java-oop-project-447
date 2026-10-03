package hexlet.code;

import static hexlet.code.Conditions.and;
import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static hexlet.code.Conditions.or;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BetweenTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  private static Sql postgres() {
    return Sql.using(new PostgresDialect(), SCHEMA);
  }

  private static Sql mysql() {
    return Sql.using(new MySqlDialect(), SCHEMA);
  }

  @Test
  @DisplayName("Диапазон печатается двумя параметрами")
  void printsRange() {
    var query = postgres().table("users").select("id").where(new Between("age", 18, 65)).toSql();

    assertEquals("SELECT \"id\" FROM \"users\" WHERE \"age\" BETWEEN $1 AND $2", query.sql());
    assertEquals(List.of(18, 65), query.params());
  }

  @Test
  @DisplayName("Своё условие работает в любом диалекте")
  void worksInEveryDialect() {
    var sqlite = Sql.using(new SqliteDialect(), SCHEMA).table("users");

    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE \"age\" BETWEEN ? AND ?",
        sqlite.select("id").where(new Between("age", 18, 65)).toSql().sql());
    assertEquals(
        "SELECT `id` FROM `users` WHERE `age` BETWEEN ? AND ?",
        mysql().table("users").select("id").where(new Between("age", 18, 65)).toSql().sql());
  }

  @Test
  @DisplayName("Вкладывается в and наравне со встроенными условиями")
  void nestsInAnd() {
    var condition = and(new Between("age", 18, 65), eq("active", true));

    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE \"age\" BETWEEN $1 AND $2 AND \"active\" = $3",
        postgres().table("users").select("id").where(condition).toSql().sql());
    assertEquals(
        "DELETE FROM `users` WHERE `age` BETWEEN ? AND ? AND `active` = ?",
        mysql().table("users").delete().where(condition).toSql().sql());
  }

  @Test
  @DisplayName("Вкладывается в or вместе с not")
  void nestsInOrAndNot() {
    var condition = or(new Between("age", 18, 65), new Between("age", 70, 99));
    var query = postgres().table("users").select("id").where(condition).toSql();

    assertEquals(
        "SELECT \"id\" FROM \"users\""
            + " WHERE \"age\" BETWEEN $1 AND $2 OR \"age\" BETWEEN $3 AND $4",
        query.sql());
    assertEquals(List.of(18, 65, 70, 99), query.params());

    var negated =
        postgres().table("users").select("id").where(Conditions.not(new Between("age", 0, 17)));
    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE NOT (\"age\" BETWEEN $1 AND $2)",
        negated.toSql().sql());
  }

  @Test
  @DisplayName("Смешивается со встроенными условиями в одном запросе")
  void mixesWithBuiltInConditions() {
    var query =
        postgres()
            .table("users")
            .select("id")
            .where(gt("age", 0))
            .where(new Between("age", 18, 65))
            .where(eq("active", true))
            .toSql();

    assertEquals(
        "SELECT \"id\" FROM \"users\""
            + " WHERE \"age\" > $1 AND \"age\" BETWEEN $2 AND $3 AND \"active\" = $4",
        query.sql());
    assertEquals(List.of(0, 18, 65, true), query.params());
  }

  @Test
  @DisplayName("Границы не ограничены числами")
  void anyValues() {
    var query = postgres().table("users").select("id").where(new Between("name", "a", "z")).toSql();

    assertEquals("SELECT \"id\" FROM \"users\" WHERE \"name\" BETWEEN $1 AND $2", query.sql());
    assertEquals(List.of("a", "z"), query.params());
  }

  @Test
  @DisplayName("Колонка проверяется по схеме, как у встроенных условий")
  void checksColumnAgainstSchema() {
    assertThrows(
        QueryException.class,
        () -> postgres().table("users").select("id").where(new Between("nope", 1, 2)).toSql());
    assertThrows(
        QueryException.class,
        () -> mysql().table("users").delete().where(new Between("nope", 1, 2)).toSql());
  }

  @Test
  @DisplayName("Пустые границы отвергаются")
  void rejectsNullBounds() {
    assertThrows(QueryException.class, () -> new Between("age", null, 65));
    assertThrows(QueryException.class, () -> new Between("age", 18, null));
    assertThrows(QueryException.class, () -> new Between(null, 18, 65));
  }

  @Test
  @DisplayName("Условие неизменяемо и переиспользуемо")
  void reusable() {
    var adults = new Between("age", 18, 65);

    var one = postgres().table("users").select("id").where(adults).toSql();
    var many = postgres().table("users").delete().where(adults).where(eq("active", true)).toSql();

    assertEquals("SELECT \"id\" FROM \"users\" WHERE \"age\" BETWEEN $1 AND $2", one.sql());
    assertEquals(
        "DELETE FROM \"users\" WHERE \"age\" BETWEEN $1 AND $2 AND \"active\" = $3", many.sql());
  }
}
