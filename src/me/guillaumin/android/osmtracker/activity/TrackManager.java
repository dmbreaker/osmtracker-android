package me.guillaumin.android.osmtracker.activity;

import java.io.File;
import java.util.Date;

import me.guillaumin.android.osmtracker.OSMTracker;
import me.guillaumin.android.osmtracker.R;
import me.guillaumin.android.osmtracker.db.DataHelper;
import me.guillaumin.android.osmtracker.db.TrackContentProvider;
import me.guillaumin.android.osmtracker.db.TrackContentProvider.Schema;
import me.guillaumin.android.osmtracker.db.TracklistAdapter;
import me.guillaumin.android.osmtracker.exception.CreateTrackException;
import me.guillaumin.android.osmtracker.gpx.ExportTrackTask;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Lists existing tracks.
 * Each track is displayed using {@link TracklistAdapter}.
 * 
 * @author Nicolas Guillaumin
 * 
 */
public class TrackManager extends ListActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = TrackManager.class.getSimpleName();

	/** Bundle key for {@link #prevItemVisible} */
	private static final String PREV_VISIBLE = "prev_visible";

	/** Constant used if no track is active (-1)*/
	private static final long TRACK_ID_NO_TRACK = -1;
	
	/** The active track being recorded, if any, or {@link TRACK_ID_NO_TRACK}; value is updated in {@link #onResume()} */
	private long currentTrackId = TRACK_ID_NO_TRACK;

	/** The previous item visible, or -1; for scrolling back to its position in {@link #onResume()} */
	private int prevItemVisible = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trackmanager);	
		getListView().setEmptyView(findViewById(R.id.trackmgr_empty));
		registerForContextMenu(getListView());
		if (savedInstanceState != null) {
			prevItemVisible = savedInstanceState.getInt(PREV_VISIBLE, -1);
		}
	}

	@Override
	protected void onResume() {
		Cursor cursor = getContentResolver().query(
				TrackContentProvider.CONTENT_URI_TRACK, null, null, null,
				Schema.COL_START_DATE + " asc");
		startManagingCursor(cursor);
		setListAdapter(new TracklistAdapter(TrackManager.this, cursor));
		getListView().setEmptyView(findViewById(R.id.trackmgr_empty));  // undo change from onPause

		// Is any track active?
		currentTrackId = DataHelper.getActiveTrackId(getContentResolver());
		if (currentTrackId != TRACK_ID_NO_TRACK) {
			((TextView) findViewById(R.id.trackmgr_hint)).setText(
					getResources().getString(R.string.trackmgr_continuetrack_hint)
						.replace("{0}", Long.toString(currentTrackId)));

			// Scroll to the active track of the list
			cursor.moveToFirst();
			// we will use the flag selectionSet to handle the while loop
			boolean selectionSet = false;
			while(!selectionSet && cursor.moveToNext()){
				if(cursor.getInt(cursor.getColumnIndex(Schema.COL_ACTIVE)) == 1){
					// This is the active track
					// set selection to the current cursor position
					getListView().setSelection(cursor.getPosition());
					selectionSet = true;
				}
			}
		} else {
			((TextView) findViewById(R.id.trackmgr_hint)).setText(R.string.trackmgr_newtrack_hint);

			// Scroll to the previous listview position,
			// now that we're bound to data again
			if (prevItemVisible != -1) {
				final int cmax = getListView().getCount() - 1;
				if (prevItemVisible > cmax) {
					prevItemVisible = cmax;
				}
				getListView().setSelection(prevItemVisible);
			}
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		// Remember position in listview (before any adapter change)
		prevItemVisible = getListView().getFirstVisiblePosition();

		CursorAdapter adapter = (CursorAdapter) getListAdapter();
		if (adapter != null) {
			// Prevents on-screen 'no tracks' message
			getListView().setEmptyView(findViewById(android.R.id.empty));
			// Properly close the adapter cursor
			Cursor cursor = adapter.getCursor();
			stopManagingCursor(cursor);
			cursor.close();
			setListAdapter(null);
		}

		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(PREV_VISIBLE, prevItemVisible);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		prevItemVisible = state.getInt(PREV_VISIBLE, -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.trackmgr_menu, menu);
		if (currentTrackId != -1) {
			MenuItem mi = menu.findItem(R.id.trackmgr_menu_newtrack);
			if (mi != null) {
				mi.setTitle(R.string.menu_continue);
				mi.setTitleCondensed(getResources().getString(R.string.menu_continue));
				mi.setIcon(android.R.drawable.ic_menu_edit);
			}
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mi = menu.findItem(R.id.trackmgr_menu_newtrack);
		if (currentTrackId != -1) {
			// Currently tracking. Set menu entry to "Continue"
			mi.setTitle(R.string.menu_continue);
			mi.setTitleCondensed(getResources().getString(R.string.menu_continue));
			mi.setIcon(android.R.drawable.ic_menu_edit);
		} else {
			// Not currently tracking. Set menu entry to "New"
			mi.setTitle(R.string.menu_newtrack);
			mi.setTitleCondensed(getResources().getString(R.string.menu_newtrack));
			mi.setIcon(android.R.drawable.ic_menu_add);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.trackmgr_menu_newtrack:
			// Start track logger activity
			try {
				Intent i = new Intent(this, TrackLogger.class);
				if (currentTrackId == -1) {
					// New track
					currentTrackId = createNewTrack();
				} else {
					i.putExtra(TrackLogger.STATE_IS_TRACKING, true);
				}
				i.putExtra(Schema.COL_TRACK_ID, currentTrackId);
				startActivity(i);
			} catch (CreateTrackException cte) {
				Toast.makeText(this,
						getResources().getString(R.string.trackmgr_newtrack_error).replace("{0}", cte.getMessage()),
						Toast.LENGTH_LONG)
						.show();
			}
			break;
		case R.id.trackmgr_menu_settings:
			// Start settings activity
			startActivity(new Intent(this, Preferences.class));
			break;
		case R.id.trackmgr_menu_about:
			// Start About activity
			startActivity(new Intent(this, About.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.trackmgr_contextmenu, menu);
		
		long selectedId = ((AdapterContextMenuInfo) menuInfo).id;
		if(currentTrackId == selectedId){
			// the selected one is the active track, so we will show the stop item
			menu.findItem(R.id.trackmgr_contextmenu_stop).setVisible(true);
		}else{
			// the selected item is not active, so we need to hide the stop item
			menu.findItem(R.id.trackmgr_contextmenu_stop).setVisible(false);
		}
		menu.setHeaderTitle(getResources().getString(R.string.trackmgr_contextmenu_title).replace("{0}", Long.toString(selectedId)));
		if ( currentTrackId ==  selectedId) {
			// User has pressed the active track, hide the delete option
			menu.removeItem(R.id.trackmgr_contextmenu_delete);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		Intent i;
		
		switch(item.getItemId()) {
		case R.id.trackmgr_contextmenu_stop:
			// stop the active track
			stopActiveTrack();
			break;
		case R.id.trackmgr_contextmenu_resume:
			// let's activate the track and start the TrackLogger activity
			setActiveTrack(info.id);
			i = new Intent(this, TrackLogger.class);
			i.putExtra(Schema.COL_TRACK_ID, info.id);
			startActivity(i);
			break;
		case R.id.trackmgr_contextmenu_delete:
			
			// Confirm and delete selected track
			new AlertDialog.Builder(this)
				.setTitle(R.string.trackmgr_contextmenu_delete)
				.setMessage(getResources().getString(R.string.trackmgr_delete_confirm).replace("{0}", Long.toString(info.id)))
				.setCancelable(true)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						getContentResolver().delete(
								ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, info.id),
								null, null);
						((CursorAdapter) TrackManager.this.getListAdapter()).getCursor().requery();
						dialog.dismiss();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();				
					}
				}).create().show();
			
			break;
		case R.id.trackmgr_contextmenu_export:	
			new ExportTrackTask(this, info.id).execute();
			break;
		case R.id.trackmgr_contextmenu_display:
			// Start display track activity, with or without OSM background
			boolean useOpenStreetMapBackground = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
					OSMTracker.Preferences.KEY_UI_DISPLAYTRACK_OSM, OSMTracker.Preferences.VAL_UI_DISPLAYTRACK_OSM);
			if (useOpenStreetMapBackground) {
				i = new Intent(this, DisplayTrackMap.class);
			} else {
				i = new Intent(this, DisplayTrack.class);
			}
			i.putExtra(Schema.COL_TRACK_ID, info.id);
			startActivity(i);
			break;
		case R.id.trackmgr_contextmenu_details:
			i = new Intent(this, TrackDetail.class);
			i.putExtra(Schema.COL_TRACK_ID, info.id);
			startActivity(i);
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * User has clicked the active track or a previous track.
	 * @param lv listview; this
	 * @param iv item clicked
	 * @param position position within list
	 * @param id  track ID
	 */
	@Override
	protected void onListItemClick(ListView lv, View iv, final int position, final long id) {
		Intent i;
		if (id == currentTrackId) {
			// continue recording the current track
			i = new Intent(this, TrackLogger.class);
			i.putExtra(Schema.COL_TRACK_ID, currentTrackId);
			i.putExtra(TrackLogger.STATE_IS_TRACKING, true);
		} else {
			// show track info
			i = new Intent(this, TrackDetail.class);
			i.putExtra(Schema.COL_TRACK_ID, id);
		}
		startActivity(i);
	}

	/**
	 * Create a new track, in DB and on SD card
	 * @returns The ID of the new track
	 * @throws CreateTrackException
	 */
	private long createNewTrack() throws CreateTrackException {

		// Create directory for track
		File sdRoot = Environment.getExternalStorageDirectory();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String storageDir = prefs.getString(OSMTracker.Preferences.KEY_STORAGE_DIR,	OSMTracker.Preferences.VAL_STORAGE_DIR);
		if (sdRoot.canWrite()) {
			// Create base OSMTracker directory on SD Card
			File osmTrackerDir = new File(sdRoot + storageDir);
			if (!osmTrackerDir.exists()) {
				osmTrackerDir.mkdir();
			}

			Date startDate = new Date();
			
			// Create entry in TRACK table
			ContentValues values = new ContentValues();
			values.put(Schema.COL_NAME, "");
			values.put(Schema.COL_START_DATE, startDate.getTime());
			
			Uri trackUri = getContentResolver().insert(TrackContentProvider.CONTENT_URI_TRACK, values);
			long trackId = ContentUris.parseId(trackUri);

			// Create track directory
			File trackDir = new File(osmTrackerDir + File.separator + "#" + trackId + "_" + DataHelper.FILENAME_FORMATTER.format(startDate));
			trackDir.mkdir();
			values.clear();
			values.put(Schema.COL_DIR, trackDir.getAbsolutePath());
			getContentResolver().update(TrackContentProvider.CONTENT_URI_TRACK, values, Schema.COL_ID + " = ?", new String[] {Long.toString(trackId)});
			
			// set the active track
			setActiveTrack(trackId);
			
			return trackId;
		} else {
			throw new CreateTrackException(getResources().getString(R.string.error_externalstorage_not_writable));
		}
	}
	
	/**
	 * Sets the active track
	 * @param trackId ID of the track to activate
	 */
	private void setActiveTrack(long trackId){
		ContentValues values = new ContentValues();
		// set all tracks inactive
		values.put(Schema.COL_ACTIVE, Schema.VAL_TRACK_INACTIVE);
		getContentResolver().update(TrackContentProvider.CONTENT_URI_TRACK, values, null, null);

		// set the track active
		values.clear();
		values.put(Schema.COL_ACTIVE, Schema.VAL_TRACK_ACTIVE);
		getContentResolver().update(TrackContentProvider.CONTENT_URI_TRACK, values, Schema.COL_ID + " = ?", new String[] {Long.toString(trackId)});
	}
	
	/**
	 * Stops the active track
	 * Sends a broadcast to be received by GPSLogger to stop logging
	 * and forces the DataHelper to stop tracking.
	 */
	private void stopActiveTrack(){
		if(currentTrackId != TRACK_ID_NO_TRACK){
			// we send a broadcast to inform all registered services to stop tracking 
			Intent intent = new Intent(OSMTracker.INTENT_STOP_TRACKING);
			sendBroadcast(intent);
			
			// need to get sure, that the database is up to date
			DataHelper dataHelper = new DataHelper(this);
			dataHelper.stopTracking(currentTrackId);

			// set the currentTrackId to "no track"
			currentTrackId = TRACK_ID_NO_TRACK;
			
		}
	}
	
}
