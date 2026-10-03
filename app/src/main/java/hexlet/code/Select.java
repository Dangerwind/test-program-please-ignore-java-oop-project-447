package hexlet.code;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Неизменяемая выборка — строитель, который не меняет себя, а возвращает новый запрос.
 *
 * <p>Каждый метод, продолжающий запрос, возвращает новую выборку, поэтому от одного базового
 * запроса можно строить разные варианты, и они не мешают друг другу.
 *
 * <p>Запрос хранит, что выбрать, откуда, условия, сортировку и страницу. Текст собирает диалект,
 * проверяя имена по схеме на лету, а части запроса печатаются всегда в порядке SELECT, FROM, WHERE,
 * ORDER BY, LIMIT, OFFSET — независимо от порядка вызовов методов.
 */
public final class Select {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final List<String> columns;
  private final Where where;
  private final List<String> orderBy;
  private final Integer limit;
  private final Integer offset;

  Select(Dialect dialect, Schema schema, String table, List<String> columns) {
    this(dialect, schema, table, columns, Where.empty(), List.of(), null, null);
  }

  private Select(
      Dialect dialect,
      Schema schema,
      String table,
      List<String> columns,
      Where where,
      List<String> orderBy,
      Integer limit,
      Integer offset) {
    this.dialect = dialect;
    this.schema = schema;
    this.table = table;
    this.columns = List.copyOf(columns);
    this.where = where;
    this.orderBy = List.copyOf(orderBy);
    this.limit = limit;
    this.offset = offset;
  }

  /** Добавляет условие. Несколько вызовов соединяются через AND. */
  public Select where(Condition condition) {
    return new Select(
        dialect,
        schema,
        table,
        columns,
        where.and(Objects.requireNonNull(condition)),
        orderBy,
        limit,
        offset);
  }

  /** Добавляет колонку в сортировку. Повторные вызовы дописывают колонки. */
  public Select orderBy(String column) {
    return new Select(
        dialect,
        schema,
        table,
        columns,
        where,
        appended(orderBy, Objects.requireNonNull(column)),
        limit,
        offset);
  }

  /** Ограничивает число строк. Повторный вызов заменяет прежнее значение. */
  public Select limit(int count) {
    requireNonNegative(count, "LIMIT");
    return new Select(dialect, schema, table, columns, where, orderBy, count, offset);
  }

  /** Пропускает строки перед результатом. Повторный вызов заменяет прежнее значение. */
  public Select offset(int count) {
    requireNonNegative(count, "OFFSET");
    return new Select(dialect, schema, table, columns, where, orderBy, limit, count);
  }

  /** Печатает запрос диалектом, проверяя имена по схеме. */
  public CompiledQuery toSql() {
    if (offset != null && limit == null) {
      // limit() разрешено вызвать и после offset(), поэтому ошибка видна только тут.
      throw new QueryException("OFFSET без LIMIT такой запрос не принимается");
    }

    var writer = new SqlWriter(dialect, schema, table);
    dialect.formatSelect(writer, table, columns);
    where.render(writer);

    if (!orderBy.isEmpty()) {
      writer.sql(" ORDER BY ");
      for (var i = 0; i < orderBy.size(); i++) {
        if (i > 0) {
          writer.sql(", ");
        }
        writer.column(orderBy.get(i));
      }
    }

    if (limit != null) {
      writer.sql(" LIMIT " + limit);
    }
    if (offset != null) {
      writer.sql(" OFFSET " + offset);
    }

    return writer.compiled();
  }

  private static <T> List<T> appended(List<T> list, T element) {
    return Stream.concat(list.stream(), Stream.of(element)).toList();
  }

  private static void requireNonNegative(int count, String keyword) {
    if (count < 0) {
      throw new QueryException(keyword + " не может быть отрицательным: " + count);
    }
  }
}
