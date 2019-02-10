package sample.video;

import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.Face;
import com.amazonaws.services.rekognition.model.FaceMatch;
import com.amazonaws.services.rekognition.model.FaceSearchSortBy;
import com.amazonaws.services.rekognition.model.GetFaceSearchRequest;
import com.amazonaws.services.rekognition.model.GetFaceSearchResult;
import com.amazonaws.services.rekognition.model.NotificationChannel;
import com.amazonaws.services.rekognition.model.PersonMatch;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.StartFaceSearchRequest;
import com.amazonaws.services.rekognition.model.StartFaceSearchResult;
import com.amazonaws.services.rekognition.model.Video;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Match a face present in collection from the video we provide.
 *
 */
public class FaceMatching {

	private static String bucket = "rekognition-sample-video-list";
	private static String video = "obamaSample.mp4";
	private static String queueUrl = "sqsQueueURL";
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

		// =================================================
		StartFaceSearchCollection(bucket, video, "MyFaceCollection");
		// =================================================
		System.out.println("Waiting for job: " + startJobId);
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
					if (operationJobId.asText().equals(startJobId)) {
						jobFound = true;
						System.out.println("Job id: " + operationJobId);
						System.out.println("Status : " + operationStatus.toString());
						if (operationStatus.asText().equals("SUCCEEDED")) {
							// ============================================
							GetResultsFaceSearchCollection();
							// ============================================
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

	// Face collection search in video
	// ==================================================================
	private static void StartFaceSearchCollection(String bucket, String video, String collectionId) throws Exception {

		StartFaceSearchRequest req = new StartFaceSearchRequest().withCollectionId(collectionId)
				.withVideo(new Video().withS3Object(new S3Object().withBucket(bucket).withName(video)))
				.withNotificationChannel(channel);

		StartFaceSearchResult startPersonCollectionSearchResult = rek.startFaceSearch(req);
		startJobId = startPersonCollectionSearchResult.getJobId();

	}

	// Face collection search in video
	// ==================================================================
	private static void GetResultsFaceSearchCollection() throws Exception {

		GetFaceSearchResult faceSearchResult = null;
		int maxResults = 10;
		String paginationToken = null;

		do {

			if (faceSearchResult != null) {
				paginationToken = faceSearchResult.getNextToken();
			}

			faceSearchResult = rek.getFaceSearch(new GetFaceSearchRequest().withJobId(startJobId)
					.withMaxResults(maxResults).withNextToken(paginationToken).withSortBy(FaceSearchSortBy.TIMESTAMP));

			// Show search results
			List<PersonMatch> matches = faceSearchResult.getPersons();

			for (PersonMatch match : matches) {
				long milliSeconds = match.getTimestamp();
				System.out.print("Timestamp: " + Long.toString(milliSeconds));
				System.out.println(" Person number: " + match.getPerson().getIndex());
				List<FaceMatch> faceMatches = match.getFaceMatches();
				if (faceMatches != null) {
					System.out.println("Matches in collection...");
					for (FaceMatch faceMatch : faceMatches) {
						Face face = faceMatch.getFace();
						System.out.println("Face Id: " + face.getFaceId());
						System.out.println("Similarity: " + faceMatch.getSimilarity().toString());
						System.out.println();
					}
				}
				System.out.println();
			}

			System.out.println();

		} while (faceSearchResult != null && faceSearchResult.getNextToken() != null);

	}
}
