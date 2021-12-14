//
//  ARMicViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/3/11.
//

import ARtmKit
import UIKit

class ARMicViewController: UIViewController, UIGestureRecognizerDelegate {
    @IBOutlet var micButton: UIButton!
    @IBOutlet var tableView: UITableView!
    @IBOutlet var noMicLabel: UILabel!
    let tap = UITapGestureRecognizer()
    var audioVc: ARAudioViewController?

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        tap.addTarget(self, action: #selector(didClickCloseButton))
        tap.delegate = self
        view.addGestureRecognizer(tap)
        tableView.tableFooterView = UIView()
        tableView.separatorStyle = .none
        noMicLabel.isHidden = (audioVc?.micArr.count != 0)
        NotificationCenter.default.addObserver(self, selector: #selector(refreshMicList), name: UIResponder.audioLiveNotificationRefreshMicList, object: nil)
    }
    
    @IBAction func didClickMicButton(_ sender: Any) {
        for userModel: ARUserModel in audioVc!.micArr {
            let dic: NSDictionary! = ["cmd": "acceptLine" as Any]
            let message = ARtmMessage(text: getJSONStringFromDictionary(dictionary: dic))
            rtmEngine.send(message, toPeer: userModel.uid!, sendMessageOptions: ARtmSendMessageOptions()) { _ in
            }
            audioVc?.micArr.removeAll()
            tableView.reloadData()
            noMicLabel.isHidden = false
            audioVc?.listButton.setTitle("0", for: .normal)
        }
    }
    
    @IBAction func didClickCloseButton(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
    
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        if touch.view == view {
            dismiss(animated: true, completion: nil)
            return true
        } else {
            return false
        }
    }
    
    @objc func refreshMicList() {
        noMicLabel.isHidden = (audioVc?.micArr.count != 0)
        tableView.reloadData()
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
}

extension ARMicViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: ARMicCell = tableView.dequeueReusableCell(withIdentifier: "ARMicCellID") as! ARMicCell
        cell.selectionStyle = .none
        cell.userModel = audioVc?.micArr[indexPath.row]
        cell.onButtonTapped = { [weak self] _ in
            self?.audioVc?.micArr.remove(at: indexPath.row)
            self?.tableView.reloadData()
            self?.noMicLabel.isHidden = (self?.audioVc?.micArr.count == 0)
            self?.audioVc?.listButton.setTitle("\(self?.audioVc?.micArr.count ?? 0)", for: .normal)
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if audioVc?.micArr.count ?? 0 > 0 {
            micButton.alpha = 1.0
            noMicLabel.isHidden = true
        } else {
            micButton.alpha = 0.6
            noMicLabel.isHidden = false
        }
        
        return audioVc?.micArr.count ?? 0
    }
}
