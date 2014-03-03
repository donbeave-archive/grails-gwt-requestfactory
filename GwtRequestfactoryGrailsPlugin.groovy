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
import com.google.web.bindery.requestfactory.server.DefaultExceptionHandler
import com.google.web.bindery.requestfactory.server.ServiceLayer
import com.google.web.bindery.requestfactory.server.SimpleRequestProcessor

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
class GwtRequestfactoryGrailsPlugin {

    def version = '0.1.3'
    def grailsVersion = '2.0 > *'
    def pluginExcludes = [
            'grails-app/views/*',
            'web-app/*',
    ]

    def title = 'GWT RequestFactory Plugin'
    def author = 'Alexey Zhokhov'
    def authorEmail = 'donbeave@gmail.com'
    def description = '''\
Controller, services and some classes needed to use [RequestFactory|http://www.gwtproject.org/doc/latest/DevGuideRequestFactory.html] with Grails app.
Based on tutorial by [Peter Quiel|http://qr-thoughts.de/2012/01/requestfactory-with-gwt-2-4-and-grails-2-0-part-i/].
'''

    def documentation = 'http://grails.org/plugin/gwt-requestfactory'

    def license = 'APACHE'

    def developers = [[name: 'Alexey Zhokhov', email: 'donbeave@gmail.com']]

    def issueManagement = [system: 'Github', url: 'https://github.com/donbeave/grails-gwt-requestfactory/issues']
    def scm = [url: 'https://github.com/donbeave/grails-gwt-requestfactory/']

    def loadAfter = ['gwt']

    def doWithSpring = {
        gwtServiceLayer(ServiceLayer, ref('rfValidationService')) { bean ->
            bean.factoryMethod = 'create'
        }
        gwtExceptionHandler(DefaultExceptionHandler) {

        }
        gwtRequestProcessor(SimpleRequestProcessor, ref('gwtServiceLayer')) {
            exceptionHandler = ref('gwtExceptionHandler')
        }
    }

}
