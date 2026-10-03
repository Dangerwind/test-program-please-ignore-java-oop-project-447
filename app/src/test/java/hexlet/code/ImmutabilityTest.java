package hexlet.code;

import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImmutabilityTest {

  private static final Sql DB =
      Sql.using(
          new PostgresDialect(),
          Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active"))));

  @Test
  @DisplayName("Ветки от одного запроса не мешают друг другу")
  void branchesFromSharedQuery() {
    var users = DB.table("users").select("name");
    var active = users.where(eq("active", true));
    var adults = active.where(gt("age", 18));
    var firstPage = active.orderBy("name").limit(10);

    assertEquals("SELECT \"name\" FROM \"users\"", users.toSql().sql());
    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"active\" = $1", active.toSql().sql());
    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2",
        adults.toSql().sql());
    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 ORDER BY \"name\" LIMIT 10",
        firstPage.toSql().sql());
  }

  @Test
  @DisplayName("where не меняет исходный запрос")
  void whereLeavesOriginalAlone() {
    var base = DB.table("users").select("id");
    base.where(eq("active", true)).where(gt("age", 18)).limit(5).offset(10);

    assertEquals("SELECT \"id\" FROM \"users\"", base.toSql().sql());
    assertEquals(List.of(), base.toSql().params());
  }

  @Test
  @DisplayName("Сортировка и страница не меняют исходный запрос")
  void orderAndPagingLeaveOriginalAlone() {
    var base = DB.table("users").select("id");
    base.orderBy("name").orderBy("age").limit(10).offset(20);

    assertEquals("SELECT \"id\" FROM \"users\"", base.toSql().sql());
  }

  @Test
  @DisplayName("Один и тот же запрос печатается одинаково сколько угодно раз")
  void repeatedPrintIsStable() {
    var select = DB.table("users").select("id").where(eq("active", true)).orderBy("id").limit(3);
    var first = select.toSql();
    var second = select.toSql();

    assertEquals(first, second);
    assertEquals(first.sql(), second.sql());
    assertEquals(List.of(true), first.params());
  }

  @Test
  @DisplayName("Условие можно переиспользовать в разных ветках")
  void sharedConditionDoesNotShareParams() {
    var active = eq("active", true);
    var base = DB.table("users").select("id");

    var withAge = base.where(active).where(gt("age", 18)).toSql();
    var withName = base.where(active).toSql();

    assertEquals(List.of(true, 18), withAge.params());
    assertEquals(List.of(true), withName.params());
  }
}
