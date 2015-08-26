package plugins;

import play.Application;
import play.Logger;
import play.Plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3Plugin extends Plugin {

	public static AmazonS3 amazonS3;
	public static final String AWS_ACCESS_KEY = "aws.access.key";
	public static final String AWS_S3_BUCKET = "aws.s3.bucket";
	public static final String AWS_SECRET_KEY = "aws.secret.key";

	public static String s3Bucket;

	private final Application application;

	public S3Plugin(Application application) {
		this.application = application;
	}

	@Override
	public boolean enabled() {
		return (application.configuration().keys().contains(AWS_ACCESS_KEY)
				&& application.configuration().keys().contains(AWS_SECRET_KEY) && application
				.configuration().keys().contains(AWS_S3_BUCKET));
	}

	@Override
	public void onStart() {
		final String accessKey = application.configuration().getString(
				AWS_ACCESS_KEY);
		final String secretKey = application.configuration().getString(
				AWS_SECRET_KEY);
		s3Bucket = application.configuration().getString(AWS_S3_BUCKET);

		if ((accessKey != null) && (secretKey != null)) {
			final AWSCredentials awsCredentials = new BasicAWSCredentials(
					accessKey, secretKey);
			amazonS3 = new AmazonS3Client(awsCredentials);
			amazonS3.createBucket(s3Bucket);
			Logger.info("Using S3 Bucket: " + s3Bucket);
		}
	}

}