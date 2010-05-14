package grails.plugin.xmlvalidator

import grails.test.*
import org.codehaus.groovy.grails.plugins.testing.*
import grails.plugin.xmlvalidator.XmlValidator
import org.xml.sax.SAXException

class XmlValidatorTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testInvalidXml() {
	def plugin = new XmlValidator()

	def xmlString = createInvalidXml()

        // create a mock request with the getInputStream() method
        // overridden to return a reader of our string
	def mockRequest = new GrailsMockHttpServletRequest()
	GrailsMockHttpServletRequest.metaClass.getInputStream = { ->
	    // should be an inputstream, but also has a getText() method
	    return new StringReader(xmlString) 
	}

	def schemaInput = createXsd()

        try {
            plugin.validateSchemaAndParse( schemaInput, mockRequest )
            fail "validate method should have thrown an exception"
        }
        catch (SAXException se) {
            assertTrue true
        }

    }

    void testValidXml() {
	def plugin = new XmlValidator()

	def xmlString = createValidXml()

        // create a mock request with the getInputStream() method
        // overridden to return a reader of our string
	GrailsMockHttpServletRequest.metaClass.getInputStream = { ->
	    // should be an inputstream, but also has a getText() method
	    return new StringReader(xmlString) 
	}
	def mockRequest = new GrailsMockHttpServletRequest()

	def schemaInput = createXsd()

        try {
            plugin.validateSchemaAndParse( schemaInput, mockRequest )
            assertTrue true
        }
        catch (SAXException se) {
            fail "validate method should NOT have thrown an exception"
        }

    }

    String createValidXml() {
	return '''
<records>
  <car name='HSV Maloo' make='Holden' year='2006'>
    <country>Australia</country>
    <record type='speed'>Production Pickup Truck with speed of 271kph</record>
  </car>
  <car name='P50' make='Peel' year='1962'>
    <country>Isle of Man</country>
    <record type='size'>Smallest Street-Legal Car at 99cm wide and 59 kg in weight</record>
  </car>
  <car name='Royale' make='Bugatti' year='1931'>
    <country>France</country>
    <record type='price'>Most Valuable Car at $15 million</record>
  </car>
</records>
'''
    }

    String createInvalidXml() {
	return '''
<records>
  <car name='HSV Maloo' mmake='Holden' year='2006'>
    <country>Australia</country>
    <record type='speed'>Production Pickup Truck with speed of 271kph</record>
  </car>
  <car name='P50' make='Peel' year='1962'>
    <country>Isle of Man</country>
    <record type='size'>Smallest Street-Legal Car at 99cm wide and 59 kg in weight</record>
  </car>
  <car name='Royale' make='Bugatti' year='1931'>
    <country>France</country>
    <record type='price'>Most Valuable Car at $15 million</record>
  </car>
</records>
'''
    }

    String createXsd() {
	return '''
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
  <xs:element name="records">
    <xs:complexType>
      <xs:sequence>
        <xs:element maxOccurs="unbounded" ref="car"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>
  <xs:element name="car">
    <xs:complexType>
      <xs:sequence>
        <xs:element ref="country"/>
        <xs:element ref="record"/>
      </xs:sequence>
      <xs:attribute name="make" use="required" type="xs:NCName"/>
      <xs:attribute name="name" use="required"/>
      <xs:attribute name="year" use="required" type="xs:integer"/>
    </xs:complexType>
  </xs:element>
  <xs:element name="country" type="xs:string"/>
  <xs:element name="record">
    <xs:complexType mixed="true">
      <xs:attribute name="type" use="required" type="xs:NCName"/>
    </xs:complexType>
  </xs:element>
</xs:schema>
'''
    }
}
