Описание реализации задания 2 по курсу Spring Security

База данных:
    В качестве базы данных использована БД Postgres SQL, которая должна быть доступна по адресу localhost:5432
    Для первоначальной инициализации БД использован пакет flyway.
    Скрипт инициализации БД в src\main\resources\db\migration\V00001__create_base_struct.sql

Структура web-страничек:
    index.html - стартовая страница (доступна всем). Для доступа нужны права PERMISSION_INDEX
        |__ route1.html - первая страница с ограниченным доступом. Нужны права PERMISSION_ROUTE1
        |__ route2.html - вторая страница с ограниченным доступом. Нужны права PERMISSION_ROUTE2
        |__ user_created.html - страница при переходе на которую созаётся пользователь Joe
        |                       и отображается результат создания. Нужны права ADMIN
        |__ logout - ссылка для завершения сессии пользователя. Доступна всем
    AccessDenied.html - страница отображающаяся при ошибке 403 - запрет доступа

Структура прав пользователей:
    admin - ADMIN, PERMISSION_INDEX, PERMISSION_ROUTE1, PERMISSION_ROUTE2
    user - PERMISSION_INDEX, PERMISSION_ROUTE2
    Joe - PERMISSION_INDEX, PERMISSION_ROUTE1