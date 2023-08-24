pipeline {
    agent { label "AGENT_LABEL" }

    options {
        timestamps()
    }
    parameters {
        string(name: 'REPOS', defaultValue: '', description: '')
        string(name: 'VERSION', defaultValue: '', description: '')
        booleanParam(name: 'EXECUTE_STAGE1', defaultValue: '', description: '')
        booleanParam(name: 'EXECUTE_STAGE2', defaultValue: '', description: '')
        booleanParam(name: 'EXECUTE_STAGE3', defaultValue: '', description: '')
    }
    stages {

        stage('Build project jobs') {
            steps {
                script {
                    
                    echo("Got value REPOS = ${params.REPOS}")
                    echo("Got value VERSION = ${params.VERSION}")

                    if (params.EXECUTE_STAGE1 == true) {
                      echo ("STAGE1 is True")
                    }
                  
                    if (params.EXECUTE_STAGE2 == true) {
                      echo ("STAGE2 is True")
                    }
                  
                    if (params.EXECUTE_STAGE3 == true) {
                      echo ("STAGE3 is True")
                    }
                } //script
            } //steps
        } //stage
    }
    post {
        cleanup {
            script {
                cleanWs(deleteDirs: true, notFailBuild: true)
            }
        }
    }
} // pipeline
