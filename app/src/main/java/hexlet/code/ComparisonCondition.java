package hexlet.code;

import java.util.Objects;

/** Сравнение колонки со значением: {@code "id" = $1}. */
final class ComparisonCondition implements Condition {

  private final String column;
  private final String operator;
  private final Object value;

  ComparisonCondition(String column, String operator, Object value) {
    this.column = Objects.requireNonNull(column);
    this.operator = Objects.requireNonNull(operator);
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public void render(SqlWriter writer) {
    writer.column(column).sql(" " + operator + " ").param(value);
  }
}
