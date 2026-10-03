package hexlet.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Готовый запрос: текст SQL и значения к плейсхолдерам.
 *
 * <p>Объект-значение: два запроса с одинаковым текстом и значениями равны.
 *
 * @param sql текст запроса
 * @param params значения плейсхолдеров в порядке их появления; null здесь означает SQL NULL,
 *     поэтому список копируется через unmodifiableList, а не List.copyOf
 */
public record CompiledQuery(String sql, List<Object> params) {

  public CompiledQuery {
    params = Collections.unmodifiableList(new ArrayList<>(params));
  }
}
