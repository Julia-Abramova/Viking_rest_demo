# Viking Demo

Минимальный демонстрационный Maven-проект для практического занятия по связке **Spring Web + REST API + Swagger/OpenAPI + Spring JDBC + H2**.

Проект показывает переход от хранения объектов в памяти приложения к сохранению данных в файловую реляционную БД H2 через Spring JDBC.

## Что делает приложение

Приложение запускает:

1. **GUI на Swing** с кнопкой **Create random viking**.
2. **Таблицу**, где отображаются созданные викинги.
3. **REST API** для получения списка созданных викингов.
4. **Swagger UI** для просмотра и тестирования API.
5. **Файловую БД H2**, в которой сохраняются созданные викинги и их снаряжение.
6. **H2 Console** для просмотра таблиц и выполнения SQL-запросов через браузер.

В начале работы список может быть пустым. Каждый клик по кнопке создаёт нового случайного викинга, после чего данные сохраняются в БД.

## Модель викинга

У викинга есть:

- `name`
- `age`
- `heightCm`
- `hairColor` (`enum`)
- `beardStyle` (`enum`)
- `equipment` (`List<EquipmentItem>`)

У предмета снаряжения есть:

- `name`
- `quality`

В REST-модели викинг представлен как объект с вложенным списком снаряжения. В БД эти данные хранятся в двух таблицах:

- `vikings`
- `equipment_items`

Связь между ними организована через внешний ключ `equipment_items.viking_id`.

## Технологии

- Java 24
- Maven
- Spring Boot
- Spring Web MVC
- Spring JDBC
- H2 Database
- springdoc-openapi + Swagger UI
- DataFaker
- Swing

## Сборка и запуск

Запуск через Maven:

```bash
mvn clean spring-boot:run
```

Сборка jar-файла:

```bash
mvn clean package
```

Запуск собранного jar-файла:

```bash
java -jar target/viking-demo-1.0.0.jar
```

После запуска приложение будет доступно на порту `8080`.

## REST API

Получить список созданных викингов:

```http
GET http://localhost:8080/api/vikings
```

Создать конкретного викинга:

```http
POST http://localhost:8080/api/vikings
Content-Type: application/json
```

```json
{
  "name": "Bjorn",
  "age": 31,
  "heightCm": 184,
  "hairColor": "Blond",
  "beardStyle": "BRAIDED",
  "equipment": [
    {
      "name": "Iron Axe",
      "quality": "Rare"
    }
  ]
}
```

Удалить викинга:

```bash
curl -X "DELETE" "http://localhost:8080/api/vikings/1" -H "accept: */*"
```

Перезаписать параметры викинга:

```bash
curl -X "PUT" "http://localhost:8080/api/vikings/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Ivar","age":28,"heightCm":176,"hairColor":"Brown","beardStyle":"SHORT","equipment":[{"name":"Sword","quality":"Legendary"}]}'
```

## Swagger UI

Swagger UI доступен по адресу:

```text
http://localhost:8080/swagger-ui.html
```

Через Swagger UI можно просматривать и тестировать REST API приложения.

## H2 Console

H2 Console доступна по адресу:

```text
http://localhost:8080/h2-console
```

Именно этот URL нужно открыть в браузере после запуска приложения.

На странице подключения к H2 Console нужно указать параметры подключения к той же БД, которую использует приложение.

Логин: **viking_user**
Пароль: **viking_password**

Важно: значение поля **JDBC URL** в H2 Console должно совпадать со значением `spring.datasource.url` в `application.yaml`.

В `application.yaml` используется относительный путь:

```yaml
spring:
  datasource:
    url: jdbc:h2:./data/viking-demo-db;AUTO_SERVER=TRUE
```

в H2 Console нужно указать тот же URL:

```text
jdbc:h2:./data/viking-demo-db
```

Расширение файла `.mv.db` в JDBC URL не указывается. Например, если на диске создан файл:

## Файловая БД H2

Приложение использует файловую БД H2. При первом запуске в каталоге `data` создаётся файл БД, например:

```text
data/viking-demo-db.mv.db
```

Файл БД создаётся автоматически, если в `application.yaml` указан файловый JDBC URL:

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/viking-demo-db;AUTO_SERVER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

Таблицы создаются через SQL-скрипт `schema.sql`, расположенный в каталоге:

```text
src/main/resources/schema.sql
```

Для автоматического выполнения SQL-скрипта при запуске приложения используется настройка:

```yaml
spring:
  sql:
    init:
      mode: always
```

## Пример SQL-запросов в H2 Console

Посмотреть всех викингов:

```sql
select * from vikings;
```

Посмотреть всё снаряжение:

```sql
select * from equipment_items;
```

Посмотреть викингов вместе со снаряжением:

```sql
select
    v.id,
    v.name,
    v.age,
    v.height_cm,
    v.hair_color,
    v.beard_style,
    v.description,
    e.name as equipment_name,
    e.quality as equipment_quality
from vikings v
left join equipment_items e on e.viking_id = v.id
order by v.id, e.id;
```
