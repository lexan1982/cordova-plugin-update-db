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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
* This class exposes methods in Cordova that can be called from JavaScript.
*/
public class DownloadDB extends CordovaPlugin {
     
	 final private String TAG = "CordovaPlugin";
	 private String dbPath;
     private String url;
     private String dbName;
     private ProgressDialog mProgressDialog;
     private Activity activity; 
     
     /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback context from which we were invoked.
     */
    @SuppressLint("NewApi") 
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("downloadDB")) {
        	
        	activity = this.cordova.getActivity();
        	
 	        String[] params = args.getString(0).split(",");
 	        
 	        Log.d(TAG, "!!! download zip DB from url: " + params.toString());
 	        
 	        url = params[0] + params[1]; //url + filename
 	        dbName = params[1];
 	        dbPath = activity.getDatabasePath(dbName).getPath();
 	        
 	        Log.d(TAG, "!!! DB path: " + dbPath);
 	       
 	        DownloadFile();
 	       
          // FIXME succes callback  
          //  callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, args.getString(0)));
        } else if(action.equals("echoAsync")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, args.optString(0)));
                }
            });
        } else if(action.equals("echoArrayBuffer")) {
            String data = args.optString(0);
            byte[] rawData= Base64.decode(data, Base64.DEFAULT);
            callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, rawData));
        } else if(action.equals("echoArrayBufferAsync")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    String data = args.optString(0);
                    byte[] rawData= Base64.decode(data, Base64.DEFAULT);
                    callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, rawData));
                }
            });
        } else if(action.equals("echoMultiPart")) {
            callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK, args.getJSONObject(0)));
        }  else {
            return false;
        }
        return true;
    }
    
    private void DownloadFile(){
        	activity.runOnUiThread(new Runnable() {

    				@Override
    				public void run() {
    					 new DownloadFileAsync().execute(url);

    				}
    		});
	
    }
    
class DownloadFileAsync extends AsyncTask<String, String, String> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		mProgressDialog = new ProgressDialog(activity);
			mProgressDialog.setMessage("Downloading file..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
    		
    		
    	}

    	@Override 
    	protected String doInBackground(String... aurl) {
    		int count;
    		
    	try {

	    	URL url = new URL(aurl[0]);
	    	URLConnection conexion = url.openConnection();
	    	conexion.connect();
	
	    	int lenghtOfFile = conexion.getContentLength();
	    
	    	mProgressDialog.setMax(lenghtOfFile/1024);
	    	InputStream input = new BufferedInputStream(url.openStream());	
	    	FileOutputStream output = activity.openFileOutput(String.format("%s.zip", dbName), Context.MODE_PRIVATE);
	
	    	byte data[] = new byte[1024]; 
	     
	    	long total = 0;
	
	    		while ((count = input.read(data)) != -1) {
	    			total += count;
	    			publishProgress(""+(int)((total*100)/lenghtOfFile));
	    			output.write(data, 0, count);
	    		} 
	
	    		output.flush();
	    		output.close();
	    		input.close();
	    		
	    		
    		
    	} catch (Exception e) {}
    	
    	
    	return null;

    	}
    	protected void onProgressUpdate(String... progress) {

    		 mProgressDialog.setProgress(Integer.parseInt(progress[0]));
    	}

    	@Override
    	protected void onPostExecute(String unused) {
    		mProgressDialog.dismiss();
    		UnzipUtility unzipper = new UnzipUtility();
    		 try {

    			 String zipFile = String.format("%s/%s", activity.getFilesDir(), dbName);			 			 			 			 
    			 
    			/* zipChecksum = getSHA1FromFileContent(zipFile + ".zip").toUpperCase();
    			
    			 if(zipChecksum != null && !zipChecksum.equals(remoteChecksum)){
    				 showAlertDialogCheckSum();
    				 File f = new File(zipFile + ".zip");		         
    		         f.delete();
    		         zipChecksum = null;
    				 return;
    				
    			 }
    			 */
    			 unzipper.unzip(zipFile + ".zip", dbPath );
    	         File f = new File(zipFile + ".zip");
    	         
    	         //reloadAppFromZip(remoteVersion);
    	         
    	         //f.delete();
    	         
    	         File[] all = activity.getFilesDir().listFiles();
    	         for(int i = 0; i < all.length; i++){
    	        	 boolean isDeleted = false;
    	        	 
    	        	 if(!all[i].getName().equals(dbName))
    	        		 isDeleted = DeleteRecursive(all[i]);
    	        	
    	        	 
    	         }
    	         
    	     } catch (Exception ex) {
    	         // some errors occurred
    	         ex.printStackTrace();
    	     }
    	}

    	 private void reloadAppFromZip(String version) {
    			// TODO Auto-generated method stub
    	
    		 ((CordovaActivity)activity).loadUrl(String.format("file:///%s/%s/index.html", activity.getFilesDir(), version) );
    		}
    	
    	 private boolean DeleteRecursive(File fileOrDirectory) {
    			
    		    if (fileOrDirectory.isDirectory()) 
    		        for (File child : fileOrDirectory.listFiles())
    		            DeleteRecursive(child);

    		    return fileOrDirectory.delete();
    		}
    }
public class UnzipUtility {
    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
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
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close(); 
    }
}  
}
