//
//  ARMainViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/2/22.
//

import UIKit
import MJRefresh
import SwiftyJSON
import SDWebImage

private let reuseIdentifier = "AudioLive_CellID"

class RefreshGifHeader: MJRefreshHeader {
    var rotatingImage: UIImageView?
    
    override var state: MJRefreshState {
        didSet {
            switch state {
            case .idle,.pulling:
                rotatingImage?.stopAnimating()
                break
            case .refreshing:
                rotatingImage?.startAnimating()
                break
            default:
                print("")
            }
        }
    }
    
    override func prepare() {
        super.prepare()
        rotatingImage = UIImageView.init()
        rotatingImage?.image = UIImage(named: "icon_refresh")
        self.addSubview(rotatingImage!)
        
        let rotationAnim = CABasicAnimation(keyPath: "transform.rotation.z")
        rotationAnim.fromValue = 0
        rotationAnim.toValue = Double.pi * 2
        rotationAnim.repeatCount = MAXFLOAT
        rotationAnim.duration = 1
        rotationAnim.isRemovedOnCompletion = false
        rotatingImage!.layer.add(rotationAnim, forKey: "rotationAnimation")
    }
    
    override func placeSubviews() {
        super.placeSubviews()
        rotatingImage?.frame = CGRect.init(x: 0, y: 0, width: 40, height: 40)
        rotatingImage?.center = CGPoint(x: self.mj_w / 2, y: self.mj_h / 2)
    }
}

class ARMainViewController: UICollectionViewController {
    
    var modelArr = [ARAudioRoomListModel]()
    
    private var flowLayout: UICollectionViewFlowLayout!

    let identifier = "AudioLive_CreateRoom"
    var index = 0
    lazy var footerView: MJRefreshAutoGifFooter = {
        let footer = MJRefreshAutoGifFooter(refreshingBlock: {
              [weak self] () -> Void in
              self?.footerRefresh()
        })
        return footer
    }()
    
    lazy var placeholderView: ARPlaceholderView = {
        let placeholderView = ARPlaceholderView()
        return ARPlaceholderView()
    }()
    
