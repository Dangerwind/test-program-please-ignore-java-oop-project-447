package hexlet.code;

/** Неизменяемое удаление. Каждый метод, продолжающий запрос, возвращает новое удаление. */
public final class Delete {

  Delete() {}

  public Delete where(Condition condition) {
    throw new UnsupportedOperationException();
  }

  public Delete all() {
    throw new UnsupportedOperationException();
  }

  public CompiledQuery toSql() {
    throw new UnsupportedOperationException();
  }
}
