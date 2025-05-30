package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

// TODO: Auto-generated Javadoc
/**
 * Represents the account that's logging into GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHMyself extends GHUser {

    /**
     * Type of repositories returned during listing.
     */
    public enum RepositoryListFilter {

        /** All public and private repositories that current user has access or collaborates to. */
        ALL,

        /** Public and private repositories that current user is a member. */
        MEMBER,

        /** Public and private repositories owned by current user. */
        OWNER,

        /** Private repositories that current user has access or collaborates to. */
        PRIVATE,

        /** Public repositories that current user has access or collaborates to. */
        PUBLIC;
    }

    /**
     * Create default GHMyself instance
     */
    public GHMyself() {
    }

    /**
     * Add public SSH key for the user.
     * <p>
     * https://docs.github.com/en/rest/users/keys?apiVersion=2022-11-28#create-a-public-ssh-key-for-the-authenticated-user
     *
     * @param title
     *            Title of the SSH key
     * @param key
     *            the public key
     * @return the newly created Github key
     * @throws IOException
     *             the io exception
     */
    public GHKey addPublicKey(String title, String key) throws IOException {
        return root().createRequest()
                .withUrlPath("/user/keys")
                .method("POST")
                .with("title", title)
                .with("key", key)
                .fetch(GHKey.class);
    }

    /**
     * Gets the organization that this user belongs to.
     *
     * @return the all organizations
     * @throws IOException
     *             the io exception
     */
    public GHPersonSet<GHOrganization> getAllOrganizations() throws IOException {
        GHPersonSet<GHOrganization> orgs = new GHPersonSet<GHOrganization>();
        Set<String> names = new HashSet<String>();
        for (GHOrganization o : root().createRequest()
                .withUrlPath("/user/orgs")
                .toIterable(GHOrganization[].class, null)
                .toArray()) {
            if (names.add(o.getLogin())) // in case of rumoured duplicates in the data
                orgs.add(root().getOrganization(o.getLogin()));
        }
        return orgs;
    }

    /**
     * Gets the all repositories this user owns (public and private).
     *
     * @return the all repositories
     */
    public synchronized Map<String, GHRepository> getAllRepositories() {
        Map<String, GHRepository> repositories = new TreeMap<String, GHRepository>();
        for (GHRepository r : listRepositories()) {
            repositories.put(r.getName(), r);
        }
        return Collections.unmodifiableMap(repositories);
    }

    /**
     * Lists installations of your GitHub App that the authenticated user has explicit permission to access. You must
     * use a user-to-server OAuth access token, created for a user who has authorized your GitHub App, to access this
     * endpoint.
     *
     * @return the paged iterable
     * @see <a href=
     *      "https://docs.github.com/en/rest/reference/apps#list-app-installations-accessible-to-the-user-access-token">List
     *      app installations accessible to the user access token</a>
     */
    public PagedIterable<GHAppInstallation> getAppInstallations() {
        return new GHAppInstallationsIterable(root());
    }

    /**
     * Gets emails.
     *
     * @return the emails
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #listEmails()}
     */
    @Deprecated
    public List<String> getEmails() throws IOException {
        return getEmails2().stream().map(email -> email.getEmail()).collect(Collectors.toList());
    }

    /**
     * Returns the read-only list of e-mail addresses configured for you.
     * <p>
     * This corresponds to the stuff you configure in https://github.com/settings/emails, and not to be confused with
     * {@link #getEmail()} that shows your public e-mail address set in https://github.com/settings/profile
     *
     * @return Always non-null.
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #listEmails()}
     */
    @Deprecated
    public List<GHEmail> getEmails2() throws IOException {
        return listEmails().toList();
    }

    /**
     * Gets your membership in a specific organization.
     *
     * @param o
     *            the o
     * @return the membership
     * @throws IOException
     *             the io exception
     */
    public GHMembership getMembership(GHOrganization o) throws IOException {
        return root().createRequest()
                .withUrlPath("/user/memberships/orgs/" + o.getLogin())
                .fetch(GHMembership.class)
                .wrap(root());
    }

    /**
     * Returns the read-only list of all the public keys of the current user.
     * <p>
     * NOTE: When using OAuth authentication, the READ/WRITE User scope is required by the GitHub APIs, otherwise you
     * will get a 404 NOT FOUND.
     *
     * @return Always non-null.
     * @throws IOException
     *             the io exception
     */
    public List<GHKey> getPublicKeys() throws IOException {
        return root().createRequest().withUrlPath("/user/keys").toIterable(GHKey[].class, null).toList();
    }

    /**
     * Returns the read-only list of all the public verified keys of the current user.
     * <p>
     * Differently from the getPublicKeys() method, the retrieval of the user's verified public keys does not require
     * any READ/WRITE OAuth Scope to the user's profile.
     *
     * @return Always non-null.
     * @throws IOException
     *             the io exception
     */
    public List<GHVerifiedKey> getPublicVerifiedKeys() throws IOException {
        return root().createRequest()
                .withUrlPath("/users/" + getLogin() + "/keys")
                .toIterable(GHVerifiedKey[].class, null)
                .toList();
    }

    /**
     * Returns the read-only list of e-mail addresses configured for you.
     * <p>
     * This corresponds to the stuff you configure in https://github.com/settings/emails, and not to be confused with
     * {@link #getEmail()} that shows your public e-mail address set in https://github.com/settings/profile
     *
     * @return Always non-null.
     */
    public PagedIterable<GHEmail> listEmails() {
        return root().createRequest().withUrlPath("/user/emails").toIterable(GHEmail[].class, null);
    }

    /**
     * List your organization memberships.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHMembership> listOrgMemberships() {
        return listOrgMemberships(null);
    }

    /**
     * List your organization memberships.
     *
     * @param state
     *            Filter by a specific state
     * @return the paged iterable
     */
    public PagedIterable<GHMembership> listOrgMemberships(final GHMembership.State state) {
        return root().createRequest()
                .with("state", state)
                .withUrlPath("/user/memberships/orgs")
                .toIterable(GHMembership[].class, item -> item.wrap(root()));
    }

    /**
     * Lists up all repositories this user owns (public and private).
     *
     * Unlike {@link #getAllRepositories()}, this does not wait until all the repositories are returned. Repositories
     * are returned by GitHub API with a 30 items per page.
     *
     * @return the paged iterable
     */
    @Override
    public PagedIterable<GHRepository> listRepositories() {
        return listRepositories(30);
    }

    /**
     * List repositories that are accessible to the authenticated user (public and private) using the specified page
     * size.
     *
     * This includes repositories owned by the authenticated user, repositories that belong to other users where the
     * authenticated user is a collaborator, and other organizations' repositories that the authenticated user has
     * access to through an organization membership.
     *
     * @param pageSize
     *            size for each page of items returned by GitHub. Maximum page size is 100.
     *
     *            Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listRepositories(final int pageSize) {
        return listRepositories(pageSize, RepositoryListFilter.ALL);
    }

    // public void addEmails(Collection<String> emails) throws IOException {
    //// new Requester(root,ApiVersion.V3).withCredential().to("/user/emails");
    // root.retrieveWithAuth3()
    // }

    /**
     * List repositories of a certain type that are accessible by current authenticated user using the specified page
     * size.
     *
     * @param pageSize
     *            size for each page of items returned by GitHub. Maximum page size is 100.
     * @param repoType
     *            type of repository returned in the listing
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listRepositories(final int pageSize, final RepositoryListFilter repoType) {
        return root().createRequest()
                .with("type", repoType)
                .withUrlPath("/user/repos")
                .toIterable(GHRepository[].class, null)
                .withPageSize(pageSize);
    }
}
