##  说明

1. 如果你还没有注册开发者账号，请先注册[开发者账号](https://console.anyrtc.io/signin)
2. 注册开发者账号后，请在创建一个应用
3. 本项目最低支持 Android 4.4（API 19）CPU架构支持armeabi-v7a，arm64-v8a
4. 请使用真机运行该项目



##  项目目录

```
audioLive-Android  //项目名称

	|_ activity							//活动目录
		|_	AboutActivity.java			//关于活动
        	ChatActivity.java			//语音连麦活动
        	CreateChatActivity.java		//主播创建房间活动
        	MainActivity.java			//主活动
        	SettingNameActivity.java	//设置名称活动
        	
   	|_ adapter							//适配器目录
   		|_	InfoPagerAdapter.java		//viewPager适配器
        	LogAdapter.java				//日志适配器
        	MainAdapter.java			//FragmentPager适配器
        	MessageAdapter.java			//消息设配器
        	RoomListAdapter.java		//房间列表设配器
        	
    |_ bean								//bean目录
   		|_	AddBean.java				//创建房间接收json字符串类
        	InfoBean.java				//展示上麦人员的详情信息类
        	JoinRoomBean.java			//加入房间接收json字符串类
        	JoinUserBean.java			//获取房间人员接收json字符串类
        	LogBean.java				//日志信息类
        	MessageListBean.java		//消息列表类
        	MusicBean.java				//获取音乐接收json字符串类
        	RoomBean.java				//房间信息类
        	RoomListBean.java			//获取房间列表接收json字符串类
        	SignInBean.java				//登录接收json字符串类
        	SignUpBean.java				//注册接收json字符串类
        	
	|_ dialog							//对话框类
   		|_	ApplyDialog.java			//申请上麦对话框
        	AutoTipDialog.java			//自动提示对话框
        	CommentDialogFragment.java	//发送消息输入对话框
        	LoadingDialog.java			//登录加载对话框
        	LogDialog.java				//日志对话框
        	VolumeDialog				//声音设置对话框
        	
	|_ fragment							//fragment类
   		|_	InfoFragment.java			//我的界面
        	MainFragment.java			//主界面
        	
	|_ manager							//管理类和接口目录
   		|_	ARServerManager.java		//服务器获取接口数据管理类
            ChatRoomEventListener.java 	//语音房间一些接口回调
            ChatRoomManager.java 		//语音放假管理类
            MessageManager.java 		//消息管理类
            RtcManager.java 			//Rtc管理类
            RtmManager.java 			//Rtm管理类
            
	|_ model							//模型目录
   		|_	ChannelData.java			//频道人员数据类型
            Member.java 				//成员信息类
            Message.java 				//消息类
      
    |_ util								//工具类
   		|_	Constans.java				//字符串工具类
            Sputil.java 				//存储和接收的工具类
            
	|_ view								//自定义视图
   		|_	TabView.java				//主界面和我的界面自定义按钮
	
	|_ ARApplication.java 				//AR应用类
            
        	
```



## 联系我们

- 如需阅读完整的文档和 API 注释，你可以访问[anyRTC开发者中心](https://docs.anyrtc.io/)。
- 如果在集成中遇到问题，你可以到[anyRTC开发者社区](https://bbs.anyrtc.io/)提问。
- 如果有售前咨询或售后技术问题，你可以拨打 021-65650071，或加入官方Q群 580477436 提问。
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/anyRTC-UseCase/AudioLive/issues)

  

## 代码许可

The MIT License (MIT).