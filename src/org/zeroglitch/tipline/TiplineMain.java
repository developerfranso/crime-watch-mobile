package org.zeroglitch.tipline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.zeroglitch.util.DataFormatter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TiplineMain extends Activity implements OnClickListener,
		LocationListener {
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
	private static final int SELECT_IMAGE = 1;
	private Button capturePhoto;
	private Button addPhoto;
	private ArrayList<String> files = new ArrayList<String>();
	private Uri imageUri;
	private String imageUriGlobal;
	private CheckBox sendAnonymous;
	private boolean sendAnon = false;
	private String deviceId;

	private Button submitData;
	private EditText desc;
	private static double longitude;
	private static double latitude;
	private File imageFile;
	private String fileName;

	private ArrayList<Communication> comms = null;
	private TextView status;
	private LocationManager lm;
	private boolean gps_enabled;
	private boolean network_enabled;

	private int increment;
	private ProgressDialog dialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.e("jamie", "xxxxxxxxxxxxxxxxxxxxxxx new xxxxxxxxxxxxxxxxxxxxx");
		setContentView(R.layout.main);
		capturePhoto = (Button) findViewById(R.id.capturePhoto);
		capturePhoto.setOnClickListener(this);

		addPhoto = (Button) findViewById(R.id.addPhoto);
		addPhoto.setOnClickListener(this);

		sendAnonymous = (CheckBox) findViewById(R.id.sendAnonymous);
		sendAnonymous.setOnClickListener(this);
		// 248.330.8706
		submitData = (Button) findViewById(R.id.submit);
		submitData.setOnClickListener(this);

		desc = (EditText) findViewById(R.id.desc);
		desc.setOnClickListener(this);

		status = (TextView) findViewById(R.id.status);
		status.setOnClickListener(this);

		if (lm == null)
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1000,
				this);

		// takePhoto();
		// editText = (EditText) findViewById(R.id.edit_text);

		// t = (TextView) findViewById(R.id.text);

	}

	// handler for the background updating
	Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			dialog.setProgress(incrementBy);
		}
	};
	private int incrementBy;

	public void onClick(View v) {
		// t.setText("Hello, " + editText.getText());

		if (v == capturePhoto) {
			takePhoto();
			return;
		}

		if (v == addPhoto) {
			startActivityForResult(
					new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
					SELECT_IMAGE);
			return;
		}

		if (v == sendAnonymous) {
			Log.i("jamie TM.onClick", "" + sendAnonymous.isChecked());
			sendAnon = sendAnonymous.isChecked();
			return;
		}

		if (v == submitData) {
			Log.i("jamie TM.onClick submit data", "");

			progressBar = progressBar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);
			// progressBar.setCancelable(true);
			progressBar.setProgress(0);

			new SubmitAsyncTask(this).execute();

			return;
		}
	}

	private void sendData() {

		setContentView(R.layout.main);

	}

	public void takePhoto() {
		// define the file-name to save photo taken by Camera activity
		fileName = new java.util.Date().getTime() + ".jpg";
		// create parameters for Intent with filename
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = lm
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location != null) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
		}
		ContentValues values = new ContentValues();
		values.put(MediaColumns.TITLE, fileName);
		values.put(ImageColumns.DESCRIPTION, "Image capture by camera");

		values.put(ImageColumns.LONGITUDE, longitude);

		values.put(ImageColumns.LATITUDE, latitude);

		Log.e("jamie", "longitude: " + longitude);
		Log.e("jamie", "latitude: " + latitude);
		// imageUri is the current activity attribute, define and save it for
		// later usage (also in onSaveInstanceState)
		this.imageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		imageUriGlobal = imageUri + "";

		// if (imageFile == null)
		imageFile = new File(Environment.getExternalStorageDirectory(),
				fileName);
		// intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(f));
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Log.e("jamie", "initial intent uri " + intent.getData());
		imageUri = Uri.fromFile(imageFile);
		Log.e("jamie", "initial image uri" + imageUri);
		Log.e("jamie", "initial image file" + imageFile);

		// intent.putExtra(
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		// intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		// intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 90);
		intent.putExtra(MediaStore.Images.Media.SIZE, 512000);
		// Log.e("jamie", "initial intent uri " + intent.getData());
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

		// finish();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.e("jamie", "statesaved");
		// ---save whatever you need to persist—
		outState.putString("filename", fileName);
		outState.putSerializable("imageFile", imageFile);
		outState.putDouble("longitude", longitude);
		outState.putDouble("latitude", latitude);
		outState.putSerializable("files", files);
		// outState.putSerializable(key, value)
		// outState.putSerializable("imageUri", coords);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.e("jamie", "state restored");
		super.onRestoreInstanceState(savedInstanceState);
		// ---retrieve the information persisted earlier---
		fileName = savedInstanceState.getString("filename");
		imageFile = (File) savedInstanceState.getSerializable("imageFile");
		latitude = savedInstanceState.getDouble("latitude");
		longitude = savedInstanceState.getDouble("longitude");
		files = (ArrayList<String>) savedInstanceState.getSerializable("files");
		// files =
		// (ArrayList<String>)savedInstanceState.getSerializable("coords");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		Log.e("jamie", "data at begining " + imageFile);
		// Toast.makeText(this, "Photo Added From Library",
		// Toast.LENGTH_LONG).show();
		System.out.println("............. onActivityResultSet()");
		// Log.i("jamie onResultSet", "in here");

		Log.i("jamie", "onResultSet.requestCode" + requestCode);
		Log.i("jamie", " resultCode" + resultCode);
		Log.i("jamie", " onResultSet RESULT_OK" + Activity.RESULT_OK);

		if (requestCode == SELECT_IMAGE) {
			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(this, "Photo Added from Library",
						Toast.LENGTH_SHORT);
				Uri selectedImage = data.getData();
				ImageFile imageFile = convertImageUriToFile(selectedImage, this);
				File f = imageFile.getFile();
				Log.d("jamie", "the file name " + f.getPath());
				files.add(f.getPath().replaceAll(" ", "_"));
				status.setText("   " + files.size() + " images");
				// TODO Do something with the select image URI
			}
		}
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// use imageUri here to access the image
				// Toast.makeText(this, "Picture was Taken Successfully.",
				// Toast.LENGTH_SHORT).show();
				// try {

				Log.e("jamie", "fileName " + fileName);

				Log.e("jamie", "after creating image file" + imageFile);
				files.add(Environment.getExternalStorageDirectory() + "/"
						+ fileName);
				status.setText("   " + files.size() + " images");
				Log.e("jamie", "the files " + files);
				// files.add(DataFormatter.parseBinaryToBase64(imageFile));

				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				Log.e("jamie", " files length" + files.size());

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Picture was not taken",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public ImageFile createImageFile(File f) {
		ImageFile image = new ImageFile();
		image.setFile(imageFile);
		image.setLatitude(latitude + "");
		image.setLongitude(longitude + "");
		return image;
	}

	public ImageFile convertImageUriToFile(Uri uri, Activity activity) {
		Cursor cursor = null;
		ImageFile image = new ImageFile();
		try {
			String[] proj = { MediaColumns.DATA, BaseColumns._ID,
					MediaStore.Images.ImageColumns.ORIENTATION,
					ImageColumns.LATITUDE, ImageColumns.LONGITUDE };
			Log.e("jamie", imageUri + " image uri");
			cursor = activity.managedQuery(uri, proj, // Which columns to
					// return
					null, // WHERE clause; which rows to return (all rows)
					null, // WHERE clause selection arguments (none)
					null); // Order-by clause (ascending by name)

			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaColumns.DATA);
			int orientation_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			int latitudeCol = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.LATITUDE);
			int longitudeCol = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.LONGITUDE);

			if (cursor.moveToFirst()) {
				String orientation = cursor.getString(orientation_ColumnIndex);
				String latitude = cursor.getString(latitudeCol);
				String longitude = cursor.getString(longitudeCol);
				Log.e("jamie", "latitude" + latitude);
				Log.e("jamie", "longitude" + longitude);
				Log.e("jamie", "global uri" + uri);
				File f = new File(cursor.getString(file_ColumnIndex));

				Log.e("jamie", "f.getbytes" + f.getPath());

				image.setFile(f);
				image.setLatitude(latitude);
				image.setLongitude(longitude);
				return image;
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	//
	// public String callWebService(String q) {
	//
	// HttpClient httpclient = new DefaultHttpClient();
	// HttpGet request = new HttpGet(URL + q);
	// request.addHeader("deviceId", deviceId);
	// ResponseHandler<String> handler = new BasicResponseHandler();
	// String result = null;
	// try {
	// result = httpclient.execute(request, handler);
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// httpclient.getConnectionManager().shutdown();
	// String tag = "TiplineMain.callWebService";
	// // Log.i(tag, result);
	// return result;
	// } // end callWebService()

	@SuppressWarnings("deprecation")
	private String getContactNameFromNumber(String number) {
		// define the columns I want the query to return
		String[] projection = new String[] { Contacts.Phones.DISPLAY_NAME,
				Contacts.Phones.NUMBER };

		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(
				Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));

		// query time
		Cursor c = getContentResolver().query(contactUri, projection, null,
				null, null);

		// if the query returns 1 or more results
		// return the first result
		if (c.moveToFirst()) {
			String name = c.getString(c
					.getColumnIndex(Contacts.Phones.DISPLAY_NAME));
			return name;
		}

		// return the original number if no match was found
		return number;
	}

	public boolean hasImageCaptureBug() {

		// list of known devices that have the bug
		ArrayList<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");

		return devices.contains(android.os.Build.BRAND + "/"
				+ android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);

	}

	public Bitmap resizeImage(int width, int height, String fileName) {
		Log.d("jamie", "resizing file: " + getResources() + fileName);
		Bitmap bitmapOrg = BitmapFactory.decodeFile(fileName);

		int oldWidth = bitmapOrg.getWidth();
		int oldHeight = bitmapOrg.getHeight();

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) width) / oldWidth;
		float scaleHeight = ((float) height) / oldHeight;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmapOrg, 640, 480,
				true);
		// (bitmapOrg, 0, 0, oldWidth,
		// oldHeight, matrix, true);

		return resizedBitmap;

	}

	public void onLocationChanged(Location arg0) {
		Log.e("jamie", " onLocationChange lat long " + arg0.getLatitude()
				+ ", " + arg0.getLongitude());
		lm.removeUpdates(this);
	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	// / Yet another inner class trying to hand
	// / Progress bar

	ProgressBar progressBar;

	public class SubmitAsyncTask extends AsyncTask<Void, Integer, Boolean> {

		int myProgress;
		TiplineMain tip;

		public SubmitAsyncTask(TiplineMain tip) {
			this.tip = tip;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			submitData.setClickable(true);
			addPhoto.setClickable(true);
			capturePhoto.setClickable(true);

			AlertDialog.Builder alt_bld = new AlertDialog.Builder(TiplineMain.this);
			alt_bld.setMessage("Hi Joe!  I hope this works like you want.").setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = alt_bld.create();
			// Title for AlertDialog
			alert.setTitle("We're on the case!!!");
			// Icon for AlertDialog
			alert.setIcon(R.drawable.icon);
			alert.show();
			progressBar.setProgress(0);

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			myProgress = 0;
			submitData.setClickable(false);
			addPhoto.setClickable(false);
			capturePhoto.setClickable(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			Communication comm = new Communication();

			incrementBy = 100 / (files.size() + 1);

			TelephonyManager tMgr = (TelephonyManager) tip
					.getSystemService(Context.TELEPHONY_SERVICE);
			String phoneNumber = tMgr.getLine1Number();

			String tranId = phoneNumber + new java.util.Date().getTime();

			Account[] accounts = AccountManager.get(tip).getAccounts();
			String email = "";
			Log.e("jamie", "length=" + accounts.length);
			/*
			 * for (Account account : accounts) { // TODO: Check possibleEmail
			 * against an email regex or treat // account.name as an email
			 * address only for certain account.type values. possibleEmail +=
			 * account.name; }
			 */

			if (accounts.length > 0) {
				email = accounts[0].name;
			}

			try {
				gps_enabled = lm
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				network_enabled = lm
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			Location location = null;
			if (gps_enabled)
				location = lm
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			else if (network_enabled)
				location = lm
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			Log.e("jamie", " gps_enabled: " + gps_enabled);
			Log.e("jamie", " network_enabled: " + network_enabled);
			if (location != null) {
				longitude = location.getLongitude();
				latitude = location.getLatitude();
			}

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("description", desc
					.getText() + ""));
			nameValuePairs.add(new BasicNameValuePair("tranId", tranId));
			nameValuePairs.add(new BasicNameValuePair("longitude", ""
					+ longitude));
			nameValuePairs
					.add(new BasicNameValuePair("latitude", "" + latitude));

			// email = "jdwfoo@gmail.com";

			// phoneNumber="8435722790";
			if (!sendAnonymous.isChecked()) {
				nameValuePairs.add(new BasicNameValuePair("email", email));
				nameValuePairs.add(new BasicNameValuePair("name",
						getContactNameFromNumber(phoneNumber)));
				nameValuePairs.add(new BasicNameValuePair("phoneNumber",
						phoneNumber));
			}

			comm = new Communication();
			comm.setParams(nameValuePairs);
			publishProgress(10);
			try {
				if (comm.postData()) {
					// TODO: set progress
					increment += incrementBy;
					publishProgress(increment);

				} else
					return false;
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}

			comms = new ArrayList<Communication>();
			for (int i = 0; i < files.size(); i++) {
				nameValuePairs = new ArrayList<NameValuePair>();
				Log.e("jamie", "sending file i " + i + files.get(i));

				try {
					// File imageFile = new
					// File(Environment.getExternalStorageDirectory(),files.get(i));
					String image;
					Bitmap smallImage = resizeImage(1024, 768, files.get(i));
					// FileOutputStream out = new
					// FileOutputStream(Environment.getExternalStorageDirectory()
					// + "small" + files.get(i));
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					smallImage.compress(Bitmap.CompressFormat.PNG, 75, out);

					image = DataFormatter
							.parseBinaryToBase64(out.toByteArray());

					Log.e("jamie", image);

					nameValuePairs
							.add(new BasicNameValuePair("tranId", tranId));

					nameValuePairs.add(new BasicNameValuePair("images", image));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} // (String)files.get(i);

				comm = new Communication();
				comm.setParams(nameValuePairs);

				try {
					comm.postData();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;

				}

				increment += incrementBy;
				publishProgress(increment);

			}

			// desc.setText("files sent" + files.size());

			files.clear();
			return true;

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
		}

	}

}