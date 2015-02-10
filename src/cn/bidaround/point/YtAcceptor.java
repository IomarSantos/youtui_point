package cn.bidaround.point;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
/**
 * 读取res,packageName,cardNum,imei等信息以便于后续使用
 * @author youtui 
 * @since  14/6/19
 */
public class YtAcceptor {
	
	/** 邀请码 */
	private static String inviteNum;
	
	/** 应用的名称 */
	private static String appName;
	
	/**Android sdk版本*/
	private static String sdk;
	
	/**Android手机型号*/
	private static String model;
	
	/**手机系统版本号*/
	private static String sys;
	
	/** SIM序列号 */
	private static String cardNum;
	
	/** 用于判断是否已找到所要的apk */
	private static boolean flag;
	

	/** 获取友推渠道号(格式：appName_yt)，以获得appName */
	private static void getYoutuiChannel(Context context) {
		ApplicationInfo info;
		try {
			PackageManager packageManager = context.getPackageManager();
			info = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			String msg = info.metaData.getString("YOUTUI_CHANNEL");
			appName = msg.substring(0, msg.length() - 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@SuppressWarnings("deprecation")
	/**获取设备信息 */
	private static void readPhoneInfo(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null) {
			YtPoint.imei = tm.getDeviceId(); /* 获取imei号 */
		}
		sdk = android.os.Build.VERSION.SDK; // SDK号
		model = android.os.Build.MODEL; // 手机型号
		sys = android.os.Build.VERSION.RELEASE; // android系统版本号
	}
	/**获取SIM序列号*/
	private static void getCardNum(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null) {
			cardNum = tm.getSimSerialNumber();
		}
	}
	
	/** 把相关信息（设备信息、appId、邀请码等）发送到服务器 */
	public static void doPost(Activity act, String appKey, String appSecret, String appUserId) {
		List<NameValuePair> params = YtPoint.buildBaseParams();
		params.add(new BasicNameValuePair("inviteCode", inviteNum));
		params.add(new BasicNameValuePair("appUserId", appUserId));
		params.add(new BasicNameValuePair("cardNum",cardNum));
		params.add(new BasicNameValuePair("sdkVersion", sdk));
		params.add(new BasicNameValuePair("phoneType", model));
		params.add(new BasicNameValuePair("sysVersion", sys));
		params.add(new BasicNameValuePair("type", "auto"));// 接收推荐类型 auto自动,manual 人工
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		params.add(new BasicNameValuePair("resolution", mDisplayMetrics.widthPixels+"*"+mDisplayMetrics.heightPixels));
		TelephonyManager telephonyManager = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
		String operator = telephonyManager.getSimOperator();
		params.add(new BasicNameValuePair("operator", operator));
		//获取网络连接管理者
		ConnectivityManager connectionManager = (ConnectivityManager)act.getSystemService(Context.CONNECTIVITY_SERVICE);    
		//获取网络的状态信息，有下面三种方式
		String gprs = null;
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
		if(networkInfo!=null&&networkInfo.getType()==ConnectivityManager.TYPE_WIFI)
			gprs = "wifi";
		else if(networkInfo!=null&&networkInfo.getType()==ConnectivityManager.TYPE_MOBILE)
			gprs = networkInfo.getExtraInfo();
		params.add(new BasicNameValuePair("gprs", gprs));
		try {
			post(YtConstants.CHECKCODE, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 关闭应用时发送相关信息（appId和IMEI码） */
	public static void toPost(String youTui_AppKey) {
		List<NameValuePair> params = YtPoint.buildBaseParams();
		try {
			post(YtConstants.CLOSE_APP, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 遍历sd卡中的文件，找到对应的apk，从名字中获得邀请码 */
	private static void getInviteNum() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// 获得SD卡父目录的路径，以便还可以遍历外置SD卡
			File path = Environment.getExternalStorageDirectory().getParentFile();
			File[] files = path.listFiles();// 读取
			getFileName(files);
		}
	}

	/** 从文件名中读取邀请码 */
	private static String getFileName(File[] files) {
		if (flag != true) {
			// 先判断目录是否为空，否则会报空指针
			if (files == null) {
				return null;
			}
			for (File file : files) {
				if (file.isDirectory()) {
					// 若是文件目录。继续读
					getFileName(file.listFiles());
				} else {
					String fileName = file.getName();
					// 扫描apk 安装包中后缀为yt.apk 且包含appName的文件名
					// 格式为appName_邀请码_yt.apk，如tuoche_100041_yt.apk
					if (fileName!=null&&fileName.endsWith(".apk") &&appName!=null&& fileName.contains(appName)&&fileName.contains("_")) {
						if(fileName.indexOf("_")<fileName.lastIndexOf("_")){
							inviteNum = fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("_"));
						}					
						flag = true;
						break;
					}
				}
			}
			return null;
		} else {
			return null;
		}
	}

	/**
	 * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	 * 
	 * @param actionUrl
	 * @param params
	 * @return
	 */
	private static void post(String actionUrl, List<NameValuePair> params) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(actionUrl);
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response;
			response = httpclient.execute(httppost);
			response.getEntity();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 此方法用于邀请码等的自动上传，必须放在用户应用的初始化函数里
	 * 
	 * @param context
	 */
	public static void init(final Activity act,final String appKey, final String appSecret,final String appUserId) {
		/* 新开一个线程 */
		new Thread() {
			public void run() {
				/* 获取SIM卡号 */
				getCardNum(act);
				
				/* 获取应用的渠道号 */
				getYoutuiChannel(act);
				
				/* 获取邀请码 */
				getInviteNum();
				
				/* 获取手机信息 */
				readPhoneInfo(act);
				
				/* 发送到服务器 */
				doPost(act,appKey, appSecret,appUserId);
			}
		}.start();
	}

	/**
	 * 此方法用于应用关闭时数据的上传（统计使用时间），必须写在应用退出的相关函数里
	 * @param context
	 */
	public static void close(final Context context,String youTui_AppKey) {
		toPost(youTui_AppKey);
	}
	
	/**
	 * 获取应用名
	 * @param context
	 * @return 应用名
	 */
    public static String getApplicationName(Context context) { 
        PackageManager packageManager = null; 
        ApplicationInfo applicationInfo = null; 
        try { 
            packageManager = context.getApplicationContext().getPackageManager(); 
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0); 
        } catch (PackageManager.NameNotFoundException e) { 
            applicationInfo = null; 
        } 
        String applicationName =  
        (String) packageManager.getApplicationLabel(applicationInfo); 
        return applicationName; 
    }
    
    /**
	 * 对appid和appSecret
	 */
	public static String md5Encrypt(String appId, String appSecret){
		try {
			String value = "appId=" + appId + "&appSecret=" + appSecret;
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(value.getBytes("utf8"));
			
			byte[] bytes = md.digest();
			StringBuilder ret=new StringBuilder(bytes.length<<1);
			for(int i=0;i<bytes.length;i++){
			  ret.append(Character.forDigit((bytes[i]>>4)&0xf,16));
			  ret.append(Character.forDigit(bytes[i]&0xf,16));
			}
			return ret.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}