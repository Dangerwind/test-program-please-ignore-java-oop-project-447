package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SelectTest {

  private static final Sql DB =
      Sql.using(
          new PostgresDialect(),
          Schema.of(
              Map.of(
                  "users", List.of("id", "name", "email", "age", "active"),
                  "posts", List.of("id", "title"))));

  @Test
  @DisplayName("Колонки и таблица печатаются в кавычках, значений нет")
  void selectPrintsColumnsAndTable() {
    var query = DB.table("users").select("id", "name").toSql();

    assertEquals("SELECT \"id\", \"name\" FROM \"users\"", query.sql());
    assertEquals(List.of(), query.params());
  }

  @Test
  @DisplayName("Без колонок печатается звездочка")
  void selectWithoutColumnsPrintsStar() {
    var query = DB.table("posts").select().toSql();

    assertEquals("SELECT * FROM \"posts\"", query.sql());
    assertEquals(List.of(), query.params());
  }

  @Test
  @DisplayName("Одинаковые запросы равны")
  void equalQueriesAreEqual() {
    var first = DB.table("users").select("name").toSql();
    var second = DB.table("users").select("name").toSql();

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  @DisplayName("Разные запросы не равны")
  void differentQueriesAreNotEqual() {
    assertNotEquals(
        DB.table("users").select("name").toSql(), DB.table("users").select("id").toSql());
  }

  @Test
  @DisplayName("Неизвестная таблица")
  void unknownTable() {
    var error = assertThrows(QueryException.class, () -> DB.table("orders").select("id").toSql());

    assertTrue(error.getMessage().contains("orders"), error.getMessage());
  }

  @Test
  @DisplayName("Неизвестная колонка")
  void unknownColumn() {
    var error = assertThrows(QueryException.class, () -> DB.table("posts").select("name").toSql());

    assertTrue(error.getMessage().contains("name"), error.getMessage());
  }

  @Test
  @DisplayName("Имена проверяются при печати, а не при выборе таблицы")
  void namesAreCheckedOnPrint() {
    var select = DB.table("orders").select("whatever");

    assertThrows(QueryException.class, select::toSql);
  }
}
