/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional  information
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
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	private PluginResult result;

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

			JSONObject obj = new JSONObject(args.getString(0));
			dbName = obj.getString("nameDB");
			url = obj.getString("url");
			Log.d(TAG, "!!! download zip DB from url: " + url);

			zipPath = activity.getApplicationContext().getFilesDir().getPath();
			zipPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/databases";
 
			Log.d(TAG, ".. !!! DB path: " + zipPath);
			
			this.callbackContext = callbackContext;
			isDownloaded = false;
			
			
			URL uri;
			try {
				uri = new URL(url);
			
	            HttpURLConnection httpConnection = (HttpURLConnection) uri.openConnection();  
	            httpConnection.setDoInput(true);  
	            httpConnection.setDoOutput(true);  
	            httpConnection.setConnectTimeout(60000);
	            httpConnection.setReadTimeout(60000);
	            httpConnection.setRequestMethod("GET");  
	            httpConnection.connect();  
	            if(httpConnection.getResponseCode() != 200){
	            	Log.d(TAG, "..callbackContext.error");
	            	callbackContext.error("Zip don't exists");
	            	((CordovaActivity)this.cordova.getActivity()).sendJavascript("UART.system.Helper.downloadDB('error')");
	            	
	            }else{
	            	DownloadFile();
	            	
	            }
			} 
			catch (SocketTimeoutException e) {
				Log.d(TAG, "..callbackContext.error");
            	callbackContext.error("Zip don't exists");
            	((CordovaActivity)this.cordova.getActivity()).sendJavascript("UART.system.Helper.downloadDB('error')");
            	
	    	} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
						 
		} 
		else if(action.equals("removeDB")) { 
			
			cordova.getActivity().runOnUiThread(new Runnable() {
            	
                public void run() {       
                	
                	Log.d(TAG, "... removeDB name " + dbName);                              	
                	 
					try {
						JSONObject obj = new JSONObject(args.getString(0));
					    		
	                	String dbName = obj.getString("nameDB");
	                	
	                	DeviceDB dDB = GetDeviceDB(dbName);
	                	 
	        			int deletedRows = dDB.master_db.delete("Databases","name='" + dbName + "'", null);
	        			dDB.master_db.close();
	        			
	        			File file = new File(dDB.cordovaDBPath + dDB.cordovaDBName);
	        			Boolean isDeleted = file.delete();
	        			
	        			Log.d(TAG, "..removeDB path " + dDB.cordovaDBPath + dDB.cordovaDBName + " isDeleted " + isDeleted + " del rows " + deletedRows);
	        			
	                	callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, args.optString(0)));
	                	
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
                }

				
            });
        }
		       
        else if(action.equals("sizeDB")) {
        	        	        	
            cordova.getActivity().runOnUiThread(new Runnable() {
            	
            	JSONObject obj = new JSONObject(args.getString(0));        		
            	String dbName = obj.getString("nameDB");
                
            	DeviceDB dDB = GetDeviceDB(dbName);
            	File file = new File(dDB.cordovaDBPath + dDB.cordovaDBName);
            	
            	public void run() {
                	
                	Log.d(TAG,".. sizeDB dbName " + dbName);    
                	try{
	                	CallbackResult(true, "" + file.length());
	                    callbackContext.success("" + file.length());
                	}catch(Exception e){
                	//	Log.d(TAG, e.getMessage());
                		
                	}
                    
                }
            });
        } 
		else {
			return false;
		}
		Log.d(TAG, "..return from plugin");
		return true;
	}

	private void CallbackResult(Boolean success, String msg){
		
		Log.d(TAG, " ..CallbackResult " + success + "  " + msg);
		final CordovaActivity activity = (CordovaActivity)this.cordova.getActivity();		
		
		if(success){
			//activity.sendJavascript("UART.system.Helper.downloadDB('ok')");
			callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, msg));			 
			}
		else{
			//activity.sendJavascript("UART.system.Helper.downloadDB('error')");
			callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.ERROR, msg));			
		}
		
		
		
	}
	
	private void DownloadFile() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				myTask = new DownloadFileAsync();
				myTask.execute(url);

			}
		});

	}
