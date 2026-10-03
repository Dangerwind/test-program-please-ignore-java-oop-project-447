package hexlet.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Писатель SQL: собирает текст запроса и значения плейсхолдеров.
 *
 * <p>Диалект решает, как выглядит имя колонки и плейсхолдер, поэтому всё строится через методы
 * писателя, а не склейкой строк.
 *
 * <p>Публичные методы предназначены условиям: своё условие пишется лямбдой, которая получает
 * писателя и зовёт эти три метода. Всё, что нужно самой библиотеке, не имеет модификатора доступа и
 * из чужого пакета не видно.
 */
public class SqlWriter {

  private final Dialect dialect;
  private final Schema schema;
  private final String table;
  private final StringBuilder sql = new StringBuilder();
  private final List<Object> params = new ArrayList<>();

  SqlWriter(Dialect dialect, Schema schema, String table) {
    this.dialect = Objects.requireNonNull(dialect);
    this.schema = Objects.requireNonNull(schema);
    this.table = Objects.requireNonNull(table);
  }

  /** Проверяет колонку по схеме и печатает её в кавычках диалекта. */
  public SqlWriter column(String name) {
    schema.requireColumn(table, Objects.requireNonNull(name));
    return sql(dialect.quoteIdentifier(name));
  }

  /** Добавляет значение в параметры и печатает плейсхолдер диалекта. */
  public SqlWriter param(Object value) {
    params.add(value);
    return sql(dialect.placeholder(params.size()));
  }

  /** Печатает текст как есть. */
  public SqlWriter sql(String text) {
    sql.append(Objects.requireNonNull(text));
    return this;
  }

  /** Проверяет таблицу по схеме и печатает её в кавычках диалекта. */
  SqlWriter table(String name) {
    schema.requireTable(Objects.requireNonNull(name));
    return sql(dialect.quoteIdentifier(name));
  }

  /** Собирает готовый запрос из написанного. */
  CompiledQuery compiled() {
    return new CompiledQuery(sql.toString(), params);
  }
}
