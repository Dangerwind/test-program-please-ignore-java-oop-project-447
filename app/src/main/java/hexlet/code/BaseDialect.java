package hexlet.code;

import java.util.List;
import java.util.Map;

/**
 * Стандартный SQL: имена в двойных кавычках, на месте значения знак {@code ?}.
 *
 * <p>Здесь живёт вся печать запросов: их порядок, запятые, пробелы, скобки условий, {@code ORDER
 * BY}, {@code LIMIT} и {@code OFFSET}. Модель запроса про SQL не знает — она только хранит, что
 * выбрано и какие условия заданы, и отдаёт это сюда.
 *
 * <p>Подкласс меняет отдельные шаги — {@link #quoteChar()}, {@link #placeholderText(int)} и {@link
 * #formatUpsert} — и не трогает раскладку целиком. Иначе правку пришлось бы повторять в каждом
 * диалекте.
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
   * Печатает условие вместе со словом WHERE.
   *
   * <p>Одиночное условие печатается как есть: AND вокруг него поставил бы лишние скобки. Несколько
   * условий соединяются через AND, а вложенный OR берётся в скобки.
   */
  @Override
  public final void formatWhere(SqlWriter writer, List<Condition> conditions) {
    if (conditions.isEmpty()) {
      return;
    }
    writer.sql(" WHERE ");
    if (conditions.size() == 1) {
      conditions.getFirst().render(writer);
    } else {
      new AndCondition(conditions).render(writer);
    }
  }

  @Override
  public final void formatOrderBy(SqlWriter writer, List<String> columns) {
    if (columns.isEmpty()) {
      return;
    }
    writer.sql(" ORDER BY ");
    printColumns(writer, columns);
  }

  @Override
  public final void formatLimit(SqlWriter writer, Integer limit, Integer offset) {
    if (limit != null) {
      writer.sql(" LIMIT " + limit);
    }
    if (offset != null) {
      writer.sql(" OFFSET " + offset);
    }
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
