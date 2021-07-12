//
//  ARAudioViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/2/22.
//

import UIKit
import ARtcKit
import ARtmKit

private let spacing: CGFloat = 7

var rtcKit: ARtcEngineKit!
var rtmEngine: ARtmKit!

class ARAudioViewController: ARBaseViewController {
    
    @IBOutlet weak var audioCollectionView: UICollectionView!
    @IBOutlet weak var effectCollectionView: UICollectionView!
    @IBOutlet weak var pageControl: UIPageControl!
    
    @IBOutlet weak var bottomStackView: UIStackView!
    @IBOutlet weak var chatButton: UIButton!
    @IBOutlet weak var micButton: UIButton!
    @IBOutlet weak var listButton: UIButton!
    @IBOutlet weak var musicButton: UIButton!
    @IBOutlet weak var musicLabel: UILabel!
    @IBOutlet weak var audioButton: UIButton!
    @IBOutlet weak var effectButton: UIButton!
    //音效
    @IBOutlet weak var soundConstraint: NSLayoutConstraint!
    
    @IBOutlet weak var bottomConstraint: NSLayoutConstraint!
    weak var logVC: LogViewController?
    
    private var flowLayout: UICollectionViewFlowLayout!
    var rtmChannel: ARtmChannel?
    
    private var micStatus: ARMicStatus = .normal
    
    fileprivate var streamKit: ARStreamingKit?
    fileprivate var mediaPlayer: ARMediaPlayer?
    
    var infoModel: ARRoomInfoModel?
    private var listArr = [ARAudioRoomMicModel]()
    private var logArr = [ARMediaPlayModel]()
    public var micArr = [ARUserModel]()
    
    private lazy var localMicModel = {
        return ARAudioRoomMicModel(uid: UserDefaults.string(forKey: .uid))
    }()
    
    public lazy var musicModel: ARMusicModel = {
        return ARMusicModel(jsonData: [:])
    }()
    
    private lazy var effectItem: [AREffectMenuItem] = {
        return [
            AREffectMenuItem(name: "哈哈哈", color: "#CE5850", identify: "chipmunk"),
            AREffectMenuItem(name: "起哄", color: "#F29025", identify: "qihong"),
            AREffectMenuItem(name: "鼓掌", color: "#8252F6", identify: "guzhang"),
            AREffectMenuItem(name: "尴尬", color: "#6AB71C", identify: "awkward"),
            AREffectMenuItem(name: "乌鸦", color: "#03A3C3", identify: "wuya"),
            AREffectMenuItem(name: "哎呀我滴妈", color: "#106CBF", identify: "wodema")
        ]
    }()
    
    private lazy var animations: CABasicAnimation = {
        let animation = CABasicAnimation.init(keyPath: "transform.rotation.z")
        animation.duration = 2.0
        animation.fromValue = 0.0
        animation.toValue = Double.pi * 2
        animation.repeatCount = MAXFLOAT
        animation.isRemovedOnCompletion = false
        return animation
    }()

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        initializeEngine()
        initializeUI()
        NotificationCenter.default.addObserver(self, selector: #selector(soundEffect), name: UIResponder.audioLiveNotificationEffect, object: nil)
    }
    
