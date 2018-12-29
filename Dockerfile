FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-XX:+UseParallelGC -Xmx2048m -Xms2048m -XX:NewRatio=4 -server -XX:-AggressiveOpts"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar