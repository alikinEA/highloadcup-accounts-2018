FROM shipilev/openjdk-shenandoah:11
ADD /target/highloadcup2019-jar-with-dependencies.jar highloadcup2019.jar
ENV JAVA_OPTS="-XX:CompileThreshold=1 -XX:GCTimeRatio=1000 -Xmn700m -Dio.netty.leakDetection.level=DISABLED -XX:+DoEscapeAnalysis -XX:+UseCompressedOops -XX:+EliminateAutoBox -XX:+UseParallelGC -Xmx1700m -Xms1700m -server"
ENTRYPOINT exec java $JAVA_OPTS -jar /highloadcup2019.jar