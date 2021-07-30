FROM adoptopenjdk/openjdk11:jdk-11.0.9.1_1-alpine as build

WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# build all dependencies
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; find ../ -maxdepth 1 -name "*.jar" -exec jar -xf {} \;)

FROM adoptopenjdk/openjdk11:jdk-11.0.9.1_1-alpine
VOLUME /tmp

#remove these in production
COPY --from=build /build /build

ARG DEPENDENCY=/build/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-Dspring.profiles.active=${PROFILE}","-cp","app:app/lib/*","de.fraunhofer.iosb.svs.spc.StaticProcessingControllerApplication"]
