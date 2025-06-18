package de.bertelsmann.eudr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

public class ServiceClient extends WebServiceGatewaySupport {

    // Custom interceptor to log SOAP messages
    public static class LoggingClientInterceptor implements ClientInterceptor {
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
    
}
