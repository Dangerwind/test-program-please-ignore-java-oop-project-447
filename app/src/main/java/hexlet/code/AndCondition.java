package hexlet.code;

import java.util.List;

/** Соединение условий через AND: {@code "a" = $1 AND "b" = $2}. */
final class AndCondition extends CompositeCondition {

  AndCondition(List<Condition> conditions) {
    super(conditions);
  }

  @Override
  public void render(SqlWriter writer) {
    var first = true;
    for (var condition : conditions()) {
      if (!first) {
        writer.sql(" AND ");
      }
      // AND связывает сильнее OR, поэтому вложенный OR без скобок изменил бы смысл.
      if (condition instanceof OrCondition) {
        writer.sql("(");
        condition.render(writer);
        writer.sql(")");
      } else {
        condition.render(writer);
      }
      first = false;
    }
  }
}
