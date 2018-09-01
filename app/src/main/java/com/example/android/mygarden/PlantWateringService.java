package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANT = "com.example.android.mygarden.action.water_plant";
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.mygarden.action.update_plant_widgets";

    public PlantWateringService() {
        super("PlantWateringService");
    }

    public static void startActionWaterPlant(Context ctx, long plantId){
        Intent intent = new Intent(ctx, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANT);
        intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        ctx.startService(intent);
    }

    public static void startActionUpdatePlantWidgets(Context ctx){
        Intent intent = new Intent(ctx, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        ctx.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_WATER_PLANT.equals(action)){
                final long plantId = intent.getLongExtra(PlantDetailActivity.EXTRA_PLANT_ID, PlantContract.INVALID_PLANT_ID);
                handleActionWaterPlant(plantId);
            } else if(ACTION_UPDATE_PLANT_WIDGETS.equals(action)){
                handleActionUpdatePlantWidgets();
            }
        }
    }

    private void handleActionWaterPlant(long plantId) {
        Uri singlePlantUri = ContentUris.withAppendedId(
                PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build(), plantId);

        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);

        // Update only living plant
        getContentResolver().update(singlePlantUri,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});

        // Update all widgets
        startActionUpdatePlantWidgets(this);
    }

    private void handleActionUpdatePlantWidgets() {

        // Default image resource
        int imgRes = R.drawable.grass;

        // Default watering option
        boolean canWater = false;

        // Default plant id
        long plantId = PlantContract.INVALID_PLANT_ID;

        // Query the plant last watered
        Uri plantUri = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();

        Cursor c = getContentResolver().query(plantUri,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        if(c != null && c.getCount() > 0 ){
            c.moveToFirst();

            int idIndex = c.getColumnIndex(PlantContract.PlantEntry._ID);
            int createTimeIndex = c.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int plantTypeIndex = c.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
            int waterTimeIndex = c.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

            long timeNow = System.currentTimeMillis();
            long wateredAt = c.getLong(waterTimeIndex);
            long createdAt = c.getLong(createTimeIndex);
            int plantType = c.getInt(plantTypeIndex);
            plantId = c.getLong(idIndex);

            c.close();

            canWater = ((timeNow - wateredAt) > PlantUtils.MIN_AGE_BETWEEN_WATER) &&
                    ((timeNow - wateredAt) < PlantUtils.MAX_AGE_WITHOUT_WATER);

            imgRes = PlantUtils.getPlantImageRes(this,
                    timeNow - createdAt, timeNow - wateredAt, plantType);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));

        // Update all widgets
        PlantWidgetProvider.updatePlantsWidget(this, appWidgetManager, appWidgetIds, imgRes, plantId, canWater);
    }
}
