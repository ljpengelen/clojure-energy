pipeline {
  agent none

  options {
    ansiColor("xterm")
  }

  stages {
    stage("Test") {
      agent {
        dockerfile {
          additionalBuildArgs "--pull"
          filename "dockerfiles/ci/Dockerfile"
        }
      }

      steps {
        sh "lein fig:ci"
      }
    }

    stage("Deploy") {
      agent {
        dockerfile {
          additionalBuildArgs "--pull"
          filename "dockerfiles/ci/Dockerfile"
        }
      }

      steps {
        sh "lein fig:min"
      }
    }
  }
}
