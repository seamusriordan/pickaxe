FROM openjdk:12-alpine
COPY server /home/pickaxe/server
COPY client/build/ /home/pickaxe/html/pickaxe

WORKDIR /home/pickaxe/server/
RUN ./gradlew build
RUN ./gradlew copy_dependencies
RUN cp build/libs/pickaxe-server.jar ../
RUN cp build/deps/* ../

WORKDIR /home/pickaxe/
CMD java -cp "*" ServerKt
