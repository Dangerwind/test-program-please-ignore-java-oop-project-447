package hexlet.code;

/**
 * Неизменяемое удаление. Каждый метод, продолжающий запрос, возвращает новое удаление.
 *
 * <p>Удаление без условий затрагивает всю таблицу, поэтому требует явного all().
 */
public final class Delete {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final Where where;

  Delete(Dialect dialect, Schema schema, String table) {
    this(dialect, schema, table, Where.empty());
  }

  private Delete(Dialect dialect, Schema schema, String table, Where where) {
    this.dialect = dialect;
    this.schema = schema;
    this.table = table;
    this.where = where;
  }

  /** Добавляет условие. Несколько вызовов соединяются через AND. */
  public Delete where(Condition condition) {
    return new Delete(dialect, schema, table, where.and(condition));
  }

  /** Явно разрешает удалить всю таблицу. */
  public Delete all() {
    return new Delete(dialect, schema, table, where.allRows());
  }

  /** Печатает удаление. */
  public CompiledQuery toSql() {
    if (where.isEmpty()) {
      throw new QueryWithoutConditionException(
          "DELETE без условия удаляет всю таблицу, позовите all(), если это нужно");
    }
    var writer = new SqlWriter(dialect, schema, table);
    dialect.formatDelete(writer, table);
    dialect.formatWhere(writer, where.conditions());
    return writer.compiled();
  }
}
