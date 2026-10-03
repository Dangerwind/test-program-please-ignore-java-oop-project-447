package hexlet.code;

/**
 * Таблица базы. Открывается через {@link Sql#table(String)} и служит точкой старта для всех видов
 * запросов.
 */
public final class Table {

  Table() {}

  public Select select(String... columns) {
    throw new UnsupportedOperationException();
  }

  public Insert insert() {
    throw new UnsupportedOperationException();
  }

  public Update update() {
    throw new UnsupportedOperationException();
  }

  public Delete delete() {
    throw new UnsupportedOperationException();
  }
}
