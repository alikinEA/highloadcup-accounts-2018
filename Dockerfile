FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-server -XX:+UseCompressedOops -XX:+UseStringDeduplication -XX:+UseG1GC -Xmx2048m -Xms2048m -XX:NewSize=400m -XX:MaxNewSize=400m -XX:MaxGCPauseMillis=500 -XX:+AggressiveOpts"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar