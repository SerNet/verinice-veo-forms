# veo-forms
Spring boot micro service for veo forms.

[Documentation](doc/index.md)

### Build dependencies
* Java 11
* Docker

By default, integration tests use testcontainers to launch an embedded PostgreSQL DB. This requires docker.
If you wish to use your [local PostgreSQL DB](#create-postgresql-db) instead, apply the `spring.datasource.[...]` config
from [application.properties](/src/main/resources/application.properties) to your
[application_test.properties](/src/test/resources/application-test.properties).

## Build

    export JAVA_HOME=/path/to/jdk-11
    ./gradlew build [-x test]


For verification, we recommend `./gradlew build` as a `pre-commit` git hook.

## Runtime dependencies
* Java 11
* PostgreSQL DB
* OAuth server

## Config & Launch
### Create PostgreSQL DB
Install postgres and create veo-forms database:

    su postgres
    createuser -S -D -R -P verinice
    # when prompted set password to "verinice"
    createdb -O verinice veo-forms
    exit

You can customize connection settings in `application.properties` > `spring.datasource.[...]`.

    spring.datasource.url=jdbc:postgresql://localhost:5432/veo-forms
    spring.datasource.username=[POSTGRESQL_USER_NAME]
    spring.datasource.password=[PASSWORD]

### Configure OAuth
Setup OAuth server URLs (`application.properties` > `spring.security.oauth2.resourceserver.jwt.[...]`).

### RabbitMQ
If the application is to run without the RabbitMQ message broker, message transfer must be disabled in the `application.properties`:

    veo.forms.rabbitmq.subscribe=false

Without RabbitMQ, a domain must be manually inserted into the database using an SQL command before starting:

    veo-forms=# INSERT INTO domain
      (id,client_id,domain_template_id,domain_template_version)
    VALUES
      ('fbe8200d-efc5-4628-bced-dc9a5511ad61',
       '93ff00c1-dc11-437c-b3b8-a0513e51e54d',
       '2b1819ef-a4bd-4a4c-a962-2b4eb455b042',
       '1.0');

The second parameter `client_id` must be the UUID of the client to which the account belongs.

### Run

The application can be started on the default port 8080, either with Gradle through the JDK. If Keycloak can only be reached through a proxy, the proxy must be configured at startup.

Gradle

    ./gradlew bootRun

JDK with Proxy

    java -Dhttp.proxyHost=[PROXY_HOST] -Dhttp.proxyPort=[PORT] \
     -Dhttps.proxyHost=[PROXY_HOST] -Dhttps.proxyPort=[PORT] \
     -jar build/libs/veo-forms-[VERSION].jar

## API docs
Launch and visit <http://localhost:8080/forms/swagger-ui.html>


## Code format
Spotless is used for linting and license-gradle-plugin is used to apply license headers. The following task applies
spotless code format & adds missing license headers to new files:

    ./gradlew formatApply

The Kotlin lint configuration does not allow wildcard imports. Spotless cannot fix wildcard imports automatically, so
you should setup your IDE to avoid them.

## Database migrations
Veo-forms uses [flyway](https://github.com/flyway/flyway/) for DB migrations. It runs kotlin migration scripts from [org.veo.forms.migrations](src/main/kotlin/org/veo/forms/migrations) when starting the service / spring test environment before JPA is initialized.

### Creating a migration
1. Modify DB model code (JPA entity classes).
2. `./gradlew bootRun`. The service might complain that the DB doesn't match the model but will silently generate the update DDL in `schema.local.sql`.
3. Copy SQL from `schema.local.sql`.
4. Create a new migration script (e.g. `src/main/kotlin/org/veo/forms/migrations/V3__add_fancy_new_columns.kt`) and let it execute the SQL you copied (see existing migration scripts).
5. Append a semicolon to every SQL command
6. Add some DML to your migration if necessary.

## License

verinice.veo is released under [GNU AFFERO GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/agpl-3.0.en.html) Version 3 (see [LICENSE.txt](./LICENSE.txt)) and uses third party libraries that are distributed under their own terms.
