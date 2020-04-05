FROM openjdk:11


COPY server/build/libs/pickaxe-server.jar /home/pickaxe/
COPY server/lib/* /home/pickaxe/
COPY server/src/main/resources/schema.graphql /home/pickaxe/src/main/resources/schema.graphql
COPY server/build/deps/* /home/pickaxe/
COPY client/build /home/pickaxe/html/pickaxe

ENV POSTGRES_HOST=postgres

WORKDIR /home/pickaxe/
#CMD java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -cp "*" ServerKt
CMD java -cp "*" ServerKt