package hexlet.code;

/**
 * Ошибка при работе с запросом: неизвестная таблица или колонка, неверное значение и прочие отказы
 * библиотеки.
 */
public class QueryException extends RuntimeException {

  public QueryException(String message) {
    super(message);
  }
}
