package hexlet.code;

import java.util.List;

/**
 * Диалект MySQL.
 *
 * <p>Отличается от стандартного SQL двумя шагами: имена берутся в обратные кавычки, а конфликт
 * разрешается через {@code ON DUPLICATE KEY UPDATE}.
 *
 * <p>MySQL сам находит нарушенный уникальный ключ, поэтому колонки конфликта в текст не попадают, а
 * вставляемой строке даётся имя {@code new}, по которому доступны её значения.
 */
public class MySqlDialect extends BaseDialect {

  /** Имя, под которым доступны значения вставляемой строки. */
  private static final String ALIAS = "new";

  @Override
  protected String quoteChar() {
    return "`";
  }

  @Override
  public void formatUpsert(
      SqlWriter writer, List<String> conflictColumns, List<String> updateColumns) {
    // conflictColumns не печатаются: MySQL определяет конфликт по уникальному ключу сам.
    writer.sql(" AS ").sql(ALIAS).sql(" ON DUPLICATE KEY UPDATE ");
    printAssignments(writer, updateColumns, ALIAS);
  }
}
