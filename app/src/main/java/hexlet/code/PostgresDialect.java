package hexlet.code;

import java.util.List;

/**
 * Диалект PostgreSQL.
 *
 * <p>Отличается от стандартного SQL двумя шагами: плейсхолдеры нумеруются, а конфликт разрешается
 * через {@code ON CONFLICT} со значениями из вставки под именем {@code EXCLUDED}.
 */
public class PostgresDialect extends BaseDialect {

  @Override
  protected String placeholderText(int index) {
    return "$" + index;
  }

  @Override
  public void formatUpsert(
      SqlWriter writer, List<String> conflictColumns, List<String> updateColumns) {
    writer.sql(" ON CONFLICT (");
    printColumns(writer, conflictColumns);
    writer.sql(") DO UPDATE SET ");
    printAssignments(writer, updateColumns, "EXCLUDED");
  }
}
