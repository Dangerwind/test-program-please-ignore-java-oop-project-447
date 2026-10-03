package hexlet.code;

import static hexlet.code.Conditions.and;
import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static hexlet.code.Conditions.in;
import static hexlet.code.Conditions.isNull;
import static hexlet.code.Conditions.lt;
import static hexlet.code.Conditions.ne;
import static hexlet.code.Conditions.not;
import static hexlet.code.Conditions.or;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConditionsTest {

  private static final Sql DB =
      Sql.using(
          new PostgresDialect(),
          Schema.of(
              Map.of(
                  "users", List.of("id", "name", "email", "age", "active"),
                  "posts", List.of("id", "title"))));

  private static CompiledQuery where(Condition condition) {
    return DB.table("users").select("name").where(condition).toSql();
  }

  private static String whereSql(Condition condition) {
    return where(condition).sql();
  }

  @Test
  @DisplayName("Равенство печатает значение параметром")
  void equality() {
    var query = where(eq("id", 1));

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"id\" = $1", query.sql());
    assertEquals(List.of(1), query.params());
  }

  @Test
  @DisplayName("Неравенство, больше и меньше")
  void otherComparisons() {
    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"name\" <> $1", whereSql(ne("name", "ann")));
    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"age\" > $1", whereSql(gt("age", 18)));
    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"age\" < $1", whereSql(lt("age", 65)));
  }

  @Test
  @DisplayName("IN печатает каждое значение своим плейсхолдером")
  void inCondition() {
    var query = where(in("id", 1, 2, 3));

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"id\" IN ($1, $2, $3)", query.sql());
    assertEquals(List.of(1, 2, 3), query.params());
  }

  @Test
  @DisplayName("IS NULL не добавляет параметров")
  void isNullCondition() {
    var query = where(isNull("email"));

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"email\" IS NULL", query.sql());
    assertEquals(List.of(), query.params());
  }

  @Test
  @DisplayName("AND соединяет условия и нумерует параметры по порядку")
  void andCondition() {
    var adults = gt("age", 18);
    var query = where(and(eq("active", true), adults, in("id", 5, 7)));

    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2 AND \"id\" IN ($3, $4)",
        query.sql());
    assertEquals(List.of(true, 18, 5, 7), query.params());
  }

  @Test
  @DisplayName("OR внутри AND берётся в скобки")
  void orInsideAndGetsParentheses() {
    var condition = and(eq("active", true), or(eq("name", "ann"), eq("name", "rob")));

    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND (\"name\" = $2 OR \"name\" = $3)",
        whereSql(condition));
  }

  @Test
  @DisplayName("AND внутри OR печатается без скобок")
  void andInsideOrHasNoParentheses() {
    var condition = or(and(eq("active", true), gt("age", 18)), eq("id", 1));

    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2 OR \"id\" = $3",
        whereSql(condition));
  }

  @Test
  @DisplayName("NOT всегда берёт своё условие в скобки")
  void notAlwaysUsesParentheses() {
    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE NOT (\"email\" IS NULL)",
        whereSql(not(isNull("email"))));
    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE NOT (\"id\" = $1 OR \"id\" = $2)",
        whereSql(not(or(eq("id", 1), eq("id", 2)))));
  }

  @Test
  @DisplayName("Своё условие пишется лямбдой через три публичных метода")
  void customCondition() {
    Condition custom =
        writer -> writer.column("id").sql(" > ").param(10).sql(" AND ").column("active");

    var query = where(custom);

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"id\" > $1 AND \"active\"", query.sql());
    assertEquals(List.of(10), query.params());
  }

  @Test
  @DisplayName("Условие можно сохранить и использовать в нескольких запросах")
  void conditionIsReusable() {
    var adult = gt("id", 0);

    var first = DB.table("users").select("name").where(adult).toSql();
    var second = DB.table("posts").select("title").where(adult).toSql();

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"id\" > $1", first.sql());
    assertEquals(List.of(0), first.params());
    assertEquals("SELECT \"title\" FROM \"posts\" WHERE \"id\" > $1", second.sql());
    assertEquals(List.of(0), second.params());
  }

  @Test
  @DisplayName("Несколько where соединяются через AND")
  void severalWhereCalls() {
    var query =
        DB.table("users").select("name").where(eq("active", true)).where(gt("age", 18)).toSql();

    assertEquals(
        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2", query.sql());
    assertEquals(List.of(true, 18), query.params());
  }

  @Test
  @DisplayName("Значение из формы не попадает в текст запроса")
  void valueFromFormStaysAParameter() {
    var query = where(eq("name", "x' OR '1'='1"));

    assertEquals("SELECT \"name\" FROM \"users\" WHERE \"name\" = $1", query.sql());
    assertEquals(List.of("x' OR '1'='1"), query.params());
    assertTrue(!query.sql().contains("'1'='1'"), "в тексте запроса не должно быть значения");
  }

  @Test
  @DisplayName("Неизвестная колонка в условии")
  void unknownColumnInCondition() {
    var error = assertThrows(QueryException.class, () -> where(eq("nope", 1)));

    assertTrue(error.getMessage().contains("nope"), error.getMessage());
  }

  static Stream<Arguments> comparisons() {
    return Stream.of(
        Arguments.of("eq", (BiFunction<String, Object, Condition>) Conditions::eq),
        Arguments.of("ne", (BiFunction<String, Object, Condition>) Conditions::ne),
        Arguments.of("gt", (BiFunction<String, Object, Condition>) Conditions::gt),
        Arguments.of("lt", (BiFunction<String, Object, Condition>) Conditions::lt));
  }

  @ParameterizedTest(name = "{0} с null бросает QueryException")
  @MethodSource("comparisons")
  void nullValueIsRejected(String name, BiFunction<String, Object, Condition> comparison) {
    assertThrows(QueryException.class, () -> comparison.apply("id", null));
  }

  @ParameterizedTest(name = "{0} с null-колонкой бросает NullPointerException")
  @MethodSource("comparisons")
  void nullColumnIsRejected(String name, BiFunction<String, Object, Condition> comparison) {
    // Null в имени колонки — это ошибка в коде приложения, а не в запросе.
    assertThrows(NullPointerException.class, () -> comparison.apply(null, 1));
  }

  @Test
  @DisplayName("IN с null, с пустым списком и с null внутри")
  void badInArguments() {
    assertThrows(QueryException.class, () -> in("id", (Object[]) null));
    assertThrows(QueryException.class, () -> in("id"));
    assertThrows(QueryException.class, () -> in("id", 1, null));
  }

  @Test
  @DisplayName("AND и OR без условий")
  void emptyComposites() {
    assertThrows(QueryException.class, () -> and());
    assertThrows(QueryException.class, () -> or());
  }
}
