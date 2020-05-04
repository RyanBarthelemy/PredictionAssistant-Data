package com.axlor.predictionassistantdd.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * This service handles making the GET request to PredictIt's API and handling the HTTP Response.
 */
@Service
public class DataDownloaderService {

    //hard coded because of this url changed, format of the response would also probably be different. A lot of changes would need to be made then...
    private final String URL = "https://www.predictit.org/api/marketdata/all/";

    /**
     * Makes a GET request to PredictIt API for all market data using the url from 'URL' field.
     * If the response status code is 200, we know the response body is a Json String that can be mapped to a Snapshot object.
     *
     * @return Returns a Json formatted String that can be mapped to a Snapshot object if the response status code is 200, otherwise returns null.
     */
    /*
    public String download() {
        HttpResponse<String> response = getResponse();

        if (response.statusCode() == 200) {
            return response.body();
        }
        if (response.statusCode() == 429) {
            //slow down
            try {
                Thread.sleep(3000);//wait 3s and try again
                response = getResponse();
                if (response.statusCode() == 200) {
                    return response.body();
                }
            } catch (InterruptedException ignored) {
            }
        }
        System.out.println("Debug: Status code = " + response.statusCode());
        return null;
    }
*/
    public String download(){
        Connection.Response response = getResponse();
        if(response == null){
            return null;
        }
        if (response.statusCode() == 200) {
            return response.body();
        }
        if (response.statusCode() == 429) {
            //slow down
            try {
                Thread.sleep(3000);//wait 3s and try again
                response = getResponse();
                if (response.statusCode() == 200) {
                    return response.body();
                }
            } catch (InterruptedException ignored) {
            }
        }
        System.out.println("Debug: Status code = " + response.statusCode());
        return null;
    }

    /* //will not work with java 8...
    private HttpResponse<String> getResponse() {
        //create the http get request object and the client that will handle sending/receiving
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .build();

        //http client sends request, saves response as an HttpResponse<T> object

        HttpResponse<String> response = null;
        try {
            response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }
    */

    private Connection.Response getResponse() {
        try {
            return Jsoup.connect(URL).timeout(3000).ignoreContentType(true).execute();
            //return Jsoup.connect(URL).ignoreContentType(true).get();
        } catch (IOException e) {
            return null; //causing io exception on a 429 status it looks like...
        }
    }
}
