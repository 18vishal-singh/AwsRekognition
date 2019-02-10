package sample.video;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.GetLabelDetectionRequest;
import com.amazonaws.services.rekognition.model.GetLabelDetectionResult;
import com.amazonaws.services.rekognition.model.LabelDetection;
import com.amazonaws.services.rekognition.model.LabelDetectionSortBy;
import com.amazonaws.services.rekognition.model.NotificationChannel;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.StartLabelDetectionRequest;
import com.amazonaws.services.rekognition.model.StartLabelDetectionResult;
import com.amazonaws.services.rekognition.model.Video;

public class LabelRekognition {
	static Set<String> objectList = new HashSet<String>();

	public static String startLabels(String bucket, String video, NotificationChannel channel, AmazonRekognition rek)
			throws Exception {

		StartLabelDetectionRequest req = new StartLabelDetectionRequest()
				.withVideo(new Video().withS3Object(new S3Object().withBucket(bucket).withName(video)))
				.withMinConfidence(50F).withJobTag("DetectingLabels").withNotificationChannel(channel);

		StartLabelDetectionResult startLabelDetectionResult = rek.startLabelDetection(req);
		return startLabelDetectionResult.getJobId();

	}

	public static void getResultsLabels(String currentStartJobId, AmazonRekognition rek) throws Exception {

		int maxResults = 10;
		String paginationToken = null;
		GetLabelDetectionResult labelDetectionResult = null;

		do {
			if (labelDetectionResult != null) {
				paginationToken = labelDetectionResult.getNextToken();
			}

			GetLabelDetectionRequest labelDetectionRequest = new GetLabelDetectionRequest().withJobId(currentStartJobId)
					.withSortBy(LabelDetectionSortBy.TIMESTAMP).withMaxResults(maxResults)
					.withNextToken(paginationToken);

			labelDetectionResult = rek.getLabelDetection(labelDetectionRequest);

			// Show labels, confidence and detection times
			List<LabelDetection> detectedLabels = labelDetectionResult.getLabels();

			for (LabelDetection detectedLabel : detectedLabels) {
				// long seconds = detectedLabel.getTimestamp();
				// System.out.print("Millisecond: " + Long.toString(seconds) + "
				// ");
				// System.out.println("\t" + detectedLabel.getLabel().getName()
				// + " \t"
				// + detectedLabel.getLabel().getConfidence().toString());
				// System.out.println();
				objectList.add(detectedLabel.getLabel().getName());
			}
		} while (labelDetectionResult != null && labelDetectionResult.getNextToken() != null);

	}

}