    func initializeUI() {
        self.navigationItem.leftBarButtonItem = createBarButtonItem(title: "\(infoModel?.roomName ?? "")（\(infoModel?.roomId ?? "")）")
        UIApplication.shared.isIdleTimerDisabled = true
        isFullScreen() ? bottomConstraint.constant = 34 : nil
        
        flowLayout = UICollectionViewFlowLayout.init()
        flowLayout.sectionInset = UIEdgeInsets(top: spacing, left: 0, bottom: -spacing, right: 0)
        flowLayout?.scrollDirection = .vertical
        flowLayout?.minimumLineSpacing = spacing
        flowLayout?.minimumInteritemSpacing = spacing
        flowLayout?.itemSize = CGSize.init(width: (ARScreenWidth - 2 * spacing)/2, height: 213)
        audioCollectionView.collectionViewLayout = flowLayout
        
        chatButton.contentHorizontalAlignment = .left
        chatTextField.placeholder = "聊点什么吧"
        chatTextField.delegate = self
        chatTextField.addTarget(self, action: #selector(chatTextFieldLimit), for: .editingChanged)
        confirmButton.addTarget(self, action: #selector(didSendChatTextField), for: .touchUpInside)
        soundConstraint.constant = 0.0
        
        if infoModel?.musicState == 1 {
            musicButton.layer.add(animations, forKey: "CABasicAnimation")
        }
        musicLabel.text = infoModel?.music?.musicName
        
        if infoModel!.isBroadcaster {
            // broadcaster
            updateCollectionViewDirection(isLog: false)
            bottomStackView.insertArrangedSubview(audioButton, at: 0)
            joinChannel()
        } else {
            // audience
            listButton.isHidden = true
            micButton.isHidden = false
            effectButton.isHidden = true
            audioButton.isHidden = true
            if infoModel?.rType != 1 {
                initializeMediaPlayer()
            } else {
                updateCollectionViewDirection(isLog: false)
                joinChannel()
            }
        }
    }
    
    func initializeEngine() {
        // init ARtcEngineKit
        rtcKit = ARtcEngineKit.sharedEngine(withAppId: UserDefaults.string(forKey: .appid)!, delegate: self)
        rtcKit.setChannelProfile(.liveBroadcasting)
        if infoModel!.isBroadcaster {
            rtcKit.setClientRole(.broadcaster)
        }
        rtcKit.enableAudioVolumeIndication(500, smooth: 3, report_vad: true)
        
        //init ARtmKit
        rtmEngine = ARtmKit.init(appId: UserDefaults.string(forKey: .appid)!, delegate: self)
        rtmEngine.login(byToken: infoModel?.rtmToken, user: UserDefaults.string(forKey: .uid) ?? "0") { [weak self](errorCode) in
            guard let weakself = self else {return}
            weakself.rtmChannel = rtmEngine.createChannel(withId: (weakself.infoModel?.roomId)!, delegate: self)
            weakself.rtmChannel?.join(completion: { (errorCode) in
                let dic: NSDictionary! = ["cmd": "join", "userName": UserDefaults.string(forKey: .userName) as Any]
                weakself.sendChannelMessage(text: weakself.getJSONStringFromDictionary(dictionary: dic))
            })
            
            if !weakself.infoModel!.isBroadcaster {
                rtmEngine.subscribePeersOnlineStatus([weakself.infoModel!.ower!.uid!]) { (errorCode) in
                    print("subscribePeersOnlineStatus \(errorCode.rawValue)")
                }
            }
        }
    }
    
    //------------ RTC实时互动 ------------------
    func joinChannel() {
        // 加入频道
        let uid = UserDefaults.string(forKey: .uid)
        rtcKit.joinChannel(byToken: infoModel?.rtcToken, channelId: (infoModel?.roomId)!, uid: uid) { [weak self](channel, uid, elapsed) in
            guard let weakself = self else {return}
            if weakself.infoModel!.isBroadcaster {
                weakself.localMicModel.identity = .owner
                weakself.listArr.append(self!.localMicModel)
                
                if weakself.infoModel?.rType == 2 {
                    weakself.initializeStreamingKit()
                } else if self!.infoModel?.rType == 3 {
                    weakself.initializeAddPublishStreamUrl()
                }
            }
            self?.updateCollection()
        }
    }
    
    func leaveChannel() {
        //离开频道
        rtcKit.leaveChannel { (stats) in
            print("leaveChannel")
        }
    }
    
    //------------ 客户端推流到CDN ------------------
    func initializeStreamingKit() {
        streamKit = ARStreamingKit()
        streamKit?.setRtcEngine(rtcKit)
        //开始推rtmp流
        streamKit?.setLiveTranscoding(ARLiveTranscoding.default())
        streamKit?.pushStream(infoModel?.pushUrl ?? "")
    }
    
    //------------ 服务端推流到CDN ------------------
    func initializeAddPublishStreamUrl() {
        //增加旁路推流地址
        let transCoding = ARLiveTranscoding.default()
        let transCodingUser = ARLiveTranscodingUser()
        transCodingUser.uid = UserDefaults.string(forKey:.uid)!
        transCoding.transcodingUsers = [transCodingUser]
        rtcKit.setLiveTranscoding(transCoding)
        
        rtcKit.addPublishStreamUrl(infoModel?.pushUrl ?? "", transcodingEnabled: true)
    }
    
    //------------ 播放器 -- 游客 ------------------
    func initializeMediaPlayer() {
        mediaPlayer = ARMediaPlayer(delegate: self)
        mediaPlayer?.open((infoModel?.pullRtmpUrl)!, startPos: 0)
        mediaPlayer?.play()
    }
    
    @IBAction func didClickAudioButton(_ sender: UIButton) {
        switch sender.tag {
        case 50:
            //音乐
            if infoModel!.isBroadcaster {
                let storyboard = UIStoryboard.init(name: "Main", bundle: nil)
                guard let musicVc = storyboard.instantiateViewController(withIdentifier: "AudioLive_Music") as? ARMusicViewController else {return}
                musicVc.audioVc = self
                musicVc.musicStatusBlock = { [weak self] in
                    if self?.musicModel.status == .playing {
                        self?.musicLabel.text = self?.musicModel.musicName
                        self?.musicButton.layer.add(self!.animations, forKey: "CABasicAnimation")
                    } else {
                        self?.musicModel.status == .normal ? (self?.musicLabel.text = "") : nil
                        self?.musicButton.layer.removeAnimation(forKey: "CABasicAnimation")
                    }
                }
                self.navigationController?.pushViewController(musicVc, animated: true)
            } else {
                XHToast.showCenter(withText: "只有主播才可以")
            }
            break
        case 51:
            //聊天
            chatTextField.becomeFirstResponder()
            break
        case 53:
            if micStatus != .exist {
                //上麦
                sender.isSelected = !sender.isSelected
                var dic: NSDictionary!
                if sender.isSelected {
                    dic = ["cmd": "apply", "userName": UserDefaults.string(forKey: .userName) as Any, "avatar": UserDefaults.string(forKey: .avatar) as Any]
                    micStatus = .cancle
                } else {
                    dic = ["cmd": "cancelApply"]
                    micStatus = .normal
                }
                
                let message: ARtmMessage = ARtmMessage.init(text: getJSONStringFromDictionary(dictionary: dic))
                rtmEngine.send(message, toPeer: (infoModel?.ower?.uid)!, sendMessageOptions: ARtmSendMessageOptions()) { (errorCode) in
                    print("errorCode:\(errorCode.rawValue)")
                }
            } else {
                //下麦
                micStatus = .normal
                audioButton.isHidden = true
                sender.setImage(UIImage(named: "icon_mic_open"), for: .normal)
                
                if infoModel?.rType == 1 {
                    rtcKit.setClientRole(.audience)
                    for (index, model) in listArr.enumerated() {
                        if localMicModel == model {
                            listArr.remove(at: index)
                        }
                    }
                    audioCollectionView.reloadData()
                } else {
                    listArr.removeAll()
                    updateCollectionViewDirection(isLog: true)
                    leaveChannel()
                    initializeMediaPlayer()
                }
                updateCollection()
            }
            break
        case 55:
            //音频开关
            sender.isSelected = !sender.isSelected
            rtcKit.enableLocalAudio(!sender.isSelected)
            break
        default:
            break
        }
    }
    
    func updateCollection() {
        if listArr.count != 0 &&  flowLayout.scrollDirection == .horizontal {
            pageControl.isHidden = false
        } else {
            pageControl.isHidden = true
        }
        pageControl.numberOfPages = Int(ceil(Double(listArr.count)/2.0))
        audioCollectionView.reloadData()
    }
    
    func updateCollectionViewDirection(isLog: Bool) {
        if isLog {
            flowLayout.scrollDirection = .vertical
            flowLayout.itemSize = CGSize.init(width: ARScreenWidth - 2 * spacing, height: 40)
            audioCollectionView.isPagingEnabled = false
            flowLayout.sectionInset = UIEdgeInsets(top: spacing, left: 0, bottom: 0, right: 0)
        } else {
            flowLayout.scrollDirection = .horizontal
            audioCollectionView.isPagingEnabled = true
            flowLayout.itemSize = CGSize.init(width: (ARScreenWidth - 2 * spacing)/2, height: 213)
            flowLayout.sectionInset = UIEdgeInsets(top: spacing, left: 0, bottom: -spacing, right: 0)
        }
        audioCollectionView.reloadData()
    }
    
    override func popBack() {
        UIAlertController.showAlert(in: self, withTitle: "退出房间", message: "当前正在互动是否退出", cancelButtonTitle: "取消", destructiveButtonTitle: nil, otherButtonTitles: ["确定"]) { [unowned self] (alertVc, action, index) in
            if index == 2 {
                let dic: NSDictionary! = ["cmd": "exit", "userName": UserDefaults.string(forKey: .userName) as Any]
                sendChannelMessage(text: getJSONStringFromDictionary(dictionary: dic))
                
                destroyRoom()
                self.navigationController?.popToRootViewController(animated: true)
            }
        }
    }
    
    @objc func soundEffect(nofi: Notification) {
        let result: Bool = nofi.userInfo!["isOn"] as! Bool
        result ? (soundConstraint.constant = 40.0) : (soundConstraint.constant = 0)
    }
    
    func destroyRoom() {
        let topVc = topViewController()
        if !(topVc is ARAudioViewController) {
            topVc.dismiss(animated: false, completion: nil)
        }
        
        if infoModel!.isBroadcaster {
            if infoModel?.rType == 2 {
                streamKit?.destroy()
            }
            deleteRoom(roomId: (infoModel?.roomId)!)
        } else {
            if mediaPlayer != nil {
                mediaPlayer?.destroy()
            }
            leaveRoom(roomId: (infoModel?.roomId)!)
            rtmEngine.unsubscribePeersOnlineStatus([infoModel!.ower!.uid!], completion: nil)
        }
        
        leaveChannel()
        ARtcEngineKit.destroy()
        rtmEngine.destroyChannel(withId: (infoModel?.roomId)!)
        rtmEngine.logout(completion: nil)
    }
    
    @objc func chatTextFieldLimit() {
        if isBlank(text: chatTextField.text) {
            confirmButton.alpha = 0.3
        } else {
            confirmButton.alpha = 1.0
        }
    }
    
    @objc func didSendChatTextField() {
        let text = chatTextField.text
        if text?.count ?? 0 > 0 {
            let dic: NSDictionary! = ["cmd": "msg", "content": chatTextField.text as Any, "userName": UserDefaults.string(forKey: .userName) as Any]
            sendChannelMessage(text: getJSONStringFromDictionary(dictionary: dic))
            self.logVC?.log(logModel: ARLogModel(userName: UserDefaults.string(forKey: .userName), uid: UserDefaults.string(forKey: .uid), text: text))
            chatTextField.resignFirstResponder()
            chatTextField.text = ""
        }
    }
    
    @objc func sendChannelMessage(text: String) {
        //发送频道消息
        let rtmMessage: ARtmMessage = ARtmMessage.init(text: text)
        let options: ARtmSendMessageOptions = ARtmSendMessageOptions()
        rtmChannel?.send(rtmMessage, sendMessageOptions: options) { (errorCode) in
            print("Send Channel Message")
        }
    }
    
    @objc func tokenExpire() {
        
        UIAlertController.showAlert(in: self, withTitle: "提示", message: "体验时间已到", cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: ["确定"]) { [unowned self](alertVc, action, index) in
            destroyRoom()
            self.navigationController?.popToRootViewController(animated: true)
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        guard let identifier = segue.identifier else {
            return
        }
        
        if identifier == "EmbedLogViewController",
            let vc = segue.destination as? LogViewController {
            self.logVC = vc
        } else if identifier == "ARVolumeViewController" {
            let vc: ARVolumeViewController = (segue.destination as? ARVolumeViewController)!
            vc.isOn = (soundConstraint.constant != 0)
        } else if identifier == "ARMicViewController" {
            let vc: ARMicViewController = (segue.destination as? ARMicViewController)!
            vc.audioVc = self
        }
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        print("deinit")
    }
}

//MARK: - ARtcEngineDelegate

extension ARAudioViewController: ARtcEngineDelegate {
    func rtcEngine(_ engine: ARtcEngineKit, didOccurWarning warningCode: ARWarningCode) {
        //发生警告回调
        print(warningCode.rawValue)
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, didOccurError errorCode: ARErrorCode) {
        //发生错误回调
        print(errorCode.rawValue)
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, tokenPrivilegeWillExpire token: String) {
        //Token 过期回调
        if infoModel!.isBroadcaster {
            let dic: NSDictionary! = ["cmd": "tokenPastDue"]
            sendChannelMessage(text: getJSONStringFromDictionary(dictionary: dic))
        }
        tokenExpire()
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, didJoinedOfUid uid: String, elapsed: Int) {
        //远端用户/主播加入回调
        let micModel = ARAudioRoomMicModel(uid: uid)
        var identity: AudioIdentity?
        (uid == infoModel?.ower?.uid) ? (identity = .broadcaster) : (identity = .audience)
        micModel.identity = identity
        listArr.append(micModel)
        updateCollection()
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, didOfflineOfUid uid: String, reason: ARUserOfflineReason) {
        //远端用户（通信场景）/主播（互动场景）离开当前频道回调
        for (index,micModel) in listArr.enumerated() {
            if micModel.uid == uid {
                self.listArr.remove(at: index)
                break
            }
        }
        updateCollection()
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [ARtcAudioVolumeInfo], totalVolume: Int) {
        //提示频道内谁正在说话、说话者音量及本地用户是否在说话的回调
        for speakInfo in speakers {
            for micModel in listArr {
                if speakInfo.uid == "0" {
                    localMicModel.volume = UInt(speakInfo.volume)
                } else if micModel.uid == speakInfo.uid {
                    micModel.volume = UInt(speakInfo.volume)
                }
            }
        }
        audioCollectionView.reloadData()
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, reportRtcStats stats: ARChannelStats) {
        //当前通话统计回调
        localMicModel.networkTransportDelay = NSInteger(stats.lastmileDelay);
        localMicModel.audioLossRate = NSInteger(stats.txPacketLossRate);
        audioCollectionView.reloadData()
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, remoteAudioStats stats: ARtcRemoteAudioStats) {
        //通话中远端音频流传输的统计信息回调
        for micModel in listArr {
            if micModel.uid == stats.uid {
                micModel.networkTransportDelay = NSInteger(stats.networkTransportDelay);
                micModel.audioLossRate = NSInteger(stats.audioLossRate);
                audioCollectionView.reloadData()
                break
            }
        }
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, rtmpStreamingChangedToState url: String, state: ARtmpStreamingState, errorCode: ARtmpStreamingErrorCode) {
        //RTMP 推流状态发生改变回调
        print("rtmpStreamingChangedToState state: \(state.rawValue) errorCode:\(errorCode.rawValue)")
    }
    
    func rtcEngine(_ engine: ARtcEngineKit, connectionChangedTo state: ARConnectionStateType, reason: ARConnectionChangedReason) {
        print("rtcEngine connectionChangedTo: \(state.rawValue)   \(reason.rawValue)")
        if state == .disconnected {
            customLoadingView(text: "连接中...", count: Float(Int.max))
            if musicModel.status == .playing {
                rtcKit.pauseAudioMixing()
            }
        } else if state == .connected {
            removeLoadingView()
            if musicModel.status == .playing {
                rtcKit.stopAudioMixing()
                rtcKit.startAudioMixing(musicModel.musicUrl ?? "", loopback: false, replace: false, cycle: -1)
            }
        }
    }
}

//MARK: - ARMediaPlayerDelegate

extension ARAudioViewController: ARMediaPlayerDelegate {
    func rtcMediaPlayer(_ playerKit: ARMediaPlayer, didChangedTo state: ARMediaPlayerState, error: ARMediaPlayerError) {
        //报告播放器的状态
        print("rtcMediaPlayer \(state.rawValue)  \(error.rawValue)")
        logArr.append(ARMediaPlayModel(time: getLocalDateTime(), playerState: state))
        updateCollectionViewDirection(isLog: true)
        audioCollectionView.reloadData()
        audioCollectionView.scrollToItem(at: NSIndexPath(item: self.logArr.count - 1, section: 0) as IndexPath, at: .bottom, animated: true)
        
        if state == .stopped && micStatus != .exist {
            mediaPlayer?.destroy()
            mediaPlayer = nil
            initializeMediaPlayer()
        }
    }
}

//MARK: - ARtmDelegate,ARtmChannelDelegate

extension ARAudioViewController: ARtmDelegate,ARtmChannelDelegate {
    
    func rtmKit(_ kit: ARtmKit, connectionStateChanged state: ARtmConnectionState, reason: ARtmConnectionChangeReason) {
        print("ARtmKit connectionChangedTo: \(state.rawValue)   \(reason.rawValue)")
        if state == .reconnecting {
            customLoadingView(text: "连接中...", count: Float(Int.max))
        } else if state == .connected {
            removeLoadingView()
        }
    }
    
    func rtmKit(_ kit: ARtmKit, messageReceived message: ARtmMessage, fromPeer peerId: String) {
        //收到点对点消息回调
        let dic = getDictionaryFromJSONString(jsonString: message.text)
        let value: String? = dic.object(forKey: "cmd") as? String
        if value == "apply" {
            micArr.append(ARUserModel(jsonData: ["userName": dic.object(forKey: "userName") as Any, "uid": peerId, "avatar": dic.object(forKey: "avatar") as Any]))
            listButton.setTitle("\(micArr.count)", for: .normal)
        } else if value == "rejectLine" {
            XHToast.showCenter(withText: "主播拒绝了你的上麦请求", duration: 2)
            micButton.isSelected = false
            micStatus = .normal
        } else if value == "acceptLine" {
            XHToast.showCenter(withText: "主播同意了你的上麦请求", duration: 2)
            audioButton.isSelected = false
            rtcKit.enableLocalAudio(true)
            
            micButton.isSelected = false
            micButton.setImage(UIImage(named: "icon_mic_close"), for: .normal)
            micStatus = .exist
            if infoModel?.rType != 1 {
                mediaPlayer?.destroy()
                mediaPlayer = nil
                updateCollectionViewDirection(isLog: false)
                joinChannel()
            }
            
            audioButton.isHidden = false
            rtcKit.setClientRole(.broadcaster)
            localMicModel.identity = .owner
            listArr.insert(localMicModel, at: 0)
            audioCollectionView.reloadData()
        } else if value == "cancelApply" {
            for (index, model) in micArr.enumerated() {
                if model.uid == peerId {
                    micArr.remove(at: index)
                    break
                }
            }
            listButton.setTitle("\(micArr.count)", for: .normal)
        }
        micButton.setTitle("\(micArr.count)", for: .normal)
        if value == "cancelApply" || value == "apply" && topViewController() is ARMicViewController && infoModel!.isBroadcaster {
            NotificationCenter.default.post(name: UIResponder.audioLiveNotificationRefreshMicList, object: self, userInfo:nil)
        }
    }
    
    func rtmKit(_ kit: ARtmKit, peersOnlineStatusChanged onlineStatus: [ARtmPeerOnlineStatus]) {
        //被订阅用户在线状态改变回调
        for status: ARtmPeerOnlineStatus in onlineStatus {
            if status.peerId == infoModel?.ower?.uid && status.state == .offline {
                XHToast.showCenter(withText: "主播已离开房间", duration: 2)
                break
            }
        }
    }
    
    func channel(_ channel: ARtmChannel, messageReceived message: ARtmMessage, from member: ARtmMember) {
        //收到频道消息回调
        let dic = getDictionaryFromJSONString(jsonString: message.text)
        let value: String? = dic.object(forKey: "cmd") as? String
        if value == "playing" {
            musicButton.layer.add(animations, forKey: "CABasicAnimation")
            musicLabel.text = dic.object(forKey: "musicName") as? String
        } else if value == "pause" {
            musicButton.layer.removeAnimation(forKey: "CABasicAnimation")
            musicLabel.text = dic.object(forKey: "musicName") as? String
        } else if value == "normal" {
            musicButton.layer.removeAnimation(forKey: "CABasicAnimation")
            musicLabel.text = ""
        } else if value == "join" {
            logVC?.log(logModel: ARLogModel(userName: dic.object(forKey: "userName") as? String, uid: member.uid, status: .join))
        } else if value == "exit" {
            logVC?.log(logModel: ARLogModel(userName: dic.object(forKey: "userName") as? String, uid: member.uid, status: .exit))
            if member.uid == infoModel!.ower?.uid {
                UIAlertController.showAlert(in: self, withTitle: "提示", message: "主播已离开，房间不存在", cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: ["确定"]) { [unowned self](alertVc, action, index) in
                    destroyRoom()
                    self.navigationController?.popViewController(animated: true)
                }
            }
        } else if value == "msg" {
            logVC?.log(logModel: ARLogModel(userName: dic.object(forKey: "userName") as? String, uid: member.uid, text:  dic.object(forKey: "content") as? String))
        } else if value == "tokenPastDue" {
            tokenExpire()
        }
    }
}

extension ARAudioViewController: UICollectionViewDelegate, UICollectionViewDataSource,UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        if collectionView == audioCollectionView {
            return (flowLayout.scrollDirection == .horizontal) ? self.listArr.count : self.logArr.count
        }
        return effectItem.count;
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        if collectionView == effectCollectionView {
            //音效
            let collectionViewCell: ARChatSoundCell! = (collectionView.dequeueReusableCell(withReuseIdentifier: "ARChat_SoundCellID", for: indexPath) as! ARChatSoundCell)
            collectionViewCell.updateSoundCell(soundName: effectItem[indexPath.row].name)
            collectionViewCell.backgroundColor = UIColor.init(hexString:effectItem[indexPath.row].color!)
            return collectionViewCell
        } else {
            if flowLayout.scrollDirection == .horizontal {
                //上麦
                let cell: ARAudioCollectionViewCell! = collectionView.dequeueReusableCell(withReuseIdentifier: "AudioLive_AudioCellID", for: indexPath) as? ARAudioCollectionViewCell
                cell.micModel = listArr[indexPath.row]
                return cell
            } else {
                //mediaPlayer日志 -- 游客
                let cell: ARLogCollectionViewCell! = collectionView.dequeueReusableCell(withReuseIdentifier: "AudioLive_LogCellID", for: indexPath) as? ARLogCollectionViewCell
                cell.playerModel = logArr[indexPath.row]
                return cell
            }
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        if collectionView == effectCollectionView {
            let str = effectItem[indexPath.row].name
            let dic = [NSAttributedString.Key.font: UIFont(name: "PingFang SC", size: 14)]
            let size = CGSize(width: CGFloat(MAXFLOAT), height: 40)
            let width = str!.boundingRect(with: size, options: .usesLineFragmentOrigin, attributes: dic as [NSAttributedString.Key : Any], context: nil).size.width
            return CGSize(width: width + 50, height: 40)
        } else {
            if flowLayout.scrollDirection == .horizontal {
                return CGSize.init(width: (ARScreenWidth - 2 * spacing)/2, height: 213)
            } else {
                return CGSize.init(width: ARScreenWidth - 2 * spacing, height: 40)
            }
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if collectionView == effectCollectionView {
            let filePath: String = Bundle.main.path(forResource: effectItem[indexPath.row].identify, ofType:"wav")!
            rtcKit.stopAllEffects()
            rtcKit.playEffect(666, filePath: filePath, loopCount: 0, pitch: 1.0, pan: 0, gain: 100, publish: true)
        }
    }
}

extension ARAudioViewController: UIScrollViewDelegate {
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        if scrollView != effectCollectionView {
            pageControl?.currentPage = Int(ceil(scrollView.contentOffset.x/scrollView.frame.width))
        }
    }
}

extension ARAudioViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField.text?.count ?? 0 > 0 {
            textField.resignFirstResponder()
            didSendChatTextField()
        }
        return true
    }
}


