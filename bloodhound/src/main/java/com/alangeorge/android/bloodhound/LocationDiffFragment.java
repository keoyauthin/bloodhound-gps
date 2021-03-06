package com.alangeorge.android.bloodhound;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alangeorge.android.bloodhound.model.LocationDiff;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;

import static com.alangeorge.android.bloodhound.MapDetailActivity.EXTRA_ACTION;
import static com.alangeorge.android.bloodhound.MapDetailActivity.EXTRA_START;
import static com.alangeorge.android.bloodhound.MapDetailActivity.MAP_ACTION_LOCATION_DIFF;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_ALL_COLUMNS;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LATITUDE1;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LATITUDE2;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LONGITUDE1;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LONGITUDE2;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF;
import static com.alangeorge.android.bloodhound.model.dao.DBHelper.LOCATIONS_DIFF_COLUMN_TIME2;
import static com.alangeorge.android.bloodhound.model.provider.LocationContentProvider.LOCATIONS_DIFF_CONTENT_URI;

/**
 * This fragment represents a ListView of differences between subsequent recorded locations.
 * The list is backed by a CursorAdaptor which in turn is feed by the LocationsContentProvider
 * which is a wrapper around a SQLite view.
 */
public class LocationDiffFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "LocationDiffFragment";

    private LocationDiffCursorAdaptor adapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_container, container, false);
        fillData();
        return rootView;
    }

    /**
     * Call back to ListItem clicks.  Here we start a {@link com.alangeorge.android.bloodhound.MapDetailActivity} for the clicked item.
     * <p/>
     * Please note this depends on the {@link com.alangeorge.android.bloodhound.model.LocationDiff} set on the passed in View using {@link View#setTag(int, Object)}.
     * @param listView the clicked on ListView
     * @param view the clicked on item's View
     * @param position clicked item's position in the ListView
     * @param id clicked item's unique ID
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d(TAG, "onListItemClick(" + position + ")");

        // convenience object set in LocationDiffCursorAdaptor.getView()
        LocationDiff diffObj = (LocationDiff) view.getTag(R.id.location_diff_view_tag);

        Intent detailIntent = new Intent(getActivity(), MapDetailActivity.class);

        detailIntent.putExtra(EXTRA_ACTION, MAP_ACTION_LOCATION_DIFF);
        detailIntent.putExtra(EXTRA_START, diffObj.getFromLocation().getId());
        startActivity(detailIntent);
    }

    //start LoaderManager.LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");

        return new CursorLoader(
                getActivity(),
                LOCATIONS_DIFF_CONTENT_URI,
                LOCATIONS_DIFF_ALL_COLUMNS,
                LOCATIONS_DIFF_COLUMN_LATITUDE_DIFF +  " != 0 or " + LOCATIONS_DIFF_COLUMN_LONGITUDE_DIFF + " != 0",
                null,
                LOCATIONS_DIFF_COLUMN_TIME2 + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        adapter.swapCursor(null);
    }
    //end LoaderManager.LoaderCallbacks<Cursor>

    private void fillData() {
        String[] from = new String[] {
                LOCATIONS_DIFF_COLUMN_LATITUDE1,
                LOCATIONS_DIFF_COLUMN_LONGITUDE1,
                LOCATIONS_DIFF_COLUMN_LATITUDE2,
                LOCATIONS_DIFF_COLUMN_LONGITUDE2,
                LOCATIONS_DIFF_COLUMN_TIME2
        };

        int[] to = new int[] {
                R.id.latitude1TextView,
                R.id.longitude1TextView,
                R.id.latitude2TextView,
                R.id.longitude2TextView,
                R.id.timeTextView
        };

        //getLoaderManager().enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, this);
        
        adapter = new LocationDiffCursorAdaptor(getActivity(), R.layout.location_diff_list_item_v3, null, from, to, 0);
        adapter.setViewBinder(new LocationDiffViewBinder());

        setListAdapter(adapter);
    }

    private class LocationDiffViewBinder implements SimpleCursorAdapter.ViewBinder {
        @SuppressWarnings("UnusedDeclaration")
        private static final String TAG = "LocationDiffViewBinder";

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            //Log.d(TAG, "setViewValue(" + view + ", " + cursor + ", " + columnIndex + ")");

            // 10 is the second recorded location time stamp in long
            if (columnIndex == 10 && view instanceof TextView) {
                Date date = new Date(cursor.getLong(10));

                ((TextView) view).setText(DateFormat.getInstance().format(date));

                return true;
            }

            return false;
        }
    }
}
