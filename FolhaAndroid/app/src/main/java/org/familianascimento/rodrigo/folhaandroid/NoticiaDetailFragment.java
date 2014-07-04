package org.familianascimento.rodrigo.folhaandroid;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A fragment representing a single Noticia detail screen.
 * This fragment is either contained in a {@link NoticiaListActivity}
 * in two-pane mode (on tablets) or a {@link NoticiaDetailActivity}
 * on handsets.
 */
public class NoticiaDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    String LOG_TAG = "NoticiaDetailFragment";

    private String mURL;

    private View rootView;
    private ProgressBar progressBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoticiaDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mURL = getArguments().getString(ARG_ITEM_ID);


            // Will populate DB with Data from internet on the first time.
            // Flag crawled will avoid doubled execution.
            AsyncTask asyncTask = new AsyncTask<Object, Object, Object[]>() {
                @Override
                protected Object[] doInBackground(Object[] params) {
                    Log.d(LOG_TAG, "Will get and parse data.");
                    //Get data online.
                    ParserNoticia.parseURL(getActivity(), mURL);
                    return params;
                }

                @Override
                protected void onPostExecute(Object[] params) {
                    super.onPostExecute(params);

                    Log.d(LOG_TAG, "Will Start Loader.");
                    // With data already loaded on DB, start the Loader system.
                    //getLoaderManager().initLoader(0, (Bundle) params[0], (LoaderManager.LoaderCallbacks) params[1]);
                }
            };
            //Do the action
            asyncTask.execute(new Object[]{savedInstanceState, this});


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_noticia_detail, container, false);
        this.rootView = rootView;

        ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        this.progressBar = progressBar;

        getLoaderManager().initLoader(0, savedInstanceState, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),
                NoticiasContentProvider.NOTICIA_URI,
                null,
                noticiasDB.NOTICIA_URL_COLUMN + "=?", new String[]{mURL},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        TextView noticia_chamada = (TextView) rootView.findViewById(R.id.noticia_chamada);
        TextView noticia_data = (TextView) rootView.findViewById(R.id.noticia_data);
        TextView noticia_corpo = (TextView) rootView.findViewById(R.id.noticia_corpo);

        try {
            data.moveToPosition(0);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
            return;
        }

        try {
            noticia_chamada.setText(data.getString(noticiasDB.NOTICIA_CHAMADA_COLUMN_POSITION));
            noticia_data.setText(data.getString(noticiasDB.NOTICIA_DATA_COLUMN_POSITION));
            noticia_corpo.setText(Html.fromHtml(data.getString(noticiasDB.NOTICIA_TEXTO_COLUMN_POSITION)));
            progressBar.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Erro tentando pegar o texto do DB");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
