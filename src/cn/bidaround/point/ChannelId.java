package cn.bidaround.point;
/**
 * 分享的频道ID
 * @author youtui
 * @since 14/6/19 2015/1/12修改
 */
public enum ChannelId {
	
	/**
	 * 渠道名字命名规则： youtui_sdk.xml的平台名称大写 + "_" + 后台提供的渠道ID
	 */
	SINAWEIBO_0,		// 新浪微博
	TENCENTWEIBO_1,		// 腾讯微博
	QZONE_2,			// qq空间
	WECHAT_3,			// 微信
	RENREN_4,			// 人人网
	QQ_5,				// qq
	QRCODE_6,			// 二维码,暂未使用
	SHORTMESSAGE_7,		// 短信
	EMAIL_8,			// 邮件
	COPYLINK_9,			// 复制链接,暂未使用
	WECHATMOMENTS_10,	// 微信朋友圈
	YIXIN_11,			// 易信
	YIXINFRIENDS_12,	// 易信朋友圈
	KAIXIN_13,			// 开心网
	MORE_100,			// 更多分享
}

