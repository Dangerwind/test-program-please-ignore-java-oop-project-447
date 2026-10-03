package hexlet.code;

/** Неизменяемая вставка. Каждый метод, продолжающий запрос, возвращает новую вставку. */
public final class Insert {

  Insert() {}

  public Insert value(String column, Object value) {
    throw new UnsupportedOperationException();
  }

  public Insert onConflict(String... columns) {
    throw new UnsupportedOperationException();
  }

  public Insert doUpdate(String... columns) {
    throw new UnsupportedOperationException();
  }

  public CompiledQuery toSql() {
    throw new UnsupportedOperationException();
  }
}
