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
//import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext


class XmlValidatorGrailsPlugin {
    // NO // private ServletContext servletContext; // BH, does grails automatically fill this in??
    //private ServletContext servletContext; // BH, does spring automatically fill this in?? (with impl interface)
    private ApplicationContext applicationContext


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

    // TODO Fill in these fields
    def author = "Brent Halsey"
    def authorEmail = "mrbrent at gmail dot com"
    def title = "Plugin summary/headline"
    def description = '''\\
Validate XML on the request with a given schema.  Throws XXX exception for any validation errors.
In a sense, overloading .XML (really adding a method call with the same name as the read accessor)
Issues: 
    -Not entirely efficient with streams, 
    -breaks cacheing of xml in request (for now)
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/xml-validator"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
	extendReqResp() 
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
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
     * Add our XML() method to the request metaclass
     *
     */
    void extendReqResp() {
	def requestMc = GroovySystem.metaClassRegistry.getMetaClass(HttpServletRequest)
	//def requestMc = GroovySystem.metaClassRegistry.getMetaClass(ApplicationHttpRequest)

	requestMc.XML << { String schemaFile ->
	    // validate with schemaFile
	    //log.error "**** Woohoo! in our plugin! ****"
	    println "**** Woot! in our plugin! ****"
	    // TODO be careful of reading in stream and using it up!
	    // BH - can we reset the stream?
	    //return XML.parse((HttpServletRequest) delegate)
	    return validateSchemaAndParse( schemaFile, (HttpServletRequest) delegate )


	}
//	    org.apache.catalina.core.ApplicationHttpRequest.metaClass.getXMLv = { String name ->
//		    log.error "**** Woohoo! in our plugin! ****"
//		    // TODO be careful of reading in stream and using it up!
//		    return XML.parse((HttpServletRequest) delegate)
//	    }
//
//	    javax.servlet.http.HttpServletRequest.metaClass.getXMLv = { String name ->
//		    log.error "**** Woohoo! in our plugin! ****"
//		    // TODO be careful of reading in stream and using it up!
//		    return XML.parse((HttpServletRequest) delegate)
//	    }
    }

    /**
     * Expects a schema path of type -- 
     * Throws a SAXException if not valid
     */
    Object validateSchemaAndParse(String schemaPath, HttpServletRequest request ) throws SAXException {
	long time1 = System.currentTimeMillis()
        // TODO cache schema object (in controller??)
	// TODO cache xml obj, read in if someone else has already cached it
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

	//ServletContext servletContext = request.servletContext  // only available in later versions of the servlet api
	//def servletContext = ApplicationHolder.application.mainContext.servletContext

	//def servletContext = applicationContext.mainContext.servletContext
	def servletContext = applicationContext.servletContext

        StreamSource schemaSource = new StreamSource( servletContext.getResourceAsStream(schemaPath) )
        Schema schema = factory.newSchema( schemaSource )
        
        // begin code we need to execute per thread
        Validator validator = schema.newValidator()
	long time2 = System.currentTimeMillis()
	println "Time to build validator in ms: " + (time2 - time1)

	// TODO read in cached xml if exists
	Object xml = request.getAttribute(grails.converters.XML.CACHED_XML);
        if (xml != null) return xml;

        //String xmlString = new StreamingMarkupBuilder().bind { out << xml }.toString()
        String xmlString = request.inputStream.text

	// BH, does this work??? --> Nope!
	//request.inputStream.reset()

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

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }


}
