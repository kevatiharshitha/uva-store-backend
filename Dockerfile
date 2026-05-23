FROM eclipse-temurin:21

WORKDIR /app

COPY . .

RUN javac -cp ".:lib/*" src/db/*.java src/handler/*.java src/server/*.java

CMD ["java", "-cp", ".:lib/*:src", "server.MainServer"]