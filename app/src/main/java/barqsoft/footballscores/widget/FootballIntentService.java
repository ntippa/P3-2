package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by Nalini on 3/8/2016.
 */
public class FootballIntentService extends IntentService {

    private static final String TAG = FootballIntentService.class.getSimpleName();

    private static final String[] FOOTBALL_WIDGET_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.DATE_COL
    };
    // these indices must match the projection
    private static final int INDEX_LEAGUE_ID = 0;
    private static final int INDEX_MATCH_ID = 1;
    private static final int INDEX_HOME = 2;
    private static final int INDEX_AWAY = 3;
    private static final int INDEX_HOME_GOALS = 4;
    private static final int INDEX_AWAY_GOALS = 5;
    private static final int INDEX_DATE = 6;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FootballIntentService() {
        super("FootballIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                FootballWidgetProvider.class));

//        // Get today's data from the ContentProvider
//        String location = Utility.getPreferredLocation(this);
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
//                location, System.currentTimeMillis());
        Cursor data = getContentResolver().query(DatabaseContract.BASE_CONTENT_URI, FOOTBALL_WIDGET_COLUMNS, null,
                null, DatabaseContract.scores_table.DATE_COL + " ASC");

        if (data == null) {
            Log.d(TAG," no data retrieved from db");
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

//        // Extract the weather data from the Cursor
//        int weatherId = data.getInt(INDEX_WEATHER_ID);
//        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
//        String description = data.getString(INDEX_SHORT_DESC);
//        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
//        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);
        String home_team = data.getString(INDEX_HOME);
        String away_team = data.getString(INDEX_AWAY);
        int home_goals = data.getInt(INDEX_HOME_GOALS);
        int away_goals = data.getInt(INDEX_AWAY_GOALS);
        int match_id = data.getInt(INDEX_MATCH_ID);
        int league_id = data.getInt(INDEX_LEAGUE_ID);
        String match_date = data.getString(INDEX_DATE);

        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {

            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                Log.d(TAG,"large widget layout");
                layoutId = R.layout.football_widget_large;
            } else if (widgetWidth >= defaultWidth) {
                Log.d(TAG,"Default width layout");
                layoutId = R.layout.football_widget_medium;
            } else {
                Log.d(TAG,"small layout");
                layoutId = R.layout.football_widget_small;
            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);


            //todo:
            views.setTextViewText(R.id.widget_date,match_date);
            views.setTextViewText(R.id.widget_home_team,home_team);
            views.setTextViewText(R.id.widget_away_team,away_team);
            views.setTextViewText(R.id.widget_score,home_goals + "-" + away_goals);


            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }
}
