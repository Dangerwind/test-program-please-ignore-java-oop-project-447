package hexlet.code;

import java.util.List;

/**
 * Диалект SQLite.
 *
 * <p>Кавычки двойные, плейсхолдер {@code ?} — как в стандартном SQL, поэтому из {@link BaseDialect}
 * наследуется всё, кроме upsert.
 *
 * <p>Upsert в SQLite записан так же, как в PostgreSQL: {@code ON CONFLICT} со значениями из
 * вставляемой строки под именем {@code EXCLUDED}.
 */
public class SqliteDialect extends BaseDialect {

  @Override
  public void formatUpsert(
      SqlWriter writer, List<String> conflictColumns, List<String> updateColumns) {
    writer.sql(" ON CONFLICT (");
    printColumns(writer, conflictColumns);
    writer.sql(") DO UPDATE SET ");
    printAssignments(writer, updateColumns, "EXCLUDED");
  }
}
