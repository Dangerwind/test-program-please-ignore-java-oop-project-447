package hexlet.code;

/** Точка входа в библиотеку: связывает диалект со схемой и открывает таблицы. */
public final class Sql {

  private Sql() {}

  public static Sql using(Dialect dialect, Schema schema) {
    throw new UnsupportedOperationException();
  }

  public Table table(String name) {
    throw new UnsupportedOperationException();
  }
}
