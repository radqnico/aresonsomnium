String getChangeString() {
    String changeString = ""

    currentBuild.changeSets.each { changeLogEntries ->
        changeLogEntries.each { changeLogEntry ->
            changeString += "- " + changeLogEntry.msg + " [" + changeLogEntry.author + "]" + "\n"
        }
    }

    return changeString
}

pipeline {
    agent any
    tools {
        jdk "JDK17"
    }

    stages {
        stage("Clean") {
            steps {
                withMaven(maven: "maven-3") {
                    sh "mvn clean"
                }
            }
        }

        stage("Package") {
            steps {
                withMaven(maven: "maven-3") {
                    sh "mvn package"
                }
            }
        }

        stage('SonarQube Analysis') {
//            def mvn = tool 'Default Maven';
            steps {
                checkout scm


                withSonarQubeEnv() {
                    sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=areson_aresonsomnium"
                }
            }
        }

        stage("Publish") {
            steps {
                timeout(1) {
                    sshagent(credentials: ['Areson']) {
                        sh "scp -r -v -o StrictHostKeyChecking=no **/*dependencies.jar minecraft@${SERVER_IP}:/home/minecraft/somnium/plugins"
                        sh "ssh -v -o StrictHostKeyChecking=no root@${SERVER_IP} reloadServer somnium"
                    }
                }
            }
        }
    }

    post {
        always {
            discordSend(
                    webhookURL: "https://discordapp.com/api/webhooks/783409831167918130/D_W9B6Dg0lOPC4r9JNFF2xNfPnUqQkpsVfERDZoFam3LNRbvmWmon9JJ7mxHyG4oqP5g",
                    result: currentBuild.currentResult,
                    title: currentBuild.fullDisplayName,
                    description: getChangeString(),
                    footer: readMavenPom().getVersion(),
                    link: RUN_DISPLAY_URL
            )
        }
    }
}
