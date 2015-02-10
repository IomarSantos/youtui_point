package cn.bidaround.point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * 友推积分操作类
 * 
 * @author youtui
 * @since 14/6/19
 */
public class YtPoint {
	/** 服务器返回的Json字符串 */
	private static String jsonStr;
	/** SIM卡序列号 */
	private static String cardNum;
	/** 手机imei */
	public static String imei;
	/** 获取积分监听 */
	private static YtPointListener listener;
	/** 友推AppKey */
	public static String youTui_AppKey;
	public static String youTui_AppSecret;
	
	public static final String BROADCAST_REFRESHPOINT = "cn.bidaround.point.BROADCAST_REFRESHPOINT";
	
	@SuppressLint("UseSparseArrays") 
	public static Map<Integer, Integer> pointMap = new HashMap<Integer, Integer>();

	/**
	 * 赠送积分
	 * 
	 * @param givePoint
	 *            赠送的积分数
	 * @param appUserId
	 *            被赠送人的id
	 * @return
	 */
	public static String givePoint(int givePoint, String appUserId) {
		String str = null;
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.GIVE_POINT);

		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("appUserId", appUserId));
			params.add(new BasicNameValuePair("givePoint", String.valueOf(givePoint)));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			str = EntityUtils.toString(entity);
			// YtLog.d("获取积分信息", str);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 该应用是否在活动中
	 * 
	 * @return
	 */
	public static String isOnAction() {
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.CHECK_ON_GOING);
		String responseStr = null;
		try {
			List<NameValuePair> params = buildBaseParams();
			
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			responseStr = EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseStr;
	}

	private YtPoint() {
		
	}

	/**
	 * 初始化积分系统
	 *  	不建议使用
	 * @param act
	 * @param youTui_AppKey
	 *            友推appkey
	 * @param appUserId
	 *            用户id
	 */
