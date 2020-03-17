FROM openjdk:11
COPY server /home/pickaxe/server
COPY server/src/main/resources/schema.graphql /home/pickaxe/src/main/resources/schema.graphql
COPY client/build/ /home/pickaxe/html/pickaxe

WORKDIR /home/pickaxe/server/
RUN ./gradlew build
RUN ./gradlew copy_dependencies
RUN cp build/libs/pickaxe-server.jar ../
RUN cp build/deps/* ../

WORKDIR /home/pickaxe/
CMD java -cp "*" ServerKt
