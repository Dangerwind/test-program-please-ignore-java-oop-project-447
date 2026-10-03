package hexlet.code;

import java.util.List;

/**
 * Готовый запрос: текст SQL и список значений к плейсхолдерам.
 *
 * @param sql текст запроса
 * @param params значения плейсхолдеров в порядке их появления
 */
public record CompiledQuery(String sql, List<Object> params) {}
