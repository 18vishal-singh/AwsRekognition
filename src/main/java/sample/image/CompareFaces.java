package sample.image;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;

public class CompareFaces {
	public static void main(String args[]) throws IOException {

		String sourcePic = "obama.jpg";
		String targetPic = "obamaGroup.jpg";
		String bucket = "rekognition-sample-images-list";

		FileReader reader = new FileReader("src\\main\\resources\\application.properties");
		Properties p = new Properties();
		p.load(reader);

		BasicAWSCredentials credentials = new BasicAWSCredentials(p.getProperty("aws_access_key_id"),
				p.getProperty("aws_secret_access_key"));
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("us-west-2")
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		CompareFacesRequest request = new CompareFacesRequest()
				.withSourceImage(new Image().withS3Object(new S3Object().withBucket(bucket).withName(sourcePic)))
				.withTargetImage(new Image().withS3Object(new S3Object().withBucket(bucket).withName(targetPic)))
				.withSimilarityThreshold(90f);
		CompareFacesResult response = rekognitionClient.compareFaces(request);
		for (CompareFacesMatch face : response.getFaceMatches()) {
			System.out.println(face.getFace().getBoundingBox());

		}
	}

}
