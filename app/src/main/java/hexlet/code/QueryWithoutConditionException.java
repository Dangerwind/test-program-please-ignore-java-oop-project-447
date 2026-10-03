package hexlet.code;

/**
 * Запрос без условия, который затрагивает всю таблицу.
 *
 * <p>Отдельный подкласс, потому что такая ошибка означает не опечатку, а возможно забытое условие.
 * Приложение может поймать именно её, а все ошибки библиотеки разом — через {@link QueryException}.
 */
public class QueryWithoutConditionException extends QueryException {

  public QueryWithoutConditionException(String message) {
    super(message);
  }
}
