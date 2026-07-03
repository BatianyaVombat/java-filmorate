package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {
    private static HttpClient client;

    @BeforeAll
    static void setUp() {
        client = HttpClient.newHttpClient();
    }

    @BeforeEach
    void setUpEach() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/test/reset"))
                .DELETE()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    //---------------Проверка User контроллера--------------
    @Test
    @DisplayName("Возвращает пустой ответ, потому что пользователи ещё не добавлены")
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[]", response.body().trim()); // пустой JSON-массив
    }

    @Test
    @DisplayName("Должен вернуть 400 если тело запроса на создание пользователя пустое")
    void shouldReturn400ForEmptyBodyOnUserCreation() throws IOException, InterruptedException {
        String jsonBody = "";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(400, response.statusCode());

        String responseBody = response.body();
        Assertions.assertTrue(responseBody.contains("error"), "Тело ответа должно содержать поле 'error'");
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    void shouldCreateUserIfFieldsValid() throws Exception {
        String jsonBody = """
                {
                    "id": 1,
                    "login": "BatyaniaVombat",
                    "name": "Name",
                    "email" : "alex.strange@yandex.ru",
                    "birthday": "1993-06-20"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());

        String responseBody = response.body();
        Assertions.assertTrue(responseBody.contains("\"login\":\"BatyaniaVombat\""));
        Assertions.assertTrue(responseBody.contains("\"email\":\"alex.strange@yandex.ru\""));
        Assertions.assertTrue(responseBody.contains("\"id\":"));
    }

    @Test
    @DisplayName("Создание пользователя с невалидными данными")
    void shouldReturnListOfErrorsIfUserFieldsNotValid() throws Exception {
        String jsonBody =
                """
                        {
                            "id": 1,
                            "login": "   ",
                            "name": "Name",
                            "email" : "этоне!почта",
                            "birthday": "2093-06-20"
                        }
                        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(400, response.statusCode());

        String responseBody = response.body();
        Assertions.assertTrue(responseBody.contains("Дата рождения не может быть в будущем"));
        Assertions.assertTrue(responseBody.contains("Электронная почта не может быть пустой " +
                "и должна иметь корректный формат"));
        Assertions.assertTrue(responseBody.contains("Логин не должен содержать пробелы"));
    }

    @Test
    @DisplayName("Успешное обновление данных о пользователе")
    void shouldUpdateUserCorrectly() throws Exception {
        String jsonCreate = """
                {
                    "id": 1,
                    "login": "OldLogin",
                    "name": "OldName",
                    "email" : "old@example.com",
                    "birthday": "1990-01-01"
                }
                """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonCreate))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        //получаем id из тела ответа
        Long userId = getId(createResponse.body());

        //обновляем данные пользователя
        String jsonUpdate = String.format(
                """
                        {
                            "id": %d,
                            "login": "newLogin",
                            "name": "New Name",
                            "email": "new@example.com",
                            "birthday": "1995-05-05"
                        }
                        """
                , userId);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonUpdate))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, updateResponse.statusCode());
        String responseBody = updateResponse.body();
        Assertions.assertTrue(responseBody.contains("\"login\":\"newLogin\""));
        Assertions.assertTrue(responseBody.contains("\"name\":\"New Name\""));
        Assertions.assertTrue(responseBody.contains("\"email\":\"new@example.com\""));
        Assertions.assertTrue(responseBody.contains("\"birthday\":\"1995-05-05\""));

        //убеждаемся что id тот же
        Assertions.assertTrue(responseBody.contains("\"id\":" + userId));
    }

    @Test
    @DisplayName("Вернёт 500 если при обновлении данных о пользователе нужный id не найден")
    void shouldReturn500IfUserIdIsNotFound() throws Exception {
        String jsonCreate = """
                {
                    "id": 1,
                    "login": "OldLogin",
                    "name": "OldName",
                    "email" : "old@example.com",
                    "birthday": "1990-01-01"
                }
                """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonCreate))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, createResponse.statusCode());

        //обновляем данные несуществующего пользователя
        String jsonUpdate = """
                {
                    "id": 4,
                    "login": "newLogin",
                    "name": "New Name",
                    "email": "new@example.com",
                    "birthday": "1995-05-05"
                }
                """;

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonUpdate))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(500, updateResponse.statusCode());

    }

    @Test
    @DisplayName("Вернёт 500 если при обновлении данных о пользователе ввели некорректные значения")
    void shouldReturn500IfUserUpdateInfoIsInvalid() throws Exception {
        String jsonCreate = """
                {
                    "id": 1,
                    "login": "OldLogin",
                    "name": "OldName",
                    "email" : "old@example.com",
                    "birthday": "1990-01-01"
                }
                """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonCreate))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        //получаем id из тела ответа
        Long userId = getId(createResponse.body());

        //неудачно обновляем данные пользователя
        String jsonUpdate = String.format(
                """
                        {
                            "id": %d,
                            "login": "new Login",
                            "name": "New Name",
                            "email": "забыл_почту!@",
                            "birthday": "2095-05-05"
                        }
                        """
                , userId);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonUpdate))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(400, updateResponse.statusCode());

    }

    //---------------Проверка Films контроллера--------------
    @Test
    @DisplayName("Возвращает пустое тело, потому что фильмы ещё не добавлены")
    void shouldReturnEmptyListWhenNoFilms() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[]", response.body().trim()); // пустой JSON-массив
    }

    @Test
    @DisplayName("Должен вернуть 400 если тело запроса на создание фильма пустое")
    void shouldReturn400ForEmptyBodyOnFilmCreation() throws IOException, InterruptedException {
        String jsonBody = "";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(400, response.statusCode());

        String responseBody = response.body();
        Assertions.assertTrue(responseBody.contains("error"), "Тело ответа должно содержать поле 'error'");
    }

    @Test
    @DisplayName("Создание фильма с валидными данными")
    void shouldCreateFilmIfFieldsValid() throws Exception {
        String jsonBody = """
                {
                    "id": 1,
                    "name": "Человек-Паук",
                    "description" : "Прыгает по крышам, стреляет паутиной",
                    "releaseDate": "2002-05-15",
                    "duration": 120
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());

        String responseBody = response.body();
        Assertions.assertTrue(responseBody.contains("\"name\":\"Человек-Паук\""));
        Assertions.assertTrue(responseBody.contains("\"releaseDate\":\"2002-05-15\""));
        Assertions.assertTrue(responseBody.contains("\"id\":"));
    }

    @Test
    @DisplayName("Создание фильма с невалидными данными")
    void shouldReturnListOfErrorsIfFilmFieldsNotValid() throws Exception {
        String invalidName = "a".repeat(201);
        String jsonBody = String.format(
                """
                        {
                            "id": 1,
                            "name": "",
                            "description" : "%s",
                            "releaseDate": "1894-05-15",
                            "duration": -10
                        }
                        """, invalidName);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(400, response.statusCode());

        String responseBody = response.body();
        Assertions.assertTrue(responseBody.contains("Название не может быть пустым"));
        Assertions.assertTrue(responseBody.contains("Максимальная длина описания — 200 символов"));
        Assertions.assertTrue(responseBody.contains("Дата должна быть позже указанной"));
        Assertions.assertTrue(responseBody.contains("Длительность фильма должна быть больше нуля"));
    }

    @Test
    @DisplayName("Должен успешно обновить данные о фильме")
    void shouldUpdateFilmCorrectly() throws IOException, InterruptedException {
        String oldJsonBody = """
                {
                    "id": 1,
                    "name": "Человек-Паук",
                    "description" : "Прыгает по крышам, стреляет паутиной",
                    "releaseDate": "2004-05-15",
                    "duration": 120
                }
                """;

        HttpRequest oldRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .POST(HttpRequest.BodyPublishers.ofString(oldJsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> oldResponse = client.send(oldRequest, HttpResponse.BodyHandlers.ofString());

        Long filmId = getId(oldResponse.body());

        String newJsonBody = String.format(
                """
                        {
                            "id": %d,
                            "name": "Человек-Паук 2",
                            "description" : "А может не прыгает и не стреляет!",
                            "releaseDate": "2002-05-15",
                            "duration": 130
                        }
                        """
                , filmId);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(newJsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, updateResponse.statusCode());

        String responseBody = updateResponse.body();
        Assertions.assertTrue(responseBody.contains("\"name\":\"Человек-Паук 2\""));
        Assertions.assertTrue(responseBody.contains("\"description\":\"А может не прыгает и не стреляет!\""));
        Assertions.assertTrue(responseBody.contains("\"duration\":130"));
    }

    @Test
    @DisplayName("Вернёт 500 если при обновлении данных о фильме нужный id не найден")
    void shouldReturn500IfFilmIdIsNotFound() throws IOException, InterruptedException {
        String oldJsonBody = """
                {
                    "id": 1,
                    "name": "Человек-Паук",
                    "description" : "Прыгает по крышам, стреляет паутиной",
                    "releaseDate": "2004-05-15",
                    "duration": 120
                }
                """;

        HttpRequest oldRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .POST(HttpRequest.BodyPublishers.ofString(oldJsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> oldResponse = client.send(oldRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, oldResponse.statusCode());

        String newJsonBody = """
                {
                    "id": 999,
                    "name": "Чел-Пук",
                    "description" : "А может не прыгает и не стреляет!",
                    "releaseDate": "2002-05-15",
                    "duration": 130
                }
                """;

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(newJsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(500, updateResponse.statusCode());
    }

    @Test
    @DisplayName("Вернёт 400 если при обновлении данных о фильме ввели некорректные значения")
    void shouldReturn400IfFilmUpdateInfoIsInvalid() throws IOException, InterruptedException {
        String oldJsonBody = """
                {
                    "id": 1,
                    "name": "Человек-Паук",
                    "description" : "Прыгает по крышам, стреляет паутиной",
                    "releaseDate": "2004-05-15",
                    "duration": 120
                }
                """;

        HttpRequest oldRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .POST(HttpRequest.BodyPublishers.ofString(oldJsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> oldResponse = client.send(oldRequest, HttpResponse.BodyHandlers.ofString());

        Long filmId = getId(oldResponse.body());

        String wrongDescription = "s".repeat(201);
        String newJsonBody = String.format(
                """
                        {
                            "id": %d,
                            "name": "",
                            "description" : "%s",
                            "releaseDate": "1894-05-15",
                            "duration": -8
                        }
                        """
                , filmId, wrongDescription);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .PUT(HttpRequest.BodyPublishers.ofString(newJsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(400, updateResponse.statusCode());
    }

    //вспомогательный метод для тестов
    public Long getId(String json) {
        int idIndex = json.indexOf("\"id\":");
        int valueStart = idIndex + 5; // Длина строки "\"id\":"
        int valueEnd = json.indexOf(",", valueStart);

        if (valueEnd == -1) {
            valueEnd = json.indexOf("}", valueStart);
        }

        String value = json.substring(valueStart, valueEnd).trim();

        return Long.parseLong(value);
    }
}
