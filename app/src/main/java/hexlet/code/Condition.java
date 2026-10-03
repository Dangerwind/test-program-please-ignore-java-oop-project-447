package hexlet.code;

/**
 * Условие запроса. Своё условие пишется реализацией этого интерфейса: интерфейс умеет только
 * отрисовать себя в писателе.
 */
public interface Condition {

  void render(SqlWriter writer);
}
