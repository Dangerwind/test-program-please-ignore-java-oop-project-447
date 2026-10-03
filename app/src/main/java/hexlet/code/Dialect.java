package hexlet.code;

import java.util.List;
import java.util.Map;

/**
 * Диалект SQL: знает, как запрос выглядит в конкретной базе данных.
 *
 * <p>Запрос хранит, что выбрать и откуда, а текст и значения собирает диалект. Поэтому новая база —
 * это новый класс, а код запросов не меняется.
 */
public interface Dialect {

  /**
   * Печатает начало запроса: что выбрать и откуда.
   *
   * @param writer писатель, в который пишется текст запроса и значения
   * @param table таблица, из которой выбираем
   * @param columns колонки; пустой список означает «все колонки»
   */
  void formatSelect(SqlWriter writer, String table, List<String> columns);

  /**
   * Печатает начало вставки: таблицу, колонки и их значения.
   *
   * @param values колонки и значения в порядке вызовов
   */
  void formatInsert(SqlWriter writer, String table, Map<String, Object> values);

  /**
   * Печатает хвост вставки с разрешением конфликта, то есть upsert.
   *
   * <p>Диалекты договорились по-разному: PostgreSQL называет колонки конфликта, а MySQL находит
   * нарушенный уникальный ключ сам и колонки не печатает. Поэтому аргумент {@code conflictColumns}
   * MySQL игнорирует.
   *
   * @param conflictColumns колонки уникального ключа в порядке вызовов
   * @param updateColumns колонки, которым достаются значения из вставляемой строки
   */
  void formatUpsert(SqlWriter writer, List<String> conflictColumns, List<String> updateColumns);

  /**
   * Печатает начало обновления: таблицу и присваивания.
   *
   * @param values колонки и значения в порядке вызовов
   */
  void formatUpdate(SqlWriter writer, String table, Map<String, Object> values);

  /** Печатает начало удаления: только имя таблицы. */
  void formatDelete(SqlWriter writer, String table);

  /**
   * Печатает условие запроса вместе со словом WHERE. Без условий ничего не печатает.
   *
   * @param conditions условия в порядке вызовов
   */
  void formatWhere(SqlWriter writer, List<Condition> conditions);

  /**
   * Печатает сортировку вместе со словами ORDER BY. Без колонок ничего не печатает.
   *
   * @param columns колонки в порядке вызовов
   */
  void formatOrderBy(SqlWriter writer, List<String> columns);

  /**
   * Печатает страницу результата: LIMIT и OFFSET, каждый только если он задан.
   *
   * @param limit сколько строк вернуть, либо {@code null}
   * @param offset сколько строк пропустить, либо {@code null}
   */
  void formatLimit(SqlWriter writer, Integer limit, Integer offset);

  /**
   * Оборачивает имя таблицы или колонки в кавычки диалекта.
   *
   * <p>Без кавычек база читает имя вроде {@code order} как ключевое слово и отказывается выполнять
   * запрос.
   */
  String quoteIdentifier(String name);

  /** Возвращает плейсхолдер для значения с номером {@code index}, нумерация с 1. */
  String placeholder(int index);
}