    lazy var createButton: UIButton = {
        let button: UIButton = UIButton.init(type: .custom)
        button.frame = CGRect.init(x: ARScreenWidth - 55, y: ARScreenHeight - (self.tabBarController?.tabBar.frame.size.height)! - 94, width: 55, height: 55)
        button.addTarget(self, action: #selector(createAudioRoom), for: .touchUpInside)
        button.setBackgroundImage(UIImage(named: "icon_add"), for: .normal)
        return button
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        (UserDefaults.string(forKey: .uid) != nil) ? login() : registered()
        createPlaceholder()
        self.navigationController?.navigationBar.setBackgroundImage(UIImage.imageWithColor(UIColor(hexString: "#121F2B")), for: .default)
        
        flowLayout = UICollectionViewFlowLayout.init()
        flowLayout.sectionInset = UIEdgeInsets(top: 7, left: 7, bottom: 0, right: 7)
        flowLayout?.scrollDirection = .vertical
        flowLayout?.minimumLineSpacing = 7
        flowLayout?.minimumInteritemSpacing = 7
        let width = (ARScreenWidth - 21)/2
        flowLayout?.itemSize = CGSize.init(width: width, height: width * 1.05)
        collectionView.collectionViewLayout = flowLayout
        
        collectionView.mj_header = RefreshGifHeader(refreshingBlock: {
              [weak self] () -> Void in
              self?.headerRefresh()
          })
        self.view.insertSubview(createButton, at: 99)
        NotificationCenter.default.addObserver(self, selector: #selector(headerRefresh), name: UIResponder.audioLiveNotificationLoginSucess, object: nil)
    }
    
    @objc func headerRefresh() {
        index = 1
        requestRoomList()
    }
    
    @objc func footerRefresh() {
        index += 1
        requestRoomList()
    }
    
    @objc func createAudioRoom() {
        let storyboard = UIStoryboard.init(name: "Main", bundle: nil)
        guard let createRoomVc = storyboard.instantiateViewController(withIdentifier: identifier) as? ARCreateRoomViewController else {return}
        createRoomVc.hidesBottomBarWhenPushed = true
        self.navigationController?.pushViewController(createRoomVc, animated: true)
    }
    
    @objc func createPlaceholder() {
        placeholderView.showPlaceholderView(self.view, placeholderImageName: "icon_placeholder", placeholderTitle: "暂时没有房间 \n请点击下方“+”创建房间") {
            self.requestRoomList()
        }
        self.view.insertSubview(createButton, at: 99)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.hidesBottomBarWhenPushed = false
        collectionView.mj_header?.beginRefreshing()
    }
    
    //请求房间列表
    func requestRoomList() {
        if UserDefaults.string(forKey: .isLogin) == "true" {
            customLoadingView(text: "", count: 5)
            let parameters : NSDictionary = ["pageSize": 10, "pageNum": index]
            ARNetWorkHepler.getResponseData("getRoomList", parameters: parameters as? [String : AnyObject], headers: true, success: { [self] (result) in
                if result["code"] == 0 {
                    (index == 1) ? modelArr.removeAll() : nil
                    let jsonArr = result["data"]["list"].arrayValue
                    for json in jsonArr {
                        self.modelArr.append(ARAudioRoomListModel(jsonData: json))
                    }
                    (result["data"]["haveNext"] == 1) ? (collectionView.mj_footer = footerView) : (collectionView.mj_footer = nil)
                    placeholderView.removeFromSuperview()
                } else if result["code"] == 1054 && (index == 1) {
                    self.modelArr.removeAll()
                    createPlaceholder()
                }
                collectionView.reloadData()
                collectionView.mj_header?.endRefreshing()
                collectionView.mj_footer?.endRefreshing()
                removeLoadingViewDelay(text: "")
            }) { (error) in
                self.collectionView.mj_header?.endRefreshing()
                self.collectionView.mj_footer?.endRefreshing()
            }
        } else {
            login()
        }
    }
    
    //加入房间
    func requestJoinRoom(roomId: String) {
        let parameters: NSDictionary = ["roomId": roomId, "cType": 2, "pkg": Bundle.main.infoDictionary!["CFBundleIdentifier"] as Any]
        ARNetWorkHepler.getResponseData("joinRoom", parameters: parameters as? [String : AnyObject], headers: true) { [weak self](result) in
            if result["code"] == 0 {
                var infoModel = ARRoomInfoModel(jsonData: result["data"]["room"])
                infoModel.roomId = roomId
                if infoModel.ower?.uid == UserDefaults.string(forKey: .uid) {
                    infoModel.isBroadcaster = true
                }
                infoModel.pullRtmpUrl = result["data"]["pullRtmpUrl"].stringValue
                infoModel.pushUrl = result["data"]["pushUrl"].stringValue
                
                let storyboard = UIStoryboard.init(name: "Main", bundle: nil)
                guard let audioRoomVc = storyboard.instantiateViewController(withIdentifier: "AudioLive_Room") as? ARAudioViewController else {return}
                audioRoomVc.hidesBottomBarWhenPushed = true
                audioRoomVc.infoModel = infoModel
                self?.navigationController?.pushViewController(audioRoomVc, animated: true)
            }
        } error: { (error) in
        
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    // MARK: UICollectionViewDataSource

    override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of items
        return modelArr.count
    }

    override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let collectionViewCell: ARMainViewCell! = (collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier, for: indexPath) as! ARMainViewCell)
        collectionViewCell.listModel = modelArr[indexPath.row]
        // Configure the cell
        return collectionViewCell
    }

    // MARK: UICollectionViewDelegate
    
    override func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let listModel: ARAudioRoomListModel = modelArr[indexPath.row]
        requestJoinRoom(roomId: listModel.roomId!)
    }
}

extension ARMainViewController: UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let width = (ARScreenWidth - 21)/2
        return CGSize.init(width: width, height: width * 1.05)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets.init(top: 7, left: 7, bottom: 0, right: 7)
    }
}

class ARMainViewCell: UICollectionViewCell {
    @IBOutlet weak var backImageView: UIImageView!
    @IBOutlet weak var roomNameLabel: UILabel!
    @IBOutlet weak var onlineLabel: UILabel!
    
    var listModel: ARAudioRoomListModel? {
        didSet {
            backImageView.sd_setImage(with: NSURL(string: listModel?.imageUrl ?? "") as URL?, placeholderImage: UIImage(named: "icon_main"))
            roomNameLabel.text = listModel?.roomName
            onlineLabel.text = String(format: "%d人在看", listModel?.userNum ?? 0)
        }
    }
}
