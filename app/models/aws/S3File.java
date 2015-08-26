package models.aws;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import plugins.S3Plugin;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

//@Entity
public class S3File {

	// @Transient
	public File file;

	public UUID id;

	public String name;

	private final String bucket = S3Plugin.s3Bucket;

	public URL getUrl() {
		URL url = null;
		try {
			url = new URL("https://s3.amazonaws.com/" + bucket + "/"
					+ getActualFileName());
		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	public boolean save() {
		if (S3Plugin.amazonS3 == null) {
			Logger.error("Could not save because amazonS3 was null");
			throw new RuntimeException("Could not save");
		} else {
			final PutObjectRequest putObjectRequest = new PutObjectRequest(
					bucket, getActualFileName(), file);
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public
																				// for
																				// all
			S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
			return true;
		}
	}

	private String getActualFileName() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		return "dynamic/" + id + "/" + name;
	}

	public static boolean delete(String str) {
		URL url = null;
		try {
			url = new URL(str);
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		return delete(url);
	}

	public static boolean delete(URL url) {
		if (StringUtils.contains(url.getPath(), "defaultHeroSlider.jpg")
				|| StringUtils.contains(url.getPath(), "defaultProfile.jpg")) {
			Logger.info("The image url is the default, so no deletion will occur since other Objects point to the same default.");
			;
			return false;
		}
		if (S3Plugin.amazonS3 == null) {
			Logger.error("Could not delete because amazonS3 was null");
			throw new RuntimeException("Could not delete");
		} else {
			final String urlStr = url.toString();
			final String actualFileName = StringUtils.substringAfter(urlStr,
					S3Plugin.s3Bucket + "/");
			S3Plugin.amazonS3.deleteObject(S3Plugin.s3Bucket, actualFileName);
			return true;
		}
	}

	public static URL getImage(String propName) {
		final String propPath = Play.application().configuration()
				.getString(propName);
		URL imageURL = null;
		try {
			imageURL = new URL(propPath);
		} catch (final MalformedURLException e) {
			Logger.warn("Unable to find the image path - {}", propPath);
		}
		return imageURL;
	}

}