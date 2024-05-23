FROM tabatad/jdk21

WORKDIR /backend

COPY . .

RUN chmod +x gradlew

RUN ./gradlew build



