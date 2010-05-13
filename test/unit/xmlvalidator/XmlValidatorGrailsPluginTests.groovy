package xmlvalidator

import grails.test.*
import org.codehaus.groovy.grails.plugins.testing.*

class XmlValidatorGrailsPluginTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSomething() {
	def plugin = new XmlValidatorGrailsPlugin()

	def xmlString = createXml()
	def mockRequest = new GrailsMockHttpServletRequest()
	mockRequest.getMetaClass().getInputStream = { ->
	    // should be an inputstream, but has a getText() method
	    return new StringReader(xmlString) 
	}
	def schemaInput = createXsd()
	plugin.validateSchemaAndParse( schemaInput, mockRequest )

	assertTrue true

    }

    String createXml() {
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
