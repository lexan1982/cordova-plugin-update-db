cordova-plugin-update-db
========================

Cordova Plugin - download remote DB and save to the sdcard

------------------------

Call function <b>cordova.downloadDB</b> from js code with params:

  <pre>cordova.downloadDB(callback, error, params);</pre>
  
  <i>callback</i> - success function<br/>
  <i>error</i> - error function<br/>
  <i>params</i> - json object: 
  <pre>{ db: 'databaseName', url: 'http://domain.com/data/' }</pre>
