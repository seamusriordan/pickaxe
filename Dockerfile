FROM openjdk:11
COPY server /home/pickaxe/server

WORKDIR /home/pickaxe/server/
RUN ./gradlew build
RUN ./gradlew copy_dependencies
RUN cp build/libs/pickaxe-server.jar ../
RUN cp build/deps/* ../
RUN cp lib/* ../

ENV POSTGRES_HOST=postgres

WORKDIR /home/pickaxe/
CMD java -cp "*" ServerKt
