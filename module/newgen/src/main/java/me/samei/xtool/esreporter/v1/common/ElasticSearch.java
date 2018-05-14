package me.samei.xtool.esreporter.v1.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ElasticSearch {

    public static String indexDocument = "doc";

    private final String host;
    private final Logger logger;

    public ElasticSearch(String host) throws MalformedURLException {

        if (host == null) throw new IllegalArgumentException("'host' can't be null");
        if (host.isEmpty()) throw new IllegalArgumentException("'host' can't be empty");

        logger = LoggerFactory.getLogger(getClass());

        new URL(host);

        this.host = host;
    }

    public void put(Report report) throws IOException {

        String url = genURL(report);

        if (logger.isTraceEnabled()) logger.trace("PUT, Index: {}, Time: {}, GeneratedURL: {}", report.index, report.time, url);

        HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();

        http.setUseCaches(false);
        http.setDoOutput(true);
        http.setRequestMethod("PUT");
        http.setRequestProperty("Content-Type", "application/json");

        OutputStream output = new DataOutputStream(http.getOutputStream());
        byte[] bytes = report.body.getBytes();

        if (logger.isTraceEnabled()) logger.trace("PUT, RequestBodyLen: {}, RequestBody: {}", bytes.length, report.body);

        output.write(bytes);

        output.flush();
        output.close();

        http.connect();

        if (logger.isTraceEnabled()) logger.trace(
                "PUT, Response({}): {}, ContentLen: {}, ContentType: {}",
                http.getResponseCode(), http.getResponseMessage(),
                http.getContentLength(), http.getContentType()
        );

        int statusCode = http.getResponseCode();
        String body = null;
        boolean successful = false;

        if (statusCode == 200 || statusCode == 201) {
            body = readStream(http.getInputStream());
            successful = true;
        } else {
            body = readStream(http.getErrorStream());
        }

        if (logger.isTraceEnabled()) logger.trace("PUT, ResponseBody: {}", body);

        if (successful) {
            if (logger.isDebugEnabled()) logger.debug(
                    "PUT, Index: {}, Time: {}, URL: {}, RequestBodyLen: {}, Response({}): {}, ResponseBodyLen: {}",
                    report.index, report.time, url,
                    bytes.length,
                    http.getResponseCode(), http.getResponseMessage(),
                    body.getBytes().length
            );
        } else {
            if (logger.isWarnEnabled()) logger.warn(
                    "PUT, Index: {}, Time: {}, URL: {}, RequestBodyLen: {}, Response({}): {}, ResponseBodyLen: {}, ResponseBody: {}, Request: {}",
                    report.index, report.time, url,
                    bytes.length,
                    http.getResponseCode(), http.getResponseMessage(),
                    body.getBytes().length,
                    body, report.body
            );
            throw new RuntimeException("Unable to PUT Report! URL: " + url);
        }
    }

    private String genURL(Report report) {
        return new StringBuilder()
                .append(this.host)
                .append("/")
                .append(report.index).append("/")
                .append(indexDocument).append("/")
                .append(report.id)
                .toString()
                .replaceAll("[\\s\\(\\)\\<\\>]", "-");
    }

    private String readStream(InputStream input) throws IOException {
        BufferedReader buffer =
                new BufferedReader(new InputStreamReader(input));

        StringBuilder str = new StringBuilder();
        String line = null;
        while ((line = buffer.readLine()) != null) {
            str.append(line);
        }
        return str.toString();
    }

    private String _toString;

    @Override
    public String toString() {
        if (_toString == null) {
            _toString = new StringBuilder()
                    .append(getClass().getName())
                    .append("(host: '")
                    .append(host)
                    .append("')")
                    .toString();

        }
        return _toString;
    }
}
