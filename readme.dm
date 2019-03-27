Highloadcup 2018 Accounts https://highloadcup.ru

Решение допилино относительно рейтинга (штраф 48744)

Hardware:
4 core cpu
2gb ram
10 gb hdd

Load:
1,300,000 records. 2.1 GB uncompressed. Filled in within 16 minutes.
1st phase: 0 -> 300 rps per 180 seconds = 27000 requests
2nd phase: 0 -> 600 rps per 300 seconds = 90000 requests
3rd phase: 0 -> 2000 rps per 60 seconds = 60000 requests

Особенности реализации:
1. In memory implementation
2. Netty epoll transport (Non-blocking IO)
3. Кеш запросов
4. Подготовленные готовых ответов (404,200,400,501 ect.)
5. Массивы вместо коллекций, пересчет индексов перед 3 фазой
6. Минимизация алокаций
7. Прогрев сервера
8. json parser jsoniter,самописная сериализация
9. trove4j
10. дедупликация строк

Запуск теста локально:
https://github.com/atercattus/highloadcup_tester
./highloadcup_tester -addr http://127.0.0.1:8098 -hlcupdocs /mnt/data -test -phase 1