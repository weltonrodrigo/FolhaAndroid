package org.familianascimento.rodrigo.folhaandroid;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class UltimasNoticias extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = "UltimasNoticias";

    // This will hold the Adapter we are using to fill Listview
    private SimpleCursorAdapter mAdapter;

    // URl from ultimas noticias
    private final String URL_TO_NOTICIAS = "http://www.folhabv.com.br/ultimas.php";

    // Flag to avoid recrawling data from server.
    private boolean crawled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultimas_noticias);

        //Model
        String[] from = new String[]{
                noticiasDB.ULTIMAS_CHAMADA_COLUMN,
                //noticiasDB.ULTIMAS_DATA_COLUMN,
                noticiasDB.ULTIMAS_SECAO_COLUMN
        };

        //View
        int[] to = new int[]{
                R.id.list_item_title,
                R.id.list_item_secao
        };

        mAdapter = new SimpleCursorAdapter(
                getApplicationContext(),
                R.layout.lista_noticias_item,
                null, //Null cursor for now, but will be changed when query results arrive.
                from,
                to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        ListView listView = (ListView) findViewById(R.id.listUltimasNoticias);
        listView.setAdapter(mAdapter);

        // Will populate DB with Data from internet on the first time.
        // Flag crawled will avoid doubled execution.
        AsyncTask asyncTask = new AsyncTask<Object, Object, Object[]>() {
            @Override
            protected Object[] doInBackground(Object[] params) {
                //Get data online.
                if (!crawled) {
                    Parser.parseURL(getApplicationContext(), URL_TO_NOTICIAS);
                    crawled = true;
                }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ultimas_noticias, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

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

        return new CursorLoader(getApplicationContext(),
                NoticiasContentProvider.ULTIMAS_URI, result_columns, where, whereArgs, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            mAdapter.swapCursor(data);
        } else {
            Log.d(LOG_TAG, "Cursor vazio??");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
