XmlValidator plugin
===================

Summary
-------

Provides a simple mechanism to validate xml on the request with a given schema.
Schema can be passed in as a relative file path or as a string.  Throws
`SAXException` for any validation errors.  Syntax is easy to remember:
change the familiar request.xml access into a method with the schema passed as 
an argument: request.XML( schemaInput )

The XmlValidator plugin simplifies validating incoming xml requests to a
RESTful web service.  The default converters plugin in Grails provides a simple
mechanism to retrieve parsed xml as a GPathResult via the `request.XML`
property accessor.  Depending on what your application does, invalid xml could
cause an obscure exception to be thrown further down the line.  Since we're dealing
with xml, it's natural to validate it with xml schema.  The XmlValidator allows
you to pass in an xml schema string or file path and validate the xml while
you're retrieving it with `request.XML( schemaInput )`.  If the xml doesn't validate, 
a `SAXException` is thrown with a helpful message pointing out the problem in the xml.

The following examples use the same car records xml and schema as the Groovy User Guide's
section on 
[http://groovy.codehaus.org/Validating+XML+with+a+W3C+XML+Schema]
(Validating XML with a W3C XML Schema).

Example without validation:
---------------------------

    class CarController {

        def index = { }

        /** pretend to add cars to something.  Doesn't perform schema validation. */
        def add = {
            def xml = request.XML
            int count = 0
            xml.car.each { car ->
                log.warn "Adding car of make ${car.@make}"
                log.warn "has a record of type: ${car.record.@type} in ${car.record.text()}"
                // add cars here
                count++
            }

            render(contentType: "text/xml") {
                message("Added ${count} cars")
            }
        }

    }

The above example demonstrates a typical usage of the XML accessor Grails
provides to the `request` object in a controller.  If a consumer of your web service
passes in invalid xml,
the forgiving GPathResult often doesn't mind, but your code might.  And without
explicitly checking for valid input, the consumer might receive a confusing
error message.  Cryptic error messages can make it hard to debug the problem.

The next example shows how we can use the XmlValidator plugin to validate incoming xml against
an xml schema.

Example with XmlValidator
-------------------------

    import org.xml.sax.SAXException

    class CarController {

        /** pretend to add cars to something.  Performs schema validation. */
        def addValidate = {
            try {
                def xml = request.XML(carRecordsSchema)

                // test that we actually cached our xml object
                xml = request.XML

                int count = 0
                xml.car.each { car ->
                    log.warn "Adding car of make ${car.@make}"
                    log.warn "has a record of type: ${car.record.@type} in ${car.record.text()}"
                    // add cars here
                    count++
                }

                render(contentType: "text/xml") {
                    message("Added ${count} cars.")
                }
            }
            catch (SAXException e) {
                render(contentType: "text/xml") {
                    message("Error in xml schema: " + e.message)
                }
            }
        }

        String carRecordsSchema = '''
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

Now when we pass in invalid xml, we'll get a message back like
    `<message>Error in xml schema: cvc-complex-type.3.2.2: Attribute 'mmake' is not allowed to appear in element 'car'.</message>`

This error message tells the consumer of your web service exactly what's wrong
with their input.

Alternatively, you can specify the path of your xml schema file.  The file is
loaded as a resource stream by the `ServletContext`, so path names beginning
with a `/` can be found in the `web-app` portion of your Grails application.  In
the next example, we use a schema file located at
`web-app/xsd/car-records.xsd`.  Keeping your schema file here has the added
benefit of making the current schema available to your consumers via a url like
`http://servername:8080/ExampleXmlValidator/xsd/car-records.xsd`

Example with validation and schema file:
----------------------------------------

    import org.xml.sax.SAXException

    class CarController {

        /** pretend to add cars to something.  Performs schema validation. */
        def addValidateSchemaFile = {
            try {
                def xml = request.XML("/xsd/car-records.xsd")

                // test that we actually cached our xml object
                xml = request.XML

                int count = 0
                xml.car.each { car ->
                    log.warn "Adding car of make ${car.@make}"
                    log.warn "has a record of type: ${car.record.@type} in ${car.record.text()}"
                    // add cars here
                    count++
                }

                render(contentType: "text/xml") {
                    message("Added ${count} cars.")
                }
            }
            catch (SAXException e) {
                render(contentType: "text/xml") {
                    message("Error in xml schema: " + e.message)
                }
            }
        }

    }

Notes
-----

The XmlValidator plugin is designed to play well with the caching used by the
default converters plugin.  If you call `request.XML( schemaInput )` and subsequently
make a call to `request.XML`, you'll get the cached GPathResult.  Likewise, you
can call `request.XML` and then make a call to `request.XML( schemaInput )` to
actually validate the xml.

Source and Examples
-------------------

The XmlValidator source code is available at:
[http://github.com/bhalsey/XmlValidator](http://github.com/bhalsey/XmlValidator)

After downloading and extracting the code, run `grails package-plugin` to create the
`grails-xml-validator-0.1.zip` file.  In your own grails application, run 
`grails install-plugin grails-xml-validator-0.1.zip` to install it.

The examples shown here are taken from an example application with sample
curl client scripts available at:
[http://github.com/bhalsey/ExampleXmlValidator](http://github.com/bhalsey/ExampleXmlValidator)
