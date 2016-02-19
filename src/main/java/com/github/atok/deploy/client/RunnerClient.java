package com.github.atok.deploy.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;

public class RunnerClient {

    private final LogInterface log;
    private final String secret;
    private final String host;

    public RunnerClient(String secret, String host, LogInterface log) {
        this.secret = secret;
        this.host = host;
        this.log = log;
    }

    public void deploy(String id, File file) throws UnirestException {
        log.info("Uploading JAR file " + file + " with id:" + id);
        uploadJar(file, id);
        log.info("Sending start command");
        start(id);
        ProcessStatus status = checkStatus(id);
        log.info(status.toString());
        if(!status.running) {
            throw new UnirestException("Deployment failed: process died");
        }

        try {
            log.info("Waiting 3 seconds...");
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            throw new UnirestException(e);
        }
        status = checkStatus(id);
        log.info(status.toString());
        if(!status.running) {
            throw new UnirestException("Deployment failed: process died");
        }
    }

    void start(String id) throws UnirestException {
        HttpResponse<String> response = Unirest.post(host + "/jar/" + id + "/run")
                .header("authorization", secret)
                .asString();

        System.out.println("Start: " + response.getBody());
    }

    void uploadJar(File file, String id) throws UnirestException {
        if(!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file);
        }

        HttpResponse<String> response = Unirest.post(host + "/jar/" + id)
                .header("authorization", secret)
                .field("file", file)
                .asString();

        System.out.println("UploadJar: " + response.getBody());
    }

    ProcessStatus checkStatus(String id) throws UnirestException {
        HttpResponse<String> response = Unirest.get(host + "/jar/" + id)
                .header("authorization", secret)
                .asString();

        Gson gson = new Gson();

        try {
            return gson.fromJson(response.getBody(), ProcessStatus.class);
        } catch (JsonSyntaxException e) {
            throw new UnirestException("Bad JSON: " + response.getBody());
        }
    }

}
