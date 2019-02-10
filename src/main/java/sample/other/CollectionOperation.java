package sample.other;

import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.CreateCollectionResult;
import com.amazonaws.services.rekognition.model.DeleteCollectionRequest;
import com.amazonaws.services.rekognition.model.DeleteCollectionResult;
import com.amazonaws.services.rekognition.model.ListCollectionsRequest;
import com.amazonaws.services.rekognition.model.ListCollectionsResult;

public class CollectionOperation {
	private static AmazonRekognition rekognitionClient = null;

	public static void main(String[] args) throws Exception {

		FileReader reader = new FileReader("src\\main\\resources\\application.properties");
		Properties p = new Properties();
		p.load(reader);

		BasicAWSCredentials credentials = new BasicAWSCredentials(p.getProperty("aws_access_key_id"),
				p.getProperty("aws_secret_access_key"));

		rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_WEST_2)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		// createNewCollection("MyFaceCollection");
		listCollection();
		// deleteCollection("MyFaceCollection");

	}

	public static void deleteCollection(String name) {
		String collectionId = name;

		System.out.println("Deleting collections");

		DeleteCollectionRequest request = new DeleteCollectionRequest().withCollectionId(collectionId);
		DeleteCollectionResult deleteCollectionResult = rekognitionClient.deleteCollection(request);

		System.out.println(collectionId + ": " + deleteCollectionResult.getStatusCode().toString());
	}

	public static void listCollection() {
		System.out.println("Listing collections");
		int limit = 10;
		ListCollectionsResult listCollectionsResult = null;
		String paginationToken = null;
		do {
			if (listCollectionsResult != null) {
				paginationToken = listCollectionsResult.getNextToken();
			}
			ListCollectionsRequest listCollectionsRequest = new ListCollectionsRequest().withMaxResults(limit)
					.withNextToken(paginationToken);
			listCollectionsResult = rekognitionClient.listCollections(listCollectionsRequest);

			List<String> collectionIds = listCollectionsResult.getCollectionIds();
			for (String resultId : collectionIds) {
				System.out.println(resultId);
			}
		} while (listCollectionsResult != null && listCollectionsResult.getNextToken() != null);
	}

	public static void createNewCollection(String name) {
		String collectionId = name;
		System.out.println("Creating collection: " + collectionId);

		CreateCollectionRequest request = new CreateCollectionRequest().withCollectionId(collectionId);

		CreateCollectionResult createCollectionResult = rekognitionClient.createCollection(request);
		System.out.println("CollectionArn : " + createCollectionResult.getCollectionArn());
		System.out.println("Status code : " + createCollectionResult.getStatusCode().toString());
	}

}
