Boolean STAGE1 = true
Boolean STAGE2 = true
Boolean STAGE3 = true

pipeline {
    agent { label "AGENT_LABEL" }

    options {
        timestamps()
        parallelsAlwaysFailFast()
    }
    parameters {
        string(name: 'REPOS', defaultValue: '', description: '')
        string(name: 'VERSION', defaultValue: '', description: '')
    }
    stages {
        stage('Build list of Projects') {
            steps {
                script {
                    def Projects = [
                            [
                                    'ProjectName'   : 'HERMON',
                                    'JobPath'       : 'Mountains/Hermon',
                            ],
                            [
                                    'ProjectName'   : 'CARMEL',
                                    'JobPath'       : 'Mountains/Carmel',
                            ],
                            [
                                    'ProjectName'   : 'TAVOR',
                                    'JobPath'       : 'Mountains/Tavor',
                            ]
                    ]

                    env.PROJECTS = writeJSON(json: Projects, returnText: true)
                } //script
            } //steps
        } //stage

        stage('Build project jobs') {
            steps {
                script {
                    if (!env.PROJECTS) {
                        error('env.PROJECTS (list of Projects to build) not found')
                    }

                    def Projects = readJSON(text: env.PROJECTS)

                    for (int i = 0; i < Projects.size(); i++) {
                        Object project = Projects[i]

                        echo ("Running down stream project: ${project['ProjectName']}")

                        if (project.ProjectName == "CARMEL"){
                            STAGE2 = false
                        }

                        def projectBuild = build(
                            job: "${project.JobPath}",
                            parameters: [
                                string(name: 'REPOS', value: env.REPOS),
                                string(name: 'VERSION', value: env.VERSION),

                                booleanParam(name: 'EXECUTE_STAGE1', value: STAGE1),
                                booleanParam(name: 'EXECUTE_STAGE2', value: STAGE2),
                                booleanParam(name: 'EXECUTE_STAGE3', value: STAGE3)
                            ]
                        )

                        echo ("< Built project ${project.ProjectName} with status: ${projectBuild.result}")
                    } //for-loop

                    echo("Running parallel Downstream Project Builds")
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
