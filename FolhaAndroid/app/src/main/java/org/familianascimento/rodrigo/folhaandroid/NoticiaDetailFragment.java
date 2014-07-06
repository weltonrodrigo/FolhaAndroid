package org.familianascimento.rodrigo.folhaandroid;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

/**
 * A fragment representing a single Noticia detail screen.
 * This fragment is either contained in a {@link NoticiaListActivity}
 * in two-pane mode (on tablets) or a {@link NoticiaDetailActivity}
 * on handsets.
 */
public class NoticiaDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
        , SwipeRefreshLayout.OnRefreshListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    String LOG_TAG = "NoticiaDetailFragment";

    private String mURL;

    private View rootView;

    // Field to hold this fragment's swipe with progressbar.
    private SwipeRefreshLayout swipeLayout;

    // This fragment's pullContent task
    private PullContent pullTask;

    // A flag to to avoid requesting a refresh when already doing.
    private boolean refreshing = false;

    private ImageView imageView;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_noticia_detail, container, false);
        this.rootView = rootView;

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.detail_swipe_view);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(true);

        getLoaderManager().initLoader(Constants.PULL_TASK_NOTICIA, savedInstanceState, this);

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

        // Check if cursor has data
        if (data.moveToFirst() && data.getCount() != 0) {
            TextView noticiaChamada = (TextView) rootView.findViewById(R.id.noticia_chamada);
            TextView noticiaData = (TextView) rootView.findViewById(R.id.noticia_data);
            TextView noticiaCorpo = (TextView) rootView.findViewById(R.id.noticia_corpo);
            imageView = (ImageView) rootView.findViewById(R.id.image_view);

            noticiaChamada.setText(data.getString(noticiasDB.NOTICIA_CHAMADA_COLUMN_POSITION));
            noticiaData.setText(data.getString(noticiasDB.NOTICIA_DATA_COLUMN_POSITION));
            noticiaCorpo.setText(Html.fromHtml(data.getString(noticiasDB.NOTICIA_TEXTO_COLUMN_POSITION)));

            // TODO: set correct width and height for timthumb.
            String imageURL = data.getString(noticiasDB.NOTICIA_IMAGEM_URL_COLUMN_POSITION);

            if (imageURL.equals("")) {

                // No image URL: take imageview out of layout.
                imageView.setVisibility(View.GONE);

            } else {

                ImageRequest imageRequest = new ImageRequest(getImageURL(imageURL),
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                imageView.setImageBitmap(response);
                            }
                        }
                        , 0, 0, Bitmap.Config.ARGB_8888, null
                );

                RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                queue.add(imageRequest);
            }

        } else {

            // Start a refresh of this view.
            onRefresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onRefresh() {

        // If we are in the middle of a refreshing, ignore swipe.
        if (refreshing) {
            return;
        } else {
            refreshing = true;
        }

        // Start animating
        swipeLayout.setRefreshing(true);

        // Start the pulling task.
        // When it ends, it'll end the refresh animation.
        try {
            new PullContent().execute();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    public String getImageURL(String originalURL) {

        String finalUrl = null;

        //TODO: Best management of this timthumb madness. Maybe use URL utilites.
        try {
            finalUrl = originalURL.replace("/timthumb.php?src=", "");
            int sub = finalUrl.lastIndexOf("&h=");

            finalUrl = finalUrl.substring(0, sub);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }

        // If we couldn' fiddle with url.
        return finalUrl != null ? finalUrl : originalURL;
    }

    private class PullContent extends AsyncTask {
        @Override
        protected Object[] doInBackground(Object[] params) {
            Log.d(LOG_TAG, "doInBackground of AsyncTask:" + this.toString());
            //Get data online.
            ParserNoticia.parseURL(getActivity(), mURL);
            return params;
        }

        @Override
        protected void onPostExecute(Object params) {
            super.onPostExecute(params);

            Log.d(LOG_TAG, "onPostExecute of AsyncTask:" + this.toString());

            // Stop refreshing animation
            swipeLayout.setRefreshing(false);

            // Unset flag so we can allow new refreshes.
            refreshing = false;
        }
    }
}
