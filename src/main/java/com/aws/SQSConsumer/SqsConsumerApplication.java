package com.aws.SQSConsumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@SpringBootApplication
public class SqsConsumerApplication  implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqsConsumerApplication.class);
	private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/your-account-id/your-queue-name";

	private final SqsClient sqsClient;

	public SqsConsumerApplication() {
		this.sqsClient = SqsClient.builder()
				.region(Region.US_EAST_1)
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}

	public static void main(String[] args) {

		SpringApplication.run(SqsConsumerApplication.class, args);
	}

	@Override
	public void run(String... args) {
		LOGGER.info("Starting SQS Consumer...");
		while (true) {
			try {
				ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
						.queueUrl(QUEUE_URL)
						.maxNumberOfMessages(10)
						.waitTimeSeconds(20)
						.build());

				List<Message> messages = response.messages();
				if (!messages.isEmpty()) {
					for (Message message : messages) {
						LOGGER.info("Processing message: {}", message.body());
						// Process the message here

						// Delete message after processing
						sqsClient.deleteMessage(DeleteMessageRequest.builder()
								.queueUrl(QUEUE_URL)
								.receiptHandle(message.receiptHandle())
								.build());
					}
				} else {
					LOGGER.info("No messages in queue, sleeping...");
					Thread.sleep(5000);  // Reduce polling rate
				}
			} catch (Exception e) {
				LOGGER.error("Error processing SQS messages", e);
			}
		}
	}

}
