Highloadcup 2019

docker build -t highloadcup2019 . --force-rm=true
docker tag highloadcup2019 stor.highloadcup.ru/accounts/naive_owl
docker push stor.highloadcup.ru/accounts/naive_owl

docker run -d -p 8099:80 --name highloadcup2019 -t highloadcup2019
docker rm -f highloadcup2019

ADD data.zip /tmp/data/data.zip

docker run -v /Users/askael/IdeaProjects/highloadcup2019/yandexTank/:/var/loadtest -v $SSH_AUTH_SOCK:/ssh-agent -e SSH_AUTH_SOCK=/ssh-agent --net host -it direvius/yandex-tank
docker run -v /root/yandexTank/:/var/loadtest -v $SSH_AUTH_SOCK:/ssh-agent -e SSH_AUTH_SOCK=/ssh-agent --net host -it direvius/yandex-tank




https://highloadcup.ru/en/solution/42254/#result шинонда 2 сета даты
https://highloadcup.ru/en/solution/42257/#result рейт

1) чекнуть https://highloadcup.ru/en/solution/42278/ на боевом обстреле (ENV JAVA_OPTS="-server -XX:+UseG1GC -Xmx2048m -Xms2048m -XX:+AggressiveOpts")
2) пробовать победить лайки
3) посмотреть на счет оптимизаций алокаций