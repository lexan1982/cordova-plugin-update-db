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
import java.io.DataInputStream;
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
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
* This class exposes methods in Cordova that can be called from JavaScript.
*/
public class DownloadDB extends CordovaPlugin {
     
	 final private String TAG = "CordovaPlugin";
	 private String zipPath;
     private String url;
     private String dbName;
     private ProgressDialog mProgressDialog;
     private AlertDialog mAlertDialog;
     private Activity activity; 
     private CordovaInterface cordov;
     
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
        	
        	cordov = this.cordova;
        	activity = this.cordova.getActivity();
        	
 	        String[] params = args.getString(0).split(",");
 	        
 	     	        
 	        url = params[0] + params[1]; //url + filename
 	        Log.d(TAG, "!!! download zip DB from url: " + url);
 	        dbName = params[1];
 	        zipPath = activity.getApplicationContext().getFilesDir().getPath() + "/app_databases";
 	        
 	         	        
 	        Log.d(TAG, ".. !!! DB path: " + zipPath);
 	       
 	        DownloadFile();
 	       
          // FIXME succes callback  
          callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, args.getString(0)));
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
	   
	    	mProgressDialog.setMax(lenghtOfFile/1024);
	    	InputStream input = new BufferedInputStream(url.openStream());	
	    	
	    	Log.d(TAG, "zip path:" + path);
	    	File zip = new File(zipPath);
	    	zip.mkdirs();
	    	zip = new File(path);
	    	zip.createNewFile();
	    	FileOutputStream output = new FileOutputStream(path); //activity.openFileOutput(String.format("%s.zip", dbName), Context.MODE_PRIVATE);
	    	
	    	Log.d(TAG, "zip path 2");
	    	
	    	
	    	byte data[] = new byte[1024]; 
	     	    	
	    	long total = 0;
	
	    		while ((count = input.read(data)) != -1) {
	    			total += count;
	    			Log.d(TAG, "total:" + total + " lengthOfFile:" + lenghtOfFile + " max:" + mProgressDialog.getMax());
	    			publishProgress(""+(int)((total*1024)/lenghtOfFile));
	    			output.write(data, 0, count);
	    			
	    	    	
	    		} 
	
	    		
	    		output.flush();
	    		output.close();
	    		input.close();
	    		isDownloaded = true;
		    	
	    		
    		
    	} catch (Exception e) {
    		    		
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
    		
    		if(isDownloaded)
    		
    		try {
    		
    			 Log.d(TAG, "unzip");
    			 String zipFile = String.format("%s/%s.zip", zipPath, dbName);			 			 			 			 
    			 
    			/* zipChecksum = getSHA1FromFileContent(zipFile + ".zip").toUpperCase();
    			
    			 if(zipChecksum != null && !zipChecksum.equals(remoteChecksum)){
    				 showAlertDialogCheckSum();
    				 File f = new File(zipFile + ".zip");		         
    		         f.delete();
    		         zipChecksum = null;
    				 return;
    				
    			 }
    			 */
    			 unzipper.unzip(zipFile, zipPath + '/' + dbName);
    			 
    			 Log.d(TAG, "unzip 2");
    			 
    	         File f = new File(zipFile + ".zip");
    	         
    	         //reloadAppFromZip(remoteVersion);
    	         
    	         //f.delete();
    	         
    	         
    	         cordov.getThreadPool().execute(new Runnable() {
    	        	    @Override
    	        	    public void run() {
    	        	    	ImportDataJsonToDb();                     
    	        	    }

    	        	});
    	         
    	         
    	         
    	       /*  File[] all = new File(zipFile).listFiles();
    	         for(int i = 0; i < all.length; i++){
    	        	 boolean isDeleted = false;
    	        	 
    	        	 if(!all[i].getName().equals(dbName))
    	        		 isDeleted = DeleteRecursive(all[i]);
    	         }
    	         */
    	     } catch (Exception ex) {
    	         // some errors occurred
    	         ex.printStackTrace();
    	     }
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

private void ImportDataJsonToDb(){
	
	String jsonPath = String.format("%s/%s/json", zipPath, dbName);
	File[] tables = new File(jsonPath).listFiles();
	DBHelper db = new DBHelper(activity.getApplicationContext(), dbName);
	
	for(int i = 0; i < tables.length; i++){
		
		String table = tables[i].getName();
		
		Log.d(TAG, "import table: " + table);
		
		db.importData(jsonPath, table);
		
		//db.close();
	}
	
	
	
}

public class DBHelper extends SQLiteOpenHelper {
    
    final static int DB_VER = 1;
    public String DB_NAME;
    SQLiteDatabase db;
    
    /*final String TABLE_NAME = "todo";
    final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                                "( _id INTEGER PRIMARY KEY , "+
                                " todo TEXT)";
    final String DROP_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME;
    final String DATA_FILE_NAME = "data.txt";
    */
    Context mContext;
    
    public DBHelper(Context context, String dbName) {    	
        super(context, dbName, null, DB_VER);
        this.DB_NAME = dbName;
        Log.d("CordovaPlugin","constructor called");
        mContext = context;
    
        
        db = super.getWritableDatabase();
        
        Log.d(TAG,"...DB ready");
    }
    
    public void importData(String filePath, String fileName) {
    	String path = filePath + '/' + fileName;
    
    	Log.d(TAG, path);
    	
    	
    	String table = fileName.substring(0, fileName.indexOf("_"));
    	File file = new File(path);
    	FileInputStream stream = null;
    	
        String jString = null;
        try {
        	stream = new FileInputStream(file);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            jString = Charset.defaultCharset().decode(bb).toString();
            this.fillData(table, jString);
          }catch(Exception e){
        	  Log.e(TAG, "error read table json");
        	  Log.e(TAG, e.getMessage());
        	  
          }
          finally {
            try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            if(jString != null){
            	//Log.d(TAG,jString);
            }
          }
    }
    
    @Override
        public void onCreate(SQLiteDatabase db) {
        Log.d("CordovaPlugin","onCreate() called");
 //       db.execSQL(CREATE_TABLE);
  //  	        fillData(db);
        }
    
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	     //  db.execSQL(DROP_TABLE);
    	       onCreate(db);
        }
    
        private void createTable(String name, String columns){
        	try{
            	db.execSQL("CREATE TABLE " + name + " ("+ columns +")");
            	}catch(Exception e){
            		Log.e(TAG, "error create table: " + name);
            		Log.e(TAG, e.getMessage());
            	}
        }
        
        private void insertData(String table, String columns, StringBuilder data){
        	Log.d(TAG, "SQL: table" + table + " columns:" + columns );
        	
        	try{
        	db.execSQL("INSERT INTO " + table + " ("+ columns +") VALUES " + data);
        	}catch(Exception e){
        		Log.e(TAG, "error write to DB");
        		Log.e(TAG, e.getMessage());
        	}
        }
            
        private void fillData(String table, String json){
        	int step=0;
        	try {
				JSONObject obj = new JSONObject(json);
				obj = obj.getJSONObject("data");
				JSONArray columnsArr = obj.getJSONArray("columns");
				Log.d(TAG, table + "rows:" + columnsArr.length());
				
				String columns = columnsArr.join(",");
				JSONArray rows = obj.getJSONArray("rows");
				StringBuilder data = new StringBuilder();
				int portion = 0;
			   	
				this.createTable(table, columns);
				
				for(int i = 0; i < rows.length(); i++ )
				{
				    	step=i;
					JSONArray row = rows.getJSONArray(i);
				    data.append("(");
				    //safeData(row);
					data.append(row.join(","));
					
					portion++;
					if(i == rows.length()-1 || portion == 450)
					{
						data.append(")");
						portion = 0;
						insertData(table, columns, data);
						data = new StringBuilder();
					}else
						data.append("),");
				}
			
        	} catch (JSONException e) {
				// TODO Auto-generated catch block
        		Log.e(TAG, step + " table:"+table+": error parse json");
        		e.printStackTrace();
			}
        	
    	     /* ArrayList<String> data = getData();
    	      for(String dt:data) Log.d("CordovaPlugin","item="+dt);
    	
    	      if( db != null ){
    		    ContentValues values;
    		
    		    for(String dat:data){
    			values = new ContentValues();
    			values.put("todo", dat);
    		//	db.insert(TABLE_NAME, null, values);
    		    }
    	      }
    	      else {
    		    Log.d("CordovaPlugin","db null");
    	      }
    	      */
        }
        
        private void safeData(JSONArray row){
        	
        	for(int i = 0; i < row.length(); i++)
        	{
        		try {
        			
        			if(row.getString(i).contains("'") || row.getString(i).contains("\"")){
        				Log.d(TAG, "       find " + row.getString(i));
        				row.put(i, "...");
        				Log.d(TAG, "       find " + row.getString(i));
        			}
        			
        			
        			
					//String s = row.getString(i).replace('"',' ');
					//s = s.replaceAll("'","");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}
        }
}

	

}
