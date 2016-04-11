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

import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
 * Default implementation of the proxy generator. It inspects the domain
 * class descriptors and uses that information to write out the proxy
 * files directly. You can specify whether the generated files have
 * native line endings or simply '\n'.
 * Based on code from generate-dto script in Grails DTO plugin.
 *
 * @author Peter Ledbrook
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
class DefaultGrailsRequestFactoryProxyGenerator {

    private final String eol
    private final String indent

    private processed

    Map packageTransforms = [:]

    /**
     * Creates a generator.
     * @param useNativeEol The generator creates files with native line
     * endings if this is <code>true</code>, otherwise it uses '\n'.
     * Default value is <code>true</code>.
     * @param indent The string to use for indenting. Defaults to 4
     * spaces.
     */
    DefaultGrailsRequestFactoryProxyGenerator(boolean useNativeEol = true, String indent = "    ") {
        if (useNativeEol) eol = System.getProperty('line.separator')
        else eol = "\n"

        this.indent = indent
    }

    /**
     * Generates the proxy files for the given domain class.
     * @param dc The domain class to generate a proxy for.
     * @param rootDir The root directory where the proxy files will be
     * generated, e.g. "src/java". Package directories will be created
     * within this directory as needed.
     * @param recursive If <code>true</code>, the method will generate
     * proxies for all related domain classes too. Otherwise only the one
     * proxy for 'dc' will be created.
     */
    void generate(GrailsDomainClass dc, File rootDir, boolean recursive) {
        processed = [] as Set
        generateInternal(dc, rootDir, recursive)
    }

    /**
     * Generates the proxy file for the given domain class, without also
     * generating proxies for related domain classes. Rather than write
     * the proxy to a file, this method will write it out to the given
     * Writer.
     * @param dc The domain class to generate a proxy for.
     * @param writer The writer to use when generating the proxy class.
     */
    Set generateNoRecurse(GrailsDomainClass dc, Writer writer) {
        final dcPkg = dc.clazz.package?.name
        generateNoRecurseInternal(dc, writer, getTargetPackage(dcPkg))
    }

    private void generateInternal(GrailsDomainClass dc, File rootDir, boolean recursive) {
        final targetPkg = getTargetPackage(dc.clazz.package?.name)
        def proxyFile = getProxyFile(rootDir, dc, targetPkg)
        proxyFile.parentFile.mkdirs()

        def writer = new BufferedWriter(new FileWriter(proxyFile))
        try {
            def relations = generateNoRecurseInternal(dc, writer, targetPkg)
            processed << dc.clazz

            if (recursive && relations) {
                relations.each { rel ->
                    if (!processed.contains(rel.clazz)) {
                        generateInternal(rel, rootDir, true)
                    }
                }
            }
        }
        finally {
            if (writer) writer.close()
        }
    }

    private Set generateNoRecurseInternal(GrailsDomainClass dc, Writer writer, String targetPkg) {
        // Deal with the persistent properties.
        def imports = ['com.google.web.bindery.requestfactory.shared.EntityProxy',
                       'com.google.web.bindery.requestfactory.shared.ProxyForName',
                       'com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation']

        def fields = []
        def relations = [] as Set
        def processProperty = { prop ->
            def propType = prop.type
            def field = [name: prop.name]

            if (prop.referencedPropertyType == propType) {
                field['typeString'] = propType.simpleName + (prop.association ? 'Proxy' : '')
                addImportIfNecessary(imports, targetPkg, propType, prop.association)
            } else {
                field['typeString'] = propType.simpleName + '<' + prop.referencedPropertyType.simpleName + (prop.association ? 'Proxy' : '') + '>'
                addImportIfNecessary(imports, targetPkg, propType, false)
                addImportIfNecessary(imports, targetPkg, prop.referencedPropertyType, prop.association)
            }

            // Store the reference domain class if this property is
            // an association. This is so that we can return a set
            // of related domain classes.
            if (prop.association) relations << prop.referencedDomainClass

            fields << field
        }

        processProperty.call(dc.identifier)
        dc.persistentProperties.each(processProperty)

        // Start with the package line.
        if (targetPkg) {
            writer.write "package ${targetPkg};${eol}${eol}"
        }

        // Now add any required imports.
        if (imports) {
            imports.unique().sort().each { str ->
                writer.write "import ${str};${eol}"
            }
            writer.write eol
        }

        writer.write "@ProxyForName(value = \"${dc.fullName}\",${eol}${indent}${indent}locator = \"grails.plugin.gwt.requestfactory.GrailsProxyLocator\")${eol}"
        writer.write "@SkipInterfaceValidation${eol}"

        // Next, the class declaration.
        writer.write "public interface ${dc.shortName}Proxy extends EntityProxy {${eol}"

        // The getters and setters.
        writer.write eol
        fields.each { field ->
            def propSuffix = new StringBuilder(field.name)
            propSuffix.setCharAt(0, Character.toUpperCase(propSuffix.charAt(0)))
            propSuffix = propSuffix.toString()

            writer.write "${indent}${field.typeString} get${propSuffix}();${eol}${eol}"
            writer.write "${indent}void set${propSuffix}(${field.typeString} ${field.name});${eol}${eol}"
        }

        // Class terminator.
        writer.write "}${eol}"

        // All done. Make sure all data has been pushed to the destination
        // before we leave.
        writer.flush()

        relations
    }

    protected void addImportIfNecessary(List imports, String hostPackage, Class clazz, boolean isAssociation) {
        def pkg = isAssociation ? getTargetPackage(clazz.package?.name) : clazz.package?.name
        if (pkg && pkg != hostPackage && pkg != 'java.lang') {
            imports << "${pkg}.${clazz.simpleName}${isAssociation ? 'Proxy' : ''}"
        }
    }

    protected File getProxyFile(File rootDir, GrailsDomainClass dc, String targetPkg) {
        def pkgPath = ''
        if (targetPkg) pkgPath = targetPkg.replace('.', '/') + '/'
        new File(rootDir, "${pkgPath}${dc.shortName}Proxy.java")
    }

    protected String getTargetPackage(final String dcPkg) {
        boolean pkgChanged = false

        String targetPkg = dcPkg ?: ''
        if (packageTransforms) {
            // Find a transform that matches the domain class package.
            // If the default package is in the transforms map (i.e.
            // the empty string is a key), the domain class package
            // must be an exact match. Otherwise, sub-packages match.
            def entry = packageTransforms.find { key, val -> key ? targetPkg?.startsWith(key) : targetPkg == key }
            if (entry) {
                // Found one, so use the associated package name as the
                // target package.
                targetPkg = targetPkg.replace(entry.key, entry.value)
                pkgChanged = true
            } else if (packageTransforms['*']) {
                // Didn't find a matching transform, but did find the
                // wildcard one.
                targetPkg = packageTransforms['*']
                pkgChanged = true
            }
        }

        if (pkgChanged) {
            return targetPkg
        } else {
            return targetPkg.replaceFirst('.server', '.shared')
        }
    }

}
