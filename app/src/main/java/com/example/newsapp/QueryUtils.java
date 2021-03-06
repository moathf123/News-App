package com.example.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    public static List<NewsDetails> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        List<NewsDetails> newsJSON = extractFeatureFromJson(jsonResponse);
        return newsJSON;
    }

    private static List<NewsDetails> extractFeatureFromJson(String newsJSON) {

        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }
        List<NewsDetails> newsAll = new ArrayList<>();
        try {
            JSONObject rootObject = new JSONObject(newsJSON);
            JSONObject responseObject = rootObject.optJSONObject("response");
            JSONArray resultArray = responseObject.getJSONArray("results");
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject resultsObject = resultArray.getJSONObject(i);
                JSONArray tagsArray = resultsObject.optJSONArray("tags");
                JSONObject tagsObject;
                String authorFullName;
                if (tagsArray != null) {
                    tagsObject = tagsArray.optJSONObject(0);
                    if(tagsObject != null) {
                        String authorFirstName = tagsObject.getString("firstName");
                        String authorLastName = tagsObject.getString("lastName");
                        authorFullName = authorFirstName + " " + authorLastName;
                    }else authorFullName = null;
                } else authorFullName = null;
                String title = resultsObject.getString("webTitle");
                String section = resultsObject.getString("sectionName");
                String date = resultsObject.getString("webPublicationDate");
                String url = resultsObject.getString("webUrl");
                NewsDetails news = new NewsDetails(title, section, url, date, authorFullName);
                newsAll.add(news);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
        }
        return newsAll;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}