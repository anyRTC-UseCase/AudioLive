//
//  ARMusicViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/3/10.
//

import UIKit
import ARtmKit

class ARMusicViewController: UITableViewController {
    var musicArr = [ARMusicModel]()
    var audioVc: ARAudioViewController?
    public var musicStatusBlock: (()->())?
    
    fileprivate lazy var placeholderView: ARPlaceholderView = {
        let placeholderView = ARPlaceholderView()
        return ARPlaceholderView()
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        // Do any additional setup after loading the view.
        let leftButton: UIButton = UIButton.init(type: .custom)
        leftButton.frame = CGRect.init(x: 0, y: 0, width: 100, height: 17)
        leftButton.setTitle("音乐列表", for: .normal)
        leftButton.titleLabel?.font = UIFont(name: "PingFang SC", size: 17)
        leftButton.setTitleColor(RGBA(r: 96, g: 96, b: 96, a: 1), for: .normal)
        leftButton.titleEdgeInsets = UIEdgeInsets.init(top: 0, left: 10, bottom: 0, right: 0);
        leftButton.setImage(UIImage(named: "icon_return"), for: .normal)
        leftButton.addTarget(self, action: #selector(popBack), for: .touchUpInside)
        self.navigationItem.leftBarButtonItem = UIBarButtonItem.init(customView: leftButton)
        tableView.tableFooterView = UIView()
        tableView.backgroundColor = UIColor(hexString: "#F5F5F5")
        createPlaceholder()
        getMusicList()
    }
    
    func getMusicList() {
        ARNetWorkHepler.getResponseData("getMusicList", parameters: nil, headers: true) { [weak self](result) in
            if result["code"] == 0 {
                let jsonArr = result["data"].arrayValue
                for json in jsonArr {
                    self?.musicArr.append(ARMusicModel(jsonData: json))
                }
                self?.tableView.reloadData()
                self?.placeholderView.removeFromSuperview()
            }
        } error: { (error) in
            
        }
    }
    
    func updateMusicState(musicModel: ARMusicModel?) {
        audioVc?.musicModel = musicModel ?? ARMusicModel(jsonData: [:])
        tableView.reloadData()
        if musicStatusBlock != nil {
            musicStatusBlock!()
        }
        
        var cmd = "normal"
        var state = 0
        if musicModel?.status != .normal {
            (musicModel?.status == .playing) ? (cmd = "playing") : (cmd = "pause")
            (musicModel?.status == .playing) ? (state = 1) : (state = 2)
        }
        let dic: NSDictionary! = ["cmd": cmd as Any, "musicName": musicModel?.musicName  as Any]
        let rtmMessage: ARtmMessage = ARtmMessage.init(text: getJSONStringFromDictionary(dictionary: dic))
        let options: ARtmSendMessageOptions = ARtmSendMessageOptions()
        //发送频道消息
        audioVc?.rtmChannel?.send(rtmMessage, sendMessageOptions: options) { (errorCode) in
            print("Send Channel Message")
        }
        
        //音乐播放状态(0:没有音乐播放;1:播放;2:暂停)
        let parameters : NSDictionary = ["musicId": musicModel?.musicId as Any, "musicState": state, "roomId": audioVc?.infoModel?.roomId as Any]
        ARNetWorkHepler.getResponseData("addMusic", parameters: parameters as? [String : AnyObject], headers: true, success: { (result) in
            print("updateMusicState \(result.rawValue)")
        }) { (error) in
            
        }
    }
    
    @objc func createPlaceholder() {
        placeholderView.showPlaceholderView(self.view, placeholderImageName: "icon_error", placeholderTitle: "网络出错啦...") {
            self.getMusicList()
        }
        placeholderView.backgroundColor = UIColor(hexString: "#F5F5F5")
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.navigationBar.setBackgroundImage(UIImage.imageWithColor(UIColor.white), for: .default)
        UIApplication.shared.setStatusBarStyle(.default, animated: true)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        self.navigationController?.navigationBar.setBackgroundImage(UIImage.imageWithColor(UIColor.black), for: .default)
        UIApplication.shared.setStatusBarStyle(.lightContent, animated: true)
    }

    // MARK: - Table view data source

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return musicArr.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let musicCell: ARMusicCell = tableView.dequeueReusableCell(withIdentifier:"ARMusicCellID", for: indexPath) as! ARMusicCell
        let model = musicArr[indexPath.row]
        musicCell.updateMusicModel(model: model, localModel: audioVc!.musicModel)
        musicCell.onButtonTapped = { [weak self] in
            self?.updateMusicState(musicModel: model)
        }
        musicCell.selectionStyle = .none
        return musicCell
    }
}
