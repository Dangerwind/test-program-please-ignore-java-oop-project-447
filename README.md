# Конструктор SQL-запросов

[![hexlet-check](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/hexlet-check.yml)
[![CI](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/ci.yml/badge.svg)](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/ci.yml)

Библиотека строит SQL-запрос из вызовов методов и печатает его для PostgreSQL, MySQL и SQLite. Условия собираются из маленьких объектов, запросы неизменяемы, а новый диалект и новое условие добавляются отдельными классами без правки остального кода.

Учебный проект Хекслета: https://ru.hexlet.io/programs/test-program-please-ignore-java-oop

## Стек

- Java 25
- Gradle 9.1 (wrapper)
- JUnit 6
- Spotless 8 + google-java-format 1.36.1

## Установка

Нужны только `git` и `make` (на macOS `make` ставится через `xcode-select --install`). Gradle
скачается через wrapper, а JDK 25 подтянется автоматически.

```bash
git clone https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447.git
cd test-program-please-ignore-java-oop-project-447
```

## Использование

Библиотека собирает SQL-запрос из вызовов методов, отдаёт текст и список значений к
плейсхолдерам. Значения в текст не попадают: запрос уходит в базу параметрами.

```java
var schema = Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));
var db = Sql.using(new PostgresDialect(), schema);
var users = db.table("users");

var query = users.select("name", "age").where(eq("active", true)).where(gt("age", 18)).toSql();

query.sql();
// SELECT "name", "age" FROM "users" WHERE "active" = $1 AND "age" > $2
query.params();
// [true, 18]
```

### Выборка

```java
users.select();                       // SELECT * FROM "users"
users.select("name");                 // SELECT "name" FROM "users"
users.orderBy("name").limit(10);      // ... ORDER BY "name" LIMIT 10
users.offset(20).limit(10);           // ... LIMIT 10 OFFSET 20
```

`OFFSET` без `LIMIT` база не выполняет, поэтому такая просьба отклоняется. Части запроса печатаются
всегда в порядке `SELECT`, `FROM`, `WHERE`, `ORDER BY`, `LIMIT`, `OFFSET` — независимо от порядка
вызовов.

### Условия

Готовые условия лежат в `Conditions`, соединяются через `and()` и `or()`, отрицаются через `not()`:

```java
eq("id", 1)                          // "id" = $1
ne("id", 1)                          // "id" <> $1
gt("age", 18)                        // "age" > $1
lt("age", 18)                        // "age" < $1
in("id", 1, 2, 3)                    // "id" IN ($1, $2, $3)
isNull("email")                      // "email" IS NULL
and(eq("active", true), gt("age", 18))
or(eq("id", 1), eq("id", 2))
not(eq("active", true))
```

### Вставка, обновление и удаление

```java
users.insert().value("name", "rob").value("age", 30).toSql();
// INSERT INTO "users" ("name", "age") VALUES ($1, $2)

users.update().set("name", "ann").where(eq("id", 1)).toSql();
// UPDATE "users" SET "name" = $1 WHERE "id" = $2

users.delete().where(lt("age", 18)).toSql();
// DELETE FROM "users" WHERE "age" < $1
```

Колонки печатаются в порядке вызовов, а не в алфавитном. Значение `null` уходит параметром, потому
что записать `NULL` в колонку — обычная операция.

`UPDATE` и `DELETE` без условия затрагивают всю таблицу. Такая ошибка означает не опечатку, а
возможно забытое условие, поэтому она лежит в отдельном подклассе `QueryWithoutConditionException`.
Всё вместе ловится одним `QueryException`. Работа со всей таблицей требует явного намерения:

```java
users.delete().toSql();               // QueryWithoutConditionException
users.delete().all().toSql();         // DELETE FROM "users"
```

### Upsert

Вставка с уже существующим значением уникального ключа вместо ошибки обновляет строку.
`onConflict()` называет колонки ключа, `doUpdate()` — колонки, которым достаются значения из
вставляемой строки:

```java
users.insert()
    .value("email", "rob@example.com")
    .value("name", "rob")
    .onConflict("email")
    .doUpdate("name")
    .toSql();
// INSERT INTO "users" ("email", "name") VALUES ($1, $2)
//   ON CONFLICT ("email") DO UPDATE SET "name" = EXCLUDED."name"
```

Два метода ходят парой: по одному конфликт или обновление бессмысленны.

### Диалекты

Код приложения не меняется — меняется диалект, переданный в `Sql.using()`:

```java
new BaseDialect();     // SELECT "name" FROM "users" WHERE "id" = ?
new PostgresDialect(); // SELECT "name" FROM "users" WHERE "id" = $1
new MySqlDialect();    // SELECT `name` FROM `users` WHERE `id` = ?
new SqliteDialect();   // SELECT "name" FROM "users" WHERE "id" = ?
```

Общая раскладка лежит в `BaseDialect`, а подклассы переопределяют только отличия: кавычки,
плейсхолдеры и upsert. Раскладка помечена `final`, поэтому диалект не может переписать её целиком.

### Своё условие

Новый оператор добавляется снаружи библиотеки, без её правки. Условие реализует `Condition` и
рисует себя через три публичных метода `SqlWriter`: `column()`, `param()` и `sql()`. Колонка
проверяется по схеме, значения уходят параметрами в любом диалекте:

```java
public final class Between implements Condition {

    public Between(String column, Object from, Object to) { ... }

    @Override
    public void render(SqlWriter writer) {
        writer.column(column).sql(" BETWEEN ").param(from).sql(" AND ").param(to);
    }
}
```

`Between` входит в библиотеку, а `LIKE` и свой диалект можно писать где угодно:

```java
postgres.select("id").where(new Between("age", 18, 65)).toSql();
// SELECT "id" FROM "users" WHERE "age" BETWEEN $1 AND $2

var adults = and(new Between("age", 18, 65), eq("active", true));
mysql.delete().where(adults).toSql();
// DELETE FROM `users` WHERE `age` BETWEEN ? AND ? AND `active` = ?
```

Тест `app/src/test/java/outside/ExtensionFromOutsideTest.java` лежит в другом пакете нарочно: он
показывает, что расширение действительно снаружи. Тесты `BetweenTest` и `SqliteDialectTest` — внутри.

### Неизменяемость

Запросы неизменяемы: каждый метод возвращает новый запрос, а исходный остаётся прежним. От одного
базового запроса можно строить ветки, и они не мешают друг другу:

```java
var adults = users.select("id", "name").where(gt("age", 18));

adults.toSql();                                  // SELECT "id", "name" ... WHERE "age" > $1
adults.where(eq("active", true)).toSql();        // та же выборка плюс условие
adults.orderBy("name").limit(5).toSql();         // та же выборка плюс страница
```

## Разработка

Gradle-проект лежит в каталоге `app/`. Все команды запускаются из него
через `make`:

```bash
cd app

make build    # сборка проекта
make test     # автоматические тесты
make lint     # проверка форматирования (Spotless)
make lint-fix # отформатировать код автоматически
```

Свои тесты лежат в `app/src/test/java/hexlet/code`. Тесты Хекслета запускаются на
каждый коммит: за это отвечает файл `.github/workflows/hexlet-check.yml` — не удаляйте
и не переименовывайте ни его, ни репозиторий.

Сборке не нужно настраивать JDK вручную. Компилятор берёт JDK 25 из
toolchain в `app/build.gradle.kts`, а сам Gradle запускается на JDK 25
благодаря `app/gradle/gradle-daemon-jvm.properties`. Если нужной версии
нет, Gradle скачает её сам. Это важно и для линтера: google-java-format
обращается к внутреннему API javac, поэтому версия форматтера должна
поддерживать ту JDK, на которой работает Gradle.

## О Хекслете

[Хекслет](https://ru.hexlet.io/) — школа программирования: авторские программы обучения с практикой, поддержкой наставников и реальными проектами, которые остаются в резюме. Этот репозиторий — один из таких проектов.
