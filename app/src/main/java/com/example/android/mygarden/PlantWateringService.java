package com.example.android.mygarden;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";

    public PlantWateringService() {
        super("PlantWateringService");
    }

    public static void startActionWaterPlants(Context ctx){
        Intent intent = new Intent(ctx, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        ctx.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_WATER_PLANTS.equals(action)){
                handleActionWaterPlants();
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
}
