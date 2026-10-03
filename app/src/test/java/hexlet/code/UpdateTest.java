package hexlet.code;

import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateTest {

  private static final Sql DB =
      Sql.using(
          new PostgresDialect(),
          Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active"))));

  @Test
  @DisplayName("Присваивания печатаются в порядке вызовов, условие после них")
  void updatePrintsAssignmentsThenWhere() {
    var query =
        DB.table("users").update().set("name", "ann").set("age", 31).where(eq("id", 1)).toSql();

    assertEquals("UPDATE \"users\" SET \"name\" = $1, \"age\" = $2 WHERE \"id\" = $3", query.sql());
    assertEquals(List.of("ann", 31, 1), query.params());
  }

  @Test
  @DisplayName("Параметры SET нумеруются раньше параметров условия")
  void setParamsComeFirst() {
    var query = DB.table("users").update().set("age", 31).where(gt("age", 18)).toSql();

    assertEquals("UPDATE \"users\" SET \"age\" = $1 WHERE \"age\" > $2", query.sql());
    assertEquals(List.of(31, 18), query.params());
  }

  @Test
  @DisplayName("null уходит параметром")
  void nullValueIsAParameter() {
    var query = DB.table("users").update().set("email", null).where(eq("id", 1)).toSql();

    assertEquals("UPDATE \"users\" SET \"email\" = $1 WHERE \"id\" = $2", query.sql());
    assertEquals(Arrays.asList(null, 1), query.params());
  }

  @Test
  @DisplayName("Без условия обновление бросает подкласс QueryException")
  void updateWithoutWhereIsRejected() {
    var error =
        assertThrows(
            QueryWithoutConditionException.class,
            () -> DB.table("users").update().set("active", false).toSql());

    assertTrue(error instanceof QueryException, "подкласс ловится как QueryException");
    assertTrue(error.getMessage().contains("all()"), error.getMessage());
  }

  @Test
  @DisplayName("all() разрешает обновить всю таблицу")
  void allUpdatesWholeTable() {
    var query = DB.table("users").update().set("active", false).all().toSql();

    assertEquals("UPDATE \"users\" SET \"active\" = $1", query.sql());
    assertEquals(List.of(false), query.params());
  }

  @Test
  @DisplayName("Пустой UPDATE нечего записывать")
  void emptyUpdateIsRejected() {
    assertThrows(QueryException.class, () -> DB.table("users").update().toSql());
    assertThrows(QueryException.class, () -> DB.table("users").update().where(eq("id", 1)).toSql());
    assertThrows(QueryException.class, () -> DB.table("users").update().all().toSql());
  }

  @Test
  @DisplayName("Неизвестная колонка в set")
  void unknownColumnInSet() {
    assertThrows(
        QueryException.class,
        () -> DB.table("users").update().set("nope", 1).where(eq("id", 1)).toSql());
  }

  @Test
  @DisplayName("where не меняет исходное обновление")
  void updateIsImmutable() {
    var base = DB.table("users").update().set("active", false);
    base.where(eq("id", 1));

    assertThrows(QueryWithoutConditionException.class, base::toSql);
  }

  @Test
  @DisplayName("Ветки от одного обновления не мешают друг другу")
  void branchesFromSharedUpdate() {
    var base = DB.table("users").update().set("active", false);
    var one = base.where(eq("id", 1)).toSql();
    var many = base.where(eq("id", 1)).where(gt("age", 18)).toSql();

    assertEquals("UPDATE \"users\" SET \"active\" = $1 WHERE \"id\" = $2", one.sql());
    assertEquals(
        "UPDATE \"users\" SET \"active\" = $1 WHERE \"id\" = $2 AND \"age\" > $3", many.sql());
    assertEquals(List.of(false, 1, 18), many.params());
  }
}
