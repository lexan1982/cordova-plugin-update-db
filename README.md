cordova-plugin-update-db
========================


Dwnload zip with DB and replace it in sdcard - call function <b>cordova.downloadDB</b> from js code with params:

  <pre>cordova.downloadDB(callback, error, action, params);
  
  <i>callback</i> - success function<br/>
  <i>error</i> - error function<br/>
  <i>action</i> - 'downloadDB'<br/>
  <i>params</i> - json object<br/>
 
  {
    nameDB: 'databaseName', 
    url: 'http://domain.com/data/' 
  }
  </pre>
  
  Cordova Plugin - download remote DB and save to the sdcard
  <pre>
  <i>action</i> - 'remoteDB'
  <i>params</i> - json object
  { nameDB: 'databaseName' }
  </pre>
  
  
  Cordova Plugin - remove all DB from master.db & card
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

