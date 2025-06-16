package de.bertelsmann.eudr;

import java.util.HashMap;
import java.util.Map;

import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

import jakarta.xml.soap.SOAPMessage;

@Configuration
public class SubmissionServiceConfiguration {


    @Bean (name = "submissionServiceMessageFactory")
    public SaajSoapMessageFactory messageFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(SOAPMessage.WRITE_XML_DECLARATION, "true");

        SaajSoapMessageFactory msgFactory = new SaajSoapMessageFactory();
        msgFactory.setMessageProperties(props);
        msgFactory.setSoapVersion(org.springframework.ws.soap.SoapVersion.SOAP_11);

        return msgFactory;
    }

    @Bean (name = "submissionServiceSecurityInterceptor")
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor security = new Wss4jSecurityInterceptor();
        security.setSecurementActions(WSHandlerConstants.TIMESTAMP + " " + WSHandlerConstants.USERNAME_TOKEN);
        security.setSecurementPasswordType(WSConstants.PW_DIGEST);
        security.setSecurementTimeToLive(60);
        security.setSecurementUsername("n00j5mmh");
        security.setSecurementPassword("9QLJq19HFW4QSFyyeW4hBSivrehQlhb7xn2DjljT");
        security.setSecurementUsernameTokenNonce(true);
        return security;
    }

    @Bean (name = "submissionServiceMarshaller")
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // this package must match the package in the <generatePackage> specified in pom.xml
        marshaller.setContextPath("eu.europa.ec.tracesnt.certificate.eudr.submission.v1");
        return marshaller;
    }

    @Bean
    public SubmissionServiceClient submissionServiceClient(Jaxb2Marshaller marshaller) {
        SubmissionServiceClient client = new SubmissionServiceClient();
        client.setDefaultUri("https://acceptance.eudr.webcloud.ec.europa.eu:443/tracesnt/ws/EUDRSubmissionServiceV1");
        // client.setDefaultUri("http://localhost:8080/xyz");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.setInterceptors(new ClientInterceptor[]{ securityInterceptor() });
        client.setMessageFactory(messageFactory());
        return client;
    }
}
