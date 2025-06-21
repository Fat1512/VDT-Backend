pipeline {
  agent {
    kubernetes {
            yaml """
                apiVersion: v1
                kind: Pod
                spec:
                  # Container 1
                  containers:
                  - name: jnlp
                    image: jenkins/inbound-agent:jdk17
                    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
                    workingDir: /home/jenkins/agent
                    volumeMounts:
                    - name: workspace-volume
                      mountPath: /home/jenkins/agent

                  # Container 2
                  - name: kaniko
                    image: gcr.io/kaniko-project/executor:debug
                    imagePullPolicy: Always
                    command: [sleep]
                    args: [9999999]
                    resources:
                    requests:
                      ephemeral-storage: "2Gi"
                    limits:
                      ephemeral-storage: "3Gi"
                    volumeMounts:
                    - name: workspace-volume
                      mountPath: /home/jenkins/agent
                    - name: docker-config
                      mountPath: /kaniko/.docker/
                  volumes:
                  - name: workspace-volume
                    emptyDir: {}
                  - name: docker-config
                    secret:
                      secretName: dockerhub-credentials
                      items:
                        - key: .dockerconfigjson
                          path: config.json
                """
                
        }
  }

  environment {
      GIT_BRANCH = 'main'
      GIT_CONFIG_REPO_CREDENTIALS_ID = 'github-cred'
      GIT_CONFIG_REPO_URL = 'https://github.com/Fat1512/VDT-Backend'

      SERVICE_AUTH_IMAGE_NAME = 'fat1512/vdt-service-auth'
      SERVICE_CRUD_IMAGE_NAME = 'fat1512/vdt-service-crud'
      
      SERVICE_AUTH_PATH = 'service-auth'
      SERVICE_CRUD_PATH = 'service-crud'
  }

  stages {
    stage('Checkout') {
      steps {
        script {
          echo "Checking out..."
          git url: GIT_CONFIG_REPO_URL, 
              branch: GIT_BRANCH, 
              credentialsId: GIT_CONFIG_REPO_CREDENTIALS_ID
          echo "Checked out."
        }
      }
    }
    stage('Build Services') {
      steps {
        script {
          def gitCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim().substring(0, 8)
          def changedFiles = sh(script: 'git diff-tree --no-commit-id --name-only -r HEAD', returnStdout: true).trim().split('\n')
          env.BUILD_SERVICE_AUTH = changedFiles.any { it.startsWith("${SERVICE_AUTH_PATH}/") } ? 'true' : 'false'
          env.BUILD_SERVICE_CRUD = changedFiles.any { it.startsWith("${SERVICE_CRUD_PATH}/") } ? 'true' : 'false'

            

          parallel(
            "ServiceAuth": {
                if (env.BUILD_SERVICE_AUTH == 'true') {
                  try {
                      container('kaniko') {
                        echo "Building ${SERVICE_AUTH_IMAGE_NAME}:${gitCommit}"
                        sh """
                        /kaniko/executor --context `pwd`/${SERVICE_AUTH_PATH} \\
                                          --dockerfile `pwd`/${SERVICE_AUTH_PATH}/Dockerfile \\
                                          --destination ${SERVICE_AUTH_IMAGE_NAME}:${gitCommit} \\
                                          --cache=true
                        """
                        echo "Build and push for ${SERVICE_AUTH_IMAGE_NAME} successful."
                      }
                  } catch (e) {
                      echo "Service1 build failed: ${e}"
                      error "Stopping pipeline due to Service1 failure"
                  }
              } else {
                  echo "No changes in ${SERVICE_AUTH_PATH}. Skipping build."
              }
            },
            "ServiceCRUD": {
              if (env.BUILD_SERVICE_CRUD == 'true') {
                  try {
                    container('kaniko') {
                        echo "Building ${SERVICE_CRUD_IMAGE_NAME}:${gitCommit}"
                        sh """
                        /kaniko/executor --context `pwd`/${SERVICE_CRUD_PATH} \\
                                          --dockerfile `pwd`/${SERVICE_CRUD_PATH}/Dockerfile \\
                                          --destination ${SERVICE_CRUD_IMAGE_NAME}:${gitCommit} \\
                                          --cache=true
                        """
                        echo "Build and push for ${SERVICE_CRUD_IMAGE_NAME} successful."
                    }
                  } catch (e) {
                      echo "Service2 build failed: ${e}"
                      error "Stopping pipeline due to Service2 failure"
                  }
              } else {
                  echo "No changes in ${SERVICE_CRUD_PATH}. Skipping build."
              }
            }
          )
        }
      }
    }
    stage('Update K8s Manifest Repo') {
      steps {
        script {
          def gitCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim().substring(0, 8)

          echo "Updating K8s Manifests in ${GIT_CONFIG_REPO_URL}..."
          sshagent(credentials: ['repo-access-credentials']) {
            sh 'mkdir -p ~/.ssh && ssh-keyscan github.com >> ~/.ssh/known_hosts'
            sh "git clone -b main git@github.com:Fat1512/VDT-Backend-Config.git"
            
            def updated = false
            dir('VDT-Backend-Config') {
              def imageTag = "${gitCommit}"

              if (env.BUILD_SERVICE_AUTH == 'true') {
                  echo "Updating ServiceAuth manifest with tag: ${imageTag}"
                  sh """
                    sed -i 's|tag: .*|tag: ${imageTag}|g' auth_service_values.yaml
                  """
                  updated = true
              }
    
              if (env.BUILD_SERVICE_CRUD == 'true') {
                  echo "Updating ServiceCRUD manifest with tag: ${imageTag}"
                  sh """
                    sed -i 's|tag: .*|tag: ${imageTag}|g' crud_service_values.yaml
                  """
                  updated = true
              }
    
              if (updated) {
                  sh """
                  git config user.email 'letanphat15122004@gmail.com'
                  git config user.name 'Fat1512'
                  git add -A
                  git commit -m 'ci: Update manifests for commit ${imageTag}'
                  git push origin main
                  """
                  echo "Updated K8s manifests successfully."
              } else {
                  echo "No manifests updated."
              }
            }
          }
        }
      }
    }
  }
  post {
    always {
        cleanWs()
    }
  }
}