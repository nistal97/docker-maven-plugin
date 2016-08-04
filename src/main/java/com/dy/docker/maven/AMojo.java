package com.dy.docker.maven;

import org.apache.maven.plugin.AbstractMojo;

/**
 * Created by yidong on 8/4/2016.
 */
public abstract class AMojo extends AbstractMojo {
    protected static final int DEFAULT_DAEMON_PORT = 2375;

    /**
     * @parameter
     */
    protected String hosts = null;

    /**
     * @parameter
     */
    protected String imageName = null;

    protected final void log(String str) {
        getLog().info(str);
    }

    protected final void debug(String str) {
        getLog().debug(str);
    }

    protected final void error(Throwable e) {
        getLog().error(e);
    }

    protected final String[] getHosts() {
        return hosts == null ? null: hosts.split(",");
    }

    protected final String getDaemonEndPoint(String h) {
        return "http://" + h.trim() + ":" + DEFAULT_DAEMON_PORT;
    }
}
