package de.bertelsmann.eudr;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.WebServiceMessage;

import javax.xml.namespace.QName;

import eu.europa.ec.tracesnt.eudr.echo.*;

import jakarta.xml.bind.JAXBElement;


public class EchoServiceClient extends ServiceClient {

    public static final String URI = "https://acceptance.eudr.webcloud.ec.europa.eu:443/tracesnt/ws/EudrEchoService";
    // public static final String URI = "http://localhost:8082/xyz";

    private static final Logger log = LoggerFactory.getLogger(EchoServiceClient.class);
    
    public EudrEchoResponseType getEchoServiceResponse(String query) {

        ObjectFactory objectFactory = new ObjectFactory();
        EudrEchoRequestType request = objectFactory.createEudrEchoRequestType();
        request.setQuery(query);

        JAXBElement<EudrEchoRequestType> jaxbEudrEchoRequestType = objectFactory.createEudrEchoRequest(request);

        SoapActionCallback callback = new SoapActionCallback("http://ec.europa.eu/tracesnt/eudr/echo") {

            @Override
            public void doWithMessage(WebServiceMessage message) throws IOException {
                // Add the WebServiceClientId element to the SOAP header
                if (message instanceof SoapMessage) {
                    SoapMessage soapMessage = (SoapMessage) message;
                    SoapHeader header = soapMessage.getSoapHeader();
                    
                    // Create the WebServiceClientId element with the correct namespace
                    QName webServiceClientIdName = new QName("http://ec.europa.eu/sanco/tracesnt/base/v4", "WebServiceClientId", "v4");
                    header.addHeaderElement(webServiceClientIdName).setText("eudr-test");
                }
                super.doWithMessage(message);
            }

        };

        // Use the standard WebServiceTemplate
        WebServiceTemplate template = getWebServiceTemplate();
        
        // Create a custom interceptor to log the request and response
        ClientInterceptor[] interceptors = template.getInterceptors();
        
        // Add a logging interceptor
        ClientInterceptor[] newInterceptors;
        if (interceptors != null) {
            newInterceptors = new ClientInterceptor[interceptors.length + 1];
            System.arraycopy(interceptors, 0, newInterceptors, 0, interceptors.length);
            newInterceptors[interceptors.length] = new LoggingClientInterceptor();
        } else {
            newInterceptors = new ClientInterceptor[] { new LoggingClientInterceptor() };
        }
        
        template.setInterceptors(newInterceptors);
        
        log.info("Requesting response from EUDR Echo Service");

        JAXBElement<EudrEchoResponseType> response = (JAXBElement<EudrEchoResponseType>) template.marshalSendAndReceive(
            EchoServiceClient.URI,
            jaxbEudrEchoRequestType, 
            callback);

        log.info("\nResponse status: " + response.getValue().getStatus());

        return response.getValue();
    }
}
