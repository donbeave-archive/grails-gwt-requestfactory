/*
 * Copyright (c) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.gwt.requestfactory

import com.google.web.bindery.requestfactory.server.DefaultExceptionHandler
import com.google.web.bindery.requestfactory.server.GrailsServiceLayer
import com.google.web.bindery.requestfactory.server.ServiceLayer
import com.google.web.bindery.requestfactory.server.SimpleRequestProcessor
import grails.config.Config
import grails.plugin.gwt.requestfactory.DefaultGrailsRequestFactoryProxyGenerator
import grails.plugins.Plugin
import grails.util.Environment

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
class GwtRequestfactoryGrailsPlugin extends Plugin {

    def grailsVersion = '3.0.0 > *'

    def title = 'GWT RequestFactory Plugin'
    def author = 'Alexey Zhokhov'
    def authorEmail = 'donbeave@gmail.com'
    def description = '''\
Controller, services and some classes needed to use [RequestFactory|http://www.gwtproject.org/doc/latest/DevGuideRequestFactory.html] with Grails app.
Based on tutorial by [Peter Quiel|http://qr-thoughts.de/2012/01/requestfactory-with-gwt-2-4-and-grails-2-0-part-i/].
'''

    def profiles = ['web']

    def documentation = 'http://grails.org/plugin/gwt-requestfactory'

    def license = 'APACHE'

    def developers = [[name: 'Alexey Zhokhov', email: 'donbeave@gmail.com']]

    def issueManagement = [system: 'Github', url: 'https://github.com/donbeave/grails-gwt-requestfactory/issues']
    def scm = [url: 'https://github.com/donbeave/grails-gwt-requestfactory/']

    Closure doWithSpring() {
        { ->
            def conf = loadConfig(grailsApplication.config)

            rfValidationService(GrailsServiceLayer) {
                messageSource = ref('messageSource')
            }
            gwtServiceLayer(ServiceLayer, ref('rfValidationService')) { bean ->
                bean.factoryMethod = 'create'
            }
            gwtExceptionHandler(DefaultExceptionHandler) {

            }
            gwtRequestProcessor(SimpleRequestProcessor, ref('gwtServiceLayer')) {
                exceptionHandler = ref('gwtExceptionHandler')
            }

            // Create the proxy generator bean.
            if (conf.generate.indent) {
                gwtProxyGenerator(DefaultGrailsRequestFactoryProxyGenerator, true, conf.generate.indent)
            } else {
                gwtProxyGenerator(DefaultGrailsRequestFactoryProxyGenerator)
            }
        }
    }

    private ConfigObject loadConfig(Config conf) {
        def classLoader = new GroovyClassLoader(getClass().classLoader)
        String environment = Environment.current.name

        // Note here the order of objects when calling merge - merge OVERWRITES values in the target object
        // Load default config as a basis
        ConfigObject newConfig = new ConfigSlurper(environment).parse(
                classLoader.loadClass('DefaultGwtRequestFactoryConfig')
        )

        // Overwrite defaults with what Config.groovy has supplied, perhaps from external files
        newConfig.putAll(conf)

        // Overwrite with contents of GwtRequestFactoryConfig
        try {
            newConfig.merge(new ConfigSlurper(environment).parse(
                    classLoader.loadClass('GwtRequestFactoryConfig'))
            )
        } catch (Exception ignored) {
            // ignore, just use the defaults
        }

        // Now merge our correctly merged DefaultGwtRequestFactoryConfig and GwtRequestFactoryConfig into the main config
        conf.merge(newConfig)

        conf.grails.plugin.gwt.requestfactory
    }

}
