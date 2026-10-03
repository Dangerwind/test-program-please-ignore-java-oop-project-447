package hexlet.code;

import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Один и тот же запрос во всех диалектах: меняются только кавычки, плейсхолдеры и upsert.
 *
 * <p>Если такой тест падает, значит подкласс переопределил раскладку целиком и разошёлся с
 * остальными диалектами.
 */
class SameQueryInDialectsTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  private static Sql db(Dialect dialect) {
    return Sql.using(dialect, SCHEMA);
  }

  @Test
  @DisplayName("Выборка с условием, сортировкой и лимитом")
  void selectWithConditionOrderAndLimit() {
    var expected =
        Map.of(
            new BaseDialect(),
                "SELECT \"name\" FROM \"users\" WHERE \"active\" = ? AND \"age\" > ? ORDER BY \"name\" LIMIT 10",
            new MySqlDialect(),
                "SELECT `name` FROM `users` WHERE `active` = ? AND `age` > ? ORDER BY `name` LIMIT 10",
            new PostgresDialect(),
                "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2 ORDER BY \"name\" LIMIT 10");

    for (var entry : expected.entrySet()) {
      var query =
          db(entry.getKey())
              .table("users")
              .select("name")
              .where(eq("active", true))
              .where(gt("age", 18))
              .orderBy("name")
              .limit(10)
              .toSql();

      assertEquals(entry.getValue(), query.sql(), entry.getKey().getClass().getSimpleName());
      assertEquals(List.of(true, 18), query.params());
    }
  }

  @Test
  @DisplayName("Вставка без конфликта")
  void plainInsert() {
    var expected =
        Map.of(
            new BaseDialect(), "INSERT INTO \"users\" (\"name\", \"age\") VALUES (?, ?)",
            new MySqlDialect(), "INSERT INTO `users` (`name`, `age`) VALUES (?, ?)",
            new PostgresDialect(), "INSERT INTO \"users\" (\"name\", \"age\") VALUES ($1, $2)");

    for (var entry : expected.entrySet()) {
      var query =
          db(entry.getKey()).table("users").insert().value("name", "rob").value("age", 30).toSql();

      assertEquals(entry.getValue(), query.sql(), entry.getKey().getClass().getSimpleName());
      assertEquals(List.of("rob", 30), query.params());
    }
  }

  @Test
  @DisplayName("Обновление с условием")
  void updateWithCondition() {
    var expected =
        Map.of(
            new BaseDialect(), "UPDATE \"users\" SET \"name\" = ? WHERE \"id\" = ?",
            new MySqlDialect(), "UPDATE `users` SET `name` = ? WHERE `id` = ?",
            new PostgresDialect(), "UPDATE \"users\" SET \"name\" = $1 WHERE \"id\" = $2");

    for (var entry : expected.entrySet()) {
      var query =
          db(entry.getKey()).table("users").update().set("name", "ann").where(eq("id", 1)).toSql();

      assertEquals(entry.getValue(), query.sql(), entry.getKey().getClass().getSimpleName());
      assertEquals(List.of("ann", 1), query.params());
    }
  }

  @Test
  @DisplayName("Удаление с условием")
  void deleteWithCondition() {
    var expected =
        Map.of(
            new BaseDialect(), "DELETE FROM \"users\" WHERE \"id\" = ?",
            new MySqlDialect(), "DELETE FROM `users` WHERE `id` = ?",
            new PostgresDialect(), "DELETE FROM \"users\" WHERE \"id\" = $1");

    for (var entry : expected.entrySet()) {
      var query = db(entry.getKey()).table("users").delete().where(eq("id", 1)).toSql();

      assertEquals(entry.getValue(), query.sql(), entry.getKey().getClass().getSimpleName());
      assertEquals(List.of(1), query.params());
    }
  }

  @Test
  @DisplayName("Upsert в двух диалектах, а в стандартном SQL его нет")
  void upsert() {
    var postgres =
        db(new PostgresDialect())
            .table("users")
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .onConflict("email")
            .doUpdate("name")
            .toSql();
    assertEquals(
        "INSERT INTO \"users\" (\"email\", \"name\") VALUES ($1, $2)"
            + " ON CONFLICT (\"email\") DO UPDATE SET \"name\" = EXCLUDED.\"name\"",
        postgres.sql());
    assertEquals(List.of("rob@example.com", "rob"), postgres.params());

    var mysql =
        db(new MySqlDialect())
            .table("users")
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .onConflict("email")
            .doUpdate("name")
            .toSql();
    assertEquals(
        "INSERT INTO `users` (`email`, `name`) VALUES (?, ?)"
            + " AS new ON DUPLICATE KEY UPDATE `name` = new.`name`",
        mysql.sql());
    assertEquals(List.of("rob@example.com", "rob"), mysql.params());
  }
}
