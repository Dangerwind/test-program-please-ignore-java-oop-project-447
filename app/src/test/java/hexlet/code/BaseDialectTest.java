package hexlet.code;

import static hexlet.code.Conditions.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseDialectTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  private final BaseDialect dialect = new BaseDialect();

  private Sql db() {
    return Sql.using(dialect, SCHEMA);
  }

  @Test
  @DisplayName("Имена берутся в двойные кавычки, на месте значения знак вопроса")
  void quotesAndPlaceholders() {
    assertEquals("\"users\"", dialect.quoteIdentifier("users"));
    assertEquals("?", dialect.placeholder(1));
    assertEquals("?", dialect.placeholder(9));
  }

  @Test
  @DisplayName("Выборка печатается по стандарту SQL")
  void select() {
    var query = db().table("users").select("name").where(eq("id", 1)).toSql();

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"id\" = ?", query.sql());
    assertEquals(List.of(1), query.params());
  }

  @Test
  @DisplayName("Вставка, обновление и удаление печатаются по стандарту SQL")
  void mutations() {
    var insert = db().table("users").insert().value("name", "rob").toSql();
    assertEquals("INSERT INTO \"users\" (\"name\") VALUES (?)", insert.sql());

    var update = db().table("users").update().set("name", "ann").where(eq("id", 1)).toSql();
    assertEquals("UPDATE \"users\" SET \"name\" = ? WHERE \"id\" = ?", update.sql());

    var delete = db().table("users").delete().where(eq("id", 1)).toSql();
    assertEquals("DELETE FROM \"users\" WHERE \"id\" = ?", delete.sql());
  }

  @Test
  @DisplayName("В стандартном SQL upsert не определён")
  void upsertIsRejected() {
    var error =
        assertThrows(
            QueryException.class,
            () ->
                db().table("users")
                    .insert()
                    .value("name", "rob")
                    .onConflict("email")
                    .doUpdate("name")
                    .toSql());

    assertEquals(true, error.getMessage().contains("upsert"));
  }

  @Test
  @DisplayName("Общие запросы работают как обычно, upsert ломает только upsert")
  void plainQueriesStillWork() {
    assertEquals(
        "INSERT INTO \"users\" (\"name\") VALUES (?)",
        db().table("users").insert().value("name", "rob").toSql().sql());
  }

  @Test
  @DisplayName("Пустой конфликт и пустое обновление не печатают upsert")
  void noUpsertWithoutPair() {
    assertThrows(
        QueryException.class,
        () -> db().table("users").insert().value("name", "rob").onConflict("email").toSql());
    assertThrows(
        QueryException.class,
        () -> db().table("users").insert().value("name", "rob").doUpdate("name").toSql());
  }
}
