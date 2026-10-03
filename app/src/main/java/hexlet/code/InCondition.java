package hexlet.code;

import java.util.List;
import java.util.Objects;

/** Вхождение в список значений: {@code "id" IN ($1, $2, $3)}. */
final class InCondition implements Condition {

  private final String column;
  private final List<Object> values;

  InCondition(String column, List<Object> values) {
    this.column = Objects.requireNonNull(column);
    this.values = List.copyOf(values);
  }

  @Override
  public void render(SqlWriter writer) {
    writer.column(column).sql(" IN (");
    for (var i = 0; i < values.size(); i++) {
      if (i > 0) {
        writer.sql(", ");
      }
      writer.param(values.get(i));
    }
    writer.sql(")");
  }
}
