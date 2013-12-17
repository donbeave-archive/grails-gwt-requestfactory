grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

grails.project.dependency.resolution = {
  dependencyManager.ivySettings.defaultCacheIvyPattern =
      '[organisation]/[module](/[branch])/ivy-[revision](-[classifier]).xml'

  inherits('global') {
  }
  log 'warn'
  repositories {
    grailsPlugins()
    grailsHome()
    grailsCentral()
    mavenCentral()
    grailsRepo "http://grails.org/plugins"
  }
  dependencies {
    compile 'com.google.web.bindery:requestfactory-apt:2.4.0'

    compile 'org.hibernate:hibernate-validator:4.2.0.Final', {
      excludes 'slf4j-log4j12', 'slf4j-api'
    }
  }

  plugins {
    compile ':gwt:0.9.2'

    build(":release:3.0.1",
        ":rest-client-builder:1.0.3") {
      export = false
    }
  }
}

gwt {
  version = '2.4.0'
  dependencies = [
      'org.json:json:20090211'
  ]
}
