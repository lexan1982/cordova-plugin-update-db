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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
* This class exposes methods in Cordova that can be called from JavaScript.
*/
public class UpdateDB extends CordovaPlugin {
	 public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	 private String url;
	 private String remoteVersion;
	 private String remoteChecksum;
	 private String zipChecksum;
	 private Activity activity;
	 
	 private ProgressDialog mProgressDialog;
     private volatile boolean bulkEchoing;
     
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
        	
        	//args ['0.22-234','http://domain/update/android/','0WE34DEYJRYBVXR4521DSFHTRHf44r4rCDVHERG']
 	       
 	        String[] params = args.getString(0).split(",");
        	 
 	        this.remoteVersion = params[0];
 	        this.url = params[1] + this.remoteVersion;
 	        this.remoteChecksum = params[2];
 	       
        	this.activity = this.cordova.getActivity();
        
        	updateToVersion();
        	
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
        } else if(action.equals("stopEchoBulk")) {
            bulkEchoing = false;
        } else if(action.equals("echoBulk")) {
            if (bulkEchoing) {
                return true;
            }
            final String payload = args.getString(0);
            final int delayMs = args.getInt(1);
            bulkEchoing = true;
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    while (bulkEchoing) {
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException e) {}
                        PluginResult pr = new PluginResult(PluginResult.Status.OK, payload);
                        pr.setKeepCallback(true);
                        callbackContext.sendPluginResult(pr);
                    }
                    PluginResult pr = new PluginResult(PluginResult.Status.OK, payload);
                    callbackContext.sendPluginResult(pr);
                }
            });
        } else {
            return false;
        }
        return true;
    }
    
   
}
