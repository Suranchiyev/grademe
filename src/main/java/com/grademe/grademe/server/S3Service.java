package com.grademe.grademe.server;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.grademe.grademe.util.FileUtils;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class S3Service {
    public void getProject(String projectId, File destination) throws Exception{
        Regions clientRegion = Regions.US_EAST_1;
        String bucketName = "grademe-input";
        String key = "projects/"+projectId+"/project.zip";

        AWSCredentials credentials = new BasicAWSCredentials(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY")
        );

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        try(S3Object fullObject = s3Client.getObject(new GetObjectRequest(bucketName, key))){

            // Get an object
            System.out.println("Downloading an object");
            System.out.println("Content-Type: " + fullObject.getObjectMetadata().getContentType());
            System.out.println("Content: ");

            FileUtils.writeStream(fullObject.getObjectContent(),destination);

        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
