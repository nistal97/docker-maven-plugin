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
 * @phase package
 *
 * Created by yidong on 8/4/2016.
 */
public class ExecutionMojo extends AMojo {
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
    private boolean bStartNow = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (hosts == null)
            throw new MojoFailureException("Required Parameter is not provided!");
        for (String h : getHosts()) {
            log("host==:" + h);
            DockerClient client = new DefaultDockerClient(getDaemonEndPoint(h));

            // Bind container ports to host ports
            final Map<String, List<PortBinding>> portBindings = new HashMap();
            for (String port : getBindedPorts()) {
                List<PortBinding> hostPorts = new ArrayList<PortBinding>();
                hostPorts.add(PortBinding.of("0.0.0.0", port));
                portBindings.put(port, hostPorts);
            }
            final HostConfig.Builder builder = HostConfig.builder().portBindings(portBindings);
            if (volumeMapper != null)
                builder.binds(volumeMapper);
            final HostConfig hostConfig = builder.build();
            try {
                final ContainerConfig containerConfig = ContainerConfig.builder()
                        .hostConfig(hostConfig)
                        .image(imageName).exposedPorts(getBindedPorts())
                        .cmd("sh", "-c", "while :; do sleep 1; done")
                        .build();
                final ContainerCreation creation = client.createContainer(containerConfig);
                final String id = creation.id();

                // Inspect container
                final ContainerInfo info = client.inspectContainer(id);
                log(info.toString());

                if (bStartNow)
                    client.startContainer(id);
            } catch (DockerException | InterruptedException e) {
                error(e);
                throw new MojoExecutionException(e.getMessage());
            }
        }
    }

    protected final String[] getBindedPorts() {
        return bindedPorts == null ? null: bindedPorts.split(",");
    }
}
