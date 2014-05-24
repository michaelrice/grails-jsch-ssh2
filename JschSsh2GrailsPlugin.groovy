import grails.util.Environment
import org.codehaus.groovy.grails.commons.GrailsApplication

import groovy.util.ConfigObject
import groovy.util.ConfigSlurper


/**
 * Grails plugin providing the JSCH lib with builder for
 * many options making implementation and use easy from
 * a grails application.
 *
 * @author Michael Rice <michael@michaelrice.org>
 * GitHub: https://github.com/michaelrice
 * Website: http://errr-online.com/
 * Licenses: MIT
 * URL: http://opensource.org/licenses/MIT
 */
class JschSsh2GrailsPlugin {
    def version = "0.2"
    def grailsVersion = "2.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "src/docs/**"
    ]

    def title = "Jsch Ssh2 Plugin"
    def author = "Michael Rice"
    def authorEmail = "michael@michaelrice.org"
    def description = 'Provides JSCH SSH library and a couple of handy builders to ease use from a grails app.'
    def documentation = "http://michaelrice.github.io/grails-jsch-ssh2"
    def license = "MIT"
    def issueManagement = [ system: "GITHUB", url: "https://github.com/michaelrice/grails-jsch-ssh2/issues" ]
    def scm = [ url: "https://github.com/michaelrice/grails-jsch-ssh2" ]

    def onShutdown = { event ->
        // TODO Seems like we should make sure we didnt leave any connections open.
        // Need to find out if lib handles that or do we need to?
    }

    def doWithSpring = {
        loadDefaultConfig(application)
    }

    def onConfigChange = {
        loadDefaultConfig(application)
    }

    /**
     * Merges plugin config with host app config, but allowing customization
     * @param app
     */
    private void loadDefaultConfig(GrailsApplication app) {
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().classLoader)

        ConfigObject defaultConfig = new ConfigSlurper(Environment.current.name).parse(classLoader.loadClass('DefaultJschSsh2Config'))

        defaultConfig.merge(app.config)
        app.config.merge(defaultConfig)
    }

}
