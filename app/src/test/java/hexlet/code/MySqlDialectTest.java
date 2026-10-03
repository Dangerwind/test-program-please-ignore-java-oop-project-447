package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MySqlDialectTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  private final MySqlDialect dialect = new MySqlDialect();

  private Sql db() {
    return Sql.using(dialect, SCHEMA);
  }

  @Test
  @DisplayName("Имена берутся в обратные кавычки")
  void quotesIdentifiers() {
    assertEquals("`users`", dialect.quoteIdentifier("users"));
    assertEquals("`order`", dialect.quoteIdentifier("order"));
  }

  @Test
  @DisplayName("На месте каждого значения знак вопроса, без номеров")
  void questionMarkPlaceholders() {
    assertEquals("?", dialect.placeholder(1));
    assertEquals("?", dialect.placeholder(2));
    assertEquals("?", dialect.placeholder(37));
  }

  @Test
  @DisplayName("Выборка с условием, сортировкой и лимитом")
  void select() {
    var query =
        db().table("users")
            .select("name")
            .where(Conditions.eq("active", true))
            .where(Conditions.gt("age", 18))
            .orderBy("name")
            .limit(10)
            .toSql();

    assertEquals(
        "SELECT `name` FROM `users` WHERE `active` = ? AND `age` > ? ORDER BY `name` LIMIT 10",
        query.sql());
    assertEquals(List.of(true, 18), query.params());
  }

  @Test
  @DisplayName("Вставка, обновление и удаление в обратных кавычках")
  void mutations() {
    var insert = db().table("users").insert().value("name", "rob").value("age", 30).toSql();
    assertEquals("INSERT INTO `users` (`name`, `age`) VALUES (?, ?)", insert.sql());
    assertEquals(List.of("rob", 30), insert.params());

    var update =
        db().table("users").update().set("name", "ann").where(Conditions.eq("id", 1)).toSql();
    assertEquals("UPDATE `users` SET `name` = ? WHERE `id` = ?", update.sql());
    assertEquals(List.of("ann", 1), update.params());

    var delete = db().table("users").delete().where(Conditions.eq("id", 1)).toSql();
    assertEquals("DELETE FROM `users` WHERE `id` = ?", delete.sql());
    assertEquals(List.of(1), delete.params());
  }

  @Test
  @DisplayName("MySQL находит конфликт сам, колонки ключа в текст не попадают")
  void upsertIgnoresConflictColumns() {
    var query =
        db().table("users")
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .onConflict("email")
            .doUpdate("name")
            .toSql();

    assertEquals(
        "INSERT INTO `users` (`email`, `name`) VALUES (?, ?)"
            + " AS new ON DUPLICATE KEY UPDATE `name` = new.`name`",
        query.sql());
    assertEquals(List.of("rob@example.com", "rob"), query.params());
  }

  @Test
  @DisplayName("Значения вставляемой строки доступны под именем new")
  void upsertAlias() {
    var query =
        db().table("users")
            .insert()
            .value("name", "rob")
            .onConflict("email")
            .doUpdate("name")
            .toSql();

    assertEquals(
        "INSERT INTO `users` (`name`) VALUES (?) AS new"
            + " ON DUPLICATE KEY UPDATE `name` = new.`name`",
        query.sql());
  }

  @Test
  @DisplayName("Колонки печатаются в порядке вызовов, несколько — через запятую")
  void severalColumns() {
    var query =
        db().table("users")
            .insert()
            .value("name", "rob")
            .value("age", 30)
            .onConflict("email", "id")
            .doUpdate("name", "age")
            .toSql();

    assertEquals(
        "INSERT INTO `users` (`name`, `age`) VALUES (?, ?) AS new"
            + " ON DUPLICATE KEY UPDATE `name` = new.`name`, `age` = new.`age`",
        query.sql());
  }

  @Test
  @DisplayName("Неизвестная колонка по-прежнему проверяется по схеме")
  void checksSchema() {
    assertThrows(QueryException.class, () -> db().table("users").insert().value("nope", 1).toSql());
    assertThrows(
        QueryException.class,
        () ->
            db().table("users")
                .insert()
                .value("name", "rob")
                .onConflict("nope")
                .doUpdate("name")
                .toSql());
    assertThrows(QueryException.class, () -> db().table("unknown").select().toSql());
  }
}
