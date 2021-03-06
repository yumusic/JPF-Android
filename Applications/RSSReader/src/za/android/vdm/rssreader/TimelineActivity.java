package za.android.vdm.rssreader;

import gov.nasa.jpf.annotation.Checkpoint;
import za.android.vdm.rssreader.provider.DatabaseInterface;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class TimelineActivity extends Activity {
	private static final String TAG = "TimelineActivity";

	private static final String SEND_TIMELINE_NOTIFICATIONS = "com.vdm.blogger.SEND_TIMELINE_NOTIFICATIONS";
	private static final String NEW_STATUS_INTENT = "com.vdm.blogger.NEW_STATUS";

	/** Interface to the database */
	DatabaseInterface database;

	/** The list view displayed in the GUI */
	ListView listTimeline;

	/** Manages the data displayed in the Listview */
	TimelineAdapter adapter;

	/** Listens for update notifications */
	BroadcastReceiver receiver;
	IntentFilter filter = null;

	/** Refresh feed items */
	Button refreshButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.timeline);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// find views
		listTimeline = (ListView) findViewById(R.id.list_timeline);

		refreshButton = (Button) findViewById(R.id.buttonRefresh);
		refreshButton.setOnClickListener(new OnClickListener() {

			@Checkpoint("update")
			@Override
			public void onClick(View v) {
				startService(new Intent( TimelineActivity.this, za.android.vdm.rssreader.service.RSSFeedUpdaterService.class));
			}
		});

		// Connect to database
		database = ((RSSReaderApplication) getApplication()).getDatabase();

		// Register the update change receiver
		receiver = new TimelineReceiver();
		filter = new IntentFilter(NEW_STATUS_INTENT);
		Log.i(TAG, "Registered TimelineReceiver");
		registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS, null);

		//updateListView(loadRSSItemsFromDB());

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
	}

	class TimelineReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("TimelineReceiver", "received broadcast for new status updates");
			Cursor c = loadRSSItemsFromDB();
			updateListView(c);

		}

	}

	@Checkpoint("updateListView")
	public void updateListView(Cursor c) {
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
			// // loop through rss feeds
			// while (c != null && c.moveToNext()) {
			// //the String url of the rss
			// System.out.println("ITEM: " + c.getInt(0) + " " + c.getString(1)
			// + " "
			// + c.getString(c.getColumnIndex("link")));
			// }

		}
	}

	@Checkpoint("loadFromDB")
	public Cursor loadRSSItemsFromDB() {
		Cursor c = database.getRSSFeedUpdates();
		Log.i("TimelineReceiver",
				"Got cursor" + c + " with items " + c.getCount());
		return c;
	}
}
