package org.example;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.microsoft.azure.storage.StorageCredentialsToken;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.StorageUri;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    public static String accountName;
    public static String clientId;
    public static String host;
    public static String tokenUri;

    public static String storageUri;
    public static String globalUrl;

    public static void main(String[] args) throws Exception {
        accountName = System.getenv("ACCOUNT_NAME");
        clientId =  System.getenv("AZURE_CLIENT_ID");
        host = System.getenv("HOST");
        tokenUri = System.getenv("TOKENURI");

        storageUri = System.getenv("STORAGE_URL");
        globalUrl = "https://storage.azure.com/";

        System.out.println("new sdk test: ");
        newSdkTest();

        System.out.println("");
        System.out.println("##############");
        System.out.println("");

        System.out.println("old with new sdk test: ");
        oldWithNewToken();

        System.out.println("");
        System.out.println("##############");
        System.out.println("");

        System.out.println("old sdk test: ");
        oldSdkTest();
    }

    public static void oldSdkTest(){
        try {
            MSICredentials msiCreds = new MSICredentials().withClientId(clientId);
            String msiToken = msiCreds.getToken(globalUrl);
            System.out.println("msiCreds: " + msiToken);
            CloudBlobClient client = new CloudBlobClient(new StorageUri(new URI(storageUri)),
                                                         new StorageCredentialsToken(accountName, msiToken));

            for (CloudBlobContainer cc : client.listContainers("")) {
                System.out.println("\t" + cc.getName());
            }
        } catch (Exception e){
            System.out.println(e.getMessage() + e.getStackTrace());
//            throw e;
        }
    }

    public static void newSdkTest(){
        try {
            DefaultAzureCredential creds = new DefaultAzureCredentialBuilder().build();
            AccessToken token = creds.getToken(new TokenRequestContext().addScopes(".default")).block();
            System.out.println("token: " + token.getToken());

            BlobServiceClient client = new BlobServiceClientBuilder()
                    .endpoint(storageUri)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();

            System.out.println("\nListing blobs...");

            // List the blob(s) in the container.
            client.listBlobContainers().stream().limit(5).forEach((blobItem) ->{
                System.out.println("\t" + blobItem.getName());
            });
        } catch (Exception e){
            System.out.println(e.getMessage() + e.getStackTrace());
//            throw e;
        }
    }

    public static void oldWithNewToken() throws URISyntaxException {
        try {
            DefaultAzureCredential creds = new DefaultAzureCredentialBuilder().build();
            AccessToken token = creds.getToken(new TokenRequestContext().addScopes(".default")).block();

//            System.out.println("token: " + token.getToken());
            CloudBlobClient client = new CloudBlobClient(new StorageUri(new URI(storageUri)),
                    new StorageCredentialsToken(accountName, System.getenv("AZURE_TOKEN")));

            for (CloudBlobContainer cc : client.listContainers("")) {
                System.out.println("\t" + cc.getName());
            }
        } catch (Exception e){
            System.out.println(e.getMessage() + e.getStackTrace());
            throw e;
        }
    }
}

