package com.dy.docker.maven;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @goal exec
 * @phase install
 *
 * Created by yidong on 8/4/2016.
 */
public class ExecutionMojo extends AMojo {
    private static final int SECONDS_WAIT_BEFORE_KILLING = 5;

    /**
     * @parameter
     */
    private boolean bDockerCompose = false;

    /**
     * @parameter
     */
    private String bindedPorts = null;

    /**
     * @parameter
     */
    private String volumeMapper = null;

    /**
     * @parameter
     */
    private int doStartThen1Stop0 = 0;

    /**
     * @parameter
     */
    private String containerId = null;

    /**
     * @parameter
     */
    private long memory = 0L;

    /**
     * @parameter
     */
    private String java_opts = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (hosts == null || bSkip == null)
            throw new MojoFailureException("Required Parameter is not provided!");
        log("bSkip:" + bSkip);
        if (bSkip)
            return;
        for (String h : getHosts()) {
            log("host==:" + h);
            DockerClient client = new DefaultDockerClient(getDaemonEndPoint(h));

            if (doStartThen1Stop0 == 0) {
                if (containerId == null) {
                    throw new MojoExecutionException("containerId not provided");
                }
                try {
                    client.stopContainer(containerId, SECONDS_WAIT_BEFORE_KILLING);
                } catch (DockerException | InterruptedException e) {
                    error(e);
                    throw new MojoExecutionException(e.getMessage());
                }
            } else {
                // Bind container ports to host ports
                final Map<String, List<PortBinding>> portBindings = new HashMap();
                for (String port : getBindedPorts()) {
                    List<PortBinding> hostPorts = new ArrayList<PortBinding>();
                    hostPorts.add(PortBinding.of("0.0.0.0", port));
                    portBindings.put(port, hostPorts);
                }
                final HostConfig.Builder builder = HostConfig.builder().portBindings(portBindings);
                if (memory > 0)
                    builder.memory(memory * 1024 * 1024);
                if (volumeMapper != null)
                    builder.binds(volumeMapper);
                final HostConfig hostConfig = builder.build();
                try {
                    final ContainerConfig.Builder ccBuilder = ContainerConfig.builder()
                            .hostConfig(hostConfig)
                            .image(imageName).exposedPorts(getBindedPorts())
                            //.cmd("/bin/bash")
                            .cmd("sh", "-c", "while :; do sleep 1; done");
                    ContainerConfig containerConfig = null;
                    if (java_opts != null) {
                        containerConfig = ccBuilder.env("JAVA_OPTS='" + java_opts + "'")
                                .build();
                    } else {
                        containerConfig = ccBuilder.build();
                    }
                    final ContainerCreation creation = client.createContainer(containerConfig);
                    final String id = creation.id();

                    // Inspect container
                    final ContainerInfo info = client.inspectContainer(id);
                    log(info.toString());

                    if (doStartThen1Stop0 == 1)
                        client.startContainer(id);
                    else
                        client.stopContainer(id, SECONDS_WAIT_BEFORE_KILLING);
                } catch (DockerException | InterruptedException e) {
                    error(e);
                    throw new MojoExecutionException(e.getMessage());
                }
            }
        }
    }

    protected final String[] getBindedPorts() {
        return bindedPorts == null ? null: bindedPorts.split(",");
    }
}
