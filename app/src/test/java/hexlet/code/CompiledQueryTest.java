package hexlet.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompiledQueryTest {

  @Test
  @DisplayName("Запросы с одинаковым содержимым равны")
  void queriesWithSameContentAreEqual() {
    assertEquals(
        new CompiledQuery("SELECT 1", List.of()), new CompiledQuery("SELECT 1", List.of()));
  }

  @Test
  @DisplayName("Список значений копируется и не меняется извне")
  void paramsAreCopied() {
    var source = new ArrayList<Object>(List.of(1));
    var query = new CompiledQuery("SELECT $1", source);

    source.add(2);

    assertEquals(List.of(1), query.params());
    assertThrows(UnsupportedOperationException.class, () -> query.params().add(3));
  }
}
