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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.ideaintech.app.UAR2015;
  
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
			url = obj.getString("url") + dbName;
			Log.d(TAG, "!!! download zip DB from url: " + url);

			zipPath = activity.getApplicationContext().getFilesDir().getPath();
			zipPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/databases";
 
			Log.d(TAG, ".. !!! DB path: " + zipPath);
			
			this.callbackContext = callbackContext;
			isDownloaded = false;
			final UAR2015 activ = (UAR2015)this.cordova.getActivity();
			
			cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                	try{
		                URL uri = new URL(url);
		                HttpURLConnection httpCon = (HttpURLConnection) uri.openConnection();
		                if(httpCon.getResponseCode() != 200){
		                	CallbackResult(false, "Zip don't exists");  
		                    //callbackContext.error("Zip don't exists"); // Thread-safe.
		                }
		                else{
		                	DownloadFile();
		                }	
		            }catch(Exception e){
		            	
		            }
                   
                }
            });
            return true;
			
/*			new Thread(){
		        public void run(){
		            try{
		                URL uri = new URL(url);
		                HttpURLConnection httpCon = (HttpURLConnection) uri.openConnection();
		                if(httpCon.getResponseCode() != 200){
		                    //throw new Exception("Failed to connect");
		                	//CallbackResult(false, "Zip don't exists");
		                	PluginResult result = new PluginResult(PluginResult.Status.ERROR);
		                    result.setKeepCallback(false);
		                    callbackContext.error("Zip don't exists");//.sendPluginResult(result);
		                }
		                else{
		                	DownloadFile();
		                }	
		            }catch(Exception e){
		            	
		            }
		        }
		    }.start();
*/
	/*		activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					URL uri;
					try {
						uri = new URL(url);
					
					URLConnection conexion = uri.openConnection();
					conexion.connect();

					int lenghtOfFile = conexion.getContentLength();

					
					
					if(lenghtOfFile > 0){
						String path = String.format("%s/%s.zip", zipPath, dbName);
						ReplaceDB();
						
					mProgressDialog.setMax(lenghtOfFile / 1024);		
						
					
					InputStream input = new BufferedInputStream(uri.openStream());

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
					int count;
					while ((count = input.read(data)) != -1) {
						total += count;					
						//publishProgress("" + (int) (total / 1024));
						output.write(data, 0, count);

					}

					output.flush();
					output.close();
					input.close();
					isDownloaded = true;
					UnzipUtility unzipper = new UnzipUtility();

					if (isDownloaded){

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
					}else{
						
						CallbackResult(false, "Can not find a zip with DB");
					}
					}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d(TAG, "..Download");
						e.printStackTrace();
						CallbackResult(false, "..Download fail");
					}
				}
			});
	*/		
			//DownloadFile();
			
			/*result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
		    this.callbackContext.sendPluginResult(result);
		    
			CheckZip mt = new CheckZip();
		    mt.execute();
			*/
			//CallbackResult(false, "Zip don't exists");
			/*new Thread(){
		        public void run(){
		            try{
		                URL uri = new URL(url);
		                HttpURLConnection httpCon = (HttpURLConnection) uri.openConnection();
		                if(httpCon.getResponseCode() != 200){
		                    //throw new Exception("Failed to connect");
		                	//CallbackResult(false, "Zip don't exists");
		                }
		                else{
		                	DownloadFile();
		                }	
		            }catch(Exception e){
		            	
		            }
		        }
		    }.start();
			cordova.getActivity().runOnUiThread(new Runnable() {
			            	
			   public void run() {     
						 
				   URL uri;
				   try {
 						uri = new URL(url);
						URLConnection conexion = uri.openConnection();
						conexion.connect();
			
						int lenghtOfFile = conexion.getContentLength();	
																
						if(lenghtOfFile > 0){									
							DownloadFile();	
									
						}else{
							callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.ERROR, args.optString(0)));
							
						}
					} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
							e.printStackTrace();
					} catch (IOException e) {
								// TODO Auto-generated catch block
							e.printStackTrace();
					}
							
							
				}
			});
				*/		
			
			
			 
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
        	
        	long fileSize;
        	
            cordova.getActivity().runOnUiThread(new Runnable() {
            	
            	JSONObject obj = new JSONObject(args.getString(0));        		
            	String dbName = obj.getString("nameDB");
                
            	DeviceDB dDB = GetDeviceDB(dbName);
            	File file = new File(dDB.cordovaDBPath + dDB.cordovaDBName);
            	
            	public void run() {
                	
                	Log.d("test",".. sizeDB dbName " + dbName);                	
                    callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, file.length()));
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
		final UAR2015 activity = (UAR2015)this.cordova.getActivity();
		
		
		if(success){
			activity.sendJavascript("UART.system.Helper.downloadDB('ok')");
			//callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, msg));			 
			}
		else{
			activity.sendJavascript("UART.system.Helper.downloadDB('error')");
			//callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.ERROR, msg));			
		}
		
		
		
	}
	
	private void DownloadFile() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				new DownloadFileAsync().execute(url);

			}
		});

	}
private DeviceDB GetDeviceDB(String dbName) {
	DeviceDB dDB = new DeviceDB();
	
	String dbPath = cordova.getActivity().getApplicationContext().getFilesDir().getPath();
	   dbPath = dbPath.substring(0, dbPath.lastIndexOf("/")) + "/app_database/";
	
	   dDB.master_db = SQLiteDatabase.openDatabase(dbPath + "Databases.db", null,  SQLiteDatabase.OPEN_READWRITE);
	
	Cursor c = dDB.master_db.rawQuery("SELECT origin, path FROM Databases WHERE name='"+dbName+"'", null);
	c.moveToFirst();
	
	dDB.cordovaDBPath = dbPath + c.getString(0) + "/";
	dDB.cordovaDBName = c.getString(1);
	c.close();
	
	return dDB;
	
}
boolean isDownloaded = false;
	class DownloadFileAsync extends AsyncTask<String, String, String> {
		

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

				
				
				if(lenghtOfFile > 0){
					String path = String.format("%s/%s.zip", zipPath, dbName);
					ReplaceDB();
					
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
					publishProgress("" + (int) (total / 1024));
					output.write(data, 0, count);

				}

				output.flush();
				output.close();
				input.close();
				isDownloaded = true;
				
				}

			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				CallbackResult(false, e.getMessage());
				
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

			if (isDownloaded){

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
		
		Log.d(TAG, "..Get physical DB name and path");
		
		String dbPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/app_database/";
		SQLiteDatabase master_db = SQLiteDatabase.openDatabase(dbPath + "Databases.db", null,  SQLiteDatabase.OPEN_READONLY);
		
		Cursor c = master_db.rawQuery("SELECT origin, path FROM Databases WHERE name='"+dbName+"'", null);
		c.moveToFirst();
		
		cordovaDBPath = dbPath + c.getString(0) + "/";
		cordovaDBName = c.getString(1);
		c.close();
		master_db.close();
		
		Log.d(TAG, ": " + cordovaDBPath + cordovaDBName);
				
       
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
























