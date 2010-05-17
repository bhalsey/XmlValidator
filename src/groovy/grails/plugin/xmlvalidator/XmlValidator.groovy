package grails.plugin.xmlvalidator

import grails.converters.XML
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletContext
import javax.xml.XMLConstants 
import javax.xml.transform.Source 
import javax.xml.transform.stream.StreamSource 
import groovy.xml.StreamingMarkupBuilder
import javax.xml.validation.Schema 
import javax.xml.validation.SchemaFactory 
import javax.xml.validation.Validator 
import org.xml.sax.SAXException
import org.springframework.context.ApplicationContext

class XmlValidator {
    protected ApplicationContext applicationContext
    protected Map<String, Schema> schemaCache

    public XmlValidator(ApplicationContext applicationContext) {
	this.applicationContext = applicationContext
	schemaCache = new HashMap<String, Schema>()
    }

    /**
     * Expects a schemaInput of type -- xsd string, or a path to an xsd file
     * Throws a SAXException if not valid
     */
    Object validateSchemaAndParse(String schemaInput, HttpServletRequest request ) throws SAXException {
	Schema schema = lookupSchema( schemaInput )

        // begin code we need to execute per thread
        Validator validator = schema.newValidator()

	// read in cached xml if exists (request.XML was previously called)
        String xmlString
	Object xmlObj = request.getAttribute(grails.converters.XML.CACHED_XML);
        if (xmlObj != null) {
	    xmlString = new StreamingMarkupBuilder().bind { out << xmlObj }.toString()
	}
	else {
	    xmlString = request.inputStream.text
	}

        StringReader xmlStringReader = new StringReader(xmlString)
        Source source = new StreamSource(xmlStringReader)
        
	validator.validate(source) // throws SAXException if not valid

	// if it was already cached, return it
	if (xmlObj != null) 
	    return xmlObj

	// Parse and cache it
	xmlObj = XML.parse(xmlString)
	request.setAttribute(grails.converters.XML.CACHED_XML, xmlObj);

	return xmlObj
    }

    /**
     * Look up the schema if we've cached it, otherwise, create a new one.
     * Synchronized because SchemaFactory is not thread safe.
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
