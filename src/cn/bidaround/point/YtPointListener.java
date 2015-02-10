package cn.bidaround.point;

/**
 * 友推积分监听
 * 
 * @author youtui
 * @since 14/6/19
 */
public abstract class YtPointListener {

	/** 获取积分成功,如果该频道此时无积分,gainedPoint为0 */
	public abstract void onSuccess(int gainedPoint);

	/** 分享成功，但是获取积分时失败 */
	public abstract void onFail();
}
