package hexlet.code;

import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static hexlet.code.Conditions.or;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderLimitTest {

  private static final Sql DB =
      Sql.using(
          new PostgresDialect(),
          Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active"))));

  private static Table users() {
    return DB.table("users");
  }

  @Test
  @DisplayName("Сортировка по одной колонке")
  void singleOrderBy() {
    assertEquals(
        "SELECT \"id\" FROM \"users\" ORDER BY \"name\"",
        users().select("id").orderBy("name").toSql().sql());
  }

  @Test
  @DisplayName("Повторный orderBy дописывает колонку")
  void repeatedOrderByAppends() {
    assertEquals(
        "SELECT \"id\" FROM \"users\" ORDER BY \"name\", \"age\"",
        users().select("id").orderBy("name").orderBy("age").toSql().sql());
  }

  @Test
  @DisplayName("Повторный limit заменяет прежнее значение")
  void repeatedLimitReplaces() {
    assertEquals(
        "SELECT \"id\" FROM \"users\" LIMIT 5",
        users().select("id").limit(10).limit(5).toSql().sql());
  }

  @Test
  @DisplayName("Повторный offset заменяет прежнее значение")
  void repeatedOffsetReplaces() {
    assertEquals(
        "SELECT \"id\" FROM \"users\" LIMIT 10 OFFSET 5",
        users().select("id").limit(10).offset(40).offset(5).toSql().sql());
  }

  @Test
  @DisplayName("Части запроса печатаются в фиксированном порядке")
  void partsArePrintedInFixedOrder() {
    var query =
        users()
            .select("name")
            .offset(20)
            .limit(10)
            .orderBy("name")
            .where(eq("active", true))
            .toSql();

    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 ORDER BY \"name\" LIMIT 10 OFFSET 20",
        query.sql());
    assertEquals(List.of(true), query.params());
  }

  @Test
  @DisplayName("Третья страница по десять записей")
  void thirdPage() {
    var query = users().select("name").orderBy("name").limit(10).offset(20).toSql();

    assertEquals(
        "SELECT \"name\" FROM \"users\" ORDER BY \"name\" LIMIT 10 OFFSET 20", query.sql());
  }

  @Test
  @DisplayName("limit после offset допустим")
  void limitAfterOffset() {
    var query = users().select("id").offset(20).limit(10).toSql();

    assertEquals("SELECT \"id\" FROM \"users\" LIMIT 10 OFFSET 20", query.sql());
  }

  @Test
  @DisplayName("Числа печатаются в текст, а не в параметры")
  void numbersGoToText() {
    var query = users().select("id").limit(10).offset(20).toSql();

    assertEquals(List.of(), query.params());
  }

  @Test
  @DisplayName("Отрицательные числа бросают QueryException")
  void negativeNumbersAreRejected() {
    assertThrows(QueryException.class, () -> users().select("id").limit(-1));
    assertThrows(QueryException.class, () -> users().select("id").offset(-1));
  }

  @Test
  @DisplayName("offset без limit бросает QueryException при печати")
  void offsetWithoutLimit() {
    var select = users().select("id").offset(20);

    var error = assertThrows(QueryException.class, select::toSql);
    assertTrue(error.getMessage().contains("LIMIT"), error.getMessage());
  }

  @Test
  @DisplayName("После добавления limit запрос становится годным")
  void offsetBecomesValidWithLimit() {
    assertEquals(
        "SELECT \"id\" FROM \"users\" LIMIT 10 OFFSET 20",
        users().select("id").offset(20).limit(10).toSql().sql());
  }

  @Test
  @DisplayName("Неизвестная колонка в сортировке")
  void unknownColumnInOrderBy() {
    assertThrows(QueryException.class, () -> users().select("id").orderBy("nope").toSql());
  }

  @Test
  @DisplayName("Сортировка печатается после WHERE")
  void orderByAfterWhere() {
    var query = users().select("id").where(eq("active", true)).orderBy("name").toSql();

    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE \"active\" = $1 ORDER BY \"name\"", query.sql());
  }

  @Test
  @DisplayName("Один where с OR печатается без скобок")
  void singleOrWhereHasNoParentheses() {
    var query = users().select("id").where(or(eq("name", "ann"), eq("name", "rob"))).toSql();

    assertEquals("SELECT \"id\" FROM \"users\" WHERE \"name\" = $1 OR \"name\" = $2", query.sql());
  }

  @Test
  @DisplayName("OR берётся в скобки, когда его соединяет AND")
  void orInsideAndWhereGetsParentheses() {
    var query =
        users()
            .select("id")
            .where(or(eq("name", "ann"), eq("name", "rob")))
            .where(gt("age", 18))
            .orderBy("name")
            .toSql();

    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE (\"name\" = $1 OR \"name\" = $2) AND \"age\" > $3"
            + " ORDER BY \"name\"",
        query.sql());
    assertEquals(List.of("ann", "rob", 18), query.params());
  }

  @Test
  @DisplayName("Ноль строк и нулевой пропуск допустимы")
  void zeroIsAllowed() {
    assertEquals(
        "SELECT \"id\" FROM \"users\" LIMIT 0 OFFSET 0",
        users().select("id").limit(0).offset(0).toSql().sql());
  }
}
