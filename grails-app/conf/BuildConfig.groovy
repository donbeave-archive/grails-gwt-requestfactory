grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    inherits('global') {
    }
    log 'warn'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        compile 'com.google.web.bindery:requestfactory-apt:2.6.1'

        runtime 'org.hibernate:hibernate-validator:4.2.0.Final', {
            excludes 'slf4j-log4j12', 'slf4j-api'
        }
    }

    plugins {
        build ':extended-dependency-manager:0.5.5'
        build ':tomcat:7.0.53', ':release:3.0.1', ':rest-client-builder:2.0.1', {
            export = false
        }

        compile ':gwt:1.0', {
            transitive = false
        }

        runtime ':resources:1.2.8'
    }
}

gwt {
    version = '2.6.1'
    dependencies = [
            'org.json:json:20140107'
    ]
}

if (System.getProperty('java.version').startsWith('1.8')) {
    gwt.javac.cmd = 'javac'
}
