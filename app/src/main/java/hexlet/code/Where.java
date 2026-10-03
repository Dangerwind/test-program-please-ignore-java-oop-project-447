package hexlet.code;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Условия запроса и осознанный выбор «вся таблица».
 *
 * <p>Хранит условия в порядке вызовов и печатает их по правилам скобок: одиночное условие как есть,
 * несколько соединяются через AND, а вложенный OR берётся в скобки.
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

  /** Пусто ли условие: нет ни условий, ни явного намерения работать со всей таблицей. */
  boolean isEmpty() {
    return conditions.isEmpty() && !all;
  }

  /** Печатает WHERE с условиями. Без условий ничего не печатает. */
  void render(SqlWriter writer) {
    if (conditions.isEmpty()) {
      return;
    }
    writer.sql(" WHERE ");
    if (conditions.size() == 1) {
      // Одиночное условие печатаем как есть: AND вокруг него поставил бы лишние скобки.
      conditions.getFirst().render(writer);
    } else {
      new AndCondition(conditions).render(writer);
    }
  }
}
