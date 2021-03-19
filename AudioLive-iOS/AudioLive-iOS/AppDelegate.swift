//
//  AppDelegate.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/2/8.
//

import UIKit
import Bugly

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        signal(SIGPIPE, SIG_IGN)
        return true
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        
    }
}

