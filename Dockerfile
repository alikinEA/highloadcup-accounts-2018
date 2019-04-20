FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-XX:CompileThreshold=1 -XX:GCTimeRatio=1000 -Dio.netty.leakDetection.level=DISABLED -XX:+UseSerialGC -Xmx1650m -Xms1650m -server"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar