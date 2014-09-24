cordova-plugin-update-db
========================


Call function <b>cordova.downloadDB</b> from js code with params:

  <pre>cordova.downloadDB(callback, error, action, params);</pre>
  
  <i>callback</i> - success function<br/>
  <i>error</i> - error function<br/>
  
  
  Cordova Plugin - download remote DB and save to the sdcard
  <i>action</i> - 'downloadDB'
  <i>params</i> - json object
  <pre>{ db: 'databaseName', url: 'http://domain.com/data/' }</pre>
  
  
  Cordova Plugin - remove DB from master.db & card
  <i>action</i> - 'removeDB': 
  <i>params</i> - json object: 
  <pre>{ db: 'databaseName' }</pre>
  
  Cordova Plugin - remove all DBs from master.db & card
  <i>action</i> - 'removeAllDBs'

  
  Cordova Plugin - get DB size
  <i>action</i> - 'sizeDB'
  <pre>{ db: 'databaseName' }</pre>
  
========================

