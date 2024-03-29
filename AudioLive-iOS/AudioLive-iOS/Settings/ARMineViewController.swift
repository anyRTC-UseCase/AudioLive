//
//  ARMineViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/2/24.
//

import UIKit

private let reuseIdentifier = "AudioLive_Modify"

struct MenuItem {
    var name: String
    var icon: String
    var detail: String?
}

class ARMineViewController: UITableViewController {
    lazy var headView: UIView = {
        let height = ARScreenHeight * 0.232
        let view = UIView(frame: CGRect(x: 0, y: 0, width: ARScreenWidth, height: height))
        let backImageView = UIImageView(frame: view.bounds)
        backImageView.image = UIImage(named: "icon_background")
        view.addSubview(backImageView)

        let headImageView = UIImageView(frame: CGRect(x: 0, y: 0, width: height/2, height: height/2))
        headImageView.center = view.center
        headImageView.layer.cornerRadius = height/4
        headImageView.layer.masksToBounds = true
        headImageView.sd_setImage(with: NSURL(string: UserDefaults.string(forKey: .avatar) ?? "") as URL?, placeholderImage: UIImage(named: "icon_head"))
        view.addSubview(headImageView)
        return view
    }()

    var menus: [MenuItem] = [MenuItem(name: "设置昵称", icon: "icon_nickname"), MenuItem(name: "关于", icon: "icon_about")]

    override func viewDidLoad() {
        super.viewDidLoad()

        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
        tableView.tableFooterView = UIView()
        tableView.tableHeaderView = headView
        tableView.tableHeaderView?.height = ARScreenHeight * 0.232
        if #available(iOS 11.0, *) {
            tableView.contentInsetAdjustmentBehavior = .never
        } else {
            // Fallback on earlier versions
            automaticallyAdjustsScrollViewInsets = false
        }

        navigationController?.navigationBar.setBackgroundImage(UIImage.imageWithColor(UIColor(hexString: "#121F2B")), for: .default)
        view.backgroundColor = UIColor(hexString: "#0A1621")
        tableView.separatorColor = UIColor(hexString: "#313437")

        NotificationCenter.default.addObserver(self, selector: #selector(refreshUserInfo), name: UIResponder.audioLiveNotificationModifySucess, object: nil)
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: animated)
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigationController?.setNavigationBarHidden(false, animated: animated)
    }

    @objc func refreshUserInfo() {
        tableView.reloadData()
    }

    // MARK: - Table view data source

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return menus.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier")
        if cell == nil {
            cell = UITableViewCell(style: .value1, reuseIdentifier: "reuseIdentifier")
        }
        // Configure the cell...
        cell?.backgroundColor = UIColor.clear
        cell?.selectionStyle = .none
        cell?.accessoryType = .disclosureIndicator
        cell?.textLabel?.text = menus[indexPath.row].name
        cell?.textLabel?.textColor = UIColor.white
        cell?.textLabel?.font = UIFont(name: "PingFang SC", size: 14)
        cell?.imageView?.image = UIImage(named: menus[indexPath.row].icon)
        cell?.detailTextLabel?.textColor = UIColor(hexString: "#CCCCCC")
        if indexPath.row == 0 {
            cell?.detailTextLabel?.text = UserDefaults.string(forKey: .userName)
        }

        let checkmark = UIImageView(frame: CGRect(x: 0, y: 0, width: 20, height: 20))
        checkmark.image = UIImage(named: "icon_arrow.png")
        cell?.accessoryView = checkmark
        return cell!
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.row == 0 {
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
            guard let modifyVc = storyboard.instantiateViewController(withIdentifier: reuseIdentifier) as? ARModifyViewController else { return }
            modifyVc.hidesBottomBarWhenPushed = true
            navigationController?.pushViewController(modifyVc, animated: true)
        } else {
            let aboutVc = ARAboutViewController()
            aboutVc.hidesBottomBarWhenPushed = true
            navigationController?.pushViewController(aboutVc, animated: true)
        }
    }
}
