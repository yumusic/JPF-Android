package za.android.vdm.rssreader;

import gov.nasa.jpf.annotation.Checkpoint;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import za.android.vdm.rssreader.provider.DatabaseInterface;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class TimelineActivity extends Activity {
  private static final String TAG = "TimelineActivity";

  private static final String SEND_TIMELINE_NOTIFICATIONS = "com.vdm.blogger.SEND_TIMELINE_NOTIFICATIONS";
  private static final String NEW_STATUS_INTENT = "com.vdm.blogger.NEW_STATUS";

  /** Interface to the database */
  DatabaseInterface database;

  /** The list view displayed in the GUI */
  ListView listTimeline;

  /** Manages the data displayed in the listview */
  TimelineAdapter adapter;

  BroadcastReceiver receiver;

  IntentFilter filter = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
    setContentView(R.layout.timeline);

  }

  @Override
  protected void onStart() {
    super.onStart();

    // Find your views
    listTimeline = (ListView) findViewById(R.id.list_timeline);
    listTimeline.setOnItemClickListener(new OnItemClickListener() {

      @SuppressLint("NewApi")
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = ((TimelineAdapter) parent.getAdapter()).getCursor();
        String URL = c.getString(c.getColumnIndex("link"));
        ArrayList<View> list = new ArrayList<View>();
        view.findViewsWithText(list, "The Mini HTC One Will Be Calle", View.FIND_VIEWS_WITH_TEXT);
        System.out.println(list);
        Intent i = new Intent(getApplicationContext(), RSSWebView.class);
        i.putExtra("url", URL);
        startActivity(i);

      }
    });

    Button refresh = (Button) findViewById(R.id.buttonReload);
    refresh.setOnClickListener(new OnClickListener() {

      @Checkpoint("clickOnReload")
      @Override
      public void onClick(View v) {
        //if (isWifiOn()) {
          startService(new Intent(TimelineActivity.this,
              za.android.vdm.rssreader.service.RSSFeedUpdaterService.class));
      //  } else {
          //notifyUserWifiOff();
       // }
      }
    });

    // Connect to database
    database = ((RSSReaderApplication) getApplication()).getDatabase();

    // Register the update change receiver
    receiver = new TimelineReceiver();
    filter = new IntentFilter(NEW_STATUS_INTENT);
    Log.i(TAG, "Registered TimelineReceiver");
    registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS, null);

    // Starting update Service
    Log.i(TAG, "Started  UpdateService");

    getRSSItemsFromDB();

  }

  @Checkpoint("notifyUserWifiOff")
  private void notifyUserWifiOff() {
    Toast.makeText(this, "WifiDisconnected", Toast.LENGTH_SHORT).show();
  }

  public boolean isWifiOn() {
//    ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//    if (activeNetwork != null) {
//      boolean isConnected = activeNetwork.isConnectedOrConnecting();
//      boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
//      if (isConnected && isWiFi) {
//        return true;
//      }
//    }
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.i(TAG, "onResume");

  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.i(TAG, "onPause - unregister receiver");

  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.i(TAG, "onStop");
    // Unregister the receiver
    unregisterReceiver(receiver);
    database = null;
    stopUpdatingService();
  }

  private void stopUpdatingService() {
    stopService(new Intent(TimelineActivity.this,
        za.android.vdm.rssreader.service.RSSFeedUpdaterService.class));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, "onDestroy");

    // Close the database
    database.close(); // TODO -- what if the service is still using it?
  }

  class TimelineReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.i("TimelineReceiver", "received broadcast for new status updates");
      getRSSItemsFromDB();
    }

  }

  @Checkpoint("updateListView")
  public void getRSSItemsFromDB() {
    Cursor c = database.getRSSFeedUpdates();
    Log.i("TimelineReceiver", "Got cursor" + c + " with items " + c.getCount());

    if (c != null) {
      if (adapter == null) {
        Log.i("TimelineReceiver", "Creating new Adapter");

        // Create the adapter
        adapter = new TimelineAdapter(TimelineActivity.this, c);
        listTimeline.setAdapter(adapter);
      } else {
        Log.i("TimelineReceiver", "Changing cursor of adapter");

        adapter.changeCursor(c);
        adapter.notifyDataSetChanged();
      }
      //      // loop through rss feeds
      //      while (c != null && c.moveToNext()) {
      //        //the String url of the rss
      //        System.out.println("ITEM: " + c.getInt(0) + " " + c.getString(1) + " "
      //            + c.getString(c.getColumnIndex("link")));
      //      }

    }
  }
}