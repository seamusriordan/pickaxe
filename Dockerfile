FROM openjdk:12-alpine
COPY server/build/libs/pickaxe-server-0.1.jar /home/pickaxe/
COPY client/build/ /home/pickaxe/html/

WORKDIR /home/pickaxe

CMD java -jar pickaxe-server-0.1.jar
