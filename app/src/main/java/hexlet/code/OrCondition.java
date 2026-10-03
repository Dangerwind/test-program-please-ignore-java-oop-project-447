package hexlet.code;

import java.util.List;

/** Соединение условий через OR: {@code "a" = $1 OR "b" = $2}. */
final class OrCondition extends CompositeCondition {

  OrCondition(List<Condition> conditions) {
    super(conditions);
  }

  @Override
  public void render(SqlWriter writer) {
    var first = true;
    for (var condition : conditions()) {
      if (!first) {
        writer.sql(" OR ");
      }
      // Вложенный AND печатается без скобок: он и так связывает сильнее OR.
      condition.render(writer);
      first = false;
    }
  }
}
