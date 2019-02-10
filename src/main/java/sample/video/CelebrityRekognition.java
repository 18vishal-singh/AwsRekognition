package sample.video;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.CelebrityDetail;
import com.amazonaws.services.rekognition.model.CelebrityRecognition;
import com.amazonaws.services.rekognition.model.CelebrityRecognitionSortBy;
import com.amazonaws.services.rekognition.model.GetCelebrityRecognitionRequest;
import com.amazonaws.services.rekognition.model.GetCelebrityRecognitionResult;
import com.amazonaws.services.rekognition.model.NotificationChannel;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.StartCelebrityRecognitionRequest;
import com.amazonaws.services.rekognition.model.StartCelebrityRecognitionResult;
import com.amazonaws.services.rekognition.model.Video;

public class CelebrityRekognition {

	static Map<String, Float> celebList = new HashMap<String, Float>();

	public static String startCelebrities(String bucket, String video, NotificationChannel channel,
			AmazonRekognition rek) throws Exception {

		StartCelebrityRecognitionRequest req = new StartCelebrityRecognitionRequest()
				.withVideo(new Video().withS3Object(new S3Object().withBucket(bucket).withName(video)))
				.withNotificationChannel(channel);

		StartCelebrityRecognitionResult startCelebrityRecognitionResult = rek.startCelebrityRecognition(req);
		return startCelebrityRecognitionResult.getJobId();

	}

	public static void getResultsCelebrities(String currentStartJobId, AmazonRekognition rek) throws Exception {

		int maxResults = 10;
		String paginationToken = null;
		GetCelebrityRecognitionResult celebrityRecognitionResult = null;

		do {
			if (celebrityRecognitionResult != null) {
				paginationToken = celebrityRecognitionResult.getNextToken();
			}
			celebrityRecognitionResult = rek.getCelebrityRecognition(
					new GetCelebrityRecognitionRequest().withJobId(currentStartJobId).withNextToken(paginationToken)
							.withSortBy(CelebrityRecognitionSortBy.TIMESTAMP).withMaxResults(maxResults));

			List<CelebrityRecognition> celebs = celebrityRecognitionResult.getCelebrities();

			for (CelebrityRecognition celeb : celebs) {
				CelebrityDetail details = celeb.getCelebrity();
				celebList.put(details.getName(),
						celebList.containsKey(details.getName()) == true
								? Math.max(celebList.get(details.getName()), details.getConfidence())
								: details.getConfidence());
			}
		} while (celebrityRecognitionResult != null && celebrityRecognitionResult.getNextToken() != null);

	}
}
