package hexlet.code;

import java.util.List;
import java.util.Map;

/**
 * Стандартный SQL: имена в двойных кавычках, на месте значения знак {@code ?}.
 *
 * <p>Здесь живёт общая раскладка запросов: их порядок, запятые, пробелы. Подкласс меняет только
 * отдельные шаги — {@link #quoteChar()} и {@link #formatUpsert} — и не трогает раскладку целиком.
 * Иначе правку пришлось бы повторять в каждом диалекте.
 *
 * <p>Раскладка помечена {@code final}: подкласс сможет подменить шаг, но не всю печать, поэтому все
 * диалекты остаются одинаковыми в том, что не различается.
 *
 * <p>В стандартном SQL upsert не определён, поэтому {@link #formatUpsert} бросает {@link
 * QueryException}.
 */
public class BaseDialect implements Dialect {

  @Override
  public final void formatSelect(SqlWriter writer, String table, List<String> columns) {
    writer.sql("SELECT ");
    if (columns.isEmpty()) {
      writer.sql("*");
    }
    for (var i = 0; i < columns.size(); i++) {
      if (i > 0) {
        writer.sql(", ");
      }
      writer.column(columns.get(i));
    }
    writer.sql(" FROM ").table(table);
  }

  @Override
  public final void formatInsert(SqlWriter writer, String table, Map<String, Object> values) {
    writer.sql("INSERT INTO ").table(table).sql(" (");
    printColumns(writer, values.keySet());
    writer.sql(") VALUES (");
    var first = true;
    for (var value : values.values()) {
      if (!first) {
        writer.sql(", ");
      }
      writer.param(value);
      first = false;
    }
    writer.sql(")");
  }

  @Override
  public final void formatUpdate(SqlWriter writer, String table, Map<String, Object> values) {
    writer.sql("UPDATE ").table(table).sql(" SET ");
    var first = true;
    for (var entry : values.entrySet()) {
      if (!first) {
        writer.sql(", ");
      }
      writer.column(entry.getKey()).sql(" = ").param(entry.getValue());
      first = false;
    }
  }

  @Override
  public final void formatDelete(SqlWriter writer, String table) {
    writer.sql("DELETE FROM ").table(table);
  }

  /**
   * В стандартном SQL upsert нет, поэтому вызывать его здесь незачем.
   *
   * @throws QueryException всегда
   */
  @Override
  public void formatUpsert(
      SqlWriter writer, List<String> conflictColumns, List<String> updateColumns) {
    throw new QueryException("В стандартном SQL upsert не определён, нужен конкретный диалект");
  }

  @Override
  public final String quoteIdentifier(String name) {
    return quoteChar() + name + quoteChar();
  }

  @Override
  public final String placeholder(int index) {
    return placeholderText(index);
  }

  /**
   * Знак кавычек диалекта. MySQL меняет его на обратные кавычки.
   *
   * <p>Публичный {@link #quoteIdentifier} собирает имя из этого знака и помечен {@code final},
   * поэтому подкласс меняет только кавычку, а не всю сборку имени.
   */
  protected String quoteChar() {
    return "\"";
  }

  /**
   * Плейсхолдер диалекта. Стандартный SQL и MySQL ставят знак вопроса, PostgreSQL нумерует
   * параметры.
   *
   * <p>Публичный {@link #placeholder} отдан подклассу шагом, а сам помечен {@code final}.
   */
  protected String placeholderText(int index) {
    return "?";
  }

  /** Печатает колонки через запятую. */
  protected final void printColumns(SqlWriter writer, Iterable<String> columns) {
    var first = true;
    for (var column : columns) {
      if (!first) {
        writer.sql(", ");
      }
      writer.column(column);
      first = false;
    }
  }

  /** Печатает «колонка = псевдоним.колонка» через запятую — тело upsert у всех диалектов. */
  protected final void printAssignments(SqlWriter writer, List<String> columns, String alias) {
    var first = true;
    for (var column : columns) {
      if (!first) {
        writer.sql(", ");
      }
      writer.column(column).sql(" = ").sql(alias).sql(".").column(column);
      first = false;
    }
  }
}
