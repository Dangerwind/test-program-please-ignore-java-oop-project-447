package hexlet.code;

import java.util.Objects;

/** Проверка на пустое значение: {@code "email" IS NULL}. */
final class IsNullCondition implements Condition {

  private final String column;

  IsNullCondition(String column) {
    this.column = Objects.requireNonNull(column);
  }

  @Override
  public void render(SqlWriter writer) {
    writer.column(column).sql(" IS NULL");
  }
}