private DeviceDB GetDeviceDB(String dbName) {
	DeviceDB dDB = new DeviceDB();
	
	String path = cordova.getActivity().getApplicationContext().getFilesDir().getPath();
	String dbPath = path.substring(0, path.lastIndexOf("/")) + "/app_database/";
	   
	   Log.d(TAG, dbPath);
	   
	   File file = new File(dbPath + "Databases.db");
		if(!file.exists()){ 
			Log.d(TAG, "Databases.db not found");
			dbPath = path.substring(0, path.lastIndexOf("/")) + "/app_webview/databases/";
		} 
		
			   
	
	   dDB.master_db = SQLiteDatabase.openDatabase(dbPath + "Databases.db", null,  SQLiteDatabase.OPEN_READWRITE);
	
	try{
		Cursor c = dDB.master_db.rawQuery("SELECT origin, path FROM Databases WHERE name='"+dbName+"'", null);
		c.moveToFirst();
		
		dDB.cordovaDBPath = dbPath + c.getString(0) + "/";
		dDB.cordovaDBName = c.getString(1);
		c.close();
	}catch(Exception e){
		Log.d(TAG, "Can not found fields   ORIGIN, PATH");
		
		Cursor c = dDB.master_db.rawQuery("SELECT origin, id FROM Databases WHERE name='"+dbName+"'", null);
		c.moveToFirst();
		
		dDB.cordovaDBPath = dbPath + c.getString(0) + "/";
		dDB.cordovaDBName = c.getString(1);
		c.close();
		
	}
	return dDB;
	
}
boolean isDownloaded = false;
boolean isCanceled = false;
DownloadFileAsync myTask;

	void CancelHandelr(){
		myTask.cancel(false);
		CallbackResult(false, "Cancel by user");
		
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {
		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isCanceled = isDownloaded = false;
			mProgressDialog = new ProgressDialog(activity);
			mProgressDialog.setMessage("Downloading file..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Log.d(TAG, "!!! Cancel dialog");
					isCanceled = true;
					mProgressDialog.dismiss();
					CancelHandelr();
				}
			} );
			
			mProgressDialog.show();

			Log.d(TAG, "mProgressDialog.show");

		}

		@Override 
		protected String doInBackground(String... aurl) {
			int count;

			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.setConnectTimeout(60000);
		    	conexion.setReadTimeout(60000);
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();

				
				
				if(lenghtOfFile > 0){
					ReplaceDB();
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

				while ((count = input.read(data)) != -1 && !isCanceled) {
					total += count;					
					publishProgress("" + (int) (total / 1024));
					output.write(data, 0, count);

				}

				output.flush();
				output.close();
				input.close();
				if(isCanceled == false)
					isDownloaded = true;
				
				}

			} 
			catch (SocketTimeoutException e) {
	    		Log.d(TAG, "Connection Timeout");
	    		mProgressDialog.dismiss();
				CancelHandelr();
	    	} 
			catch (Exception e) {
				Log.e(TAG, e.getMessage());
				CancelHandelr();
				
				
			}

			return null;

		}

		protected void onProgressUpdate(String... progress) {

			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String unused) {
			mProgressDialog.dismiss();
			
			if(isCanceled == true){
				CancelHandelr();
				return;
			} 

			UnzipUtility unzipper = new UnzipUtility();
			
			
			if (isDownloaded){

				try {

					//Log.d(TAG, "unzip");
					String zipFile = String
							.format("%s/%s.zip", zipPath, dbName);
					
					unzipper.unzip(zipFile, cordovaDBPath);

					/*SQLiteDatabase master_db = SQLiteDatabase.openDatabase(cordovaDBPath + cordovaDBName, null,  SQLiteDatabase.OPEN_READWRITE);
					
					master_db.execSQL("CREATE TABLE \"__WebKitDatabaseInfoTable__\" (\"key\" TEXT, \"value\" TEXT)");
					ContentValues values = new ContentValues();
			        values.put("key", "WebKitDatabaseVersionKey");
					master_db.insert("__WebKitDatabaseInfoTable__", null, values);					
					
					master_db.close(); 
					*/
					callbackContext.success("db imported");
					CallbackResult(true, "db imported_");

					
					Log.d(TAG, "unziped");
					
				} catch (Exception ex) {
					// some errors occurred
					ex.printStackTrace();
					CallbackResult(false, ex.getMessage());
				}
			}else{
				
				CallbackResult(false, "Can not find a zip with DB");
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
			//Log.d(TAG, "..unzip" + entry.getName());
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
		
		Log.d(TAG, "..Get physical DB name and path. zipPath " + zipPath);
		
		String dbPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/app_database/";
		SQLiteDatabase master_db = null;
		String field = "path";
		
		Log.d(TAG, dbPath + "Databases.db");
		File file = new File(dbPath + "Databases.db");
		
		if(!file.exists()){
			
			Log.d(TAG, "Databases.db not found");
			field = "id";
			dbPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/app_webview/databases/";
		} 
		
		
		try{
			master_db = SQLiteDatabase.openDatabase(dbPath + "Databases.db", null,  SQLiteDatabase.OPEN_READONLY);
		}catch(Exception e){
			
			
		}
		
		if(master_db != null){
		Cursor c = master_db.rawQuery("SELECT origin, "+field+" FROM Databases WHERE name='"+dbName+"'", null);
		c.moveToFirst();
		
		cordovaDBPath = dbPath + c.getString(0) + "/";
		cordovaDBName = c.getString(1);
		
		if(field == "id"){
			field += ".db";
		}
		
		c.close();
		master_db.close();
		
		Log.d(TAG, ": " + cordovaDBPath + cordovaDBName);
				
		}
	}

	class DeviceDB{
		
		SQLiteDatabase master_db;
		String cordovaDBPath;
		String cordovaDBName;		
 
	} 
	
	class CheckZip extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	      
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	    	URL uri; 
			try {
				uri = new URL(url);			
				HttpURLConnection httpCon = (HttpURLConnection) uri.openConnection();
           
				if(httpCon.getResponseCode() != 200){
				    //throw new Exception("Failed to connect");
					CallbackResult(false, "Zip don't exists");
				}
				else{
					DownloadFile();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
	      return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	      super.onPostExecute(result);
	      
	    }
	  }
	
}








