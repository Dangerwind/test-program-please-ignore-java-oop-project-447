package hexlet.code;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Схема базы: имена таблиц и их колонок.
 *
 * <p>Схема нужна библиотеке, чтобы проверять имена до того, как запрос уйдёт в базу.
 */
public final class Schema {

  private final Map<String, List<String>> tables;

  private Schema(Map<String, List<String>> tables) {
    this.tables = tables;
  }

  /**
   * Создаёт схему из описания таблиц.
   *
   * @param tables имя таблицы и список её колонок
   */
  public static Schema of(Map<String, List<String>> tables) {
    var copy = new LinkedHashMap<String, List<String>>();
    for (var entry : tables.entrySet()) {
      copy.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return new Schema(Map.copyOf(copy));
  }

  /** Бросает {@link QueryException}, если таблицы нет в схеме. */
  void requireTable(String name) {
    if (!tables.containsKey(name)) {
      throw new QueryException("В схеме нет таблицы: " + name);
    }
  }

  /** Бросает {@link QueryException}, если таблицы нет или в ней нет такой колонки. */
  void requireColumn(String table, String column) {
    requireTable(table);
    if (!tables.get(table).contains(column)) {
      throw new QueryException("В таблице " + table + " нет колонки: " + column);
    }
  }
}
