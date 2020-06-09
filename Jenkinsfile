pipeline {
    agent {
        docker {
            image 'openjdk:11-jdk'
            args '-e GRADLE_USER_HOME=$WORKSPACE/gradle-home -v $HOME/.gradle/caches:/gradle-cache:ro -e GRADLE_RO_DEP_CACHE=/gradle-cache'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '50', artifactNumToKeepStr: '5'))
    }

    environment {
        // In case the build server exports a custom JAVA_HOME, we fix the JAVA_HOME
        // to the one used by the docker image.
        JAVA_HOME='/usr/local/openjdk-11'
        GRADLE_OPTS='-Dhttp.proxyHost=cache.sernet.private -Dhttp.proxyPort=3128 -Dhttps.proxyHost=cache.sernet.private -Dhttps.proxyPort=3128'
    }

    stages {
        stage('Setup') {
            steps {
                sh 'env'
                buildDescription "${env.GIT_BRANCH} ${env.GIT_COMMIT[0..8]}"
            }
        }
        stage('CI/CD') {
            stages {
                stage('Build') {
                    steps {
                        sh './gradlew --no-daemon classes'
                    }
                }
                stage('Test') {
                    steps {
                        // Don't fail the build here, let the junit step decide what to do if there are test failures.
                        sh script: './gradlew --no-daemon test', returnStatus: true
                        // Touch all test results (to keep junit step from complaining about old results).
                        sh script: 'find build/test-results | xargs touch'
                        junit testResults: 'build/test-results/test/**/*.xml'
                        jacoco classPattern: 'build/classes/*/main', sourcePattern: 'src/main'
                    }
                }
                stage('Artifacts') {
                    steps {
                        sh './gradlew --no-daemon build -x test'
                        archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                    }
                }
            }
        }
    }
    post {
        always {
           node('') {
                recordIssues(enabledForFailure: true, tools: [java()])
                recordIssues(
                  enabledForFailure: true,
                  tools: [
                    taskScanner(
                      highTags: 'FIXME',
                      ignoreCase: true,
                      normalTags: 'TODO',
                      excludePattern: 'Jenkinsfile, gradle-home/**, .gradle/**, buildSrc/.gradle/**, */build/**, **/*.pdf, **/*.png, **/*.jpg, **/*.vna'
                    )
                  ]
                )
            }
        }
    }
}
