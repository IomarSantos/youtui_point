package cn.bidaround.point;

/**
 * 友推sdk所需要的部分常量
 * @author youtui
 * @since 14/3/25
 */
public class YtConstants {
	
	/**友推网址*/
	public static final String YT_URL = "http://youtui.mobi";
//	public static final String YT_URL = "http://192.168.4.131";
	/**文件保存路径*/
	public static final String FILE_SAVE_PATH = "/youtui/";
	/**链接跳转网页*/
	public static final String YOUTUI_LINK_URL = "http://youtui.mobi/link/";
//	public static final String YOUTUI_LINK_URL = "http://192.168.4.131/link/";
	
	/** 赠送积分接口 */
	public static final String GIVE_POINT = YT_URL + "/activity/givePoint";
	
	/** 分享获取积分 */
	public static final String SHARE_POINT = YT_URL + "/activity/sharePoint";
	
	/** 获取用户总积分 */
	public static final String GET_POINT = YT_URL + "/app/getPoint";
	
	/** 扣除积分 */
	public static final String REDUCE_POINT = YT_URL + "/app/reducePoint";
	
	/** 积分明细 */
	public static final String POINT_DETAIL = YT_URL + "/app/allRecord";
	
	/** 获取各个平台当天还能获取的积分数 */
	public static final String PLATFORM_POINT_TODAY = YT_URL + "/activity/sharePointRule";
	
	/** 查看应用是否有活动 */
	public static final String CHECK_ON_GOING = YT_URL + "/activity/checkOngoing";
	
	/** 积分中心*/
	public static final String POINT_CENTER = YT_URL + "/activity/checkLotterySeniority";
	
	public static final String CHECKCODE = YT_URL + "/activity/checkCode";
	
	public static final String CLOSE_APP = YT_URL + "/activity/closeApp";
	
	public static final String SHARE_POINT_RULE = YT_URL + "/activity/sharePointRule";
	
	/** 签到得积分*/
	public static final String LOGIN = YT_URL + "/activity/login";
	
	/** 开发者自己添加积分;用户自定义的积分项目,例如"签到","分享"等,具体获得分数需要在友推后台设置*/
	public static final String CUSTOM_POINT = YT_URL + "/activity/customPoint";
	
	public static final String RECORD_URL = YT_URL + "/activity/artUrl";
	
	public static final String INFO_BY_ID = YT_URL + "/app/infoById";
	
	public static final String SHARE_CONTENT = YT_URL + "/app/shareContent";
	
	/**新浪微博授权权限*/
	public static final String SINA_WEIBO_SCOPE = "email,direct_messages_read,direct_messages_write,"
			+ "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
			+ "follow_app_official_microblog," + "invitation_write";
	
	public static final String TENCENT_SCOPE = "all";
	/**微信4.2以上支持分享至朋友圈*/
	public static final int WEIXIN_TIMELINE_SUPPORTED_VERSION = 0x21020001;
	/**人人网授权权限*/
	public static final String RENREN_SCOPE = "read_user_blog read_user_photo read_user_status read_user_album read_user_comment read_user_share publish_blog publish_share send_notification photo_upload status_update create_album publish_comment publish_feed";

	/**新浪微博包名*/
	public static final String PACKAGE_NAME_SINA_WEIBO = "com.sina.weibo";
	/**qq包名*/
	public static final String PACKAGE_NAME_TENCENT_QQ = "com.tencent.mobileqq";
	/**人人网包名*/
	public static final String PACKAGE_NAME_RENREN = "com.renren.mobile.android";
	/**微信包名*/
	public static final String PACKAGE_NAME_WEIXIN = "com.tencent.mm";
}
