package com.samdelong.timemachine;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
/*
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
*/
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final int REQUEST_CODE = 1000;
	private float initialTouchX;
	private float initialTouchY;
	private int mScreenDensity;
	private MediaProjectionManager mProjectionManager;
	private MediaProjection mMediaProjection;
	private VirtualDisplay mVirtualDisplay;
	private MediaProjectionCallback mMediaProjectionCallback;
	private Switch mToggleButton;
	private MediaRecorder mMediaRecorder;
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
	private static final int REQUEST_PERMISSIONS = 10;
	private List<String> myList;
	private Integer imageList[];
	private Integer _recWidth;
	private Integer _recHeight;
	private Integer _fps;
	private Integer rNumber;
	private CheckBox micCheck;
	private WindowManager fcWindowManager;
	final Handler handler = new Handler();
	Runnable runnable;
	NotificationManager mNotificationManager;
	private ViewFlipper mainFlipper;
	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.navigation_moments:
					mainFlipper.setDisplayedChild(mainFlipper.indexOfChild(findViewById(R.id.momentsFlip)));
					return true;
				case R.id.navigation_settings:
					mainFlipper.setDisplayedChild(mainFlipper.indexOfChild(findViewById(R.id.settingsLayout)));
					return true;

			}
			return false;
		}

	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mainFlipper = (ViewFlipper) findViewById(R.id.viewFlipperMain);
		rNumber = 0;
		//BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		//navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
		setup();
		getRecordings();
	}


	private void setup(){
		//Make sure there are no files in the temp folder
		File directory = Environment
				.getExternalStoragePublicDirectory(Environment
						.DIRECTORY_DOWNLOADS + "/TimeMachine/temp/");
		directory.mkdirs();
		File file = new File(directory + "");
		final File list[] = file.listFiles();
		try {
			for (int i = 0; i < list.length; i++) {
				list[i].delete();
			}
		}catch(NullPointerException ignored){

		}
		//Setup Media Recording and Projection
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenDensity = metrics.densityDpi;

		mMediaRecorder = new MediaRecorder();

		mProjectionManager = (MediaProjectionManager) getSystemService
				(Context.MEDIA_PROJECTION_SERVICE);
		//Setup switch and permissions
		mToggleButton = (Switch) findViewById(R.id.toggleButton);
		mToggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getRecordings();
				if (ContextCompat.checkSelfPermission(MainActivity.this,
						Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
						.checkSelfPermission(MainActivity.this,
								Manifest.permission.RECORD_AUDIO)
						!= PackageManager.PERMISSION_GRANTED) {
					if (ActivityCompat.shouldShowRequestPermissionRationale
							(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
							ActivityCompat.shouldShowRequestPermissionRationale
									(MainActivity.this, Manifest.permission.RECORD_AUDIO)) {
						mToggleButton.setChecked(false);
						Snackbar.make(findViewById(android.R.id.content), "",
								Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
								new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										ActivityCompat.requestPermissions(MainActivity.this,
												new String[]{Manifest.permission
														.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
												REQUEST_PERMISSIONS);
									}
								}).show();
					} else {
						ActivityCompat.requestPermissions(MainActivity.this,
								new String[]{Manifest.permission
										.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
								REQUEST_PERMISSIONS);
					}
				} else {
					onToggleScreenShare(v);
				}
			}
		});
		//Finished with setup
	}


	void openSavedAlert(){



		try {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int width = size.x;
			int height = size.y;
			fcWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
			TextView savedV = new TextView(MainActivity.this);
			savedV.setText("Recording Saved!");
			final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
					400,
					400,
					WindowManager.LayoutParams.TYPE_PHONE,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					PixelFormat.TRANSLUCENT);
			params.gravity = Gravity.TOP | Gravity.CENTER;
			params.x = 50;
			params.y = 50;
			savedV.setAlpha(0f);
			savedV.setTypeface(Typeface.DEFAULT_BOLD);
			savedV.setTextColor(-1);
			fcWindowManager.addView(savedV, params);
			savedV.animate().alpha(1f).setDuration(1000);
			fcWindowManager.removeView(savedV);
		}catch(Exception ignored){}

	}

	@Override
	protected void onNewIntent(Intent intent){
	super.onNewIntent(intent);
		try {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			handler.removeCallbacks(runnable);
			manageNotify(false);
		} catch (Exception f) {
			f.printStackTrace();
			handler.removeCallbacks(runnable);

		}

		mergeVideo();
		File directory = Environment
				.getExternalStoragePublicDirectory(Environment
						.DIRECTORY_DOWNLOADS + "/TimeMachine/temp/");
		File file = new File(directory + "");
		final File list[] = file.listFiles();
		for (int i = 0; i < list.length; i++) {
			list[i].delete();
		}
		initRecorder();
		shareScreen();
		startTimer();
		getRecordings();
		manageNotify(true);

	}


	void manageNotify(Boolean isStart){

		if(isStart){

			mNotificationManager = (NotificationManager)
					this.getSystemService(Context.NOTIFICATION_SERVICE);

			Intent notificationIntent = new Intent(this, MainActivity.class);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
					Intent.FLAG_ACTIVITY_SINGLE_TOP);

			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
			NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_menu_camera, "Save moment!", contentIntent).build();

			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(this)
							.setSmallIcon(R.drawable.ic_menu_camera)
							.setContentTitle(getString(R.string.app_name))
							.setContentText("Recording...")
							.addAction(action)
							.setContentIntent(contentIntent)
							.setPriority(Notification.PRIORITY_MAX)
							.setWhen(0);
			mNotificationManager.notify(001, mBuilder.build());

		}
		else{
			mNotificationManager.cancelAll();
		}


	}

	//Get recordings, add them to the list, and give user interface for sharing, deleting and renaming
	public void getRecordings() {

		final GridView listView = (GridView) findViewById(R.id.recordingsList);
		myList = new ArrayList<String>();

		TextView noRecordings = (TextView) findViewById(R.id.noRecordingsView);
		final File file;
		File directory = new File(Environment.getExternalStorageDirectory() + "/Download/TimeMachine/v/");

		directory.mkdirs();
		file = new File(directory + "");
		try {
			final File list[] = file.listFiles();

			if (list.length == 0) {
				noRecordings.setAlpha(1f);
			} else {
				noRecordings.setAlpha(0f);
			}
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
				} else {
					myList.add(list[i].getName().replace(".mp4", ""));
				}

			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1, myList);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
				                        int position, long id) {
					File selectedItem = list[position];
					Log.i("-----------", selectedItem.toString());

					Intent myIntent = new Intent();
					myIntent.setAction(Intent.ACTION_VIEW);
					final String downloads = selectedItem.toString();
					myIntent.setDataAndType(Uri.parse(downloads), "video/*");
					startActivity(myIntent);
				}
			});
			//Setting uip a button
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				                               int pos, long id) {
					final File systemFile = list[pos];

					final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
					alertDialog.setTitle("Options");
					alertDialog.setMessage("Share, Delete, or Rename this recording");
					alertDialog.setButton(Dialog.BUTTON_NEUTRAL, "DELETE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							systemFile.delete();
							getRecordings();

						}
					});
					alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Rename", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							alertDialog.closeOptionsMenu();
							final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							final EditText input = new EditText(MainActivity.this);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
							input.setInputType(InputType.TYPE_CLASS_TEXT);
							input.setHint("New Recording Name");
							builder.setView(input)

									.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int id) {


											String newName = String.valueOf(Environment
													.getExternalStoragePublicDirectory(Environment
															.DIRECTORY_DOWNLOADS) + "/TimeMachine/v/" + input.getText().toString() + ".mp4");
											Uri newFileUri = (Uri.parse(newName));
											File newFile = new File(newFileUri.getPath());
											systemFile.renameTo(newFile);
											getRecordings();

										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {

										}
									});
							builder.show();


						}
					});

					alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "SHARE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							MediaScannerConnection.scanFile(MainActivity.this, new String[]{systemFile.toString()},
									null, new MediaScannerConnection.OnScanCompletedListener() {
										public void onScanCompleted(String path, Uri uri) {
											Intent intent = new Intent(android.content.Intent.ACTION_SEND);
											intent.setType("video/*");
											// intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject/Title");
											intent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(systemFile));
											startActivity(Intent.createChooser(intent, "Choose sharing method"));
										}
									});
						}
					});
					alertDialog.show();

					return true;
				}
			});
		} catch (NullPointerException n) {

		}


	}

	//On result of mediaprojection
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_CODE) {
			Log.e(TAG, "Unknown request code: " + requestCode);
			return;
		}
		if (resultCode != RESULT_OK) {
			Toast.makeText(this,
					"Please Allow Permissions", Toast.LENGTH_SHORT).show();

			mToggleButton.setChecked(false);
			handler.removeCallbacks(runnable);

			return;
		} else {
			mMediaProjectionCallback = new MediaProjectionCallback();
			mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
			mMediaProjection.registerCallback(mMediaProjectionCallback, null);
			mVirtualDisplay = createVirtualDisplay();
			mMediaRecorder.start();
		}
	}

	public void onToggleScreenShare(final View view) {
getRecordings();
		if (((Switch) view).isChecked()) {

			try {
				File directory = Environment
						.getExternalStoragePublicDirectory(Environment
								.DIRECTORY_DOWNLOADS + "/TimeMachine/temp/");
				File file = new File(directory + "");
				final File list[] = file.listFiles();
				for (int i = 0; i < list.length; i++) {
					list[i].delete();
				}
				//Start recorder and get permissions
				initRecorder();
				shareScreen();
				//Start loop recording timer
				startTimer();
				manageNotify(true);
			}catch(Exception e){

				mToggleButton.setChecked(false);

			}
		}else {

			try {
				mMediaRecorder.stop();
				mMediaRecorder.reset();
				handler.removeCallbacks(runnable);
				manageNotify(false);
			} catch (Exception f) {

				handler.removeCallbacks(runnable);

			}


		}
	}

	//Mp4 parser merging
	public void mergeVideo(){
		Log.i("this", "begin");



		try {
			String filefirst;
			String filesecond;
			File directory = Environment
					.getExternalStoragePublicDirectory(Environment
							.DIRECTORY_DOWNLOADS + "/TimeMachine/temp/");
			directory.mkdirs();
			File file = new File(directory + "");
			final File list[] = file.listFiles();






			filefirst = new File(directory + "/" + (rNumber - 2) + ".mp4").getPath();
			filesecond = new File(directory + "/" + (rNumber -1) + ".mp4").getPath();
			Log.i("FILEFIRST", filefirst);
			Log.i("FILESECOND", filesecond);

			Movie[] inMovies = new Movie[]{

					MovieCreator.build(filefirst),
					MovieCreator.build(filesecond),
			};
			List<Track> videoTracks = new LinkedList<Track>();
			List<Track> audioTracks = new LinkedList<Track>();
			for (Movie m : inMovies) {
				for (Track t : m.getTracks()) {
					if (t.getHandler().equals("soun")) {
						audioTracks.add(t);
					}
					if (t.getHandler().equals("vide")) {
						videoTracks.add(t);
					}
				}
			}
			Movie result = new Movie();
			if (audioTracks.size() > 0) {
				result.addTrack(new AppendTrack(audioTracks
						.toArray(new Track[audioTracks.size()])));
			}
			if (videoTracks.size() > 0) {
				result.addTrack(new AppendTrack(videoTracks
						.toArray(new Track[videoTracks.size()])));
			}
			Log.i("this", "beforebasic");
			BasicContainer out = (BasicContainer) new DefaultMp4Builder().build(result);
			Log.i("this", "afterbasic");
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd:hh:mm:s");
			Date d = new Date();
			String s = dateFormat.format(d).replace(":","_");
			WritableByteChannel fc = new RandomAccessFile(
					String.format(Environment.getExternalStorageDirectory() + "/Download/TimeMachine/v/" + s +

							".mp4" ), "rw").getChannel();
			Log.i("this", "afterwritable");

			out.writeContainer(fc);
			fc.close();

			Log.i("BYTECHANNEL", fc.toString());

			for(int i = 0; i<list.length; i++){
				list[i].delete();
			}
			getRecordings();
			openSavedAlert();
		}catch(Exception e) {
			File directory = Environment
					.getExternalStoragePublicDirectory(Environment
							.DIRECTORY_DOWNLOADS + "/TimeMachine/temp/");


			File file = new File(directory + "");
			final File list[] = file.listFiles();

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd:hh:mm:s");
			Date d = new Date();
			String s = dateFormat.format(d).replace(":","_");
			File gg = new File(list[0].getPath());

			File newFile = new File(Environment.getExternalStorageDirectory() + "/Download/TimeMachine/v/" + s + ".mp4");
			boolean ssss = gg.renameTo(newFile);

			Log.i("FILEEEE",String.valueOf(ssss));
			Log.i("FILEEEE",gg.getPath());

			e.printStackTrace();
		}
	}



	//Loop recording timer
	void startTimer() {


		try {
			runnable = new Runnable() {
				@Override
				public void run() {

					mMediaRecorder.stop();
					mMediaRecorder.reset();
					try {
						// mMediaRecorder.start();
						File directory = Environment
								.getExternalStoragePublicDirectory(Environment
										.DIRECTORY_DOWNLOADS + "/TimeMachine/temp/");
						directory.mkdirs();

						File file = new File(directory + "");
						final File list[] = file.listFiles();

						if (list.length > 1) {
							Integer s = rNumber - 2;
							File file2 = new File(directory + "/" + s.toString() + ".mp4");
							file2.delete();
							Log.i("Direc", file2.toString());
							Log.i("file", s.toString());
						}



						initRecorder();
						shareScreen();
					}catch(Exception e){
						e.printStackTrace();

					}

					handler.postDelayed(this, 30000);
				}
			};

			handler.postDelayed(runnable, 30000);

		}catch(IllegalStateException f){

			handler.removeCallbacks(runnable);

		}
// And From your main() method or any other method
	}
	private void shareScreen() {
		if (mMediaProjection == null) {
			startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
			return;
		}
		mVirtualDisplay = createVirtualDisplay();
		mMediaRecorder.start();
	}
	private VirtualDisplay createVirtualDisplay() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x/2;
		int height = size.y/2;

		return mMediaProjection.createVirtualDisplay("MainActivity",
				width, height, mScreenDensity,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
				mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
	}
	private void initRecorder() {


		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = (size.x)/2;
		int height = (size.y)/2;

		File s = new File(Environment
				.getExternalStoragePublicDirectory(Environment
						.DIRECTORY_DOWNLOADS) + "/TimeMachine/temp/");
		if (!s.exists()) {
			s.mkdirs();
		}

		if (width < 768) {
			width = 360;
			height = 640;
		}
		try {
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mMediaRecorder.setOutputFile(Environment
					.getExternalStoragePublicDirectory(Environment
							.DIRECTORY_DOWNLOADS) + "/TimeMachine/temp/" + rNumber + ".mp4");
			mMediaRecorder.setVideoSize(width, height);
			mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mMediaRecorder.setVideoEncodingBitRate(5000 * 4000);
			mMediaRecorder.setVideoFrameRate(30);
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			int orientation = ORIENTATIONS.get(rotation + 90);
			mMediaRecorder.setOrientationHint(orientation);
			mMediaRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}

		rNumber += 1;


	}
	private class MediaProjectionCallback extends MediaProjection.Callback {
		@Override
		public void onStop() {
			if (mToggleButton.isChecked()) {
				mToggleButton.setChecked(false);
				mMediaRecorder.stop();
				mMediaRecorder.reset();
				Log.v(TAG, "Recording Stopped");
			}
			mMediaProjection = null;
			stopScreenSharing();
		}
	}
	private void stopScreenSharing() {
		if (mVirtualDisplay == null) {
			return;
		}
		mVirtualDisplay.release();
		//mMediaRecorder.release(); //If used: mMediaRecorder object cannot
		// be reused again
		destroyMediaProjection();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroyMediaProjection();
	}

	private void destroyMediaProjection() {
		if (mMediaProjection != null) {
			mMediaProjection.unregisterCallback(mMediaProjectionCallback);
			mMediaProjection.stop();
			mMediaProjection = null;
		}
		Log.i(TAG, "MediaProjection Stopped");
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String permissions[],
	                                       @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_PERMISSIONS: {
				if ((grantResults.length > 0) && (grantResults[0] +
						grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
					onToggleScreenShare(mToggleButton);
				} else {
					mToggleButton.setChecked(false);
					Snackbar.make(findViewById(android.R.id.content), "",
							Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
							new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent();
									intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
									intent.addCategory(Intent.CATEGORY_DEFAULT);
									intent.setData(Uri.parse("package:" + getPackageName()));
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
									intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
									startActivity(intent);
								}
							}).show();
				}
				return;
			}
		}
	}
}
