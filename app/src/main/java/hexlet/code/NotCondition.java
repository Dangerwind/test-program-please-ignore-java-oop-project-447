package hexlet.code;

import java.util.Objects;

/** Отрицание: {@code NOT ("email" IS NULL)}. */
final class NotCondition implements Condition {

  private final Condition condition;

  NotCondition(Condition condition) {
    this.condition = Objects.requireNonNull(condition);
  }

  @Override
  public void render(SqlWriter writer) {
    // NOT всегда берёт своё условие в скобки, даже простое.
    writer.sql("NOT (");
    condition.render(writer);
    writer.sql(")");
  }
}
