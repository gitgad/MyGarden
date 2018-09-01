package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    public static final String TAG = PlantWidgetProvider.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgSrcId, int appWidgetId, long plantId, boolean showWater) {

        // Start details/main activity pending intent
        Intent startActivityIntent;
        if(plantId != PlantContract.INVALID_PLANT_ID){
            Log.d(TAG, "PlantId = " + plantId + ", starting details activity");
            startActivityIntent = new Intent(context, PlantDetailActivity.class);
            startActivityIntent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        } else {
            Log.w(TAG, "Invalid plant id, starting main activity");
            startActivityIntent = new Intent(context, MainActivity.class);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, 0);

        // Start plant watering service
        Intent startWaterPlantsService = new Intent(context, PlantWateringService.class);
        startWaterPlantsService.setAction(PlantWateringService.ACTION_WATER_PLANT);
        startWaterPlantsService.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        PendingIntent wateringPendingIntent = PendingIntent.getService(context, 0, startWaterPlantsService, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget_provider);

        // Show/hide water image
        if(showWater){
            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
        }

        // Update plant id text
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));

        // Set image resource
        views.setImageViewResource(R.id.widget_plant_image, imgSrcId);

        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

    }

    public static void updatePlantsWidget(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds, int imgSrcId, long plantId, boolean showWater){
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(ctx, appWidgetManager, imgSrcId, appWidgetId, plantId, showWater);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

