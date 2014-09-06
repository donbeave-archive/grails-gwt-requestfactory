/*
 * Copyright (c) 2014 the original author or authors.
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

import com.google.web.bindery.requestfactory.shared.Locator

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
class GrailsProxyLocator<T> extends Locator<T, String> {

    public T create(Class<? extends T> clazz) {
        clazz.newInstance()
    }

    public T find(Class<? extends T> clazz, String id) {
        clazz.get(id)
    }

    public Class<T> getDomainType() {
        throw new UnsupportedOperationException() //unused
    }

    public String getId(T domainObject) {
        domainObject.id.toString()
    }

    public Class<String> getIdType() {
        String.class
    }

    public Object getVersion(T domainObject) {
        domainObject.version ? domainObject.version : 0l
    }

}
