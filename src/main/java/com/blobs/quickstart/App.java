package com.blobs.quickstart;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class App {

    // TODO: Replace <storage-account-name> with your actual storage account name

    public static final String ENDPOINT =  "https://storageaccountmetasich.blob.core.windows.net/";

    public static void main(String[] args) {

        /*
         * The default credential first checks environment variables for configuration
         * If environment configuration is incomplete, it will try managed identity
         */
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

// Azure SDK client builders accept the credential as a parameter
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(ENDPOINT)
                .credential(defaultCredential)
                .buildClient();


        // Create a unique name for the container
        String containerName = "quickstartblobs" + java.util.UUID.randomUUID();

        // Create the container and return a container client object
        BlobContainerClient blobContainerClient = blobServiceClient.createBlobContainer(containerName);


// Create a local file in the ./data/ directory for uploading and downloading
        String localPath = "./data/";
        String fileName = "quickstart" + java.util.UUID.randomUUID() + ".txt";

// Get a reference to a blob
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);


// Write text to the file
        FileWriter writer = null;
        try {
            writer = new FileWriter(localPath + fileName, true);
            writer.write("Hello, World!");
            writer.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        uploadBlob(localPath, fileName, blobClient);


        blobContainerClient = printContainerBlobList(blobServiceClient, containerName);

        printContainerProperties(blobContainerClient);
        setContainerMetadata(blobContainerClient);
        readContainerMetadata(blobContainerClient);


        String downloadFileName = downloadBlobToLocalFile(blobContainerClient, localPath, fileName);

        cleanUpresources(blobContainerClient, localPath, fileName, downloadFileName);

    }

    private static void uploadBlob(String localPath, String fileName, BlobClient blobClient) {
        System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());

// Upload the blob
        try {
            blobClient.uploadFromFile(localPath + fileName);
        } catch (Exception e) {
            System.out.println("Deleting the local source and downloaded files...");
            File localFile = new File(localPath + fileName);
            localFile.delete();        }
    }

    private static BlobContainerClient printContainerBlobList(BlobServiceClient blobServiceClient, String containerName) {
        BlobContainerClient blobContainerClient;
        System.out.println("\nListing blobs...");

        // Create the container and return a container client object
        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
// List the blob(s) in the container.
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            System.out.println("\t" + blobItem.getName());
        }
        return blobContainerClient;
    }

    private static String downloadBlobToLocalFile(BlobContainerClient blobContainerClient, String localPath, String fileName) {
        BlobClient blobClient;
        // Download the blob to a local file

        // Append the string "DOWNLOAD" before the .txt extension for comparison purposes


        String downloadFileName = fileName.replace(".txt", "DOWNLOAD.txt");

        System.out.println("\nDownloading blob to\n\t " + localPath + downloadFileName);
// Get a reference to a blob
        blobClient = blobContainerClient.getBlobClient(fileName);
        blobClient.downloadToFile("./data/" + downloadFileName);
        return downloadFileName;
    }

    private static void cleanUpresources(BlobContainerClient blobContainerClient, String localPath, String fileName, String downloadFileName) {
        // Clean up resources
        File downloadedFile = new File(localPath + downloadFileName);
        File localFile = new File(localPath + fileName);


    /*    System.out.println("\nPress the Enter key to begin clean up");
        System.console().readLine();*/

        System.out.println("Deleting blob container...");
        blobContainerClient.delete();

        System.out.println("Deleting the local source and downloaded files...");
        localFile.delete();
        downloadedFile.delete();

        System.out.println("Done");
    }

    public static void printContainerProperties(BlobContainerClient blobContainerClient) {
        BlobContainerProperties properties = blobContainerClient.getProperties();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                properties.getBlobPublicAccess(),
                properties.hasLegalHold(),
                properties.hasImmutabilityPolicy());
    }

    public static void setContainerMetadata(BlobContainerClient blobContainerClient) {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("docType", "text");
        metadata.put("category", "reference");

        try {
            blobContainerClient.setMetadata(metadata);
            System.out.printf("Set metadata completed %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Failure while setting metadata %n");
        }
    }

    public static void readContainerMetadata(BlobContainerClient blobContainerClient) {
        BlobContainerProperties properties = blobContainerClient.getProperties();

        System.out.printf("Container metadata: %n");
        properties.getMetadata().entrySet().forEach(metadataItem -> {
            System.out.printf(" %s = %s%n", metadataItem.getKey(), metadataItem.getValue());
        });
    }

}
