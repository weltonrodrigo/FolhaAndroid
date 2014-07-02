package org.familianascimento.rodrigo.folhaandroid;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by welton.torres on 01/07/2014.
 */
public class ContentPuller extends IntentService {

    // Used to write to the system log from this class.
    public static final String LOG_TAG = "DocumentPullService";

    public ContentPuller() {
        super("DocumentPullService");
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param workIntent The value passed to {@link
     *               android.content.Context#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets a URL to read from the incoming Intent's "data" value
        String localUrlString = workIntent.getDataString();

        try {
            URL url = new URL("http", "www.folhabv.com.br", 80, "ultimas.php");

            Document document = Jsoup.parse(url, 10000);

            // Will use this to get a Date 14\05\2014
            Pattern datePattern = Pattern.compile("^(\\d\\d/\\d\\d/\\d\\d\\d\\d).*");

            // Will first get the rows, which are TDs with this class.
            // Sub elements are all child of this.
            Elements row = document.select(".texto_ult2");

            for (Element e : row) {
                String secao = e.select("strong").text();

                Element a = e.select("a").first();

                // get the absolute href from a link.
                String href = a.attr("abs:href");
                String titu = a.text();

                // Match the regex agains the text.
                Matcher m = datePattern.matcher(e.text());

                // go to the first match.
                if (m.find()) {
                    String date = m.group(1);
                }
            }

            //Log.d(LOG_TAG, document.data());
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }
}