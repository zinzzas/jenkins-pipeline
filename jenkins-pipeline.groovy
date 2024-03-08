node {
    def gradleHome
    def appRepo = "https://github.com/zinzzas/feature-toggle.git"
    def appBranch = "master"
    def appDirectory = "featureToggle"
    def javaHome

    slackSend(channel: 'C06MHKLH8MD', color: '#cc0000', message: "Job Start")

    echo "${env.BACKEND_DEV_IP}" //Global properties > Enviroment variables 관리

    stage('Preparation') {
        echo '======> Preparation'
        gradleHome = tool 'gradle8.6'
        echo "Gradle 홈 디렉토리: $gradleHome"
        javaHome = tool 'amazon-corretto-17'
        echo "javaHome 홈 디렉토리: $javaHome"

    }

    //parameters {
    //    string(defaultValue: '/path/to/java17', description: 'Java 17 설치 경로', name: 'JAVA_HOME')
    //}

    //echo "parameter $JAVA_HOME"

    stage('Git checkout') {
        checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${appBranch}"]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [[$class: 'WipeWorkspace'], [$class: 'LocalBranch', localBranch: '**']],
                userRemoteConfigs: [[url: appRepo]]
        ])
    }

    stage('Build') {
        echo '======> Build'
        withEnv(["JAVA_HOME=$javaHome", "PATH+JDK=$javaHome/bin", "GRADLE_HOME=$gradleHome"]) {
            if (isUnix()) {
                sh 'chmod +x gradlew'
                sh './gradlew clean bootJar'
            } else {
                bat(/"%GRADLE_HOME%\bin\gradle" clean bootJar/)
            }
        }
    }

    stage('Deploy') {
        echo '=========> Deploy'
        sh 'pwd'
    }

    stage('Deploy') {
        sh 'rsync -avz -e "ssh -o StrictHostKeyChecking=no" build/libs/*.jar ec2-user@${env.BACKEND_DEV_IP}:/home/ec2-user/featureToggle/'
    }

    stage('Results') {
        echo '결과를 처리 중입니다.'
        //junit '**/build/test-results/test/*.xml'
        archiveArtifacts 'build/libs/*.jar'
    }

    slackSend(channel: 'C06MHKLH8MD', message: "Job End")
}