package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * The model user for comparing 2 commits in the GitHub API.
 *
 * @author Michael Clarke
 */
public class GHCompare {

    /**
     * Compare commits had a child commit element with additional details we want to capture. This extension of GHCommit
     * provides that.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class Commit extends GHCommit {

        private InnerCommit commit;

        /**
         * Create default Commit instance
         */
        public Commit() {
        }

        /**
         * Gets commit.
         *
         * @return the commit
         */
        public InnerCommit getCommit() {
            return commit;
        }
    }

    /**
     * The type InnerCommit.
     */
    public static class InnerCommit {

        private GitUser author, committer;

        private Tree tree;
        private String url, sha, message;
        /**
         * Create default InnerCommit instance
         */
        public InnerCommit() {
        }

        /**
         * Gets author.
         *
         * @return the author
         */
        public GitUser getAuthor() {
            return author;
        }

        /**
         * Gets committer.
         *
         * @return the committer
         */
        public GitUser getCommitter() {
            return committer;
        }

        /**
         * Gets message.
         *
         * @return the message
         */
        public String getMessage() {
            return message;
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
         * Gets tree.
         *
         * @return the tree
         */
        public Tree getTree() {
            return tree;
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }
    }
    /**
     * The enum Status.
     */
    public static enum Status {

        /** The ahead. */
        ahead,
        /** The behind. */
        behind,
        /** The diverged. */
        diverged,
        /** The identical. */
        identical
    }
    /**
     * The type Tree.
     */
    public static class Tree {

        private String url, sha;

        /**
         * Create default Tree instance
         */
        public Tree() {
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
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }
    }
    /**
     * Iterable for commit listing.
     */
    class GHCompareCommitsIterable extends PagedIterable<Commit> {

        private GHCompare result;

        /**
         * Instantiates a new GH compare commits iterable.
         */
        public GHCompareCommitsIterable() {
        }

        /**
         * Iterator.
         *
         * @param pageSize
         *            the page size
         * @return the paged iterator
         */
        @Nonnull
        @Override
        public PagedIterator<Commit> _iterator(int pageSize) {
            GitHubRequest request = owner.root()
                    .createRequest()
                    .injectMappingValue("GHCompare_usePaginatedCommits", usePaginatedCommits)
                    .withUrlPath(owner.getApiTailUrl(url.substring(url.lastIndexOf("/compare/"))))
                    .build();

            // page_size must be set for GHCompare commit pagination
            if (pageSize == 0) {
                pageSize = 10;
            }
            return new PagedIterator<>(
                    adapt(GitHubPageIterator.create(owner.root().getClient(), GHCompare.class, request, pageSize)),
                    item -> item.wrapUp(owner));
        }

        /**
         * Adapt.
         *
         * @param base
         *            the base
         * @return the iterator
         */
        protected Iterator<Commit[]> adapt(final Iterator<GHCompare> base) {
            return new Iterator<Commit[]>() {
                public boolean hasNext() {
                    return base.hasNext();
                }

                public Commit[] next() {
                    GHCompare v = base.next();
                    if (result == null) {
                        result = v;
                    }
                    return v.commits;
                }
            };
        }
    }
    private int aheadBy, behindBy, totalCommits;
    private Commit baseCommit, mergeBaseCommit;

    private Commit[] commits;

    private GHCommit.File[] files;

    private GHRepository owner;

    private Status status;

    private String url, htmlUrl, permalinkUrl, diffUrl, patchUrl;

    @JacksonInject("GHCompare_usePaginatedCommits")
    private boolean usePaginatedCommits;

    /**
     * Create default GHCompare instance
     */
    public GHCompare() {
    }

    /**
     * Gets ahead by.
     *
     * @return the ahead by
     */
    public int getAheadBy() {
        return aheadBy;
    }

    /**
     * Gets base commit.
     *
     * @return the base commit
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public Commit getBaseCommit() {
        return baseCommit;
    }

    /**
     * Gets behind by.
     *
     * @return the behind by
     */
    public int getBehindBy() {
        return behindBy;
    }

    /**
     * Gets an array of commits.
     *
     * By default, the commit list is limited to 250 results.
     *
     * Since
     * <a href="https://github.blog/changelog/2021-03-22-compare-rest-api-now-supports-pagination/">2021-03-22</a>,
     * compare supports pagination of commits. This makes the initial {@link GHCompare} response return faster and
     * supports comparisons with more than 250 commits. To read commits progressively using pagination, set
     * {@link GHRepository#setCompareUsePaginatedCommits(boolean)} to true before calling
     * {@link GHRepository#getCompare(String, String)}.
     *
     * @return A copy of the array being stored in the class.
     */
    public Commit[] getCommits() {
        try {
            return listCommits().withPageSize(100).toArray();
        } catch (IOException e) {
            throw new GHException(e.getMessage(), e);
        }
    }

    /**
     * Gets diff url.
     *
     * @return the diff url
     */
    public URL getDiffUrl() {
        return GitHubClient.parseURL(diffUrl);
    }

    /**
     * Gets an array of files.
     *
     * By default, the file array is limited to 300 results. To retrieve the full list of files, iterate over each
     * commit returned by {@link GHCompare#listCommits} and use {@link GHCommit#listFiles} to get the files for each
     * commit.
     *
     * @return A copy of the array being stored in the class.
     */
    public GHCommit.File[] getFiles() {
        GHCommit.File[] newValue = new GHCommit.File[files.length];
        System.arraycopy(files, 0, newValue, 0, files.length);
        return newValue;
    }

    /**
     * Gets html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets merge base commit.
     *
     * @return the merge base commit
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public Commit getMergeBaseCommit() {
        return mergeBaseCommit;
    }

    /**
     * Gets patch url.
     *
     * @return the patch url
     */
    public URL getPatchUrl() {
        return GitHubClient.parseURL(patchUrl);
    }

    /**
     * Gets permalink url.
     *
     * @return the permalink url
     */
    public URL getPermalinkUrl() {
        return GitHubClient.parseURL(permalinkUrl);
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets total commits.
     *
     * @return the total commits
     */
    public int getTotalCommits() {
        return totalCommits;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Iterable of commits for this comparison.
     *
     * By default, the commit list is limited to 250 results.
     *
     * Since
     * <a href="https://github.blog/changelog/2021-03-22-compare-rest-api-now-supports-pagination/">2021-03-22</a>,
     * compare supports pagination of commits. This makes the initial {@link GHCompare} response return faster and
     * supports comparisons with more than 250 commits. To read commits progressively using pagination, set
     * {@link GHRepository#setCompareUsePaginatedCommits(boolean)} to true before calling
     * {@link GHRepository#getCompare(String, String)}.
     *
     * @return iterable of commits
     */
    public PagedIterable<Commit> listCommits() {
        if (usePaginatedCommits) {
            return new GHCompareCommitsIterable();
        } else {
            // if not using paginated commits, adapt the returned commits array
            return new PagedIterable<Commit>() {
                @Nonnull
                @Override
                public PagedIterator<Commit> _iterator(int pageSize) {
                    return new PagedIterator<>(Collections.singleton(commits).iterator(), null);
                }
            };
        }
    }

    /**
     * Wrap gh compare.
     *
     * @param owner
     *            the owner
     * @return the gh compare
     */
    GHCompare lateBind(GHRepository owner) {
        this.owner = owner;
        for (Commit commit : commits) {
            commit.wrapUp(owner);
        }
        mergeBaseCommit.wrapUp(owner);
        baseCommit.wrapUp(owner);
        return this;
    }
}
