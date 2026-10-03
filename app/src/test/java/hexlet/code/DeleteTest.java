package hexlet.code;

import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.lt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteTest {

  private static final Sql DB =
      Sql.using(
          new PostgresDialect(),
          Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active"))));

  @Test
  @DisplayName("Несколько условий соединяются через AND")
  void severalConditions() {
    var query = DB.table("users").delete().where(lt("age", 18)).where(eq("active", false)).toSql();

    assertEquals("DELETE FROM \"users\" WHERE \"age\" < $1 AND \"active\" = $2", query.sql());
    assertEquals(List.of(18, false), query.params());
  }

  @Test
  @DisplayName("Без условия удаление бросает подкласс QueryException")
  void deleteWithoutWhereIsRejected() {
    var error =
        assertThrows(
            QueryWithoutConditionException.class, () -> DB.table("users").delete().toSql());

    assertInstanceOf(QueryException.class, error);
    assertEquals(true, error.getMessage().contains("all()"));
  }

  @Test
  @DisplayName("all() разрешает удалить всю таблицу")
  void allDeletesWholeTable() {
    var query = DB.table("users").delete().all().toSql();

    assertEquals("DELETE FROM \"users\"", query.sql());
    assertEquals(List.of(), query.params());
  }

  @Test
  @DisplayName("Подкласс ловится вместе со всеми остальными ошибками")
  void subclassIsCaughtAsQueryException() {
    assertThrows(QueryException.class, () -> DB.table("users").delete().toSql());
    assertThrows(QueryException.class, () -> DB.table("users").update().set("a", 1).toSql());
    assertThrows(QueryException.class, () -> DB.table("nope").delete().all().toSql());
  }

  @Test
  @DisplayName("Ошибка без условия видна только при печати")
  void errorShowsOnPrintOnly() {
    DB.table("users").delete();

    assertThrows(QueryWithoutConditionException.class, () -> DB.table("users").delete().toSql());
  }

  @Test
  @DisplayName("where не меняет исходное удаление")
  void deleteIsImmutable() {
    var base = DB.table("users").delete();
    base.where(eq("id", 1));

    assertThrows(QueryWithoutConditionException.class, base::toSql);
  }

  @Test
  @DisplayName("Неизвестная колонка в условии удаления")
  void unknownColumnInCondition() {
    assertThrows(
        QueryException.class, () -> DB.table("users").delete().where(eq("nope", 1)).toSql());
  }
}
