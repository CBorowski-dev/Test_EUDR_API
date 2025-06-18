package de.bertelsmann.eudr;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.StatementInfoType;
import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.SupplierStatement;
import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.GetStatementByIdentifiersRequest;
import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.GetStatementByIdentifiersResponse;
import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.GetStatementInfoRequestType;
import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.GetStatementInfoResponseType;
import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.ObjectFactory;
import jakarta.xml.bind.JAXBElement;

public class RetrievalServiceClient extends ServiceClient {

    public static final String URI = "https://acceptance.eudr.webcloud.ec.europa.eu:443/tracesnt/ws/EUDRRetrievalServiceV1";
    // public static final String URI = "http://localhost:8082/xyz";

    private static final Logger log = LoggerFactory.getLogger(RetrievalServiceClient.class);
    
    /**
     * 
     * @param ddsIdentifier
     * @return
     */
    public List<StatementInfoType> getDdsInfoServiceResponse(String ddsIdentifier) {

        ObjectFactory objectFactory = new ObjectFactory();
        GetStatementInfoRequestType request = objectFactory.createGetStatementInfoRequestType();
        request.getIdentifier().add(ddsIdentifier);

        JAXBElement<GetStatementInfoRequestType> jaxbGetStatementInfoRequestType = objectFactory.createGetStatementInfoRequest(request);

        SoapActionCallback callback = new SoapActionCallback("http://ec.europa.eu/tracesnt/certificate/eudr/retrieval/getDdsInfo") {

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
        
        log.info("Requesting response from EUDR Retrieval Service (method getDdsInfo)");

        JAXBElement<GetStatementInfoResponseType> response = (JAXBElement<GetStatementInfoResponseType>) template.marshalSendAndReceive(
            RetrievalServiceClient.URI,
            jaxbGetStatementInfoRequestType, 
            callback);

        return response.getValue().getStatementInfo();
    }

    /**
     * 
     * @param referenceNumber
     * @param verificationNumber
     * @return
     */
    public SupplierStatement getStatementByIdentifiersServiceResponse(String referenceNumber, String verificationNumber) {

        ObjectFactory objectFactory = new ObjectFactory();
        GetStatementByIdentifiersRequest request = objectFactory.createGetStatementByIdentifiersRequest();
        request.setReferenceNumber(referenceNumber);
        request.setVerificationNumber(verificationNumber);

        JAXBElement<GetStatementByIdentifiersRequest> jaxbGetStatementByIdentifiersRequest = objectFactory.createGetStatementByIdentifiersRequest(request);

        SoapActionCallback callback = new SoapActionCallback("http://ec.europa.eu/tracesnt/certificate/eudr/eudr4authorities/getStatementByIdentifiers") {

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
        
        log.info("Requesting response from EUDR Retrieval Service (method getStatementByIdentifiers)");

        JAXBElement<GetStatementByIdentifiersResponse> response = (JAXBElement<GetStatementByIdentifiersResponse>) template.marshalSendAndReceive(
            RetrievalServiceClient.URI,
            jaxbGetStatementByIdentifiersRequest, 
            callback);

        return response.getValue().getStatement();
    }

}
