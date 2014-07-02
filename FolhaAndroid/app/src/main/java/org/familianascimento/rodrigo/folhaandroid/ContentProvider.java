package org.familianascimento.rodrigo.folhaandroid;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentProvider extends ContentProvider {

    public static final String LOG_TAG = "ContentProvider";

    public static final String DATE_REGEX = "^(\\d\\d/\\d\\d/\\d\\d\\d\\d).*";

    public static final String URL_ULTIMAS =
            "http://www.folhabv.com.br/ultimas.php";

    public ContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // TODO: Implement URI filtering to get a single id on row
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // TODO: Maybe use a Dictionary or something like that.
    private List<Map<Integer, String>> getContent() {

        List<Map<Integer, String>> resultados = new ArrayList<Map<Integer, String>>();

        try {

            URL url = new URL(URL_ULTIMAS);

            // TODO: lidar com erros do servidor.
            Document document = Jsoup.parse(url, 60);

            // Will use this to get a Date 14\05\2014
            Pattern datePattern = Pattern.compile(DATE_REGEX);

            // Will first get the rows, which are TDs with this class.
            // Sub elements are all child of this.
            Elements row = document.select(".texto_ult2");

            for (Element e : row) {

                //Row
                Map<Integer, String> entry = new ArrayMap<Integer, String>();

                entry.put(DataContract.SECAO, e.select("strong").text());

                Element a = e.select("a").first();

                // get the absolute href from a link.
                entry.put(DataContract.URL_NOTICIA, a.attr("abs:href"));
                entry.put(DataContract.TITULO, a.text());

                // Match the regex agains the text.
                Matcher m = datePattern.matcher(e.text());

                // go to the first match.
                if (m.find()) {
                    entry.put(DataContract.DATA, m.group(1));
                }
            }

            //Log.d(LOG_TAG, document.data());
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
            return null;
        }


    }

}

