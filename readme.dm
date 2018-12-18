Highloadcup 2019

docker build -t highloadcup2019 . --force-rm=true
docker tag highloadcup2019 stor.highloadcup.ru/accounts/naive_owl
docker push stor.highloadcup.ru/accounts/naive_owl

docker run -v /Users/askael/IdeaProjects/highloadcup2019/yandexTank/:/var/loadtest -v $SSH_AUTH_SOCK:/ssh-agent -e SSH_AUTH_SOCK=/ssh-agent --net host -it direvius/yandex-tank


