/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package com.ideateam.plugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class exposes methods in Cordova that can be called from JavaScript.
 */
public class DownloadDB extends CordovaPlugin {

	final private String TAG = "CordovaPlugin";
	private String zipPath;
	private String cordovaDBPath;
	private String cordovaDBName;
	private String url;
	private String dbName;
	private ProgressDialog mProgressDialog;	
	private Activity activity; 
	private CallbackContext callbackContext;

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 * @param callbackContext
	 *            The callback context from which we were invoked.
	 */
	@SuppressLint("NewApi")
	public boolean execute(String action, final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		if (action.equals("downloadDB")) {
		
			activity = this.cordova.getActivity();

			String[] params = args.getString(0).split(",");

			url = params[0] + params[1]; // url + filename
			Log.d(TAG, "!!! download zip DB from url: " + url);
			dbName = params[1];
			zipPath = activity.getApplicationContext().getFilesDir().getPath();
			zipPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/databases";
 
			Log.d(TAG, ".. !!! DB path: " + zipPath);
			
			ReplaceDB();
			DownloadFile();
			 
			this.callbackContext = callbackContext;
			
			
		} else if (action.equals("echoAsync")) {
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					callbackContext.sendPluginResult(new PluginResult(
							PluginResult.Status.OK, args.optString(0)));
				}
			});
		} else {
			return false;
		}
		
		return true;
	}

	private void CallbackResult(Boolean success,String msg){
		
		if(success)
			this.callbackContext.sendPluginResult(new PluginResult(
						PluginResult.Status.OK, msg));
		else
			this.callbackContext.sendPluginResult(new PluginResult(
					PluginResult.Status.ERROR, msg));
	}
	
	private void DownloadFile() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				new DownloadFileAsync().execute(url);

			}
		});

	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {
		boolean isDownloaded = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(activity);
			mProgressDialog.setMessage("Downloading file..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();

			Log.d(TAG, "mProgressDialog.show");

		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;

			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();

				String path = String.format("%s/%s.zip", zipPath, dbName);

				mProgressDialog.setMax(lenghtOfFile / 1024);
				InputStream input = new BufferedInputStream(url.openStream());

				Log.d(TAG, "zip path:" + path);
				File zip = new File(zipPath);
				zip.mkdirs();
				zip = new File(path);
				zip.createNewFile();
				FileOutputStream output = new FileOutputStream(path); // activity.openFileOutput(String.format("%s.zip",
																		// dbName),
																		// Context.MODE_PRIVATE);

				Log.d(TAG, "zip path 2");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;					
					publishProgress("" + (int) ((total * 1024) / lenghtOfFile));
					output.write(data, 0, count);

				}

				output.flush();
				output.close();
				input.close();
				isDownloaded = true;

			} catch (Exception e) {
				CallbackResult(false, e.getMessage());
				Log.e(TAG, e.getMessage());
			}

			return null;

		}

		protected void onProgressUpdate(String... progress) {

			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String unused) {
			mProgressDialog.dismiss();

			UnzipUtility unzipper = new UnzipUtility();

			if (isDownloaded)

				try {

					Log.d(TAG, "unzip");
					String zipFile = String
							.format("%s/%s.zip", zipPath, dbName);
					
					unzipper.unzip(zipFile, cordovaDBPath);

					Log.d(TAG, "unzip 2");
					
					CallbackResult(true, "db imported");
					
					
				} catch (Exception ex) {
					// some errors occurred
					ex.printStackTrace();
					CallbackResult(false, ex.getMessage());
				}
		}

		
	}

	public class UnzipUtility {
		/**
		 * Size of the buffer to read/write data
		 */
		private static final int BUFFER_SIZE = 4096;

		/**
		 * Extracts a zip file specified by the zipFilePath to a directory
		 * specified by destDirectory (will be created if does not exists)
		 * 
		 * @param zipFilePath
		 * @param destDirectory
		 * @throws IOException
		 */
		public void unzip(String zipFilePath, String destDirectory)
				throws IOException {
			File destDir = new File(destDirectory);
			if (!destDir.exists()) {
				destDir.mkdir();
			}
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(
					zipFilePath));
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			Log.d(TAG, "..unzip" + entry.getName());
			while (entry != null) {
				String filePath = destDirectory + File.separator
						+ cordovaDBName;
				if (!entry.isDirectory()) {
					// if the entry is a file, extracts it
					extractFile(zipIn, filePath);
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					dir.mkdir();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			zipIn.close();
		}

		/**
		 * Extracts a zip entry (file entry)
		 * 
		 * @param zipIn
		 * @param filePath
		 * @throws IOException
		 */
		private void extractFile(ZipInputStream zipIn, String filePath)
				throws IOException {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(filePath));
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
			bos.close();
		}
	}

		
	private void ReplaceDB() {
		
		Log.d(TAG, "..ReplaceDB");
		
		String dbPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/app_database/";
		SQLiteDatabase master_db = SQLiteDatabase.openDatabase(dbPath + "Databases.db", null,  SQLiteDatabase.OPEN_READONLY);
		
		Cursor c = master_db.rawQuery("SELECT origin, path FROM Databases WHERE name='"+dbName+"'", null);
		c.moveToFirst();
		
		cordovaDBPath = dbPath + c.getString(0) + "/";
		cordovaDBName = c.getString(1);
				
       
	}

}
























