eventCreatePluginArchiveStart = { stagingDir ->
    def appVersion = event.plugin.version
    ant.mkdir(dir: "${stagingDir}/lib")
    ant.jar(destfile: "${stagingDir}/lib/grails-gwt-rflayer-${appVersion}.jar", basedir: 'target/classes', includes: 'com/**')
    ant.delete(dir: "${stagingDir}/src/groovy/com")
    ant.delete {
        fileset(dir: "${stagingDir}/scripts/", includes: '_Events.groovy')
    }
}
