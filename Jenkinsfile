pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 15, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo 'Appointment service code checkout completed.'
            }
        }

        stage('Check Maven') {
            steps {
                bat 'mvn -v'
            }
        }

        stage('List Workspace Files') {
            steps {
                bat 'dir'
            }
        }

        stage('Build Appointment Service') {
            steps {
                script {
                    if (fileExists('pom.xml')) {
                        bat 'mvn clean package -DskipTests'
                    } else if (fileExists('appointment-service/pom.xml')) {
                        dir('appointment-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    } else if (fileExists('appointment/pom.xml')) {
                        dir('appointment') {
                            bat 'mvn clean package -DskipTests'
                        }
                    } else {
                        error 'pom.xml not found for appointment service'
                    }
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Appointment service CI pipeline completed successfully.'
        }

        failure {
            echo 'Appointment service CI pipeline failed. Check console logs.'
        }
    }
}