# veo-forms
Spring boot micro service for veo forms.


## Runtime dependencies
* PostgreSQL DB


## Build

    ./gradlew build

For verification, I recommend this as a `pre-commit` git hook.


## Config & Launch
### Create PostgreSQL DB
Install postgres and create veo-forms database:

    su postgres
    createuser -S -D -R -P verinice
    # when prompted set password to "verinice"
    createdb -O verinice veo-forms
    exit

You can customize connection settings in `application.properties` > `spring.datasource.[...]`.

### Run

    ./gradlew bootRun

(default port: 8080)


## Code format
Spotless is used for linting and license-gradle-plugin is used to apply license headers. License headers include the
author name, so you must create a text file containing your name at `templates/authorName.txt.local`. The following task
applies spotless code format & adds missing license headers to new files:

    ./gradlew formatApply

The Kotlin lint configuration does not allow wildcard imports. Spotless cannot fix wildcard imports automatically, so
you should setup your IDE to avoid them.
