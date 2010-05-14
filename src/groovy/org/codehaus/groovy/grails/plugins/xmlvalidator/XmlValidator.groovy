package org.codehaus.groovy.grails.plugins.xmlvalidator

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

class XmlValidator {
    protected Map<String, Schema> schemaCache

    public XmlValidator() {
	schemaCache = new HashMap<String, Schema>()
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
        println "inputStream is: ${request.inputStream}"
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
