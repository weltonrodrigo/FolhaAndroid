package org.familianascimento.rodrigo.folhaandroid;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;


/**
 * An activity representing a list of Noticias. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NoticiaDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NoticiaListFragment} and the item details
 * (if present) is a {@link NoticiaDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link NoticiaListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class NoticiaListActivity extends Activity
        implements NoticiaListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * This field keeps a reference to SwipeRefreshLayout of this activity for the fragments
     * must deal with this.
     */
    private SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticia_list);

        if (findViewById(R.id.noticia_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((NoticiaListFragment) getFragmentManager()
                    .findFragmentById(R.id.noticia_list))
                    .setActivateOnItemClick(true);

        }

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_view);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /**
     * Callback method from {@link NoticiaListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(NoticiaDetailFragment.ARG_ITEM_ID, id);
            NoticiaDetailFragment fragment = new NoticiaDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.noticia_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, NoticiaDetailActivity.class);
            detailIntent.putExtra(NoticiaDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }

    }

    public SwipeRefreshLayout getSwipeLayout() {
        return swipeLayout;
    }

}
