package org.kohsuke.github;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Represents a deployment.
 *
 * @see <a href="https://developer.github.com/v3/repos/deployments/">documentation</a>
 * @see GHRepository#listDeployments(String, String, String, String) GHRepository#listDeployments(String, String,
 *      String, String)
 * @see GHRepository#getDeployment(long) GHRepository#getDeployment(long)
 */
public class GHDeployment extends GHObject {

    private GHRepository owner;

    /** The creator. */
    protected GHUser creator;

    /** The description. */
    protected String description;

    /** The environment. */
    protected String environment;

    /** The original environment. */
    protected String originalEnvironment;

    /** The payload. */
    protected Object payload;

    /** The production environment. */
    protected boolean productionEnvironment;

    /** The ref. */
    protected String ref;

    /** The repository url. */
    protected String repositoryUrl;

    /** The sha. */
    protected String sha;

    /** The statuses url. */
    protected String statusesUrl;

    /** The task. */
    protected String task;

    /** The transient environment. */
    protected boolean transientEnvironment;

    /**
     * Create default GHDeployment instance
     */
    public GHDeployment() {
    }

    /**
     * Create status gh deployment status builder.
     *
     * @param state
     *            the state
     * @return the gh deployment status builder
     */
    public GHDeploymentStatusBuilder createStatus(GHDeploymentState state) {
        return new GHDeploymentStatusBuilder(owner, getId(), state);
    }

    /**
     * Gets creator.
     *
     * @return the creator
     */
    public GHUser getCreator() {
        return root().intern(creator);
    }

    /**
     * Gets environment.
     *
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * The environment defined when the deployment was first created.
     *
     * @return the original deployment environment
     */
    public String getOriginalEnvironment() {
        return originalEnvironment;
    }

    /**
     * Gets payload. <b>NOTE:</b> only use this method if you can guarantee the payload will be a simple string,
     * otherwise use {@link #getPayloadObject()}.
     *
     * @return the payload
     */
    public String getPayload() {
        return (String) payload;
    }

    /**
     * Gets payload. <b>NOTE:</b> only use this method if you can guarantee the payload will be a JSON object (Map),
     * otherwise use {@link #getPayloadObject()}.
     *
     * @return the payload
     */
    public Map<String, Object> getPayloadMap() {
        return Collections.unmodifiableMap((Map<String, Object>) payload);
    }

    /**
     * Gets payload without assuming its type. It could be a String or a Map.
     *
     * @return the payload
     */
    public Object getPayloadObject() {
        return payload;
    }

    /**
     * Gets ref.
     *
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets repository url.
     *
     * @return the repository url
     */
    public URL getRepositoryUrl() {
        return GitHubClient.parseURL(repositoryUrl);
    }

    /**
     * Gets sha.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * Gets statuses url.
     *
     * @return the statuses url
     */
    public URL getStatusesUrl() {
        return GitHubClient.parseURL(statusesUrl);
    }

    /**
     * Gets task.
     *
     * @return the task
     */
    public String getTask() {
        return task;
    }

    /**
     * Specifies if the given environment is one that end-users directly interact with.
     *
     * @return the environment is used by end-users directly
     */
    public boolean isProductionEnvironment() {
        return productionEnvironment;
    }

    /**
     * Specifies if the given environment is specific to the deployment and will no longer exist at some point in the
     * future.
     *
     * @return the environment is transient
     */
    public boolean isTransientEnvironment() {
        return transientEnvironment;
    }

    /**
     * List statuses paged iterable.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHDeploymentStatus> listStatuses() {
        return root().createRequest()
                .withUrlPath(statusesUrl)
                .toIterable(GHDeploymentStatus[].class, item -> item.lateBind(owner));
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    // test only
    GHRepository getOwner() {
        return owner;
    }

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH deployment
     */
    GHDeployment wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
