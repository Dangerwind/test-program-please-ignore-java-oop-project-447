package hexlet.code;

import java.util.List;

/**
 * Готовый запрос: текст SQL и значения к плейсхолдерам.
 *
 * <p>Объект-значение: два запроса с одинаковым текстом и значениями равны.
 *
 * @param sql текст запроса
 * @param params значения плейсхолдеров в порядке их появления
 */
public record CompiledQuery(String sql, List<Object> params) {

  public CompiledQuery {
    params = List.copyOf(params);
  }
}
