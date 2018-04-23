package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract.PlantEntry;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;
import static com.example.android.mygarden.provider.PlantContract.PlantEntry.COLUMN_CREATION_TIME;
import static com.example.android.mygarden.ui.PlantDetailActivity.EXTRA_PLANT_ID;

/**
 * TODO (2): Create a RemoteViewsService class and a RemoteViewsFactory class with:
 */

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    Cursor mCursor;

    public GridRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {
    }

    // Called onStart and when notifyAppWidgetViewDataChanged is called
    //Todo (2a) - onDataSetChanged querying the list of all plants in the database
    @Override
    public void onDataSetChanged() {
        // Get all plant info ordered by creation time
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        if (mCursor !=null) mCursor.close();
        mCursor = mContext.getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                COLUMN_CREATION_TIME
        );
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);

        long plantId = mCursor.getLong(mCursor.getColumnIndex(PlantEntry._ID));
        int plantType = mCursor.getInt(mCursor.getColumnIndex(PlantEntry.COLUMN_PLANT_TYPE));
        long createdAt = mCursor.getLong(mCursor.getColumnIndex(PlantEntry.COLUMN_CREATION_TIME));
        long wateredAt = mCursor.getLong(mCursor.getColumnIndex(PlantEntry.COLUMN_LAST_WATERED_TIME));
        long timeNow = System.currentTimeMillis();

        // Todo (2b) - getViewAt creating a RemoteView using the plant_widget layout
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        // Update the plant widget
        int imgRes = PlantUtils.getPlantImageRes(mContext, timeNow - createdAt, timeNow - wateredAt, plantType);
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));

        // Hide the water drop
        views.setViewVisibility(R.id.widget_water_button, View.GONE);

        // Fill in the pending intent template using the specific plant Id for each item
        // Todo (2b) - getViewAt setting the fillInIntent for widget_plant_image with the plant ID as extras
        Bundle extras = new Bundle();
        extras.putLong(EXTRA_PLANT_ID, plantId);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

        // Return the views
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Treat all items in the GridView the same
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
