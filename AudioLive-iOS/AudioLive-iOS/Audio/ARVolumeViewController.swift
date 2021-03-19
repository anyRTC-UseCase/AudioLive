//
//  ARVolumeViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/3/11.
//

import UIKit
import ARtcKit

class ARVolumeViewController: UIViewController,UIGestureRecognizerDelegate{
    
    @IBOutlet weak var effectSwitch: UISwitch!
    @IBOutlet weak var musicSlider: UISlider!
    @IBOutlet weak var voicesSlider: UISlider!
    @IBOutlet weak var earSlider: UISlider!
    
    public var isOn: Bool = false
    
    let tap = UITapGestureRecognizer()

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        tap.addTarget(self, action: #selector(didClickCloseButton))
        tap.delegate = self
        self.view.addGestureRecognizer(tap)
        effectSwitch.isOn = isOn
    }
    
    @IBAction func didClickVolumeSlider(_ sender: UISlider) {
        if sender.tag == 50 {
            //音乐声
            rtcKit.adjustAudioMixingVolume(Int(sender.value * 100))
        } else if (sender.tag == 51) {
            //人声
            rtcKit.adjustRecordingSignalVolume(Int(sender.value * 100))
        } else if (sender.tag == 52) {
            //耳返
            rtcKit.setInEarMonitoringVolume(Int(sender.value * 100))
        }
    }
    
    @IBAction func switchValueChanged(_ sender: UISwitch) {
        
        NotificationCenter.default.post(name: UIResponder.audioLiveNotificationEffect, object: self, userInfo: ["isOn": NSNumber.init(value: sender.isOn)])
    }
    
    @IBAction func didClickCloseButton(_ sender: Any) {
        saveVolume()
        self.dismiss(animated: true, completion: nil)
    }
    
    func saveVolume() {
        //保存设置

    }
    
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        if(touch.view == self.view) {
            saveVolume()
            self.dismiss(animated: true, completion: nil)
            return true
        } else {
            return false
        }
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
}
