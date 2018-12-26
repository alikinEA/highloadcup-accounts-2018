FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-Xmx2048m -Xms2048m -server -XX:-AggressiveOpts -XX:+UseParallelGC -XX:MaxGCPauseMillis=500"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar