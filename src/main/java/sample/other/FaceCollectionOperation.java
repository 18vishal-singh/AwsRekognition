package sample.other;

import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DeleteFacesRequest;
import com.amazonaws.services.rekognition.model.DeleteFacesResult;
import com.amazonaws.services.rekognition.model.Face;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.ListFacesRequest;
import com.amazonaws.services.rekognition.model.ListFacesResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FaceCollectionOperation {
	private static AmazonRekognition rekognitionClient = null;
	public static String collectionId = "";

	public static final String faces[] = { "faceID" }; // id
																						// to
																						// be
																						// deleted.
	public static final String bucket = "bucketName";
	public static final String photo = "obama.jpg";

	public static void main(String[] args) throws Exception {

		FileReader reader = new FileReader("src\\main\\resources\\application.properties");
		Properties p = new Properties();
		p.load(reader);

		BasicAWSCredentials credentials = new BasicAWSCredentials(p.getProperty("aws_access_key_id"),
				p.getProperty("aws_secret_access_key"));

		rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_WEST_2)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		collectionId = "MyFaceCollection";
		// addFaceToCollection(collectionId);
		listFaceOfCollection(collectionId);
		// deleteFaceFromCollectino(collectionId);
	}

	/**
	 * Delete a face with a faceId(provided at top).
	 * 
	 * @param collectionId2
	 */
	private static void deleteFaceFromCollectino(String collectionId2) {
		DeleteFacesRequest deleteFacesRequest = new DeleteFacesRequest().withCollectionId(collectionId2)
				.withFaceIds(faces);

		DeleteFacesResult deleteFacesResult = rekognitionClient.deleteFaces(deleteFacesRequest);

		List<String> faceRecords = deleteFacesResult.getDeletedFaces();
		System.out.println(Integer.toString(faceRecords.size()) + " face(s) deleted:");
		for (String face : faceRecords) {
			System.out.println("FaceID: " + face);
		}
	}

	/**
	 * List all the faces present in colleciton.
	 * 
	 * @param collectionId2
	 * @throws JsonProcessingException
	 */
	private static void listFaceOfCollection(String collectionId2) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();

		ListFacesResult listFacesResult = null;
		System.out.println("Faces in collection " + collectionId);

		String paginationToken = null;
		do {
			if (listFacesResult != null) {
				paginationToken = listFacesResult.getNextToken();
			}

			ListFacesRequest listFacesRequest = new ListFacesRequest().withCollectionId(collectionId2).withMaxResults(1)
					.withNextToken(paginationToken);

			listFacesResult = rekognitionClient.listFaces(listFacesRequest);
			List<Face> faces = listFacesResult.getFaces();
			for (Face face : faces) {
				System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(face));
			}
		} while (listFacesResult != null && listFacesResult.getNextToken() != null);
	}

	/**
	 * It will add face collection from the image provided in s3
	 * 
	 * @param collectionid2
	 */
	private static void addFaceToCollection(String collectionid2) {

		Image image = new Image().withS3Object(new S3Object().withBucket(bucket).withName(photo));

		IndexFacesRequest indexFacesRequest = new IndexFacesRequest().withImage(image).withCollectionId(collectionid2)
				.withExternalImageId(photo).withDetectionAttributes("ALL");

		IndexFacesResult indexFacesResult = rekognitionClient.indexFaces(indexFacesRequest);

		System.out.println(photo + " added");
		List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
		for (FaceRecord faceRecord : faceRecords) {
			System.out.println("Face detected: Faceid is " + faceRecord.getFace().getFaceId());
		}
	}

}
