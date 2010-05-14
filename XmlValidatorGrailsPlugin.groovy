import org.codehaus.groovy.grails.plugins.xmlvalidator.XmlValidator
import grails.converters.XML
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletContext
import javax.xml.XMLConstants 
import javax.xml.transform.Source 
import javax.xml.transform.stream.StreamSource 
import javax.xml.validation.Schema 
import javax.xml.validation.SchemaFactory 
import javax.xml.validation.Validator 
import org.xml.sax.SAXException
import org.springframework.context.ApplicationContext


class XmlValidatorGrailsPlugin {
    protected ApplicationContext applicationContext
    protected XmlValidator xmlValidator

    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Brent Halsey"
    def authorEmail = "mrbrent at gmail dot com"
    def title = "Plugin summary/headline"
    def description = '''
Provides a simple mechanism to validate XML on the request with a given schema.
Schema can be passed in as a relative file path or as a string.  Throws
SAXException exception for any validation errors.  In a sense, overloads .XML
(really adding a method call with the same name as the read accessor).
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/xml-validator"

    // Constructor
    public XmlValidatorGrailsPlugin() {
	xmlValidator = new XmlValidator()
    }

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
	extendReqResp() 
    }

    def doWithApplicationContext = { applicationContext ->
	this.applicationContext = applicationContext
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.

	extendReqResp() 
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    /**
     * Add our XML(schemaInput) method to the request metaclass
     *
     */
    void extendReqResp() {
	def requestMc = GroovySystem.metaClassRegistry.getMetaClass(HttpServletRequest)

	requestMc.XML << { String schemaInput ->
	    return xmlValidator.validateSchemaAndParse( schemaInput, (HttpServletRequest) delegate )
	}
    }


}
