package com.dy.docker.maven;

import com.spotify.docker.client.AnsiProgressHandler;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
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
public class BuildMojo extends AMojo {
    /**
     * @parameter expression="${project.build.directory}"
     */
    protected String buildDirectory = null;

    /**
     * @parameter
     */
    private String baseImage = null;

    /**
     * @parameter
     */
    private String executor = null;

    /**
     * @parameter
     */
    private String main = null;

    /**
     * @parameter
     */
    private List<Resource> resources = null;

    /**
     * @parameter
     */
    private String libDirectory = null;

    /**
     * @parameter
     */
    private boolean pullOnBuild = false;

    /**
     * @parameter
     */
    private boolean noCache = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (hosts == null || imageName == null || baseImage == null || executor == null || main == null)
            throw new MojoFailureException("Required Parameter is not provided!");
        log("hosts:" + hosts);
        log("buildDirectory:" + buildDirectory);
        log("libDirectory:" + libDirectory);
        log("baseImage:" + baseImage);
        log("imageName:" + imageName);
        log("executor:" + executor);
        log("main:" + main);
        log("pullOnBuild:" + pullOnBuild);
        log("noCache:" + noCache);
        for (Resource resource : resources) {
            log("resource:" + resource.toString());
        }

        try {
            copyResources();
            createDockerFile();
        } catch (IOException e) {
            error(e);
            throw new MojoExecutionException(e.getMessage());
        }
        for (String h : getHosts()) {
            DockerClient client = new DefaultDockerClient(getDaemonEndPoint(h));
            try {
                client.build(Paths.get(getDestination()), imageName, new AnsiProgressHandler(), buildParams());
            } catch (DockerException | InterruptedException | IOException e) {
                error(e);
                throw new MojoExecutionException(e.getMessage());
            }
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
            if (includes != null && !includes.isEmpty()) {
                for (String i : includes) {
                    doCopyResource(r, targetLib, i);
                }
            } else {
                File f = new File(r.getDirectory());
                for (String s : f.list()) {
                    doCopyResource(r, targetLib, s);
                }
            }
        }
    }

    private void doCopyResource(Resource r, String targetLib, String resourceFile) throws IOException {
        Path dest = Paths.get(getDestination(), targetLib).resolve(resourceFile);
        Path src = Paths.get(r.getDirectory()).resolve(resourceFile);
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);
        debug("Copy Resource:" + src + " -> " + dest);
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
        commands.add("WORKDIR .");
        commands.add("ENTRYPOINT [\"java\", \"-cp\", \"/lib/" + strs[strs.length - 1] + ":/lib/*\"," + " \"" + main + "\"]");
        //commands.add("ENTRYPOINT [\"java\", \"-jar\", \"/lib/" + strs[strs.length - 1] + "\"]");

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

}
