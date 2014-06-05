package org.jfrog.hudson.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jfrog.build.api.util.NullLog;
import org.jfrog.build.client.ArtifactoryBuildInfoClient;
import org.jfrog.build.client.ProxyConfiguration;
import org.jfrog.hudson.ArtifactoryServer;
import org.jfrog.hudson.DeployerOverrider;
import org.jfrog.hudson.ResolverOverrider;
import org.jfrog.hudson.VirtualRepository;

import java.io.IOException;
import java.util.List;

/**
 * @author Shay Yaakov
 */
public abstract class RepositoriesUtils {

    public static List<String> getReleaseRepositoryKeysFirst(DeployerOverrider deployer, ArtifactoryServer server) {
        if (server == null) {
            return Lists.newArrayList();
        }

        return server.getReleaseRepositoryKeysFirst(deployer);
    }

    public static List<String> getSnapshotRepositoryKeysFirst(DeployerOverrider deployer, ArtifactoryServer server) {
        if (server == null) {
            return Lists.newArrayList();
        }

        return server.getSnapshotRepositoryKeysFirst(deployer);
    }

    public static List<VirtualRepository> getVirtualRepositoryKeys(ResolverOverrider resolverOverrider,
                                                                   DeployerOverrider deployerOverrider, ArtifactoryServer server) {
        if (server == null) {
            return Lists.newArrayList();
        }

        return server.getVirtualRepositoryKeys(resolverOverrider, deployerOverrider);
    }

    public static List<VirtualRepository> generateVirtualRepos(ArtifactoryBuildInfoClient client) throws IOException {
        List<VirtualRepository> virtualRepositories;

        List<String> keys = client.getVirtualRepositoryKeys();
        virtualRepositories = Lists.newArrayList(Lists.transform(keys, new Function<String, VirtualRepository>() {
            public VirtualRepository apply(String from) {
                return new VirtualRepository(from, from);
            }
        }));

        return virtualRepositories;
    }

    public static List<VirtualRepository> getVirtualRepositoryKeys(String url, String credentialsUsername,
                                                                   String credentialsPassword, boolean overridingDeployerCredentials,
                                                                   ArtifactoryServer artifactoryServer) throws IOException {
        List<VirtualRepository> virtualRepositories;
        String username;
        String password;
        if (overridingDeployerCredentials && StringUtils.isNotBlank(credentialsUsername) && StringUtils.isNotBlank(credentialsPassword)) {
            username = credentialsUsername;
            password = credentialsPassword;
        } else {
            Credentials deployedCredentials = artifactoryServer.getResolvingCredentials();
            username = deployedCredentials.getUsername();
            password = deployedCredentials.getPassword();
        }

        ArtifactoryBuildInfoClient client;
        if (StringUtils.isNotBlank(username)) {
            client = new ArtifactoryBuildInfoClient(url, username, password, new NullLog());
        } else {
            client = new ArtifactoryBuildInfoClient(url, new NullLog());
        }
        client.setConnectionTimeout(10);

        if (Jenkins.getInstance().proxy != null) {
            client.setProxyConfiguration(createProxyConfiguration(Jenkins.getInstance().proxy));
        }

        virtualRepositories = RepositoriesUtils.generateVirtualRepos(client);
        return virtualRepositories;
    }

    public static List<String> getLocalRepositories(String url, String credentialsUsername,
                                                    String credentialsPassword, boolean overridingDeployerCredentials,
                                                    ArtifactoryServer artifactoryServer) throws IOException {
        List<String> localRepository;
        String username;
        String password;
        if (overridingDeployerCredentials && StringUtils.isNotBlank(credentialsUsername) && StringUtils.isNotBlank(credentialsPassword)) {
            username = credentialsUsername;
            password = credentialsPassword;
        } else {
            Credentials deployedCredentials = artifactoryServer.getDeployerCredentials();
            username = deployedCredentials.getUsername();
            password = deployedCredentials.getPassword();
        }

        ArtifactoryBuildInfoClient client;
        if (StringUtils.isNotBlank(username)) {
            client = new ArtifactoryBuildInfoClient(url, username, password, new NullLog());
        } else {
            client = new ArtifactoryBuildInfoClient(url, new NullLog());
        }
        client.setConnectionTimeout(10);

        if (Jenkins.getInstance().proxy != null && !artifactoryServer.isBypassProxy()) {
            client.setProxyConfiguration(createProxyConfiguration(Jenkins.getInstance().proxy));
        }

        localRepository = client.getLocalRepositoriesKeys();

        return localRepository;
    }

    private static ProxyConfiguration createProxyConfiguration(hudson.ProxyConfiguration proxy) {
        ProxyConfiguration proxyConfiguration = null;
        if (proxy != null) {
            proxyConfiguration = new ProxyConfiguration();
            proxyConfiguration.host = proxy.name;
            proxyConfiguration.port = proxy.port;
            proxyConfiguration.username = proxy.getUserName();
            proxyConfiguration.password = proxy.getPassword();
        }

        return proxyConfiguration;
    }
}