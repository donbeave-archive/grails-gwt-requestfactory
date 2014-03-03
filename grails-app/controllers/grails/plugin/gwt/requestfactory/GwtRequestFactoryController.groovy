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

import com.google.gwt.user.server.rpc.RPCServletUtils
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
class GwtRequestFactoryController {

    public static final String GWT_LANGUAGE = 'X-GWT-Language'

    private static final String JSON_CHARSET = 'UTF-8'
    private static final String JSON_CONTENT_TYPE = 'application/json'

    RequestFactoryService requestFactoryService
    def grailsApplication

    def index = {
        String gwtLanguage = request.getHeader(GWT_LANGUAGE)
        if (gwtLanguage) {
            Locale gwtLocale = new Locale(gwtLanguage)
            RequestContextUtils.getLocaleResolver(request).setLocale(request, response, gwtLocale)
            RequestContextHolder.requestAttributes.setAttribute(GWT_LANGUAGE, gwtLocale, RequestAttributes.SCOPE_REQUEST)
        }
        def jsonRequestString = RPCServletUtils.readContent(request,
                request.contentType != null ? JSON_CONTENT_TYPE : null, JSON_CHARSET)
        if (log.isDebugEnabled()) {
            log.debug(">>>$jsonRequestString")
        }
        try {
            def accessConfig = grailsApplication.config.gwt?.requestfactory?.accessControl

            if (accessConfig) {
                if (accessConfig.allowOrigin) {
                    response.setHeader('Access-Control-Allow-Origin', accessConfig.allowOrigin.toString())
                }

                if (accessConfig.allowMethods) {
                    response.addHeader('Access-Control-Allow-Methods', accessConfig.allowMethods.toString());
                }

                if (accessConfig.allowHeaders) {
                    response.addHeader('Access-Control-Allow-Headers', accessConfig.allowHeaders.toString())
                }
            }

            if (jsonRequestString) {
                def payload = requestFactoryService.process(jsonRequestString)
                if (log.isDebugEnabled()) {
                    log.debug("<<<$payload")
                }

                render(text: payload, contentType: JSON_CONTENT_TYPE, encoding: JSON_CHARSET)
            } else {
                render(text: '', contentType: JSON_CONTENT_TYPE, encoding: JSON_CHARSET)
            }
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            log.error('Unexpected error', e)
        }
    }

}