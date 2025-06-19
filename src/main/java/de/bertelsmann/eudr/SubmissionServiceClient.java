package de.bertelsmann.eudr;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import eu.europa.ec.tracesnt.certificate.eudr.model.v1.OperatorActivityType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.ActivityType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.AssociatedStatementsType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.CommercialDescriptionType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.CommodityType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.CountryType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.DueDiligenceStatementBaseType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.GoodsMeasureType;
import eu.europa.ec.tracesnt.certificate.eudr.model.v1.ProducerType;
import eu.europa.ec.tracesnt.certificate.eudr.submission.v1.*;
import jakarta.xml.bind.JAXBElement;

public class SubmissionServiceClient extends ServiceClient {

    public static final String URI = "https://acceptance.eudr.webcloud.ec.europa.eu:443/tracesnt/ws/EUDRSubmissionServiceV1";
    // public static final String URI = "http://localhost:8082/xyz";

    private static final Logger log = LoggerFactory.getLogger(SubmissionServiceClient.class);
    
    /**
     * 
     * @return
     */
    public SubmitStatementResponseType getSubmissionServiceResponse() {

        ObjectFactory objectFactory = new ObjectFactory();
        SubmitStatementRequestType request = objectFactory.createSubmitStatementRequestType();

        // set Operator Type
        request.setOperatorType(OperatorActivityType.OPERATOR);

        eu.europa.ec.tracesnt.certificate.eudr.model.v1.ObjectFactory objectFactoryDDS = new eu.europa.ec.tracesnt.certificate.eudr.model.v1.ObjectFactory();
        DueDiligenceStatementBaseType dds = objectFactoryDDS.createDueDiligenceStatementBaseType();

        // set internal reference number
        dds.setInternalReferenceNumber("BER-000010");

        // set Activity Type
        dds.setActivityType(ActivityType.DOMESTIC);

        // set HSHeading
        CommodityType commodityType = new CommodityType();
        CommercialDescriptionType cdType = new CommercialDescriptionType();
        cdType.setDescriptionOfGoods("Rentenbescheid [Druckzeugsergebnis]");
        GoodsMeasureType gmType = new GoodsMeasureType();
        gmType.setNetWeight(new BigDecimal("7560"));
        cdType.setGoodsMeasure(gmType);
        commodityType.setDescriptors(cdType);

        commodityType.setHsHeading("490110");

        // set Producers
        List<ProducerType> producers = commodityType.getProducers();
        ProducerType producer = new ProducerType();
        producer.setCountry("SE");
        producer.setName("Papermill 2");
        producer.setGeometryGeojson("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"ProductionPlace\":\"Farm 2\"},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[3.537598,44.840291],[4.855957,46.377254],[5.427246,44.964798],[3.537598,44.840291]]]}}]}".getBytes(StandardCharsets.UTF_8));
        producers.add(producer);

        dds.getCommodities().add(commodityType);

        // set Associated Statements
        AssociatedStatementsType associatedStatementsType = new AssociatedStatementsType();
        associatedStatementsType.setReferenceNumber("25DEEPK0O98738");
        associatedStatementsType.setVerificationNumber("YXNAGQHJ");
        dds.getAssociatedStatements().add(associatedStatementsType);

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

        JAXBElement<SubmitStatementResponseType> response = (JAXBElement<SubmitStatementResponseType>) template.marshalSendAndReceive(
            SubmissionServiceClient.URI,
            jaxbSubmitStatementRequestType, 
            callback);

        return response.getValue();
    }

    /**
     * 
     * @param ddsIdentifier
     * @return
     */
    public StatementModificationResponseType getAmendServiceResponse(String ddsIdentifier) {

        ObjectFactory objectFactory = new ObjectFactory();
        AmendStatementRequestType request = objectFactory.createAmendStatementRequestType();

        // set DDS Identifier
        request.setDdsIdentifier(ddsIdentifier);

        eu.europa.ec.tracesnt.certificate.eudr.model.v1.ObjectFactory objectFactoryDDS = new eu.europa.ec.tracesnt.certificate.eudr.model.v1.ObjectFactory();
        DueDiligenceStatementBaseType dds = objectFactoryDDS.createDueDiligenceStatementBaseType();

        // set internal reference number
        dds.setInternalReferenceNumber("BER-000010");

        // set Activity Type
        dds.setActivityType(ActivityType.DOMESTIC);

        // set Country of Activity
        dds.setCountryOfActivity(CountryType.DE);

        // set HSHeading
        CommodityType commodityType = new CommodityType();
        CommercialDescriptionType cdType = new CommercialDescriptionType();
        cdType.setDescriptionOfGoods("Rentenbescheid [Druckzeugsergebnis]");
        GoodsMeasureType gmType = new GoodsMeasureType();
        gmType.setNetWeight(new BigDecimal("7560"));
        cdType.setGoodsMeasure(gmType);
        commodityType.setDescriptors(cdType);

        commodityType.setHsHeading("490110");

        // set Producers
        List<ProducerType> producers = commodityType.getProducers();
        ProducerType producer = new ProducerType();
        producer.setCountry("SE");
        producer.setName("Papermill 2");
        producer.setGeometryGeojson("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"ProductionPlace\":\"Farm 2\"},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[3.537598,44.840291],[4.855957,46.377254],[5.427246,44.964798],[3.537598,44.840291]]]}}]}".getBytes(StandardCharsets.UTF_8));
        producers.add(producer);

        dds.getCommodities().add(commodityType);

        // set Associated Statements
        AssociatedStatementsType associatedStatementsType = new AssociatedStatementsType();
        associatedStatementsType.setReferenceNumber("25DEEPK0O98738");
        associatedStatementsType.setVerificationNumber("YXNAGQHJ");
        dds.getAssociatedStatements().add(associatedStatementsType);

        request.setStatement(dds);

        JAXBElement<AmendStatementRequestType> jaxbAmendStatementRequestType = objectFactory.createAmendStatementRequest(request);


        SoapActionCallback callback = new SoapActionCallback("http://ec.europa.eu/tracesnt/certificate/eudr/submission/amendDds") {

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

        JAXBElement<StatementModificationResponseType> response = (JAXBElement<StatementModificationResponseType>) template.marshalSendAndReceive(
            SubmissionServiceClient.URI,
            jaxbAmendStatementRequestType, 
            callback);

        return response.getValue();
    }

        /**
     * 
     * @param ddsIdentifier
     * @return
     */
    public StatementModificationResponseType getRetractServiceResponse(String ddsIdentifier) {

        ObjectFactory objectFactory = new ObjectFactory();
        RetractStatementRequestType request = objectFactory.createRetractStatementRequestType();

        // set DDS Identifier
        request.setDdsIdentifier(ddsIdentifier);

        JAXBElement<RetractStatementRequestType> jaxbRetractStatementRequestType = objectFactory.createRetractStatementRequest(request);


        SoapActionCallback callback = new SoapActionCallback("http://ec.europa.eu/tracesnt/certificate/eudr/submission/retractDds") {

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
        
        log.info("Requesting response from EUDR Retract Service");

        JAXBElement<StatementModificationResponseType> response = (JAXBElement<StatementModificationResponseType>) template.marshalSendAndReceive(
            SubmissionServiceClient.URI,
            jaxbRetractStatementRequestType, 
            callback);

        return response.getValue();
    }

}
