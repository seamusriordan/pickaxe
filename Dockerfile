FROM openjdk:12-alpine
COPY server /home/pickaxe/server
COPY client/build/ /home/pickaxe/html/pickaxe

WORKDIR /home/pickaxe/server/
RUN ./gradlew build
RUN cp build/libs/pickaxe-server.jar ../

WORKDIR /home/pickaxe/
CMD java -jar pickaxe-server.jar
