package org.familianascimento.rodrigo.folhaandroid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;


/**
 * Created by weltonnascimento on 02/07/14.
 */
public class ParserNoticia {

    public static final String LOG_TAG = "ParserNoticia";

    final static class MODEL {
        static final String DATA = "#conteudo .Texto_Not";
        static final String CHAMADA = ".entry-title";
        static final String CORPO_NOTICIA = "#corpoNoticia p";
        static final String IMG = ".textbox_Direita img";
        static final String CAPTION = ".textbox_legenda";
        //static final String IMG_CREDITO = ".textbox_Direita .style8";
    }

    // TODO: Maybe use a Dictionary or something like that.
    public static void parseURL(Context context, String urlString) {

        try {

            URL url = new URL(urlString);

            // TODO: lidar com erros do servidor.
            Document document = Jsoup.parse(url, 60000);

            // Values for DB insertion.
            ContentValues entry = new ContentValues();

            entry.put(noticiasDB.NOTICIA_URL_COLUMN, urlString);
            entry.put(noticiasDB.NOTICIA_DATA_COLUMN, document.select(MODEL.DATA).text());
            entry.put(noticiasDB.NOTICIA_CHAMADA_COLUMN, document.select(MODEL.CHAMADA).text());
            entry.put(noticiasDB.NOTICIA_IMAGEM_URL_COLUMN, document.select(MODEL.IMG).attr("abs:src"));
            entry.put(noticiasDB.NOTICIA_IMAGEM_CAPTION_COLUMN, document.select(MODEL.CAPTION).text());

            StringBuilder sb = new StringBuilder();

            for (Element p : document.select(MODEL.CORPO_NOTICIA)) {
                sb.append("<p>" + p.text() + "</p>");
            }

            entry.put(noticiasDB.NOTICIA_TEXTO_COLUMN, sb.toString());


            ContentResolver cr = context.getContentResolver();
            cr.delete(NoticiasContentProvider.NOTICIA_URI, noticiasDB.NOTICIA_URL_COLUMN + "=?",
                    new String[]{urlString});
            cr.insert(NoticiasContentProvider.NOTICIA_URI, entry);

            Log.d(LOG_TAG, entry.toString());

        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }
}
