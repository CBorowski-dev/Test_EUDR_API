package de.bertelsmann.eudr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.WebServiceMessage;

import javax.xml.namespace.QName;

import eu.europa.ec.tracesnt.eudr.echo.*;

import jakarta.xml.bind.JAXBElement;


public class EchoServiceClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(EchoServiceClient.class);
    
    // Custom interceptor to log SOAP messages
    private static class LoggingClientInterceptor implements ClientInterceptor {
        private static final Logger interceptorLog = LoggerFactory.getLogger(LoggingClientInterceptor.class);
        
        @Override
        public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
            try {
                ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
                messageContext.getRequest().writeTo(requestStream);
                System.out.println("\nSOAP Request XML:");
                System.out.println(requestStream.toString(StandardCharsets.UTF_8));
            } catch (IOException e) {
                interceptorLog.error("Error logging request", e);
            }
            return true;
        }
        
        @Override
        public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
            try {
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                messageContext.getResponse().writeTo(responseStream);
                System.out.println("\nSOAP Response XML:");
                System.out.println(responseStream.toString(StandardCharsets.UTF_8));
            } catch (IOException e) {
                interceptorLog.error("Error logging response", e);
            }
            return true;
        }
        
        @Override
        public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
            try {
                ByteArrayOutputStream faultStream = new ByteArrayOutputStream();
                messageContext.getResponse().writeTo(faultStream);
                interceptorLog.error("SOAP Fault:\n{}", faultStream.toString(StandardCharsets.UTF_8));
            } catch (IOException e) {
                interceptorLog.error("Error logging fault", e);
            }
            return true;
        }
        
        @Override
        public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
            if (ex != null) {
                interceptorLog.error("Exception during SOAP communication", ex);
            }
        }
    }

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
        /**WebServiceMessageCallback callback = message -> {
            // Add the WebServiceClientId element to the SOAP header
            if (message instanceof SoapMessage) {
                SoapMessage soapMessage = (SoapMessage) message;
                SoapHeader header = soapMessage.getSoapHeader();
                
                // Create the WebServiceClientId element with the correct namespace
                QName webServiceClientIdName = new QName("http://ec.europa.eu/sanco/tracesnt/base/v4", "WebServiceClientId", "v4");
                header.addHeaderElement(webServiceClientIdName).setText("eudr-test");
            }
        };*/

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

        EudrEchoResponseType response = (EudrEchoResponseType) template.marshalSendAndReceive(
            "https://acceptance.eudr.webcloud.ec.europa.eu:443/tracesnt/ws/EudrEchoService?testEcho",
            // "http://localhost:8080/xyz",
            jaxbEudrEchoRequestType, 
            callback);

        log.info("\nResponse status: " + response.getStatus());

        return response;
    }
}
