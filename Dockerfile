FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-XX:+UseParallelGC -Xmx1700m -Xms1700m -server -XX:-AggressiveOpts"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar