//
//  DownloadDB.m
//  UAR2014
//
//  Created by roman on 9/11/14.
//
//

#import "DownloadDB.h"
#import "SSZipArchive.h"
#import "AppDelegate.h"

@implementation DownloadDB

@synthesize activityIndicator;
@synthesize dbName;
@synthesize cordovaDBName;
@synthesize cordovaDBPath;
@synthesize url;
@synthesize zipPath;

#define downloadTimeoutValue 10.0

- (void) startAnimation
{
    if (!self.activityIndicator) {
        //        self.activityIndicator = [[UIActivityIndicatorView alloc] initWithFrame: CGRectMake(window.bounds.size.width / 2 - 36, window.bounds.size.height / 2 - 36, 72, 72)];
        self.activityIndicator = [[UIActivityIndicatorView alloc] initWithFrame: CGRectMake(5, 3, 36, 36)];
        self.activityIndicator.center = self.viewController.view.center;
        self.activityIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyleWhiteLarge;
        self.activityIndicator.hidesWhenStopped = YES;
        self.activityIndicator.autoresizingMask = (UIViewAutoresizingFlexibleLeftMargin |
                                                   UIViewAutoresizingFlexibleRightMargin |
                                                   UIViewAutoresizingFlexibleTopMargin |
                                                   UIViewAutoresizingFlexibleBottomMargin);
       
       
        [self.viewController.view addSubview: self.activityIndicator];
        NSLog(@" activity is at %@", NSStringFromCGRect(self.activityIndicator.frame));
    }
    if (!self.activityView) {
        int updMsgWidth = 205;
        int updMsgFont = 18;
        int updMsgHeight = 50;
        
        UIDevice* thisDevice = [UIDevice currentDevice];
        if(thisDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad)
        {
            updMsgWidth = 270;
            updMsgFont = 24;
            updMsgHeight = 70;
        }

        
        self.activityView = [[UIView alloc] initWithFrame:self.viewController.view.bounds];
        self.activityView.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight);
        self.activityView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.6f];
        self.activityView.tag = 102;
        UILabel* chLbl = [[UILabel alloc]initWithFrame:CGRectMake(0, 0, updMsgWidth, updMsgHeight)];
        CGPoint labelCenter = self.activityView.center;
        labelCenter.y += 36*2;
        chLbl.center = labelCenter;
        chLbl.autoresizingMask = (UIViewAutoresizingFlexibleLeftMargin |
                                  UIViewAutoresizingFlexibleRightMargin |
                                  UIViewAutoresizingFlexibleTopMargin |
                                  UIViewAutoresizingFlexibleBottomMargin);
        [chLbl setText:@"Importing event data..."];
        [chLbl setBackgroundColor:[UIColor clearColor]];
        [chLbl setTextColor:[UIColor whiteColor]];
        [chLbl setFont:[UIFont boldSystemFontOfSize:updMsgFont]];
        chLbl.textAlignment = NSTextAlignmentCenter;
        chLbl.lineBreakMode = NSLineBreakByWordWrapping;
        chLbl.numberOfLines = 0;
        [self.activityView addSubview:chLbl];
        [self.viewController.view insertSubview:self.activityView belowSubview:self.activityIndicator];
    }
	[self.activityIndicator startAnimating];
    self.viewController.view.userInteractionEnabled = NO;
    //self.viewController.webView.alpha = 0.4;
	UIApplication *application = [UIApplication sharedApplication];
	application.networkActivityIndicatorVisible = YES;
}

/* show the user that loading activity has stopped */

- (void) stopAnimation
{
    if (self.activityView) {
        
        [self.activityView removeFromSuperview];
        self.activityView = nil;
        
    }
    
    UIView* updView = [self.viewController.view viewWithTag:102];
    
    if (updView) {
        
        [updView removeFromSuperview];
    }

	[self.activityIndicator stopAnimating];
    self.activityView.hidden = YES;
    self.viewController.view.userInteractionEnabled = YES;
 //   self.viewController.webView.alpha = 1.0;
	UIApplication *application = [UIApplication sharedApplication];
	application.networkActivityIndicatorVisible = NO;
}

- (void) downloadDB: (CDVInvokedUrlCommand*)command
{
    NSMutableDictionary* args = [command.arguments objectAtIndex:0];
    
    self.dbName = [args objectForKey:@"nameDB"];
    self.url = [args objectForKey:@"url"];
    callbackId = command.callbackId;
    
    [self replaceDB];
    [self startDBDownload:self.url];
}

- (void) removeDB: (CDVInvokedUrlCommand*)command
{
    NSMutableDictionary* args = [command.arguments objectAtIndex:0];
    
    self.dbName = [args objectForKey:@"nameDB"];
    callbackId = command.callbackId;
    [self replaceDB];

    NSString* destination = [self.zipPath stringByAppendingPathComponent:self.cordovaDBPath];
    NSString* cordovaDBFullName = [destination stringByAppendingPathComponent:self.cordovaDBName];
    NSError* error;
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:cordovaDBFullName]) {
    
        [[NSFileManager defaultManager] removeItemAtPath:cordovaDBFullName error:&error];
    }
    
    [self deleteMasterDBRecord:self.dbName];
    
    plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Database deleted"];
    [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
}

- (void) deleteMasterDBRecord:(NSString*) _dbName
{
    NSString *localStorageDirectory = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *dbPath = [localStorageDirectory stringByAppendingPathComponent:@"WebKit/LocalStorage/Databases.db"];
    
    sqlite3 *db;
    if (sqlite3_open([dbPath UTF8String], &db) != SQLITE_OK) {
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database delete error"];
        [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
        return;
    }
    else {
        
        NSString* query = [NSString stringWithFormat:@"DELETE FROM Databases WHERE name='%@'", _dbName];
        sqlite3_stmt *compiledStatement;
        
        if(sqlite3_prepare_v2(db, [query UTF8String], -1, &compiledStatement, nil) == SQLITE_OK) {
            
            sqlite3_step(compiledStatement);
        } else {
            plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
            [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
        }
        
        sqlite3_finalize(compiledStatement);
        sqlite3_close(db);
    }
}

- (void) removeAllDBs: (CDVInvokedUrlCommand*)command
{
    
}

- (void) sizeDB: (CDVInvokedUrlCommand*)command
{
    NSMutableDictionary* args = [command.arguments objectAtIndex:0];
    
    self.dbName = [args objectForKey:@"nameDB"];
    callbackId = command.callbackId;
    [self replaceDB];
    
    NSString* destination = [self.zipPath stringByAppendingPathComponent:self.cordovaDBPath];
    NSString* cordovaDBFullName = [destination stringByAppendingPathComponent:self.cordovaDBName];
   
    NSString* dbSize;
    NSError* error;
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:cordovaDBFullName]) {
        
        NSDictionary* attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:cordovaDBFullName error:&error];
        
        unsigned long long size = [attributes fileSize];
        dbSize = [NSString stringWithFormat:@"%llu", size];
        
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:dbSize];
        [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
    }
    
}


- (void) replaceDB
{
    
    NSString *localStorageDirectory = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *dbPath = [localStorageDirectory stringByAppendingPathComponent:@"WebKit/LocalStorage/Databases.db"];
    self.zipPath = [localStorageDirectory stringByAppendingPathComponent:@"WebKit/LocalStorage"];
    
    sqlite3 *db;
    if (sqlite3_open([dbPath UTF8String], &db) != SQLITE_OK) {
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
        [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
        return;
    }
    else {
        
        NSString* query = [NSString stringWithFormat:@"SELECT origin, path FROM Databases WHERE name='%@'", self.dbName];
        sqlite3_stmt *compiledStatement;
        
        if(sqlite3_prepare_v2(db, [query UTF8String], -1, &compiledStatement, nil) == SQLITE_OK) {
            
            if(sqlite3_step(compiledStatement) == SQLITE_ROW)
            {
                self.cordovaDBPath = [NSString stringWithUTF8String:sqlite3_column_text(compiledStatement, 0)];
                
                self.cordovaDBName = [NSString stringWithUTF8String:sqlite3_column_text(compiledStatement, 1)];
            }
            
        
        } else {
            plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
            [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
        }
        
        sqlite3_finalize(compiledStatement);
        sqlite3_close(db);
    }
}

- (void) startDBDownload:(NSString*)dbUrl
{
	[m_data setData:nil];
    NSLog(@"dbPath %@", dbUrl);
    
    [self startAnimation];
    [self startTimer];
	NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    m_data = [[NSMutableData alloc] init];
    
    [request setURL:[NSURL URLWithString:dbUrl]];
	m_requestType = 1;      // our primary request...
	m_connection = [[NSURLConnection alloc] initWithRequest:request delegate:self startImmediately:YES];
    
}


#pragma mark URL CONNECTION DELEGATE
- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
	assert(m_connection == connection);
	
	if(data == nil)
		return;
    [m_data appendData:data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
	assert(m_connection == connection);
    //	[m_connection release];
	m_connection = nil;
    
    [self stopTimer];
	printf("error = %s\n", [[error description] UTF8String]);
    NSLog(@"connection - download fail!");
	
	NSString *errormsg = [NSString stringWithFormat:@"%s",[[error description] UTF8String]];
    [self stopAnimation];
    
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    NSLog(@"Connection finished loading...");
    
    [self stopTimer];
	if(m_requestType == 1)
	{
		[self handleNewWebData];
	}

}

-(void)handleNewWebData {
    if ( m_data )
    {
        NSLog(@"Have data and extracting zip...");
        
        NSString* filePath = [NSString stringWithFormat:@"%@/%@", self.zipPath, @"database.zip"];
        
        NSLog(@"filePaths %@", filePath);
        
        [m_data writeToFile:filePath atomically:YES];
        [NSTimer scheduledTimerWithTimeInterval:0.5 target:self selector:@selector(unzipDatabase) userInfo:@"" repeats:NO];
    }
    else {
        
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
        [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
    }
    
    [self stopAnimation];
}

- (void) unzipDatabase {
    
    NSLog(@"UPZIP ");
    NSString* filePath = [NSString stringWithFormat:@"%@/%@", self.zipPath, @"database.zip"];
    NSString* unzip_destination = [[self.zipPath stringByAppendingPathComponent:self.cordovaDBPath] stringByAppendingPathComponent:@"unzip"];
    
    [[NSFileManager defaultManager] removeItemAtPath:unzip_destination error:nil];
    NSString* destination = [self.zipPath stringByAppendingPathComponent:self.cordovaDBPath];
    NSError* error;
    
    BOOL unzipWorked = [SSZipArchive unzipFileAtPath:filePath toDestination:unzip_destination];
    NSString* cordovaDBFullName = [destination stringByAppendingPathComponent:self.cordovaDBName];
    
    if (!unzipWorked) {
        
         [[NSFileManager defaultManager] removeItemAtPath:filePath error:&error];
         plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
        [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];

        return;
    }
    
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:cordovaDBFullName]) {
    
        [[NSFileManager defaultManager] removeItemAtPath:cordovaDBFullName error:&error];

    }
    
    NSString* dbFullName; //[[destination stringByAppendingPathComponent:self.dbName] stringByAppendingString:@".db"];
    
    NSURL* dirUrl = [NSURL fileURLWithPath:unzip_destination];
    NSDirectoryEnumerator *dirEnumerator = [[NSFileManager defaultManager] enumeratorAtURL:dirUrl includingPropertiesForKeys:[NSArray arrayWithObjects:NSURLNameKey, NSURLIsDirectoryKey,nil] options:NSDirectoryEnumerationSkipsSubdirectoryDescendants  errorHandler:nil] ;
    NSMutableArray *theArray=[NSMutableArray array];
    
    for (NSURL *theURL in dirEnumerator) {
        
        // Retrieve the file name. From NSURLNameKey, cached during the enumeration.
        NSString *fileName;
        [theURL getResourceValue:&fileName forKey:NSURLNameKey error:NULL];
        
        // Retrieve whether a directory. From NSURLIsDirectoryKey, also
        // cached during the enumeration.
        
        NSNumber *isDirectory;
        [theURL getResourceValue:&isDirectory forKey:NSURLIsDirectoryKey error:NULL];
        
        
        if([isDirectory boolValue] == NO)
        {
            [theArray addObject: fileName];
            dbFullName = [unzip_destination stringByAppendingPathComponent:fileName];
        }
    }
    
   
    if ([[NSFileManager defaultManager] fileExistsAtPath:dbFullName]) {
        
        [[NSFileManager defaultManager] moveItemAtPath:dbFullName toPath:cordovaDBFullName error:&error];
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Database switched"];
        
    }
    else {
     
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
    }
    
    [[NSFileManager defaultManager] removeItemAtPath:filePath error:&error];
    [[NSFileManager defaultManager] removeItemAtPath:unzip_destination error:&error];
    
    [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
    
}


- (void) downloadTimeout {
    downloadTimer = nil;
    if(m_connection == nil || m_data == nil) return;
    
    downloadTimeouts++;
    long downSpeed = m_data.length/(downloadTimeoutValue * downloadTimeouts);//bytes per sec
    if(downloadTimeouts == 1 && downSpeed > 10000){//80kbps
        //give more time
        NSLog(@"download timer - continue");
        downloadTimer = [NSTimer scheduledTimerWithTimeInterval:downloadTimeoutValue*3 target:self selector:@selector(downloadTimeout) userInfo:@"" repeats:NO];
    }else{
        NSLog(@"connection error - timeout");
        //cancel and show error
        [m_connection cancel];
        m_connection = nil;
        [self stopAnimation];
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
        [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
    }

}

-(void) startTimer
{
    [self stopTimer];
    downloadTimeouts = 0;
    downloadTimer = [NSTimer scheduledTimerWithTimeInterval:downloadTimeoutValue target:self selector:@selector(downloadTimeout) userInfo:@"" repeats:NO];
}

-(void) stopTimer
{
    [downloadTimer invalidate];
    downloadTimer = nil;
}
@end
