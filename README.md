cordova-plugin-update-db
========================


Call function <b>cordova.downloadDB</b> from js code with params:

  <pre>cordova.downloadDB(callback, error, action, params);
  
  <i>callback</i> - success function<br/>
  <i>error</i> - error function<br/>
  </pre>
  
  Cordova Plugin - download remote DB and save to the sdcard
  <pre><i>action</i> - 'downloadDB'
  <i>params</i> - json object
  {
    nameDB: 'databaseName', 
    url: 'http://domain.com/data/' 
  }</pre>
  
  
  Cordova Plugin - remove DB from master.db & card
  <pre><i>action</i> - 'removeDB': 
  <i>params</i> - json object: 
  { nameDB: 'databaseName' }</pre>
  
  
  Cordova Plugin - get DB size
  <pre><i>action</i> - 'sizeDB'
  <i>params</i> - json object:
  { nameDB: 'databaseName' }</pre>
  
========================

