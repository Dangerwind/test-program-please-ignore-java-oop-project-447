package hexlet.code;

import java.util.List;
import java.util.Objects;

/**
 * Составное условие: хранит вложенные условия и печатает их через {@link
 * Condition#render(SqlWriter)}.
 *
 * <p>Сам тип условия тоже реализует {@link Condition}, поэтому составное условие можно вложить в
 * следующее.
 */
abstract class CompositeCondition implements Condition {

  private final List<Condition> conditions;

  CompositeCondition(List<Condition> conditions) {
    this.conditions = List.copyOf(Objects.requireNonNull(conditions));
  }

  final List<Condition> conditions() {
    return conditions;
  }
}
