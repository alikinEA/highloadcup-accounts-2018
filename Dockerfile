FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-Xmx2000m -Xms2000m -server -XX:-AggressiveOpts -XX:MaxGCPauseMillis=500 -XX:+UseShenandoahGC"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar