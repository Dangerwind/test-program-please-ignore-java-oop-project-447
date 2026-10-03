package hexlet.code;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Условия запроса и осознанный выбор «вся таблица».
 *
 * <p>Хранит условия в порядке вызовов. Печатать их не бдет: как именно выглядит условие, решает
 * диалект, поэтому класс только помнит, какие условия вызвали и разрешено ли работать со всей
 * таблицей.
 *
 * <p>UPDATE и DELETE без условия затрагивают всю таблицу, поэтому для них пустой набор условий
 * означает отказ: такой запрос строится только через all().
 */
final class Where {

  private static final Where EMPTY = new Where(List.of(), false);

  private final List<Condition> conditions;
  private final boolean all;

  private Where(List<Condition> conditions, boolean all) {
    this.conditions = conditions;
    this.all = all;
  }

  static Where empty() {
    return EMPTY;
  }

  /** Присоединяет условие через AND. */
  Where and(Condition condition) {
    var added = Stream.concat(conditions.stream(), Stream.of(Objects.requireNonNull(condition)));
    return new Where(added.toList(), all);
  }

  /** Явно разрешает запрос без условия. */
  Where allRows() {
    return new Where(conditions, true);
  }

  /** Условия в порядке вызовов. */
  List<Condition> conditions() {
    return conditions;
  }

  /** Пусто ли условие: нет ни условий, ни явного намерения работать со всей таблицей. */
  boolean isEmpty() {
    return conditions.isEmpty() && !all;
  }
}
