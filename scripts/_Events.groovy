eventCreatePluginArchiveStart = { stagingDir ->
    ant.mkdir(dir: "${stagingDir}/lib")
    ant.jar(destfile: "${stagingDir}/lib/grails-gwt-rflayer.jar", basedir: 'target/classes', includes: 'com/**')
    ant.delete(dir: "${stagingDir}/src/groovy/com")
    ant.delete(dir: "${stagingDir}/scripts")
}
