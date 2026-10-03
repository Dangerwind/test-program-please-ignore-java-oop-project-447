package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InsertTest {

  private static final Sql DB =
      Sql.using(
          new PostgresDialect(),
          Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active"))));

  @Test
  @DisplayName("Колонки печатаются в порядке вызовов, значения уходят параметрами")
  void insertPrintsColumnsAndValues() {
    var query = DB.table("users").insert().value("name", "rob").value("age", 30).toSql();

    assertEquals("INSERT INTO \"users\" (\"name\", \"age\") VALUES ($1, $2)", query.sql());
    assertEquals(List.of("rob", 30), query.params());
  }

  @Test
  @DisplayName("Порядок вызовов важнее алфавитного")
  void keepsCallOrder() {
    var query = DB.table("users").insert().value("age", 30).value("name", "rob").toSql();

    assertEquals("INSERT INTO \"users\" (\"age\", \"name\") VALUES ($1, $2)", query.sql());
    assertEquals(List.of(30, "rob"), query.params());
  }

  @Test
  @DisplayName("null уходит параметром, а не попадает в текст")
  void nullValueIsAParameter() {
    var query = DB.table("users").insert().value("name", "rob").value("email", null).toSql();

    assertEquals("INSERT INTO \"users\" (\"name\", \"email\") VALUES ($1, $2)", query.sql());
    // List.of не принимает null, поэтому ожидаемое значение собираем через Arrays.asList.
    assertEquals(Arrays.asList("rob", null), query.params());
  }

  @Test
  @DisplayName("Повторный value для той же колонки заменяет значение")
  void repeatedValueReplaces() {
    var query =
        DB.table("users")
            .insert()
            .value("name", "rob")
            .value("age", 30)
            .value("name", "ann")
            .toSql();

    assertEquals("INSERT INTO \"users\" (\"name\", \"age\") VALUES ($1, $2)", query.sql());
    assertEquals(List.of("ann", 30), query.params());
  }

  @Test
  @DisplayName("Пустой INSERT нечего записывать")
  void emptyInsertIsRejected() {
    assertThrows(QueryException.class, () -> DB.table("users").insert().toSql());
  }

  @Test
  @DisplayName("Неизвестная колонка и неизвестная таблица")
  void namesAreChecked() {
    assertThrows(QueryException.class, () -> DB.table("users").insert().value("nope", 1).toSql());
    assertThrows(QueryException.class, () -> DB.table("orders").insert().value("id", 1).toSql());
  }

  @Test
  @DisplayName("value не меняет исходную вставку")
  void insertIsImmutable() {
    var base = DB.table("users").insert().value("name", "rob");
    base.value("age", 30);

    assertEquals("INSERT INTO \"users\" (\"name\") VALUES ($1)", base.toSql().sql());
    assertEquals(List.of("rob"), base.toSql().params());
  }
}
