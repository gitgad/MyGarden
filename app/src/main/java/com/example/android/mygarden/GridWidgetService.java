package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

public class GridWidgetService extends RemoteViewsService {

    public static final String TAG = GridWidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }

    private class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private Cursor mCursor;

        public GridRemoteViewsFactory(Context ctx) {
            mContext = ctx;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            //called on start and when notifyAppWidgetViewDataChanged is called
            Uri plantUri = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();
            if(mCursor != null){
                mCursor.close();
            }

            mCursor = mContext.getContentResolver().query(plantUri,
                    null,
                    null,
                    null,
                    PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            if(mCursor == null){
                return 0;
            }

            return mCursor.getCount();
        }

        /**
         * This method acts like the onBindViewHolder method in an Adapte
         */
        @Override
        public RemoteViews getViewAt(int position) {
            if(mCursor == null || mCursor.getCount() == 0){
                return null;
            }

            mCursor.moveToPosition(position);

            int idIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
            int createTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

            int plantId = mCursor.getInt(idIndex);
            int plantType = mCursor.getInt(plantTypeIndex);
            long createdAt = mCursor.getLong(createTimeIndex);
            long wateredAt = mCursor.getLong(waterTimeIndex);
            long timeNow = System.currentTimeMillis();

            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget_provider);

            // Update plant image and text
            int imgRes = PlantUtils.getPlantImageRes(mContext, timeNow - createdAt, timeNow - wateredAt, plantType);
            remoteViews.setImageViewResource(R.id.widget_plant_image, imgRes);
            remoteViews.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));

            // Always hide the water drop button in grid view mode
            remoteViews.setViewVisibility(R.id.widget_water_button, View.GONE);

            // Fill in the onClick PendingIntent template using the specific plant id for each item individually
            Bundle extras = new Bundle();
            extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            remoteViews.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            // Treat all items in the GridView the same
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
