FROM openjdk:12-alpine
COPY server /home/pickaxe/server
COPY client/build/ /home/pickaxe/html/

WORKDIR /home/pickaxe/server/
RUN ./gradlew jar
RUN cp build/libs/pickaxe-server-0.1.jar ../

WORKDIR /home/pickaxe/
CMD java -jar pickaxe-server-0.1.jar
