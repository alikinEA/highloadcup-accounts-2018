FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-Xmx2g -Xms2g -server -XX:+UseShenandoahGC"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar