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

- (void) startAnimation
{
    if (!self.activityIndicator) {
        //        self.activityIndicator = [[UIActivityIndicatorView alloc] initWithFrame: CGRectMake(window.bounds.size.width / 2 - 36, window.bounds.size.height / 2 - 36, 72, 72)];
        self.activityIndicator = [[UIActivityIndicatorView alloc] initWithFrame: CGRectMake(5, 3, 36, 36)];
        self.activityIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyleWhiteLarge;
        self.activityIndicator.hidesWhenStopped = YES;
        self.activityIndicator.autoresizingMask = (UIViewAutoresizingFlexibleLeftMargin |
                                                   UIViewAutoresizingFlexibleRightMargin |
                                                   UIViewAutoresizingFlexibleTopMargin |
                                                   UIViewAutoresizingFlexibleBottomMargin);
       
       
        [self.viewController.view addSubview: self.activityIndicator];
        NSLog(@" activity is at %@", NSStringFromCGRect(self.activityIndicator.frame));
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
	[self.activityIndicator stopAnimating];
    self.viewController.view.userInteractionEnabled = YES;
 //   self.viewController.webView.alpha = 1.0;
	UIApplication *application = [UIApplication sharedApplication];
	application.networkActivityIndicatorVisible = NO;
}

- (void) downloadDB: (CDVInvokedUrlCommand*)command
{
    NSMutableDictionary* args = [command.arguments objectAtIndex:0];
    
    self.dbName = [args objectForKey:@"nameDB"];
    self.url = [[[args objectForKey:@"url"] stringByAppendingString:self.dbName] stringByAppendingString:@".zip"];
    callbackId = command.callbackId;
    
    [self replaceDB];
    [self startDBDownload:self.url];
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
	printf("error = %s\n", [[error description] UTF8String]);
    NSLog(@"connection - download fail!");
	
	NSString *errormsg = [NSString stringWithFormat:@"%s",[[error description] UTF8String]];
    [self stopAnimation];
	
	[[NSNotificationCenter defaultCenter] postNotificationName:@"NoConnectionNotification" object:self];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    NSLog(@"Connection finished loading...");
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
    
    NSString* filePath = [NSString stringWithFormat:@"%@/%@", self.zipPath, @"database.zip"];
    NSString* destination = [self.zipPath stringByAppendingPathComponent:self.cordovaDBPath];
    
    BOOL unzipWorked = [SSZipArchive unzipFileAtPath:filePath toDestination:destination];
    NSString* cordovaDBFullName = [destination stringByAppendingPathComponent:self.cordovaDBName];
    
    NSError* error;
    if ([[NSFileManager defaultManager] fileExistsAtPath:cordovaDBFullName]) {
    
        [[NSFileManager defaultManager] removeItemAtPath:cordovaDBFullName error:&error];

    }
    
    NSString* dbFullName = [[destination stringByAppendingPathComponent:self.dbName] stringByAppendingString:@".db"];
    if ([[NSFileManager defaultManager] fileExistsAtPath:dbFullName]) {
        
        [[NSFileManager defaultManager] moveItemAtPath:dbFullName toPath:cordovaDBFullName error:&error];
        
    }
    else {
     
        plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Database download error"];
    }
    
    [[NSFileManager defaultManager] removeItemAtPath:filePath error:&error];
    
    
     plgResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Database switched"];
    [self.commandDelegate sendPluginResult:plgResult callbackId:callbackId];
    
}

@end
