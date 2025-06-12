package de.bertelsmann.eudr;

import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

@Configuration
public class EchoServiceConfiguration {

    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor security = new Wss4jSecurityInterceptor();
        security.setSecurementActions(WSHandlerConstants.TIMESTAMP + " " + WSHandlerConstants.USERNAME_TOKEN);
        security.setSecurementPasswordType(WSConstants.PW_DIGEST);
        security.setSecurementTimeToLive(60);
        security.setSecurementUsername("<login>");
        security.setSecurementPassword("<password>");
        security.setSecurementUsernameTokenNonce(true);
        return security;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // this package must match the package in the <generatePackage> specified in pom.xml
        marshaller.setContextPath("eu.europa.ec.tracesnt.eudr.echo");
        return marshaller;
    }

    @Bean
    public EchoServiceClient echoServiceClient(Jaxb2Marshaller marshaller) {
        EchoServiceClient client = new EchoServiceClient();
        client.setDefaultUri("https://acceptance.eudr.webcloud.ec.europa.eu:443/tracesnt/ws/EudrEchoService?testEcho");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.setInterceptors(new ClientInterceptor[]{ securityInterceptor() });
        return client;
    }

}
