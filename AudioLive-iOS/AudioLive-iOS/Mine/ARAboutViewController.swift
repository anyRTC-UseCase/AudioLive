//
//  ARAboutViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/2/24.
//

import UIKit
import ARtcKit

private let reuseIdentifier = "AudioLive_Statement"

class ARAboutViewController: UITableViewController {
    
    var officialButton: UIButton!
    var menus: [MenuItem] = [
        MenuItem(name: "隐私条例", icon: "icon_lock"),
        MenuItem(name: "免责声明", icon: "icon_log"),
        MenuItem(name: "注册anyRTC账号", icon: "icon_register"),
        MenuItem(name: "发版时间", icon: "icon_time", detail: "2021.03.18"),
        MenuItem(name: "SDK版本", icon: "icon_sdkversion", detail: String(format: "V %@", ARtcEngineKit.getSdkVersion())),
        MenuItem(name: "软件版本", icon: "icon_appversion", detail: String(format: "V %@", Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! CVarArg))
    ]

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false
        tableView.isScrollEnabled = false
        tableView.tableFooterView = UIView()
        self.view.backgroundColor = RGBA(r: 10, g: 22, b: 33, a: 1)
        self.navigationItem.leftBarButtonItem = createBarButtonItem(title: "关于")
        
        officialButton = UIButton(type: .custom)
        officialButton.setTitle("www.anyrtc.io", for: .normal)
        officialButton.setTitleColor(UIColor(hexString: "#8F8F9A"), for: .normal)
        officialButton.addTarget(self, action: #selector(jumpWebsite), for: .touchUpInside)
        officialButton.titleLabel?.font = UIFont(name: "PingFang SC", size: 12)
        
        officialButton.frame = CGRect.init(x: (ARScreenWidth - 200)/2, y: ARScreenHeight - 56 - (isFullScreen() ? 88 : 44), width: 200, height: 12)
        self.view.addSubview(officialButton)
        tableView.separatorColor = UIColor(hexString: "#313437")
    }
    
    @objc func jumpWebsite() {
        UIApplication.shared.open(NSURL(string: "https://www.anyrtc.io")! as URL, options: [:], completionHandler: nil)
    }

    // MARK: - Table view data source

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return menus.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        var cell: UITableViewCell? = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier")
        // Configure the cell...
        if cell == nil {
            cell = UITableViewCell.init(style: .value1, reuseIdentifier: "reuseIdentifier")
        }
        cell?.backgroundColor = UIColor.clear
        cell?.selectionStyle = .none
        cell?.accessoryType = .disclosureIndicator
        cell?.textLabel?.text = menus[indexPath.row].name
        cell?.textLabel?.textColor = UIColor.white
        cell?.textLabel?.font = UIFont(name: "PingFang SC", size: 14)
        cell?.imageView?.image = UIImage(named: menus[indexPath.row].icon)
        cell?.detailTextLabel?.text = menus[indexPath.row].detail
        cell?.detailTextLabel?.font = UIFont(name: "PingFang SC", size: 14)
        cell?.detailTextLabel?.textColor = UIColor(hexString: "#CCCCCC")
        
        let checkmark  = UIImageView(frame:CGRect(x:0, y:0, width: 20, height:20))
        checkmark.image = UIImage(named: "icon_arrow.png")
        cell?.accessoryView = checkmark
        return cell!
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        switch indexPath.row {
        case 0:
            UIApplication.shared.open(NSURL(string: "https://anyrtc.io/termsOfService")! as URL, options: [:], completionHandler: nil)
            break
        case 1:
            let storyboard = UIStoryboard.init(name: "Main", bundle: nil)
            let statementVc = storyboard.instantiateViewController(withIdentifier: reuseIdentifier)
            statementVc.hidesBottomBarWhenPushed = true
            self.navigationController?.pushViewController(statementVc, animated: true)
            break
        case 2:
            UIApplication.shared.open(NSURL(string: "https://console.anyrtc.io/signup")! as URL, options: [:], completionHandler: nil)
            break
        default:
            break
        }
    }
}
