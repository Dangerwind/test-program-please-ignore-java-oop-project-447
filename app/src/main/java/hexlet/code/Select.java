package hexlet.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Неизменяемая выборка. Каждый метод, продолжающий запрос, возвращает новую выборку.
 *
 * <p>Запрос хранит, что выбрать, откуда и какие условия. Текст собирает диалект, проверяя имена по
 * схеме на лету.
 */
public final class Select {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final List<String> columns;
  private final List<Condition> conditions;

  Select(Dialect dialect, Schema schema, String table, List<String> columns) {
    this(dialect, schema, table, columns, List.of());
  }

  Select(
      Dialect dialect,
      Schema schema,
      String table,
      List<String> columns,
      List<Condition> conditions) {
    this.dialect = dialect;
    this.schema = schema;
    this.table = table;
    this.columns = columns;
    this.conditions = conditions;
  }

  /** Добавляет условие. Несколько вызовов соединяются через AND. */
  public Select where(Condition condition) {
    var added = new ArrayList<>(conditions);
    added.add(Objects.requireNonNull(condition));
    return new Select(dialect, schema, table, columns, List.copyOf(added));
  }

  public Select orderBy(String column) {
    throw new UnsupportedOperationException();
  }

  public Select limit(int count) {
    throw new UnsupportedOperationException();
  }

  public Select offset(int count) {
    throw new UnsupportedOperationException();
  }

  /** Печатает запрос диалектом, проверяя имена по схеме. */
  public CompiledQuery toSql() {
    var writer = new SqlWriter(dialect, schema, table);
    dialect.formatSelect(writer, table, columns);
    if (conditions.size() == 1) {
      // Одиночное условие печатаем как есть: AND вокруг него поставил бы лишние скобки.
      writer.sql(" WHERE ");
      conditions.getFirst().render(writer);
    } else if (conditions.size() > 1) {
      // Несколько вызовов where() соединяются через AND.
      writer.sql(" WHERE ");
      new AndCondition(conditions).render(writer);
    }
    return writer.compiled();
  }
}
