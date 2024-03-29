//
//  ARStatisticsInfo.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/3/2.
//

import UIKit
import SwiftyJSON
import ARtcKit

enum ARMicStatus {
    case normal
    case cancle
    case exist
}

struct AREffectMenuItem {
    var name: String?
    var color: String?
    var identify: String?
}

struct ARAudioRoomListModel {
    var roomPwd: String?
    var ownerUid: String?
    var userNum: NSInteger = 0
    //房间自增长id
    var id: NSInteger = 0
    var roomId: String?
    //是否私密房间(1:私密房间;2:非私密房间)
    var isPrivate: NSInteger = 2
    var imageUrl: String?
    var roomName: String?
    
    init(jsonData: JSON) {
        roomPwd = jsonData["roomPwd"].stringValue
        ownerUid = jsonData["ownerUid"].stringValue
        userNum = jsonData["userNum"].intValue
        id = jsonData["id"].intValue
        roomId = jsonData["roomId"].stringValue
        isPrivate = jsonData["isPrivate"].intValue
        imageUrl = jsonData["imageUrl"].stringValue
        roomName = jsonData["roomName"].stringValue
    }
}

struct ARUserModel {
    var uid: String?
    var userName: String?
    var avatar: String?
    
    init(jsonData: JSON) {
        uid = jsonData["uid"].stringValue
        userName = jsonData["userName"].stringValue
        avatar = jsonData["avatar"].stringValue
    }
}

class ARMusicModel: NSObject {
    enum ARMusicStatus {
        case normal
        case playing
        case pause
    }
    
    var musicId: NSInteger
    var musicName: String?
    var singer: String?
    var musicUrl: String?
    var status: ARMusicStatus = .normal
    
    init(jsonData: JSON) {
        musicId = jsonData["musicId"].intValue
        musicName = jsonData["musicName"].stringValue
        singer = jsonData["singer"].stringValue
        musicUrl = jsonData["musicUrl"].stringValue
    }
}

struct ARRoomInfoModel {
    var isBroadcaster: Bool = false {
        didSet {
            if isBroadcaster {
                ower?.uid = UserDefaults.string(forKey: .uid)
                ower?.userName = UserDefaults.string(forKey: .userName)
            }
        }
    }
    var roomId: String?
    var roomName: String?
    //1:RTC实时互动;2.客户端推流到CDN;3.服务端推流到CDN
    var rType: NSInteger?
    //rtmp地址
    var pullRtmpUrl: String?
    //FLV地址
    var pullFlvUrl: String?
    
    //推流地址
    var pushUrl: String?
    var rtcToken: String?
    var rtmToken: String?
    var musicState: NSInteger?
    
    var ower: ARUserModel?
    var music: ARMusicModel?
    
    init(jsonData: JSON) {
        roomName = jsonData["roomName"].stringValue
        roomId = jsonData["roomId"].stringValue
        rType = jsonData["rType"].intValue
        rtcToken = jsonData["rtcToken"].stringValue
        rtmToken = jsonData["rtmToken"].stringValue
        ower = ARUserModel.init(jsonData: jsonData["ower"])
        musicState = jsonData["musicState"].intValue
        music = ARMusicModel.init(jsonData: jsonData["music"])
        pushUrl = jsonData["pushUrl"].stringValue
    }
}

enum AudioIdentity: NSInteger {
    case broadcaster
    case audience
    case owner
}

class ARAudioRoomMicModel: NSObject {
    var uid: String?
    var identity: AudioIdentity?
    var audioLossRate: NSInteger?
    var networkTransportDelay: NSInteger?
    var volume: UInt?
    
    init(uid: String?) {
        self.uid = uid
    }
}

struct ARMediaPlayModel {
    var time: String?
    var playerState: ARMediaPlayerState?
}

extension NSObject {
    
    func registered() {
        //注册
        customLoadingView(text: "连接中...", count: 5)
        let parameters : NSDictionary = ["sex": 0, "userName": randomCharacter(length: 6)]
        ARNetWorkHepler.getResponseData("signUp", parameters: parameters as? [String : AnyObject], headers: false, success: { [weak self] (result) in
            let uid: String = result["data"]["uid"].stringValue
            UserDefaults.set(value: uid , forKey: .uid)
            self?.login()
        }) { (error) in
            print(error)
        }
    }
    
    func login() {
        //登录
        //Bundle.main.infoDictionary!["CFBundleIdentifier"]
        if UserDefaults.string(forKey: .uid)?.count ?? 0 > 0 {
            customLoadingView(text: "连接中...", count: 5)
            UserDefaults.set(value: "false" , forKey: .isLogin)
            let parameters : NSDictionary = ["cType": 2, "uid": UserDefaults.string(forKey: .uid) as Any, "pkg": Bundle.main.infoDictionary!["CFBundleIdentifier"] as Any]
            ARNetWorkHepler.getResponseData("signIn", parameters: parameters as? [String : AnyObject], headers: false, success: { [weak self](result) in
                UserDefaults.set(value: result["data"]["avatar"].stringValue , forKey: .avatar)
                UserDefaults.set(value: result["data"]["userName"].stringValue , forKey: .userName)
                UserDefaults.set(value: result["data"]["userToken"].stringValue , forKey: .userToken)
                UserDefaults.set(value: result["data"]["appid"].stringValue , forKey: .appid)
                UserDefaults.set(value: "true" , forKey: .isLogin)
                NotificationCenter.default.post(name: UIResponder.audioLiveNotificationLoginSucess, object: self, userInfo: nil)
                self?.removeLoadingView()
            }) { (error) in
                print(error)
            }
        } else {
            registered()
        }
    }
    
    func leaveRoom(roomId: String) {
        //离开房间
        let parameters : NSDictionary = ["roomId": roomId as Any]
        ARNetWorkHepler.getResponseData("updateV2UserLeaveTs", parameters: parameters as? [String : AnyObject], headers: true, success: { (result) in
            
        }) { (error) in
            print(error)
        }
    }
    
    func deleteRoom(roomId: String) {
        //删除房间
        let parameters : NSDictionary = ["roomId": roomId as Any]
        ARNetWorkHepler.getResponseData("deleteRoom", parameters: parameters as? [String : AnyObject], headers: true, success: { (result) in
            
        }) { (error) in
            print(error)
        }
    }
}
