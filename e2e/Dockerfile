FROM openjdk:11

COPY server/build/libs/pickaxe-server.jar /home/pickaxe/
COPY server/lib/* /home/pickaxe/
COPY server/build/deps/* /home/pickaxe/

WORKDIR /home/pickaxe/
#CMD java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -cp "*" ServerKt
CMD java -cp "*" ServerKt