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
package com.google.web.bindery.requestfactory.server

import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

import javax.validation.ConstraintViolation
import javax.validation.Path
import java.lang.annotation.ElementType

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
class GrailsServiceLayer extends ServiceLayerDecorator {

    public static final String GWT_LANGUAGE = 'X-GWT-Language'

    def messageSource

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T domainObject) {
        try {
            if (!domainObject.validate()) {
                List<ConstraintViolation<T>> violations = []
                Errors errors = domainObject.errors
                Locale locale = resolveLocale()
                for (FieldError fieldError : errors.fieldErrors) {
                    ConstraintViolation<T> violation = convertFieldError(fieldError, locale, domainObject)
                    violations << violation
                }
                return violations
            }
        } catch (Exception e) {
            // Nothing to do.. validate method does not exists. Thats ok.
        }
        return super.validate(domainObject)
    }

    private <T> ConstraintViolation<T> convertFieldError(FieldError fieldError, Locale locale, T domainObject) {
        String messageTemplate = fieldError.codes?.length > 0 ? fieldError.codes[0] : fieldError.code
        String interpolatedMessage = messageSource.getMessage(fieldError, locale)
        T rootBean = domainObject
        Path path = PathImpl.createPathFromString(fieldError.field)
        Object invalidValue = fieldError.rejectedValue
        Class<T> rootBeanClass = domainObject.class
        ConstraintViolation<T> violation = new ConstraintViolationImpl<T>(messageTemplate, interpolatedMessage,
                rootBeanClass, rootBean, domainObject,
                invalidValue, path, null, ElementType.FIELD)
        return violation
    }

    static Locale resolveLocale() {
        RequestContextHolder.requestAttributes.getAttribute(GWT_LANGUAGE, RequestAttributes.SCOPE_REQUEST)
    }

}