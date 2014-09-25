cordova-plugin-update-db
========================


Call function <b>cordova.downloadDB</b> from js code with params:

  <pre>cordova.downloadDB(callback, error, action, params);
  
  <i>callback</i> - success function<br/>
  <i>error</i> - error function<br/>
  <i>action</i> - 'downloadDB'<br/>
  <i>params</i> - json object<br/>
 
  {
    nameDB: 'databaseName', 
    url: 'http://domain.com/data/' 
  }</pre>
  
  Cordova Plugin - download remote DB and save to the sdcard
  <i>action</i> - 'remoteDB'
  <i>params</i> - json object
  <pre>
  { nameDB: 'databaseName' }
  </pre>
  
  
  Cordova Plugin - remove all DB from master.db & card
  <pre><i>action</i> - 'removeAllDBs': 
  <i>params</i> - null: 
  
  
  Cordova Plugin - get DB size
  <i>action</i> - 'sizeDB'
  <i>params</i> - json object:
  <pre>
  { nameDB: 'databaseName' }
  </pre>
  
========================

