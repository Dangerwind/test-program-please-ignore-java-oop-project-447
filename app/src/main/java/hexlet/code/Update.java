package hexlet.code;

/** Неизменяемое обновление. Каждый метод, продолжающий запрос, возвращает новое обновление. */
public final class Update {

  Update() {}

  public Update set(String column, Object value) {
    throw new UnsupportedOperationException();
  }

  public Update where(Condition condition) {
    throw new UnsupportedOperationException();
  }

  public Update all() {
    throw new UnsupportedOperationException();
  }

  public CompiledQuery toSql() {
    throw new UnsupportedOperationException();
  }
}
