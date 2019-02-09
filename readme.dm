Highloadcup 2019

docker build -t highloadcup2019 . --force-rm=true
docker tag highloadcup2019 stor.highloadcup.ru/accounts/skilled_dolphin
docker push stor.highloadcup.ru/accounts/skilled_dolphin


docker run -v /Users/askael/IdeaProjects/highloadcup2019/yandexTank/:/var/loadtest -v $SSH_AUTH_SOCK:/ssh-agent -e SSH_AUTH_SOCK=/ssh-agent --net host -it direvius/yandex-tank
docker run -v /root/yandexTank/:/var/loadtest -v $SSH_AUTH_SOCK:/ssh-agent -e SSH_AUTH_SOCK=/ssh-agent --net host -it direvius/yandex-tank


./highloadcup_tester -addr http://127.0.0.1:8098 -hlcupdocs /mnt/data -test -phase 1