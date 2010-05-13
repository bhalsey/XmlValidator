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
    protected Map<String, Schema> schemaCache

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

    public XmlValidatorGrailsPlugin() {
	schemaCache = new HashMap<String, Schema>()
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
	    return validateSchemaAndParse( schemaInput, (HttpServletRequest) delegate )
	}
    }

    /**
     * Expects a schemaInput of type -- xsd string, or a path to an xsd file
     * Throws a SAXException if not valid
     */
    Object validateSchemaAndParse(String schemaInput, HttpServletRequest request ) throws SAXException {
	long time1 = System.currentTimeMillis()
        
	Schema schema = lookupSchema( schemaInput )

        // begin code we need to execute per thread
        Validator validator = schema.newValidator()
	long time2 = System.currentTimeMillis()
	println "Time to build validator in ms: " + (time2 - time1)

	// TODO read in cached xml if exists
	Object xml = request.getAttribute(grails.converters.XML.CACHED_XML);
        if (xml != null) return xml;

        //String xmlString = new StreamingMarkupBuilder().bind { out << xml }.toString()
        String xmlString = request.inputStream.text

        StringReader xmlStringReader = new StringReader(xmlString)
        Source source = new StreamSource(xmlStringReader)
        
	// Validate
	validator.validate(source) // throws exception if not valid

	long time3 = System.currentTimeMillis()
	println "Time to validate in ms: " + (time3 - time2)

	//log.info "request is valid"
	println "request is valid"

	// Now Parse
	def xmlObj = XML.parse(xmlString)
	request.setAttribute(grails.converters.XML.CACHED_XML, xmlObj);

	return xmlObj
    }

    /**
     * Look up the schema if we've cached it, otherwise, create a new one.
     *
     */
    protected synchronized Schema lookupSchema( String schemaInput ) {
	if (schemaCache.containsKey( schemaInput )) {
	    return schemaCache.get( schemaInput )
	}

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

	StreamSource schemaSource 
	if (schemaInput.trim().startsWith("<xs:schema")) {
	    // schema input is an xsd string
	    schemaSource = new StreamSource( new StringReader(schemaInput) )
	}
	else {
	    // schema input is an xsd file
	    def servletContext = applicationContext.servletContext
	    schemaSource = new StreamSource( servletContext.getResourceAsStream(schemaInput) )
	}

        Schema schema = factory.newSchema( schemaSource )
	schemaCache.put( schemaInput, schema )
	return schema
    }

}