//	public static void init(Activity act, String appKey, String appUserId) {
//		init(act, appKey, appKey, appUserId);
//	}
	
	/**
	 * 初始化积分系统
	 * @param appKey
	 *            友推appkey
	 * @param appUserId
	 *            用户id
	 */
	public static void init(Activity act, String appKey, String appSecret, String appUserId) {
		TelephonyManager tm = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
		cardNum = tm.getSimSerialNumber();
		imei = tm.getDeviceId();
		YtPoint.youTui_AppKey = appKey;
		YtPoint.youTui_AppSecret = appSecret;
		YtAcceptor.init(act, youTui_AppKey, appSecret, appUserId);
	}

	public static void setListener(YtPointListener listener) {
		YtPoint.listener = listener;
	}

	/**
	 * 分享获得积分
	 */
	public static void sharePoint(final Context context, final int channelId, final String url, final boolean isShareContent, final String uniqueCode) {
		/** 异步操作获取积分 */
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				List<NameValuePair> params = buildBaseParams();
				HttpPost post = new HttpPost(YtConstants.SHARE_POINT);
				// 传入手机号，设备号，友推id,频道id
				params.add(new BasicNameValuePair("cardNum", cardNum));
				params.add(new BasicNameValuePair("channelId", String.valueOf(channelId)));
				params.add(new BasicNameValuePair("artId  ", "101"));
//				// 用户分享的真实url
//				params.add(new BasicNameValuePair("realUrl", url));
				// YtLog.e("--send after share--",url);
//				// 传入唯一标示
//				params.add(new BasicNameValuePair("virtualUrl", uniqueCode));
//				// 是否为分享内容
//				params.add(new BasicNameValuePair("shareContent", Boolean.toString(isShareContent)));
//				// 是否为YOUTUI分享(可能为积分版本分享)
//				params.add(new BasicNameValuePair("isYoutui", Boolean.toString(true)));
				
				Integer point = pointMap.get(channelId);
				
				point = point == null ? 0  : point;
				
				params.add(new BasicNameValuePair("point", point.toString()));

				try {
					post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					@SuppressWarnings("unused")
					HttpResponse response = client.execute(post);
					Map<Integer, Integer> map = parse(context);
					if (listener != null) {
						Integer newId = map.get(channelId);
						Integer oldId = pointMap.get(channelId);
						
						newId = isChannelIdNull(newId) ? 0 : newId; 
						oldId = isChannelIdNull(oldId) ? 0 : oldId; 
						listener.onSuccess(newId - oldId);
					}
				} catch (Exception e) {
					if (listener != null) {
						listener.onFail();
					}
				}
			};
		}.start();
	}
	
	private static boolean isChannelIdNull(Integer id){
		return id == null || "null".equalsIgnoreCase(id.toString());
	}

	/**
	 * @param context
	 * @return 返回获取积分的Json字符串
	 */
	private static String getPointJson(Context context) {

		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.SHARE_POINT_RULE);
		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("cardNum", cardNum));
			params.add(new BasicNameValuePair("phoneType", android.os.Build.MODEL));
			params.add(new BasicNameValuePair("sysVersion", android.os.Build.VERSION.RELEASE));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			jsonStr = EntityUtils.toString(entity);
			// YtLog.d("获取积分信息", jsonStr);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			jsonStr = null;
		} catch (IOException e) {
			e.printStackTrace();
			jsonStr = null;
		}
		return jsonStr;
	}

	/**
	 * 
	 * 解析平台积分并装入pointArr
	 * 
	 * @return
	 * @throws JSONException
	 */
	private static Map<Integer, Integer> parse(Context context) throws JSONException {
		Map<Integer, Integer> map = pointMap;
		String str = getPointJson(context);
		if (str != null) {
			// 获取成功的话，装入pointArr
			JSONObject objectJson = new JSONObject(str).getJSONObject("object");
			
			for(ChannelId channelId : ChannelId.values()){
				String id = channelId.toString().split("_")[1];
				map.put(Integer.parseInt(id), objectJson.getInt("channel" + id));
			}
		} 
		Intent it = new Intent(BROADCAST_REFRESHPOINT);
		context.sendBroadcast(it);
		return map;
	}

	/**
	 * 刷新积分
	 */
	public static void refresh(Context context) {
		try {
			parse(context);
		} catch (JSONException e) {
		}
	}

	/**
	 * 获取指定平台积分
	 * 
	 * @param channelId
	 * @return 指定平台积分
	 */
	public static int getPlatformPoint(int channelId) {
		return pointMap.get(channelId);
	}

	/**
	 * 判断是否有积分活动
	 * 
	 * @return 是否有积分活动
	 */
	public static boolean hasPoint() {
		return !pointMap.isEmpty();
	}

	/** 将一个数组的元素填充到另一个数组用来保存元素信息 */
	public static void addArr(int[] fromArr, int[] toArr) {
		for (int i = 0; i < toArr.length; i++) {
			toArr[i] = fromArr[i];
		}
	}

	/**
	 * 释放listener,记录应用关闭
	 */
	public static void release(final Context context) {
		listener = null;
		new Thread() {
			public void run() {
				YtAcceptor.close(context, youTui_AppKey);
			};
		}.start();
	}

	/**
	 * 获得签到积分
	 * 
	 * @return 请求失败则返回null
	 */
	public static String addLoginPoint() {
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.LOGIN);
		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("cardNum", cardNum));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			return str;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 开发者自己添加积分
	 * 
	 * @param pointName
	 *            用户自定义的积分项目,例如"签到","分享"等,具体获得分数需要在友推后台设置
	 * @return 服务器返回的数据或者null
	 */
	public static String customPoint(String pointName) {
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.CUSTOM_POINT);
		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("pointName", pointName));
			params.add(new BasicNameValuePair("cardNum", cardNum));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			return str;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取该用户积分数
	 * 
	 * @param appUserId
	 *            用户id
	 * @return 服务器返回数据或者null
	 */
	public static String getAppPoint(String appUserId) {
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.GET_POINT);
		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("appUserId", appUserId));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			return str;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 扣除用户积分
	 * 
	 * @param appUserId
	 *            用户id
	 * @param reducePoint
	 *            减少的积分数
	 * @param reason
	 *            扣除积分的原因
	 * @return
	 */
	public static String reduceAppPoint(String appUserId, int reducePoint, String reason) {
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.REDUCE_POINT);
		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("appUserId", appUserId));
			params.add(new BasicNameValuePair("reducePoint", appUserId));
			params.add(new BasicNameValuePair("reason", appUserId));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			return str;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 积分明细
	 * 
	 * @param appUserId
	 *            用户id
	 * @return 服务器返回数据或者null
	 */
	public static String getAppPointDetail(String appUserId) {
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.POINT_DETAIL);
		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("appUserId", appUserId));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			return str;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取当天各个平台可以获得的积分
	 * 
	 * @param context
	 * @param youTui_AppKey
	 * @return 服务器返回的数据
	 */
	public static String getTodayPoints(Context context) {
		String jsonStr = null;
		HttpParams httpParam = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParam, 10000);
		HttpClient client = new DefaultHttpClient(httpParam);
		HttpPost post = new HttpPost(YtConstants.PLATFORM_POINT_TODAY);
		try {
			List<NameValuePair> params = buildBaseParams();
			params.add(new BasicNameValuePair("cardNum", cardNum));
			params.add(new BasicNameValuePair("phoneType", android.os.Build.MODEL));
			params.add(new BasicNameValuePair("sysVersion", android.os.Build.VERSION.RELEASE));
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			jsonStr = EntityUtils.toString(entity);
			// YtLog.d("获取积分信息", jsonStr);
			return jsonStr;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}
	
	/**
	 * 打开积分活动页面
	 * 
	 * @param context
	 */
	public static void showPointView(Context context) {
		Intent it = new Intent(context, PointActivity.class);
		context.startActivity(it);
	}
	
	/**
	 * 获得各平台积分的数组
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getPoint() {
		return pointMap;
	}
	
	public static int getPoint(int channelId){
		if(pointMap.containsKey(channelId)){
			Integer point = pointMap.get(channelId);
			if(!isChannelIdNull(point))
				return point;
		}
		return 0;
	}
	
	/**
	 * 服务器请求，必须的基本参数
	 */
	public static List<NameValuePair> buildBaseParams(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("imei", imei));
		params.add(new BasicNameValuePair("appId", youTui_AppKey));
		params.add(new BasicNameValuePair("sign", YtAcceptor.md5Encrypt(youTui_AppKey, youTui_AppSecret)));
		return params;
	}
	
	public static void setDefaultListener(){
		setListener(new YtPointListener() {
			
			@Override
			public void onSuccess(int gainedPoint) {
				
			}
			
			@Override
			public void onFail() {
				
			}
		});
	}

}
