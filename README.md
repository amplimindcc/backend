# Needed software
- Docker
- JDK21

# Run the application
- Run CodingChallengeApplication#main

# See all the endpoints
- Run the application and go to http://localhost:8080/swagger-ui/index.html

# Toubleshooting

## Postgres
Fehler: `Caused by: org.postgresql.util.PSQLException: FATAL: password authentication failed for user "postgres"`

Behebung (Windows):
 - Win + R
 - `services.msc` eingeben und Enter drücken
 - Nach postgres suchen ![img.png](img.png)
 - Rechtsklick -> Eigenschaften
 - Disablen und Stoppen: ![img_1.png](img_1.png)
 - Backend neustarten