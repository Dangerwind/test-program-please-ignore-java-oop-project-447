package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SqliteDialectTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  private Sql db() {
    return Sql.using(new SqliteDialect(), SCHEMA);
  }

  @Test
  @DisplayName("Кавычки двойные, плейсхолдер знак вопроса")
  void quotesAndPlaceholders() {
    var dialect = new SqliteDialect();

    assertEquals("\"users\"", dialect.quoteIdentifier("users"));
    assertEquals("?", dialect.placeholder(1));
    assertEquals("?", dialect.placeholder(5));
  }

  @Test
  @DisplayName("Выборка печатается как в стандартном SQL")
  void select() {
    var query = db().table("users").select("name").where(Conditions.eq("id", 1)).toSql();

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"id\" = ?", query.sql());
    assertEquals(List.of(1), query.params());
  }

  @Test
  @DisplayName("Upsert печатается так же, как в PostgreSQL, но со знаком вопроса")
  void upsert() {
    var query =
        db().table("users")
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .onConflict("email")
            .doUpdate("name")
            .toSql();

    assertEquals(
        "INSERT INTO \"users\" (\"email\", \"name\") VALUES (?, ?)"
            + " ON CONFLICT (\"email\") DO UPDATE SET \"name\" = EXCLUDED.\"name\"",
        query.sql());
    assertEquals(List.of("rob@example.com", "rob"), query.params());
  }

  @Test
  @DisplayName("Вставка, обновление и удаление в двойных кавычках")
  void mutations() {
    var insert = db().table("users").insert().value("name", "rob").value("age", 30).toSql();
    assertEquals("INSERT INTO \"users\" (\"name\", \"age\") VALUES (?, ?)", insert.sql());

    var update =
        db().table("users").update().set("name", "ann").where(Conditions.eq("id", 1)).toSql();
    assertEquals("UPDATE \"users\" SET \"name\" = ? WHERE \"id\" = ?", update.sql());

    var delete = db().table("users").delete().where(Conditions.eq("id", 1)).toSql();
    assertEquals("DELETE FROM \"users\" WHERE \"id\" = ?", delete.sql());
  }

  @Test
  @DisplayName("Несколько колонок в upsert печатаются через запятую")
  void upsertSeveralColumns() {
    var query =
        db().table("users")
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .value("age", 30)
            .onConflict("email", "id")
            .doUpdate("name", "age")
            .toSql();

    assertEquals(
        "INSERT INTO \"users\" (\"email\", \"name\", \"age\") VALUES (?, ?, ?)"
            + " ON CONFLICT (\"email\", \"id\")"
            + " DO UPDATE SET \"name\" = EXCLUDED.\"name\", \"age\" = EXCLUDED.\"age\"",
        query.sql());
  }

  @Test
  @DisplayName("Условия печатаются как в стандартном SQL")
  void conditions() {
    var query =
        db().table("users")
            .select("id")
            .where(Conditions.eq("active", true))
            .where(Conditions.gt("age", 18))
            .where(Conditions.in("id", 1, 2))
            .where(Conditions.isNull("email"))
            .orderBy("name")
            .limit(10)
            .toSql();

    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE \"active\" = ? AND \"age\" > ?"
            + " AND \"id\" IN (?, ?) AND \"email\" IS NULL ORDER BY \"name\" LIMIT 10",
        query.sql());
    assertEquals(List.of(true, 18, 1, 2), query.params());
  }

  @Test
  @DisplayName("Колонки по-прежнему проверяются по схеме")
  void checksSchema() {
    assertThrows(QueryException.class, () -> db().table("users").select("nope").toSql());
    assertThrows(
        QueryException.class, () -> db().table("unknown").insert().value("name", "rob").toSql());
    assertThrows(
        QueryException.class,
        () ->
            db().table("users")
                .insert()
                .value("name", "rob")
                .onConflict("nope")
                .doUpdate("name")
                .toSql());
  }
}
