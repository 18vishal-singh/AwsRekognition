package sample.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import sample.video.VideoAnalysis;

public class LambdaFuctionHandler implements RequestHandler<S3Event, String> {

	public String handleRequest(S3Event event, Context context) {
		new VideoAnalysis();
		return null;
	}
}