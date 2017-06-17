grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
    }
    // log level of Ivy resolver, either 'error',
    // 'warn', 'info', 'debug' or 'verbose'
    log "warn"
    // whether to do a secondary resolve on plugin installation,
    // not advised and here for backwards compatibility
    repositories {
        grailsCentral()
        mavenCentral()
        mavenLocal()
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "https://repo.grails.org/grails/plugins/"
    }
    dependencies {
        compile 'com.jcraft:jsch:0.1.53'
    }

    plugins {
        build(":tomcat:$grailsVersion",
              ":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
    }
}
