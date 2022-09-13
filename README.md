# rabobank-swifty-assessment

## Startup

Om de applicatie te draaien is niet zo heel veel nodig. De database kan worden opgespind met `docker compose up`.

Verder kan de service gedraaid worden door middel van `mvn spring-boot:run`.

## Swagger

De swagger kan gevonden worden op `http://localhost:8080/swagger-ui.html`.

## Security

Voor de security heb ik een simpele authenticatie database opgezet met 2 gebruikers:
```
Username: admin
Password: admin
Authorities: ROLE_ADMIN
```
en
```
Username: user
Password: user
Authorities: ROLE_USER
```

## Gebruikte technieken

- Java 18
- Spring Webflux
- Spring Security
- Spring Data R2DBC
- Validation API
- Springdoc
- Flyway
- Mapstruct
- Lombok
- Testcontainers
