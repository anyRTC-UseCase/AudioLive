//
//  LogViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/3/9.
//

import UIKit

struct ARLogModel {
    enum ARLogStatus {
        //普通消息
        case normal
        case music
        case join
        case exit
    }
    var userName: String?
    var uid: String?
    var text: String?
    var status: ARLogStatus? = .normal
}

class LogCell: UITableViewCell {
    @IBOutlet weak var contentLabel: UILabel!
    @IBOutlet weak var colorView: UIView!

    override func awakeFromNib() {
        super.awakeFromNib()
        colorView.layer.cornerRadius = 12.25
    }
    
    func update(logModel: ARLogModel) {
        var userName = logModel.userName
        (userName == nil) ? userName = "" : nil
        if logModel.status == .normal {
            colorView.backgroundColor = UIColor.init(hue: 0.0, saturation: 0.0, brightness: 0.0, alpha: 0.3)
            let text = String(format: "%@ %@", userName!, logModel.text!)
            
            contentLabel.attributedText = changeFontColor(totalString: text, subString: logModel.userName!, font: UIFont.systemFont(ofSize: 14), textColor: UIColor.init(hexString: "#7BE3FD"))
        } else if logModel.status == .join {
            contentLabel.text = String(format: "%@ 加入了房间", userName!)
        } else if logModel.status == .exit {
            contentLabel.text = String(format: "%@ 离开了房间", userName!)
        }
    }
}

class LogViewController: UITableViewController {

    private lazy var list = [ARLogModel]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 44
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return list.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "LogCell", for: indexPath) as! LogCell
        cell.update(logModel: list[indexPath.row])
        return cell
    }
}

extension LogViewController {
    func log(logModel: ARLogModel) {
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.25) {
            self.list.append(logModel)
            let index = IndexPath(row: self.list.count - 1, section: 0)
            self.tableView.insertRows(at: [index], with: .automatic)
            self.tableView.scrollToRow(at: index, at: .middle, animated: false)
        }
    }
}
