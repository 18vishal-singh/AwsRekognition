package sample.image;

import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;

public class DetectText {

	public static void main(String[] args) throws Exception {

		String photo = "12img.PNG";
		String bucket = "rekognition-sample-images-list";

		FileReader reader = new FileReader("src\\main\\resources\\application.properties");
		Properties p = new Properties();
		p.load(reader);

		BasicAWSCredentials credentials = new BasicAWSCredentials(p.getProperty("aws_access_key_id"),
				p.getProperty("aws_secret_access_key"));

		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("us-west-2")
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		DetectTextRequest request = new DetectTextRequest()
				.withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(bucket)));

		try {
			DetectTextResult result = rekognitionClient.detectText(request);
			List<TextDetection> textDetections = result.getTextDetections();

			for (TextDetection text : textDetections) {

				System.out.println("Detected: " + text.getDetectedText());
				System.out.println("Confidence: " + text.getConfidence().toString());

				System.out.println();
			}
		} catch (AmazonRekognitionException e) {
			e.printStackTrace();
		}
	}
}