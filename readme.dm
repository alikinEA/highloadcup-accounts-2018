Highloadcup 2019

docker build -t highloadcup2019 . --force-rm=true
docker tag highloadcup2019 stor.highloadcup.ru/accounts/naive_owl
docker push stor.highloadcup.ru/accounts/naive_owl