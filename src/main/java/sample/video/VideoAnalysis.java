package sample.video;

import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.NotificationChannel;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VideoAnalysis {
	private static String bucket = "bucketName";
	private static String video = "sample2.mp4";
	private static String queueUrl = "sqsQueueURl";
	private static String topicArn = "topicArn";
	private static String roleArn = "roleArn";
	private static AmazonSQS sqs = null;
	private static AmazonRekognition rek = null;

	private static NotificationChannel channel = new NotificationChannel().withSNSTopicArn(topicArn)
			.withRoleArn(roleArn);

	private static String startJobId = null;

	public static void main(String[] args) throws Exception {
		FileReader reader = new FileReader("src\\main\\resources\\application.properties");
		Properties p = new Properties();
		p.load(reader);

		BasicAWSCredentials credentials = new BasicAWSCredentials(p.getProperty("aws_access_key_id"),
				p.getProperty("aws_secret_access_key"));
		sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_2)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
		rek = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_WEST_2)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		startJobId = LabelRekognition.startLabels(bucket, video,channel,rek);
		analyseVideo(startJobId,rek, 1);

//		startJobId = CelebrityRekognition.startCelebrities(bucket, video,channel,rek);
//		analyseVideo(startJobId,rek, 2);
		
		for (String name : LabelRekognition.objectList) {
			System.out.println(name);
		}
		for (Map.Entry m : CelebrityRekognition.celebList.entrySet()) {
			System.out.println(m.getKey() + " " + m.getValue());
		}

	}

	private static void analyseVideo(String currentStartJobId, AmazonRekognition rek, int a) throws Exception {
		System.out.println("Waiting for job: " + currentStartJobId);
		// Poll queue for messages
		List<Message> messages = null;
		int dotLine = 0;
		boolean jobFound = false;

		// loop until the job status is published. Ignore other messages in
		// queue.
		do {
			messages = sqs.receiveMessage(queueUrl).getMessages();
			if (dotLine++ < 60) {
				System.out.print(".");
			} else {
				System.out.println();
				dotLine = 0;
			}

			if (!messages.isEmpty()) {
				// Loop through messages received.
				for (Message message : messages) {
					String notification = message.getBody();

					// Get status and job id from notification.
					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonMessageTree = mapper.readTree(notification);
					JsonNode messageBodyText = jsonMessageTree.get("Message");
					ObjectMapper operationResultMapper = new ObjectMapper();
					JsonNode jsonResultTree = operationResultMapper.readTree(messageBodyText.textValue());
					JsonNode operationJobId = jsonResultTree.get("JobId");
					JsonNode operationStatus = jsonResultTree.get("Status");
					System.out.println("Job found was " + operationJobId);
					// Found job. Get the results and display.
					if (operationJobId.asText().equals(currentStartJobId)) {
						jobFound = true;
						System.out.println("Job id: " + operationJobId);
						System.out.println("Status : " + operationStatus.toString());
						if (operationStatus.asText().equals("SUCCEEDED")) {
							if (a == 1)
								LabelRekognition.getResultsLabels(currentStartJobId,rek);
							if (a == 2)
								CelebrityRekognition.getResultsCelebrities(currentStartJobId,rek);
							if (a == 3)
								;
						} else {
							System.out.println("Video analysis failed");
						}

						sqs.deleteMessage(queueUrl, message.getReceiptHandle());
					}

					else {
						System.out.println("Job received was not job " + startJobId);
						// Delete unknown message. Consider moving message to
						// dead letter queue
						sqs.deleteMessage(queueUrl, message.getReceiptHandle());
					}
				}
			}
		} while (!jobFound);

		System.out.println("Done!");
	}

}