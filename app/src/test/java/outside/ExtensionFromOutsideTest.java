package outside;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hexlet.code.Condition;
import hexlet.code.Conditions;
import hexlet.code.MySqlDialect;
import hexlet.code.PostgresDialect;
import hexlet.code.QueryException;
import hexlet.code.Schema;
import hexlet.code.Sql;
import hexlet.code.SqlWriter;
import hexlet.code.SqliteDialect;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Расширение библиотеки снаружи.
 *
 * <p>Тест лежит в другом пакете нарочно: так видно, что условие и диалект пишутся извне, а
 * библиотека для этого не менялась. Здесь нет доступа к {@code SqlWriter.table()}, {@code
 * SqlWriter.compiled()} и проверкам схемы напрямую — только три публичных метода, которыми условие
 * рисует себя: {@link SqlWriter#column}, {@link SqlWriter#param} и {@link SqlWriter#sql}.
 *
 * <p>Так же поступает проверка Хекслета: своё условие снаружи библиотеки.
 */
class ExtensionFromOutsideTest {

  private static final Schema SCHEMA =
      Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));

  /** Своё условие с LIKE, написанное снаружи библиотеки. */
  static final class Like implements Condition {

    private final String column;
    private final String pattern;

    Like(String column, String pattern) {
      this.column = Objects.requireNonNull(column);
      this.pattern = Objects.requireNonNull(pattern);
    }

    @Override
    public void render(SqlWriter writer) {
      writer.column(column).sql(" LIKE ").param(pattern);
    }
  }

  @Test
  @DisplayName("Своё условие печатается параметрами в любом диалекте")
  void customConditionWorksInEveryDialect() {
    var condition = new Like("name", "r%");

    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE \"name\" LIKE $1",
        Sql.using(new PostgresDialect(), SCHEMA)
            .table("users")
            .select("id")
            .where(condition)
            .toSql()
            .sql());
    assertEquals(
        "SELECT `id` FROM `users` WHERE `name` LIKE ?",
        Sql.using(new MySqlDialect(), SCHEMA)
            .table("users")
            .select("id")
            .where(condition)
            .toSql()
            .sql());
    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE \"name\" LIKE ?",
        Sql.using(new SqliteDialect(), SCHEMA)
            .table("users")
            .select("id")
            .where(condition)
            .toSql()
            .sql());
  }

  @Test
  @DisplayName("Колонка в своём условии проверяется по схеме")
  void customConditionChecksColumn() {
    assertThrows(
        QueryException.class,
        () ->
            Sql.using(new PostgresDialect(), SCHEMA)
                .table("users")
                .select("id")
                .where(new Like("nope", "r%"))
                .toSql());
  }

  @Test
  @DisplayName("Своё условие вкладывается в and и or")
  void customConditionNests() {
    var users = Sql.using(new PostgresDialect(), SCHEMA).table("users");
    var query =
        users
            .select("id")
            .where(Conditions.and(new Like("name", "r%"), Conditions.eq("active", true)))
            .toSql();

    assertEquals(
        "SELECT \"id\" FROM \"users\" WHERE \"name\" LIKE $1 AND \"active\" = $2", query.sql());
    assertEquals(List.of("r%", true), query.params());

    var many =
        users
            .select("id")
            .where(Conditions.or(new Like("name", "r%"), Conditions.eq("age", 1)))
            .toSql();
    assertEquals("SELECT \"id\" FROM \"users\" WHERE \"name\" LIKE $1 OR \"age\" = $2", many.sql());
  }

  @Test
  @DisplayName("Свой диалект извне печатает и обычный запрос, и upsert")
  void customDialectFromOutside() {
    /** Свой диалект снаружи библиотеки: кавычки как в MySQL, upsert как в PostgreSQL. */
    class MixedDialect extends MySqlDialect {
      @Override
      public void formatUpsert(
          SqlWriter writer, List<String> conflictColumns, List<String> updateColumns) {
        writer.sql(" ON CONFLICT (");
        for (var i = 0; i < conflictColumns.size(); i++) {
          if (i > 0) {
            writer.sql(", ");
          }
          writer.column(conflictColumns.get(i));
        }
        writer.sql(") DO UPDATE SET ");
        for (var i = 0; i < updateColumns.size(); i++) {
          if (i > 0) {
            writer.sql(", ");
          }
          writer.column(updateColumns.get(i)).sql(" = EXCLUDED.").column(updateColumns.get(i));
        }
      }
    }

    var users = Sql.using(new MixedDialect(), SCHEMA).table("users");

    assertEquals(
        "SELECT `id` FROM `users` WHERE `age` > ?",
        users.select("id").where(Conditions.gt("age", 18)).toSql().sql());

    var upsert =
        users
            .insert()
            .value("email", "rob@example.com")
            .value("name", "rob")
            .onConflict("email")
            .doUpdate("name")
            .toSql();
    assertEquals(
        "INSERT INTO `users` (`email`, `name`) VALUES (?, ?)"
            + " ON CONFLICT (`email`) DO UPDATE SET `name` = EXCLUDED.`name`",
        upsert.sql());
    assertEquals(List.of("rob@example.com", "rob"), upsert.params());
  }

  @Test
  @DisplayName("Условие из библиотеки видно снаружи и печатается как прежде")
  void libraryConditionVisibleFromOutside() {
    var users = Sql.using(new PostgresDialect(), SCHEMA).table("users");
    var query =
        users
            .select("id")
            .where(Conditions.and(new hexlet.code.Between("age", 18, 65), new Like("name", "r%")))
            .toSql();

    assertEquals(
        "SELECT \"id\" FROM \"users\"" + " WHERE \"age\" BETWEEN $1 AND $2 AND \"name\" LIKE $3",
        query.sql());
    assertEquals(List.of(18, 65, "r%"), query.params());
  }
}
