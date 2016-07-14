package com.dy.docker.maven;

import com.spotify.docker.client.AnsiProgressHandler;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal build
 * @phase package
 *
 * Created by yidong on 7/13/2016.
 */
public class DockerMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.build.directory}"
     */
    protected String buildDirectory;

    /**
     * @parameter
     */
    private String imageName;

    /**
     * @parameter
     */
    private String baseImage;

    /**
     * @parameter
     */
    private String executor;

    /**
     * @parameter
     */
    private List<Resource> resources;

    /**
     * @parameter
     */
    private String libDirectory;

    /**
     * @parameter
     */
    private boolean pullOnBuild = false;

    /**
     * @parameter
     */
    private boolean noCache = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (imageName == null || baseImage == null || executor == null)
            throw new MojoFailureException("Required Parameter is not provided!");
        log("buildDirectory:" + buildDirectory);
        log("libDirectory:" + libDirectory);
        log("baseImage:" + baseImage);
        log("imageName:" + imageName);
        log("executor:" + executor);
        log("pullOnBuild:" + pullOnBuild);
        log("noCache:" + noCache);
        for (Resource resource : resources) {
            log("resource:" + resource.toString());
        }

        DockerClient client = new DefaultDockerClient("http://localhost:2375");
        try {
            copyResources();
            createDockerFile();
            client.build(Paths.get(getDestination()), imageName, new AnsiProgressHandler(), buildParams());
        } catch (DockerException | InterruptedException | IOException e) {
            error(e);
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private String getDestination() throws IOException {
        Path dockerPath = Paths.get(buildDirectory, "docker");
        Files.createDirectories(dockerPath);
        return dockerPath.toString();
    }

    private void copyResources() throws IOException {
        String targetLib = "./lib";
        Files.createDirectories(Paths.get(getDestination(), "lib"));
        for (Resource r : resources) {
            List<String> includes = r.getIncludes();
            for (String i : includes) {
                Path dest = Paths.get(getDestination(), targetLib).resolve(i);
                Files.copy(Paths.get(r.getDirectory()).resolve(i), dest, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    private void createDockerFile()
            throws IOException {
        final List<String> commands = new ArrayList();
        if (baseImage != null) {
            commands.add("FROM " + baseImage);
        }
        commands.add("MAINTAINER Dong Yi");
        if (libDirectory != null) {
            commands.add(String.format("COPY %s /lib/", libDirectory));
        }
        commands.add("COPY ./lib /lib/");
        String[] strs = executor.split("/");
        commands.add("ENTRYPOINT [\"java\", \"-jar\", \"/lib/" + strs[strs.length - 1] + "\"]");

        log("Writing Dockerfile...");
        // this will overwrite an existing file
        Files.write(Paths.get(getDestination(), "Dockerfile"), commands, StandardCharsets.UTF_8);
    }

    private DockerClient.BuildParam[] buildParams()
            throws UnsupportedEncodingException {
        final List<DockerClient.BuildParam> buildParams = new ArrayList();
        if (pullOnBuild) {
            buildParams.add(DockerClient.BuildParam.pullNewerImage());
        }
        if (noCache) {
            buildParams.add(DockerClient.BuildParam.noCache());
        }
        return buildParams.toArray(new DockerClient.BuildParam[buildParams.size()]);
    }

    private void log(String str) {
        getLog().info(str);
    }

    private void error(Throwable e) {
        getLog().error(e);
    }
}
