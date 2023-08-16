import groovy.transform.Field

@Field static final String MAIN_CI_RESULT_PASS = '+1'
@Field static final String MAIN_CI_RESULT_FAIL = '-1'
@Field static final String MAIN_CI_RESULT_NULL = '0'
@Field Boolean STAGE1 = true
@Field Boolean STAGE2 = true
@Field Boolean STAGE3 = true
@Field Boolean STAGE4 = true

pipeline {
    agent { label AGENT_LABEL }

    options {
        timestamps()
        buildDiscarder(logRotator(daysToKeepStr: '30', artifactDaysToKeepStr: '7'))
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
            failFast true

            steps {
                script {
                    if (!env.PROJECTS) {
                        error('env.PROJECTS (list of Projects to build) not found')
                    }

                    def Projects = readJSON(text: env.PROJECTS)

                    // Create downstream jobs
                    def parallelProjects = [:]

                    for (int i = 0; i < Projects.size(); i++) {
                        Object project = Projects[i]

                        parallelProjects[project['ProjectName']] = {

                            if (project.ProjectName == "CARMEL"){
                                STAGE2 = false
                            }

                            def projectBuild = build(
                                job: "${project.JobPath}",
                                propagate: false,
                                parameters: [
                                    string(name: 'REPOS', value: env.REPOS),
                                    string(name: 'VERSION', value: env.VERSION),

                                    booleanParam(name: 'EXECUTE_STAGE1', value: STAGE1),
                                    booleanParam(name: 'EXECUTE_STAGE2', value: STAGE2),
                                    booleanParam(name: 'EXECUTE_STAGE3', value: STAGE3)
                                ]
                            )

                            echo "< Built project ${project.ProjectName} with status: ${projectBuild.result}"
                        } //parallelProjects
                    } //for-loop

                    echo('running parallel Downstream Project Builds')
                    parallel(parallelProjects)

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
