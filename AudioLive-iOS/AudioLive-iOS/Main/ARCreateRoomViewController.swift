//
//  ARCreateRoomViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/3/1.
//

import UIKit
import SwiftyJSON

class ARCreateRoomViewController: UIViewController {

    @IBOutlet weak var roomNameTextField: UITextField!
    @IBOutlet weak var createRoomButton: UIButton!
    let identifier = "AudioLive_Room"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        // Do any additional setup after loading the view.
        self.navigationItem.leftBarButtonItem = createBarButtonItem(title: "创建语音房间")
        roomNameTextField.addTarget(self, action: #selector(limitRoomName), for: .editingChanged)
    }
    
    @objc func limitRoomName() {
        let roomName = roomNameTextField.text
        if roomName?.count ?? 0 > 9 {
            roomNameTextField.text = String((roomName?.prefix(9))!)
        }
        
        (roomName?.count ?? 0 > 0) ? (createRoomButton.alpha = 1.0) : (createRoomButton.alpha = 0.5)
        createRoomButton.isEnabled = roomName?.count ?? 0 > 0
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }

    @IBAction func createAudioRoom(_ sender: Any) {
        view.endEditing(true)
        //创建房间
        let roomName = roomNameTextField.text
        if Int(roomName?.count ?? 0) > 0 {
            UIAlertController.showActionSheet(in: self, withTitle: "方案选择", message: nil, cancelButtonTitle: "取消", destructiveButtonTitle: nil, otherButtonTitles: ["RTC 实时互动","客户端推流到CDN","服务端推流到CDN"], popoverPresentationControllerBlock: nil) { [weak self](alertVc, action, index) in
                self!.requestAudioRoom(name: roomName, type: index - 1)
            }
        }
    }
    
    func requestAudioRoom(name: String!, type: NSInteger) {
        //rType 1:RTC实时互动;2.客户端推流到CDN;3.服务端推流到CDN
        let parameters : NSDictionary = ["cType": 2, "pkg": Bundle.main.infoDictionary!["CFBundleIdentifier"] as Any, "rType": type, "roomName": name as Any]
        ARNetWorkHepler.getResponseData("addRoom", parameters: parameters as? [String : AnyObject], headers: true, success: { [weak self] (result) in
            if result["code"] == 0 {
                let jsonData = JSON(result["data"])
                var model = ARRoomInfoModel.init(jsonData: jsonData)
                model.rType = type
                model.roomName = name
                model.isBroadcaster = true
                
                let storyboard = UIStoryboard.init(name: "Main", bundle: nil)
                guard let audioRoomVc = storyboard.instantiateViewController(withIdentifier: self!.identifier) as? ARAudioViewController else {return}
                audioRoomVc.infoModel = model
                self?.navigationController?.pushViewController(audioRoomVc, animated: true)
            } else {
                print(result)
            }
        }) { (error) in
            print(error)
        }
    }
}
