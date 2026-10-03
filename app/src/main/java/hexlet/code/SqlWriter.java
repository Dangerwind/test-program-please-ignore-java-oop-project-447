package hexlet.code;

/**
 * Писатель SQL: собирает текст запроса и значения плейсхолдеров.
 *
 * <p>Диалект решает, как выглядит имя колонки и плейсхолдер, поэтому всё строится через методы
 * писателя, а не склейкой строк.
 */
public class SqlWriter {

  public SqlWriter column(String name) {
    throw new UnsupportedOperationException();
  }

  public SqlWriter param(Object value) {
    throw new UnsupportedOperationException();
  }

  public SqlWriter sql(String text) {
    throw new UnsupportedOperationException();
  }
}
