package com.github.atok.deploy;

import com.github.atok.deploy.client.LogInterface;
import com.github.atok.deploy.client.RunnerClient;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.util.Properties;


@Mojo(name = "startupper")
public class StartupperMojo extends AbstractMojo {

    @Parameter(property = "startupper.deploymentConfiguration", required = true)
    private File deploymentConfiguration;

    public void execute() throws MojoExecutionException {
        File f = deploymentConfiguration;
        Properties props = new Properties();

        try(FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
        } catch (IOException e) {
            throw new MojoExecutionException("File not found: " + f, e);
        }

        String secret = (String) props.get("secret");
        if(secret == null) throw new MojoExecutionException("Missing property: secret");

        String host = (String) props.get("host");
        if(host == null) throw new MojoExecutionException("Missing property: host");

        String id = (String) props.get("id");
        if(id == null) throw new MojoExecutionException("Missing property: id");

        String jarFile = (String) props.get("jarFile");
        if(jarFile == null) throw new MojoExecutionException("Missing property: jarFile");

        String configId = (String) props.get("configId");
        if(configId == null) throw new MojoExecutionException("Missing property: configId");

        LogInterface log = new LogInterface() {
            @Override
            public void info(CharSequence text) { getLog().info(text); }
            @Override
            public void error(CharSequence text) { getLog().error(text); }
        };

        RunnerClient client = new RunnerClient(secret, host, log);
        try {
            client.deploy(id, new File(jarFile));
        } catch (UnirestException e) {
            throw new MojoExecutionException("Failed to deploy", e);
        }
    }
}
