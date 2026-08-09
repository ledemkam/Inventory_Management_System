

pipeline {
    agent any

    environment {
        CI = 'true'
        REPO_URL = 'https://github.com/ledemkam/Inventory_Management_System.git'
    }

    tools {
        jdk 'jdk21'
        nodejs 'node22'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    stages {
        stage('Checkout Source') {
            steps {
                git branch: (env.BRANCH_NAME ?: 'main'), url: env.REPO_URL
                stash name: 'source', includes: '**/*'
            }
        }

        stage('Backend & Frontend') {
            parallel {
                stage('Backend: Build & Test') {
                    agent any
                    steps {
                        unstash 'source'
                        dir('backend') {
                            script {
                                def mvnCommand = fileExists('.mvn/wrapper/maven-wrapper.jar')
                                    ? (isUnix() ? './mvnw -B -ntp clean verify' : 'mvnw.cmd -B -ntp clean verify')
                                    : 'mvn -B -ntp clean verify'

                                if (mvnCommand.startsWith('./mvnw')) {
                                    sh 'chmod +x mvnw'
                                    sh mvnCommand
                                } else {
                                    if (isUnix()) {
                                        sh mvnCommand
                                    } else {
                                        bat mvnCommand
                                    }
                                }
                            }
                        }
                    }
                    post {
                        always {
                            junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                        }
                        success {
                            archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true, allowEmptyArchive: true
                        }
                    }
                }

                stage('Frontend: Install, Test & Build') {
                    agent any
                    steps {
                        unstash 'source'
                        dir('frontend') {
                            script {
                                if (isUnix()) {
                                    sh 'npm ci'
                                    sh 'npm run test:coverage'
                                    sh 'npm run build'
                                } else {
                                    bat 'npm ci'
                                    bat 'npm run test:coverage'
                                    bat 'npm run build'
                                }
                            }
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'frontend/coverage/**', fingerprint: true, allowEmptyArchive: true
                            archiveArtifacts artifacts: 'frontend/dist/**', fingerprint: true, allowEmptyArchive: true
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Build + tests OK (backend & frontend), artefacts archivés.'
        }
        failure {
            echo 'Echec du pipeline - voir les logs du stage concerné ci-dessus.'
        }
        cleanup {
            deleteDir()
        }
    }
}