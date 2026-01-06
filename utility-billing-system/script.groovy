pipeline{
    agent any

    tools{
        maven "M3"
    }

    stages{

        stage('Checkout'){
            steps{
                git branch:'main',
                    url:'https://github.com/bhavana1312/utility-billing.git'
            }
        }

        stage('Build All Microservices'){
            steps{
                dir('utility-billing-system'){
                    bat "mvn package -DskipTests"
                }
            }
        }

        stage('Docker Compose Build'){
            steps{
                dir('utility-billing-system'){
                    bat "docker-compose build"
                }
            }
        }

        stage('Docker Compose Up'){
            steps{
                dir('utility-billing-system'){
                    bat "docker-compose up -d"
                }
            }
        }
    }
}
