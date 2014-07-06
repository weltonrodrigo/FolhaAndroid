package org.familianascimento.rodrigo.folhaandroid;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
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
public class NoticiaListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
        , SwipeRefreshLayout.OnRefreshListener {

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

    // The SwipeRefreshLayout of this fragment' parent Activity.
    private SwipeRefreshLayout swipeLayout;

    // A flag to to avoid requesting a refresh when already doing.
    private boolean refreshing = false;

    // The activity holding this fragment.
    private Activity mActivity;

    // This class LOG_TAG
    private static final String LOG_TAG = "NoticiasListFragment";

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

        // Check if cursor has data
        if (data.moveToFirst() && data.getCount() != 0) {
            mAdapter.swapCursor(data);
        } else {
            // Start a refresh of this view when where is no data on DB.
            onRefresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
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
        // Async tasks cannot be recycled.
        try {
            new PullContent().execute();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
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

        // Give this list an Cursor adapter with no Cursor, because we'll give it one when
        // data arrives from DB. see @link<onLoadFinished>
        setListAdapter(mAdapter);

        // Start the load, which will query the DB.
        getLoaderManager().initLoader(Constants.PULL_TASK_ULTIMAS, savedInstanceState, this);

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

        // Save a reference to this fragment activity for further usage.
        mActivity = activity;
    }

    @Override
    public void onStart() {
        super.onStart();

        swipeLayout = ((NoticiaListActivity) mActivity).getSwipeLayout();
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(false);

        final ListView listView = getListView();

        // Avoid firing onRefresh when scrolling the list
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            // Enables SwipeRefreshLayout only when on top of the list.
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listView == null || listView.getChildCount() == 0) ?
                                0 : listView.getChildAt(0).getTop();
                swipeLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

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


    /**
     * This Class is just a wrapper aroung an AsyncTask.
     */
    private class PullContent extends AsyncTask {

        @Override
        protected Object[] doInBackground(Object[] params) {
            //Get data online.
            Parser.parseURL(getActivity(), URL_TO_NOTICIAS);
            return params;
        }

        @Override
        protected void onPostExecute(Object params) {
            super.onPostExecute(params);

            // Notify SwipeLayout that refresh ended.
            swipeLayout.setRefreshing(false);

            // Unset flag so we can allow new refreshes.
            refreshing = false;
        }
    }
}
