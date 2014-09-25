cordova-plugin-update-db

Call function <b>cordova.downloadDB</b> from js code with params:

<pre>cordova.downloadDB(callback, error, action, params);</pre>
  
========================


Cordova Plugin - download zip with DB and replace it on sdcard 
<pre>
<i>callback</i> - success function<br/>
<i>error</i> - error function<br/>
<i>action</i> - 'downloadDB'<br/>
<i>params</i> - json object<br/>
 
 {
    nameDB: 'databaseName', 
    url: 'http://domain.com/data/' 
 }
</pre>
  
  Cordova Plugin - remove DB from master.db & sdcard
  <pre>
  <i>action</i> - 'removeDB'
  <i>params</i> - json object
  { nameDB: 'databaseName' }
  </pre>
  
  
  Cordova Plugin - remove all DBs from master.db & sdcard
  <pre>
  <i>action</i> - 'removeAllDBs': 
  <i>params</i> - null: 
  </pre>
   
  Cordova Plugin - get DB size
  <pre>
  <i>action</i> - 'sizeDB'
  <i>params</i> - json object:
  { nameDB: 'databaseName' }
  </pre>
  
========================

