//
//  DownloadDB.h
//  UAR2014
//
//  Created by roman on 9/11/14.
//
//

#import <Cordova/CDV.h>
#import "sqlite3.h"


@interface DownloadDB :CDVPlugin {
    
    NSString* zipPath;
	NSString* cordovaDBPath;
	NSString* cordovaDBName;
	NSString* url;
	NSString* dbName;
    
    NSURLConnection* m_connection;
	NSMutableData* m_data;
	int m_requestType;
    
    UIActivityIndicatorView *activityIndicator;
    
    CDVPluginResult* plgResult;
    NSString* callbackId;
    
    NSTimer* downloadTimer;
    int downloadTimeouts;
    
}

@property(nonatomic, retain) UIActivityIndicatorView * activityIndicator;
@property(nonatomic, retain) UIView * activityView;

@property(nonatomic, retain)  NSString* zipPath;
@property(nonatomic, retain)  NSString* cordovaDBPath;
@property(nonatomic, retain)  NSString* cordovaDBName;
@property(nonatomic, retain)  NSString* url;
@property(nonatomic, retain)  NSString* dbName;

@end
