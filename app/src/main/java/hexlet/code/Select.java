package hexlet.code;

/** Неизменяемая выборка. Каждый метод, продолжающий запрос, возвращает новую выборку. */
public final class Select {

  Select() {}

  public Select where(Condition condition) {
    throw new UnsupportedOperationException();
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

  public CompiledQuery toSql() {
    throw new UnsupportedOperationException();
  }
}
