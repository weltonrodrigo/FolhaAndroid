package org.familianascimento.rodrigo.folhaandroid;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/**
 * A list fragment representing a list of Noticias. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link NoticiaDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class NoticiaListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    // Hold this list adapter for further reference
    private SimpleCursorAdapter mAdapter;

    // URl from ultimas noticias
    private final String URL_TO_NOTICIAS = "http://www.folhabv.com.br/ultimas.php";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        String[] result_columns = new String[]{
                noticiasDB.ULTIMAS__ID_COLUMN,
                noticiasDB.ULTIMAS_IDNOTICIA_COLUMN,
                noticiasDB.ULTIMAS_URL_COLUMN,
                noticiasDB.ULTIMAS_CHAMADA_COLUMN,
                noticiasDB.ULTIMAS_SECAO_COLUMN,
                noticiasDB.ULTIMAS_DATA_COLUMN,
        };
        String where = null;
        String whereArgs[] = null;
        String order = null;

        return new CursorLoader(getActivity(),
                NoticiasContentProvider.ULTIMAS_URI, result_columns, where, whereArgs, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String url);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String url) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoticiaListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getActivity().setProgressBarIndeterminateVisibility(true);

        //Model
        String[] from = new String[]{
                noticiasDB.ULTIMAS_CHAMADA_COLUMN,
                noticiasDB.ULTIMAS_DATA_COLUMN,
                noticiasDB.ULTIMAS_SECAO_COLUMN,
        };

        //View
        int[] to = new int[]{
                R.id.list_item_title,
                R.id.list_item_data,
                R.id.list_item_secao
        };

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.lista_noticias_item,
                null, //Null cursor for now, but will be changed when query results arrive.
                from,
                to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        setListAdapter(mAdapter);

        getActivity().setProgressBarIndeterminate(true);
        getLoaderManager().initLoader(0, savedInstanceState, this);

        // Will populate DB with Data from internet on the first time.
        // Flag crawled will avoid doubled execution.
        AsyncTask asyncTask = new AsyncTask<Object, Object, Object[]>() {
            @Override
            protected Object[] doInBackground(Object[] params) {
                //Get data online.
                Parser.parseURL(getActivity(), URL_TO_NOTICIAS);
                return params;
            }

            @Override
            protected void onPostExecute(Object[] params) {
                super.onPostExecute(params);

                // With data already loaded on DB, start the Loader system.
                getLoaderManager().initLoader(0, (Bundle) params[0], (LoaderManager.LoaderCallbacks) params[1]);
            }
        };
        //Do the action
        asyncTask.execute(new Object[]{savedInstanceState, this});

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        Cursor c = ((SimpleCursorAdapter) listView.getAdapter()).getCursor();

        c.moveToPosition(position);

        String url = c.getString(c.getColumnIndex(noticiasDB.ULTIMAS_URL_COLUMN));

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(url);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
