/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.kohsuke.github.function.InputStreamFunction;
import org.kohsuke.github.internal.graphql.response.GHGraphQLResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * A thin helper for {@link GitHubRequest.Builder} that includes {@link GitHubClient}.
 *
 * @author Kohsuke Kawaguchi
 */
class Requester extends GitHubRequest.Builder<Requester> {

    /**
     * Helper function to make it easy to pull streams.
     *
     * Copies an input stream to an in-memory input stream. The performance on this is not great but
     * {@link GitHubConnectorResponse#bodyStream()} is closed at the end of every call to
     * {@link GitHubClient#sendRequest(GitHubRequest, GitHubClient.BodyHandler)}, so any reads to the original input
     * stream must be completed before then. There are a number of deprecated methods that return {@link InputStream}.
     * This method keeps all of them using the same code path.
     *
     * @param inputStream
     *            the input stream to be copied
     * @return an in-memory copy of the passed input stream
     * @throws IOException
     *             if an error occurs while copying the stream
     */
    @NonNull public static InputStream copyInputStream(InputStream inputStream) throws IOException {
        return new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
    }

    /** The client. */
    /* private */ final transient GitHubClient client;

    /**
     * Instantiates a new requester.
     *
     * @param client
     *            the client
     */
    Requester(GitHubClient client) {
        this.client = client;
        this.withApiUrl(client.getApiUrl());
    }

    /**
     * Sends a request and parses the response into the given type via databinding.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return an instance of {@code T}
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T> T fetch(@Nonnull Class<T> type) throws IOException {
        return client.sendRequest(this, (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, type))
                .body();
    }

    /**
     * Sends a request and parses the response into the given type via databinding in GraphQL response.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return an instance of {@code GHGraphQLResponse<T>}
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T extends GHGraphQLResponse<S>, S> S fetchGraphQL(@Nonnull Class<T> type) throws IOException {
        T response = fetch(type);

        if (!response.isSuccessful()) {
            throw new IOException("GraphQL request failed by:" + response.getErrorMessages());
        }

        return response.getData();
    }

    /**
     * Makes a request and just obtains the HTTP status code. Method does not throw exceptions for many status codes
     * that would otherwise throw.
     *
     * @return the int
     * @throws IOException
     *             the io exception
     */
    public int fetchHttpStatusCode() throws IOException {
        return client.sendRequest(build(), null).statusCode();
    }

    /**
     * Like {@link #fetch(Class)} but updates an existing object instead of creating a new instance.
     *
     * @param <T>
     *            the type parameter
     * @param existingInstance
     *            the existing instance
     * @return the updated instance
     * @throws IOException
     *             the io exception
     */
    public <T> T fetchInto(@Nonnull T existingInstance) throws IOException {
        return client
                .sendRequest(this, (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, existingInstance))
                .body();
    }

    /**
     * Response input stream. There are scenarios where direct stream reading is needed, however it is better to use
     * {@link #fetch(Class)} where possible.
     *
     * @param <T>
     *            the generic type
     * @param handler
     *            the handler
     * @return the t
     * @throws IOException
     *             the io exception
     */
    public <T> T fetchStream(@Nonnull InputStreamFunction<T> handler) throws IOException {
        return client.sendRequest(this, (connectorResponse) -> handler.apply(connectorResponse.bodyStream())).body();
    }

    /**
     * Sends a request to the specified URL and checks that it is successful.
     *
     * @throws IOException
     *             the io exception
     */
    public void send() throws IOException {
        // Send expects there to be some body response, but doesn't care what it is.
        // If there isn't a body, this will throw.
        client.sendRequest(this, (connectorResponse) -> GitHubResponse.getBodyAsString(connectorResponse));
    }

    /**
     * Sends a GraphQL request with no response
     *
     * @throws IOException
     *             the io exception
     */
    public void sendGraphQL() throws IOException {
        fetchGraphQL(GHGraphQLResponse.ObjectResponse.class);
    }

    /**
     * Creates {@link PagedIterable <R>} from this builder using the provided {@link Consumer}{@code <R>}.
     * <p>
     * This method and the {@link PagedIterable <R>} do not actually begin fetching data until {@link Iterator#next()}
     * or {@link Iterator#hasNext()} are called.
     * </p>
     *
     * @param <R>
     *            the element type for the pages returned from
     * @param type
     *            the type of the pages to retrieve.
     * @param itemInitializer
     *            the consumer to execute on each paged item retrieved.
     * @return the {@link PagedIterable} for this builder.
     */
    public <R> PagedIterable<R> toIterable(Class<R[]> type, Consumer<R> itemInitializer) {
        return new GitHubPageContentsIterable<>(client, build(), type, itemInitializer);

    }
}
