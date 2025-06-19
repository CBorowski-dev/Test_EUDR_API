package de.bertelsmann.eudr;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.StatementInfoType;
import eu.europa.ec.tracesnt.certificate.eudr.submission.v1.SubmitStatementResponseType;
import eu.europa.ec.tracesnt.certificate.eudr.retrieval.v1.SupplierStatement;
import eu.europa.ec.tracesnt.certificate.eudr.submission.v1.StatementModificationResponseType;
import eu.europa.ec.tracesnt.eudr.echo.EudrEchoResponseType;

@SpringBootApplication
public class EudrApplication {

	public static void main(String[] args) {
		SpringApplication.run(EudrApplication.class, args);
	}

/* 	@Bean
	CommandLineRunner lookup(EchoServiceClient client) {
		return args -> {
			// For Test 1
			String query = "Hello";
			EudrEchoResponseType response = client.getEchoServiceResponse(query);
			System.out.println(response.getStatus());
		};
	}
 */
	@Bean
	CommandLineRunner lookup(SubmissionServiceClient client) {
		return args -> {
			// For Test 2, 4, 5, 8 and 9 (submitDds)
			// SubmitStatementResponseType submitResponse = client.getSubmissionServiceResponse();
			// System.out.println(submitResponse.getDdsIdentifier());

			// For Test 6 (amendDds)
			String ddsIdentifier = "7de67128-3a1d-4a37-a5ce-29d8fc5c5475";
			StatementModificationResponseType amendResponse = client.getAmendServiceResponse(ddsIdentifier);
			System.out.println(amendResponse.getStatus());

			// For Test 8 (retractDds)
			// String ddsIdentifier = "af578328-e426-4eba-a189-c76b18a1a46c";
			// StatementModificationResponseType amendResponse = client.getRetractServiceResponse(ddsIdentifier);
			// System.out.println(amendResponse.getStatus());
		};
	}

/*  	@Bean
	CommandLineRunner lookup(RetrievalServiceClient client) {
		return args -> {
			// For Test 3
			// String ddsIdentifier = "6c58a09c-20d9-4a91-9300-74b4a71d8dc5";
			// List<StatementInfoType> statementInfos = client.getDdsInfoServiceResponse(ddsIdentifier);
			// for (StatementInfoType entry : statementInfos) {
			// 	System.out.println(entry.getIdentifier());
			// 	System.out.println(entry.getInternalReferenceNumber());
			// 	System.out.println(entry.getReferenceNumber());
			// 	System.out.println(entry.getVerificationNumber());
			// 	System.out.println(entry.getStatus());
			// 	System.out.println(entry.getDate());
			// }

			// For Test 7
			String referenceNumber = "25DEEPK0O98738";
			String verificationNumber = "YXNAGQHJ";
			SupplierStatement statement = client.getStatementByIdentifiersServiceResponse(referenceNumber, verificationNumber);
		};
	}
 */}
