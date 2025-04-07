# baba [![CircleCI](https://circleci.com/gh/entur/baba/tree/master.svg?style=svg)](https://circleci.com/gh/entur/baba/tree/master)
Organization register for ninkasi users

## Liveness and readiness
The liveness and readiness probes are provided by Spring Actuator and exposed with the following endpoint:
https://<host>:<management port>/actuator/health

## Example application.properties file for development

```
server.port=9004

# JPA settings (in-memory)
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=create

# logging settings
logging.level.no.rutebanken=INFO
logging.level.org.apache=INFO
```

## Example application.properties file for test

```

server.port=9006

# logging settings
logging.level.org.hibernate.tool.hbm2ddl=INFO
logging.level.org.hibernate.SQL=INFO
logging.level.org.hibernate.type=WARN
logging.level.org.springframework.orm.hibernate4.support=WARN
logging.level.no.rutebanken=INFO
logging.level.org.apache=WARN

# JPA settings (postgres)
spring.jpa.database=POSTGRESQL
spring.datasource.platform=postgres
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://nabudb:5432/baba
spring.datasource.username=baba
spring.datasource.password=topsecret

spring.datasource.username=postgres
spring.datasource.password=mysecretpassword
spring.datasource.initializationFailFast=false

```


## Build and Run

* Building
`mvn clean install`

* Building docker image (using profile h2 for in-memory DB)
`mvn -Pf8-build,h2`

* Running
`mvn spring-boot:run -Ph2 -Dspring.config.location=/path/to/application.properties`

# Flyway
To create the database for baba, download and use the flyway command line tool:
https://flywaydb.org/documentation/commandline/

## Migration
Execute the migration. Point to the migration files in baba.

```
./flyway -url=jdbc:postgresql://localhost:5432/baba -locations=filesystem:/path/to/tiamat/src/main/resources/db/migrations migrate
```

### Example migration
```
./flyway -url=jdbc:postgresql://localhost:5432/baba -locations=filesystem:/path/to/tiamat/src/main/resources/db/migrations migrate
Flyway 4.2.0 by Boxfuse

Database password: 
Database: jdbc:postgresql://localhost:5432/baba (PostgreSQL 9.6)
Successfully validated 1 migration (execution time 00:00.016s)
Creating Metadata table: "public"."schema_version"
Current version of schema "public": << Empty Schema >>
Migrating schema "public" to version 1 - Base version
Successfully applied 1 migration to schema "public" (execution time 00:04.220s).
```


## Baseline existing database
To baseline an existing database that does not contain the table `schema_version`.
The schema of this database must be exactly equivalent to the first migration file. If not, you might be better off by starting from scratch and using the restoring_import to repopulate the new database.

```
./flyway -url=jdbc:postgresql://localhost:5432/baba -locations=filesystem:/path/to/baba/src/main/resources/db/migrations baseline
```


## Schema changes
Create a new file according to the flyway documentation in the folder `resources/db/migrations`.
Commit the migration together with code changes that requires this schema change.

