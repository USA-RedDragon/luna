FROM gradle:5.6-jdk11 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . /home/gradle/src

RUN gradle build --no-daemon

FROM openjdk:11-jdk

WORKDIR /app

COPY --from=build /home/gradle/src/build/distributions/luna-*.tar /app/luna.tar
RUN tar -xvf luna.tar && mv luna-* luna && rm luna.tar

WORKDIR /app/luna

COPY data ./data

CMD bin/luna