package de.bertelsmann.eudr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import eu.europa.ec.tracesnt.certificate.eudr.model.v1.OperatorActivityType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.SupplementaryUnitQualifier;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.ActivityType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.CommercialDescriptionType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.CommodityType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.DueDiligenceStatementBaseType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.GoodsMeasureType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.ProducerType;
import eu.europa.ec.tracesnt.certificate.eudr.submission.v1.*;
import jakarta.xml.bind.JAXBElement;

public class SubmissionServiceClient extends WebServiceGatewaySupport {

    // public static final String URI = "https://acceptance.eudr.webcloud.ec.europa.eu:443/tracesnt/ws/EUDRSubmissionServiceV1";
    public static final String URI = "http://localhost:8082/xyz";

    private static final Logger log = LoggerFactory.getLogger(SubmissionServiceClient.class);
    
    // Custom interceptor to log SOAP messages
    private static class LoggingClientInterceptor implements ClientInterceptor {
        private static final Logger interceptorLog = LoggerFactory.getLogger(LoggingClientInterceptor.class);
        
        @Override
        public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
            try {
                // HttpUrlConnection connection = (HttpUrlConnection) TransportContextHolder.getTransportContext().getConnection();
                // connection.addRequestHeader("Accept", "application/soap+xml");

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

    public SubmitStatementResponseType getServiceResponse() {

        ObjectFactory objectFactory = new ObjectFactory();
        SubmitStatementRequestType request = objectFactory.createSubmitStatementRequestType();

        // set Operator Type
        request.setOperatorType(OperatorActivityType.OPERATOR);

        eu.europa.ec.tracesnt.certificate.eudr.model.v1.ObjectFactory objectFactoryDDS = new eu.europa.ec.tracesnt.certificate.eudr.model.v1.ObjectFactory();
        DueDiligenceStatementBaseType dds = objectFactoryDDS.createDueDiligenceStatementBaseType();

        // set internal reference number
        dds.setInternalReferenceNumber("BER-000001");

        // set Activity Type
        dds.setActivityType(ActivityType.DOMESTIC);

        // set HSHeading
        CommodityType commodityType = new CommodityType();
        CommercialDescriptionType cdType = new CommercialDescriptionType();
        cdType.setDescriptionOfGoods("Buch [Druckzeugsergebnis]");
        GoodsMeasureType gmType = new GoodsMeasureType();
        //gmType.setNetWeight(new BigDecimal("2000"));
        gmType.setSupplementaryUnit(new BigInteger("2000"));
        gmType.setSupplementaryUnitQualifier(SupplementaryUnitQualifier.DTN);
        cdType.setGoodsMeasure(gmType);
        commodityType.setDescriptors(cdType);

        commodityType.setHsHeading("490110");

        List<ProducerType> producers = commodityType.getProducers();
        ProducerType producer = new ProducerType();
        producer.setCountry("DE");
        producer.setName("Mohn Media Mohndruck GmbH");
        // see https://stevage.github.io/geojson-spec/
        byte[] geometryGeojson = Base64.getEncoder().encode("{ \"type\": \"Point\",\"coordinates\": [ 51.907644, 08.411583 ] }".getBytes());
        producer.setGeometryGeojson(geometryGeojson);
        producers.add(producer);

        dds.getCommodities().add(commodityType);
        request.setStatement(dds);

        JAXBElement<SubmitStatementRequestType> jaxbSubmitStatementRequestType = objectFactory.createSubmitStatementRequest(request);


        SoapActionCallback callback = new SoapActionCallback("http://ec.europa.eu/tracesnt/certificate/eudr/submission/submitDds") {

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
        
        log.info("Requesting response from EUDR Submission Service");

        SubmitStatementResponseType response = (SubmitStatementResponseType) template.marshalSendAndReceive(
            URI,
            jaxbSubmitStatementRequestType, 
            callback);

        log.info("\nDDS Identifier: " + response.getDdsIdentifier());

        return response;
    }

}
