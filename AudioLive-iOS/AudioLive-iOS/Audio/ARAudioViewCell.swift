//
//  ARAudioViewCell.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/3/9.
//

import UIKit
import ARtcKit
import ARtmKit

class ARAudioCollectionViewCell: UICollectionViewCell {
    
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var identityLabel: UILabel!
    @IBOutlet weak var delayLabel: UILabel!
    @IBOutlet weak var lossRateLabel: UILabel!
    @IBOutlet weak var progressView: UIProgressView!
    
    var micModel: ARAudioRoomMicModel? {
        didSet {
            nameLabel.text = micModel?.uid
            delayLabel.text = "延迟：\(micModel?.networkTransportDelay ?? 0)ms"
            lossRateLabel.text = "丢包率：\(micModel?.audioLossRate ?? NSInteger(0.00))%"
            progressView.setProgress(Float(micModel?.volume ?? 0)/255.0, animated: true)
            if micModel?.identity == .broadcaster  {
                identityLabel.text = "【主播】"
            } else if micModel?.identity == .audience {
                identityLabel.text = "【观众】"
            } else if micModel?.identity == .owner {
                identityLabel.text = "【自己】"
            }
        }
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        let layerView = UIView()
        layerView.frame = CGRect(x: 0, y: 0, width: (ARScreenWidth - 14)/2, height: 199)
        layerView.layer.cornerRadius = 6
        //添加背景色
        let backlayer = CAGradientLayer()
        backlayer.colors = [UIColor(hexString: "#2C3440").cgColor, UIColor(hexString: "#0D2236").cgColor]
        backlayer.locations = [0.00, 0.99]
        backlayer.frame = layerView.bounds
        layerView.layer.addSublayer(backlayer)
        self.contentView.insertSubview(layerView, at: 0)
    }
}

class ARLogCollectionViewCell: UICollectionViewCell {
    @IBOutlet weak var timeLabel: UILabel!
    @IBOutlet weak var stateLabel: UILabel!
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    var playerModel: ARMediaPlayModel? {
        didSet {
            timeLabel.text = playerModel?.time
            switch playerModel?.playerState {
            case .opening:
                stateLabel.text = "正在打开媒体文件"
                break
            case .openCompleted:
                stateLabel.text = "打开媒体文件成功"
                break
            case .playing:
                stateLabel.text = "正在播放媒体文件"
                break
            case .stopped:
                stateLabel.text = "媒体文件停止播放"
                break
            default:
                stateLabel.text = "默认状态"
                break
            }
        }
    }
    
}

class ARChatSoundCell: UICollectionViewCell {
    
    @IBOutlet weak var soundLabel: UILabel!
    
    func updateSoundCell(soundName: String!) {
        soundLabel.text = soundName
    }
}

class ARMusicCell: UITableViewCell {
    
    @IBOutlet weak var markLabel: UILabel!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var playButton: UIButton!
    @IBOutlet weak var stopButton: UIButton!
    @IBOutlet weak var animationImageView: UIImageView!
    
    var onButtonTapped : (() -> Void)? = nil
    
    let list: NSMutableArray! = NSMutableArray()
    var musicModel: ARMusicModel?
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        for i in 0...1 {
             let animationImage: UIImage! = UIImage(named: String(format: "icon_volume%d", i))
             list.add(animationImage as Any)
         }
    }
    
    func startAnimation() {
        animationImageView.animationImages = list as? [UIImage]
        animationImageView.animationDuration = 0.5
        animationImageView.animationRepeatCount = 0
        animationImageView.startAnimating()
    }
    
    func updateMusicModel(model: ARMusicModel, localModel:ARMusicModel) {
        musicModel = model
        nameLabel.text = model.musicName
        if model.musicId == localModel.musicId {
            if localModel.status == .playing {
                playButton.isSelected = true
                stopButton.isHidden = false
                markLabel.isHidden = false
                animationImageView.isHidden = false
                startAnimation()
            } else if localModel.status == .pause {
                playButton.isSelected = false
                stopButton.isHidden = false
                markLabel.isHidden = false
                
            } else if localModel.status == .normal {
                playButton.isSelected = false
                stopButton.isHidden = true
                markLabel.isHidden = true
                animationImageView.isHidden = true
            }
        } else {
            playButton.isSelected = false
            stopButton.isHidden = true
            markLabel.isHidden = true
            animationImageView.isHidden = true
            model.status = .normal
        }
    }
    
    @IBAction func didClickControlButton(_ sender: UIButton) {
        if let onButtonTapped = self.onButtonTapped {
            if sender.tag == 50 {
                //结束
                musicModel?.status = .normal
                rtcKit.stopAudioMixing()
            } else {
                if !sender.isSelected {
                    if musicModel?.status == .normal {
                        rtcKit.stopAudioMixing()
                        rtcKit.startAudioMixing((musicModel?.musicUrl)!, loopback: false, replace: false, cycle: -1)
                        onButtonTapped()
                    } else {
                        rtcKit.resumeAudioMixing()
                    }
                    musicModel?.status = .playing
                } else {
                    rtcKit.pauseAudioMixing()
                    musicModel?.status = .pause
                }
            }
            onButtonTapped()
        }
    }
}

class ARMicCell: UITableViewCell {
    @IBOutlet weak var headImageView: UIImageView!
    @IBOutlet weak var rejuctButton: UIButton!
    @IBOutlet weak var acceptButton: UIButton!
    @IBOutlet weak var nameLabel: UILabel!
    
    var onButtonTapped : ((_ index: NSInteger) -> Void)? = nil
    
    var userModel: ARUserModel? {
        didSet {
            headImageView.sd_setImage(with: NSURL(string: userModel?.avatar ?? "") as URL?, placeholderImage: UIImage(named: "icon_head"))
            nameLabel.text = userModel?.userName
        }
    }
    
    
    @IBAction func didClickControlButton(_ sender: UIButton) {
        if let onButtonTapped = self.onButtonTapped {
            var cmd: String?
            (sender.tag == 50) ? (cmd = "acceptLine") : (cmd = "rejectLine")
            let dic: NSDictionary! = ["cmd": cmd as Any]
            let message: ARtmMessage = ARtmMessage.init(text: getJSONStringFromDictionary(dictionary: dic))
            rtmEngine.send(message, toPeer: (userModel?.uid)!, sendMessageOptions: ARtmSendMessageOptions()) { (errorCode) in
                
            }
            onButtonTapped(sender.tag)
        }
    }
}
