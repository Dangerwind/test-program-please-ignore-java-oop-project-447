package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpsertTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  private static Sql postgres() {
    return Sql.using(new PostgresDialect(), SCHEMA);
  }

  private static Sql mysql() {
    return Sql.using(new MySqlDialect(), SCHEMA);
  }

  @Test
  @DisplayName("PostgreSQL печатает конфликт через ON CONFLICT и значения через EXCLUDED")
  void postgresUpsert() {
    var query =
        postgres()
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
        query.sql());
    assertEquals(List.of("rob@example.com", "rob"), query.params());
  }

  @Test
  @DisplayName("MySQL даёт вставляемой строке имя new и не печатает колонки ключа")
  void mysqlUpsert() {
    var query =
        mysql()
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
        query.sql());
    assertEquals(List.of("rob@example.com", "rob"), query.params());
  }

  @Test
  @DisplayName("Несколько колонок печатаются через запятую")
  void severalColumns() {
    var postgres =
        postgres()
            .table("users")
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .value("age", 30)
            .onConflict("email", "id")
            .doUpdate("name", "age")
            .toSql();
    assertEquals(
        "INSERT INTO \"users\" (\"email\", \"name\", \"age\") VALUES ($1, $2, $3)"
            + " ON CONFLICT (\"email\", \"id\")"
            + " DO UPDATE SET \"name\" = EXCLUDED.\"name\", \"age\" = EXCLUDED.\"age\"",
        postgres.sql());

    var mysql =
        mysql()
            .table("users")
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .value("age", 30)
            .onConflict("email", "id")
            .doUpdate("name", "age")
            .toSql();
    assertEquals(
        "INSERT INTO `users` (`email`, `name`, `age`) VALUES (?, ?, ?) AS new"
            + " ON DUPLICATE KEY UPDATE `name` = new.`name`, `age` = new.`age`",
        mysql.sql());
  }

  @Test
  @DisplayName("Повторные вызовы дописывают колонки")
  void repeatedCallsAppend() {
    var query =
        postgres()
            .table("users")
            .insert()
            .value("name", "rob")
            .onConflict("email")
            .onConflict("id")
            .doUpdate("name")
            .doUpdate("age")
            .toSql();

    assertEquals(
        "INSERT INTO \"users\" (\"name\") VALUES ($1)"
            + " ON CONFLICT (\"email\", \"id\")"
            + " DO UPDATE SET \"name\" = EXCLUDED.\"name\", \"age\" = EXCLUDED.\"age\"",
        query.sql());
  }

  @Test
  @DisplayName("Конфликт без обновления и обновление без конфликта отвергаются")
  void methodsWorkAsPair() {
    for (var db : List.of(postgres(), mysql())) {
      assertThrows(
          QueryException.class,
          () -> db.table("users").insert().value("name", "rob").onConflict("email").toSql());
      assertThrows(
          QueryException.class,
          () -> db.table("users").insert().value("name", "rob").doUpdate("name").toSql());
    }
  }

  @Test
  @DisplayName("Без onConflict и doUpdate вставка обычная")
  void plainInsertHasNoUpsert() {
    var query = postgres().table("users").insert().value("name", "rob").toSql();

    assertEquals("INSERT INTO \"users\" (\"name\") VALUES ($1)", query.sql());
  }

  @Test
  @DisplayName("Неизвестная колонка ловится во всех диалектах, даже там, где не печатается")
  void unknownColumnsRejected() {
    // MySQL не печатает колонки конфликта, но опечатку в них всё равно надо ловить.
    for (var db : List.of(postgres(), mysql())) {
      assertThrows(
          QueryException.class,
          () ->
              db.table("users")
                  .insert()
                  .value("name", "rob")
                  .onConflict("nope")
                  .doUpdate("name")
                  .toSql());
      assertThrows(
          QueryException.class,
          () ->
              db.table("users")
                  .insert()
                  .value("name", "rob")
                  .onConflict("email")
                  .doUpdate("nope")
                  .toSql());
    }
  }

  @Test
  @DisplayName("Upsert не меняет исходную вставку")
  void upsertIsImmutable() {
    var base = postgres().table("users").insert().value("name", "rob");
    base.onConflict("email").doUpdate("name");

    assertEquals("INSERT INTO \"users\" (\"name\") VALUES ($1)", base.toSql().sql());
    assertEquals(List.of("rob"), base.toSql().params());
  }
}
