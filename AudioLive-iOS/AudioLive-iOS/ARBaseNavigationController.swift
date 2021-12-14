//
//  ARBaseNavigationController.swift
//  AudioLive-iOS
//
//  Created by 余生丶 on 2021/02/22.
//

import UIKit

class ARBaseNavigationController: UINavigationController {
    
    var navigationBarColor: UIColor? {
        didSet {
            if #available(iOS 15.0, *) {
                let appearance = UINavigationBarAppearance()
                appearance.configureWithOpaqueBackground()
                appearance.backgroundColor = navigationBarColor
                
                var textAttributes: [NSAttributedString.Key: AnyObject] = [:]
                textAttributes[.foregroundColor] = UIColor.white
                appearance.titleTextAttributes = textAttributes
                
                navigationBar.standardAppearance = appearance
                navigationBar.scrollEdgeAppearance = navigationBar.standardAppearance
            } else {
                // Fallback on earlier versions
            }
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        self.navigationBarColor = UIColor(hexString: "#121F2B")
    }
    
    override var childForStatusBarStyle: UIViewController? {
        return topViewController
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
