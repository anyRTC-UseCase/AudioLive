//
//  ARModifyViewController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/2/24.
//

import UIKit

class ARModifyViewController: UIViewController {

    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var numLabel: UILabel!
    
    var rightButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        // Do any additional setup after loading the view.
        initializeUI()
    }
    
    func initializeUI() {
        self.navigationItem.leftBarButtonItem = createBarButtonItem(title: "设置昵称")
        
        rightButton = UIButton.init(type: .custom)
        rightButton.setTitle("保存", for: .normal)
        rightButton.titleLabel?.font = UIFont(name: "PingFang SC", size: 18)
        rightButton.setTitleColor(UIColor(hexString: "#40A3FB"), for: .normal)
        rightButton.addTarget(self, action: #selector(saveNickname), for: .touchUpInside)
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(customView: rightButton)
        
        let clearButton: UIButton = nameTextField.value(forKey: "_clearButton") as! UIButton
        clearButton.setImage(UIImage(named: "icon_clear"), for: .normal)
        
        nameTextField.text = UserDefaults.string(forKey: .userName)
        nameTextField.becomeFirstResponder()
        nameTextField.addTarget(self, action: #selector(textFieldValueChange), for: .editingChanged)
        nameTextField.placeHolderColor = UIColor(hexString: "#7A7A82")
        numLabel.text = "\(16 - nameTextField.text!.count)"
    }
    
    @objc func textFieldValueChange() {
        let nickName = nameTextField.text
        if nickName?.count ?? 0 > 16 {
            nameTextField.text = String((nickName?.prefix(16))!)
        }
        numLabel.text = "\(16 - Int(nameTextField.text?.count ?? 0))"
        (nickName?.count ?? 0 > 0) ? (rightButton.alpha = 1.0) : (rightButton.alpha = 0.5)
        rightButton.isEnabled = nickName?.count ?? 0 > 0
    }
    
    @objc func saveNickname() {
        let nickName = nameTextField.text
        if Int(nickName?.count ?? 0) > 0 {
            if nickName != UserDefaults.string(forKey: .userName) {
                UserDefaults.set(value: nickName! , forKey: .userName)
                //修改昵称
                let parameters : NSDictionary = [ "userName": nickName as Any]
                ARNetWorkHepler.getResponseData("updateUserName", parameters: parameters as? [String : AnyObject], headers: true, success: { [self] (result) in
                    NotificationCenter.default.post(name: UIResponder.audioLiveNotificationModifySucess, object: self, userInfo: nil)
                    XHToast.showCenter(withText: "保存成功", duration: 3)
                    popBack()
                }) { (error) in
                    print(error)
                }
            } else {
                popBack()
            }
        } else {
            UIAlertController.showAlert(in: self, withTitle: "提示", message: "昵称不能为空", cancelButtonTitle: "", destructiveButtonTitle: nil, otherButtonTitles: ["确定"]) { (alertVc, action, index) in
                print("\(index)")
            }
        }
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }
}
