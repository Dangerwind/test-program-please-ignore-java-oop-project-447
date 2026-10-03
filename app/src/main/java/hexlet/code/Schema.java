package hexlet.code;

import java.util.List;
import java.util.Map;

/** Схема базы: имена таблиц и их колонок. */
public final class Schema {

  private Schema() {}

  public static Schema of(Map<String, List<String>> tables) {
    throw new UnsupportedOperationException();
  }
}
