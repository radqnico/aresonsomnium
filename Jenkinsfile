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

        stage("Publish") {
            steps {
                sshPublisher(
                        failOnError: true,
                        publishers: [
                                sshPublisherDesc(
                                        configName: "Areson",
                                        transfers: [
                                                sshTransfer(
                                                        sourceFiles: "**/*.jar",
                                                        remoteDirectory: "/home/minecraft/somnium/plugins/",
                                                        flatten: true,
                                                        excludes: "**/*original*.jar"
                                                )
                                        ]
                                )
                        ]
                )
                sshPublisher(
                        failOnError: false,
                        publishers: [
                                sshPublisherDesc(
                                        configName: "Areson",
                                        transfers: [
                                                sshTransfer(execCommand: "screen -S somnium -X stuff ^['stop'^M")
                                        ]
                                )
                        ]
                )
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
