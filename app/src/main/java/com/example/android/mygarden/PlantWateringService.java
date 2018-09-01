package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.mygarden.action.update_plant_widgets";

    public PlantWateringService() {
        super("PlantWateringService");
    }

    public static void startActionWaterPlants(Context ctx){
        Intent intent = new Intent(ctx, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
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
            if(ACTION_WATER_PLANTS.equals(action)){
                handleActionWaterPlants();
            } else if(ACTION_UPDATE_PLANT_WIDGETS.equals(action)){
                handleActionUpdatePlantWidgets();
            }
        }
    }

    private void handleActionWaterPlants() {
        Uri plantsUri = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();

        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);

        getContentResolver().update(plantsUri,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }

    private void handleActionUpdatePlantWidgets() {

        // Default image resource
        int imgRes = R.drawable.grass;

        // Query the plant last watered
        Uri plantUri = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();

        Cursor c = getContentResolver().query(plantUri,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        if(c != null && c.getCount() > 0 ){
            c.moveToFirst();

            int createTimeIndex = c.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int plantTypeIndex = c.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
            int waterTimeIndex = c.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

            long timeNow = System.currentTimeMillis();
            long wateredAt = c.getLong(waterTimeIndex);
            long createdAt = c.getLong(createTimeIndex);
            int plantType = c.getInt(plantTypeIndex);

            c.close();

            imgRes = PlantUtils.getPlantImageRes(this,
                    timeNow - createdAt, timeNow - wateredAt, plantType);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));

        // Update all widgets
        PlantWidgetProvider.updatePlantsWidget(this, appWidgetManager, appWidgetIds, imgRes);
    }
}
