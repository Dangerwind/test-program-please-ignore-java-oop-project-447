# Конструктор SQL-запросов

[![hexlet-check](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/hexlet-check.yml)
[![CI](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/ci.yml/badge.svg)](https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447/actions/workflows/ci.yml)

Вы напишете библиотеку, которая строит SQL-запрос из вызовов методов и печатает его для PostgreSQL, MySQL и SQLite. Условия собираются из маленьких объектов, запросы неизменяемы, а новый диалект добавляется отдельным классом без правки остального кода.

Учебный проект Хекслета: https://ru.hexlet.io/programs/test-program-please-ignore-java-oop


## Стек

- Java 25
- Gradle 9.1 (wrapper)
- JUnit 6
- Spotless 8 + google-java-format 1.36.1

## Установка

Нужен JDK 25 и Gradle 9.1+.

```bash
git clone https://github.com/Dangerwind/test-program-please-ignore-java-oop-project-447.git
cd test-program-please-ignore-java-oop-project-447
```

## Использование

Gradle-проект лежит в каталоге `app/`. Все команды запускаются из него
через `make`:

```bash
cd app

make build    # сборка проекта
make test     # автоматические тесты
make lint     # проверка форматирования (Spotless)
make lint-fix # отформатировать код автоматически
```

Сборке не нужно настраивать JDK вручную. Компилятор берёт JDK 25 из
toolchain в `app/build.gradle.kts`, а сам Gradle запускается на JDK 25
благодаря `app/gradle/gradle-daemon-jvm.properties`. Если нужной версии
нет, Gradle скачает её сам. Это важно и для линтера: google-java-format
обращается к внутреннему API javac, поэтому версия форматтера должна
поддерживать ту JDK, на которой работает Gradle.

---

<details>
<summary>Автоматические тесты Хекслета</summary>

Тесты запускаются на каждый коммит. За запуск отвечает файл `.github/workflows/hexlet-check.yml` — не удаляйте и не переименовывайте ни его, ни репозиторий.

</details>

## О Хекслете

[Хекслет](https://ru.hexlet.io/) — школа программирования: авторские программы обучения с практикой, поддержкой наставников и реальными проектами, которые остаются в резюме. Этот репозиторий — один из таких проектов.
