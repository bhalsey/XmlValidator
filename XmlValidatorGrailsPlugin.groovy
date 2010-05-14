import grails.plugin.xmlvalidator.XmlValidator
import javax.servlet.http.HttpServletRequest
import org.xml.sax.SAXException
import org.springframework.context.ApplicationContext


class XmlValidatorGrailsPlugin {
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
SAXException exception for any validation errors.  Syntax is easy to remember:
change the familiar request.XML access into a method with the schema passed as 
an argument: request.XML( schemaInput )
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/xml-validator"

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
	extendReqResp() 
    }

    def doWithApplicationContext = { applicationContext ->
	xmlValidator = new XmlValidator(applicationContext)
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
     * Add our XML(schemaInput) method to the request metaclass.  The backing method,
     * XmlValidator.validateSchemaAndParse() throws a SAXException when there's a 
     * validation error.
     *
     */
    void extendReqResp() {
	def requestMc = GroovySystem.metaClassRegistry.getMetaClass(HttpServletRequest)

	requestMc.XML << { String schemaInput ->
	    return xmlValidator.validateSchemaAndParse( schemaInput, (HttpServletRequest) delegate )
	}
    }


}
