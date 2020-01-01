package com.grademe.grademe.server;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.grademe.grademe.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class S3Service {
    @Autowired
    private Environment env;

    public void getProject(String projectId, File destination, int retryNumber) throws InterruptedException{
        try{
            getProject(projectId,destination);
        }catch (Exception e){
            if(retryNumber > 0){
                retryNumber--;
                Thread.sleep(5000);
                getProject(projectId,destination,retryNumber);
            }else {
                throw new RuntimeException("Error while getting Project from S3 bucket");
            }
        }
    }

    public void getProject(String projectId, File destination) throws Exception{
        Regions clientRegion = Regions.US_EAST_1;
        String bucketName = "grademe-input";
        String key = "projects/"+projectId+"/project.zip";

        S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;
        try {

//            AWSCredentials credentials = new BasicAWSCredentials(
//                    env.getProperty("AWS_ACCESS_KEY_ID"),
//                    env.getProperty("AWS_SECRET_ACCESS_KEY")
//            );

            AWSCredentials credentials = new BasicAWSCredentials(
                    System.getenv("AWS_ACCESS_KEY_ID"),
                    System.getenv("AWS_SECRET_ACCESS_KEY")
            );

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();

            // Get an object
            System.out.println("Downloading an object");
            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, key));
            System.out.println("Content-Type: " + fullObject.getObjectMetadata().getContentType());
            System.out.println("Content: ");

            FileUtils.writeStream(fullObject.getObjectContent(),destination);

        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
            if (fullObject != null) {
                fullObject.close();
            }
            if (objectPortion != null) {
                objectPortion.close();
            }
            if (headerOverrideObject != null) {
                headerOverrideObject.close();
            }
        }
    }


}
