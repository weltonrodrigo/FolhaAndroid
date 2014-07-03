package org.familianascimento.rodrigo.folhaandroid;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by weltonnascimento on 02/07/14.
 */
public class Parser {

    public static final String LOG_TAG = "Parser";

    public static final String DATE_REGEX = "^(\\d\\d/\\d\\d/\\d\\d\\d\\d).*";

    // TODO: Maybe use a Dictionary or something like that.
    public static void parseURL(Context context, String urlString) {

        List<ContentValues> resultados = new ArrayList<ContentValues>();

        try {

            URL url = new URL(urlString);

            // TODO: lidar com erros do servidor.
            Document document = Jsoup.parse(url, 60000);

            // Will use this to get a Date 14\05\2014
            Pattern datePattern = Pattern.compile(DATE_REGEX);

            // Will first get the rows, which are TDs with this class.
            // Sub elements are all child of this.
            Elements row = document.select(".texto_ult2");

            for (Element e : row) {
                // A temporary holder for values.
                ContentValues entry = new ContentValues();

                entry.put(noticiasDB.ULTIMAS_SECAO_COLUMN, e.select("strong").text());

                Element a = e.select("a").first();

                // get the absolute href from a link.
                entry.put(noticiasDB.ULTIMAS_URL_COLUMN, a.attr("abs:href"));
                entry.put(noticiasDB.ULTIMAS_CHAMADA_COLUMN, a.text());

                // Match the regex agains the text.
                Matcher m = datePattern.matcher(e.text());

                // go to the first match.
                if (m.find()) {
                    entry.put(noticiasDB.ULTIMAS_DATA_COLUMN, m.group(1));
                }
                // Put this entry on the list for further insertion on DB.
                resultados.add(entry);
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }

        // New Batch operation
        NoticiasContentProviderBatchClient batch = new NoticiasContentProviderBatchClient();
        batch.start();

        for (ContentValues values : resultados) {
            //New insert operation
            ContentProviderOperation.Builder operationBuilder =
                    ContentProviderOperation.newInsert(NoticiasContentProvider.ULTIMAS_URI);

            // Add to the batch.
            operationBuilder.withValues(values);
            batch.add(operationBuilder.build());

        }

        try {
            // Commit those operations to DB
            batch.commit(context);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error trying to insert noticias on DB:" + e.toString());
        }
    }
}
