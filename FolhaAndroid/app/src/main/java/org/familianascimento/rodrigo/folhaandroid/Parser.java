package org.familianascimento.rodrigo.folhaandroid;

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
    public static List<Map<Integer, String>> parseURL(URL url) {

        List<Map<Integer, String>> resultados = new ArrayList<Map<Integer, String>>();

        try {

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

            return resultados;

            //Log.d(LOG_TAG, document.data());
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
            return null;
        }
    }
}
