package cn.bidaround.point;

import java.io.IOException;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 显示积分activity
 * @author youtui
 * @since 14/6/19
 */
public class PointActivity extends Activity {
	/**显示积分的WebView*/
	private WebView webView;
	/**显示WebView加载进度的ProgressDialog*/
	private static ProgressDialog mProgressDialog;
	/**sim卡序列号*/
	private String cardNum;
	/**android手机imei*/
	private String imei;
	/**返回按钮id*/
	private final int BACK_ID = 140801; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		cardNum = tm.getSimSerialNumber();
		imei = tm.getDeviceId();
		
		showProgressDialog(this, "加载中...",true);
		
		initPointView();
	}

	/**
	 * 初始化查看和了解积分界面
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	private void initPointView() {	
		LinearLayout view = new LinearLayout(this);
		view.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams viewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		view.setLayoutParams(viewLayoutParams);
		
		RelativeLayout headerLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams headerLinearParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dip2px(this, 50));
		headerLayout.setLayoutParams(headerLinearParams);
		headerLayout.setBackgroundColor(0xff66c0ff);
		
		//返回键
		LinearLayout back = new LinearLayout(this);
		RelativeLayout.LayoutParams backParams = new RelativeLayout.LayoutParams(dip2px(this, 50), RelativeLayout.LayoutParams.MATCH_PARENT);
		backParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		back.setHorizontalGravity(Gravity.CENTER);
		back.setVerticalGravity(Gravity.CENTER);
		back.setId(BACK_ID);
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PointActivity.this.finish();

			}

		});
		//返回键图片
		ImageView backImage = new ImageView(this);
		LayoutParams backImageParams = new LayoutParams(dip2px(this, 20), dip2px(this, 20));
		backImage.setLayoutParams(backImageParams);
		AssetManager asset = getAssets();
		Bitmap backBitmap = null;
		try {
			backBitmap = BitmapFactory.decodeStream(asset.open("yt_left_arrow.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		backImage.setImageBitmap(backBitmap);
		back.addView(backImage);
		//标题栏
		TextView title = new TextView(this);
		RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		title.setGravity(Gravity.CENTER);
		title.setText("积分活动");
		title.setTextSize(16);
		title.setTextColor(0xffffffff);
		
		headerLayout.addView(back, backParams);
		headerLayout.addView(title, titleParams);			
		webView = new WebView(this);
		LinearLayout.LayoutParams webLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		webView.setLayoutParams(webLayoutParams);
		webView.setWebViewClient(new WebViewClient());
		WebSettings webSettings = webView.getSettings();
		webSettings.setDefaultTextEncodingName("utf-8");
		webSettings.setBuiltInZoomControls(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSavePassword(true);
		webSettings.setSaveFormData(false);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webSettings.setDatabaseEnabled(true);
		// 重新弹出框
		webView.setWebChromeClient(new MyWebChromeClient());
		webView.setOnKeyListener(new WebViewOnKayListener());
		// 查看和了解积分
		webView.loadUrl(YtConstants.POINT_CENTER + "?appId=" + YtPoint.youTui_AppKey + 
				"&cardNum=" + cardNum + "&imei=" + imei  + "&sign=" + YtAcceptor.md5Encrypt(YtPoint.youTui_AppKey, YtPoint.youTui_AppSecret));

		view.addView(headerLayout);
		view.addView(webView);
		setContentView(view);
	}

	/**
	 * 加载到80%结束进度条
	 */
	final class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int progress) {
			PointActivity.this.setTitle("加载中...");
			PointActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, progress * 100);
			PointActivity.this.setProgress(progress);
			if (progress >= 80) {
				PointActivity.this.setTitle("");
				dismissDialog();
			}
		}

	}

	/**重写webview的返回键*/
	class WebViewOnKayListener implements View.OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (webView.canGoBack()) {
						webView.goBack();
						return true;
					} else {
						PointActivity.this.finish();
						return true;
					}
				}
			}
			return false;
		}

	}
	
	/**
	 * webview显示加载进度
	 * @param act
	 * @param message
	 * @param isFinishActivity
	 */
	public static final void showProgressDialog(final Activity act, String message,final boolean isFinishActivity) {
		dismissDialog();
		mProgressDialog = new ProgressDialog(act);
		// 设置进度条风格，风格为圆形，旋转的
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// 设置ProgressDialog 提示信息
		mProgressDialog.setMessage(message);
		// 设置ProgressDialog 的进度条是否不明确
		mProgressDialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if(isFinishActivity){
					act.finish();
				}				
			}
		});
		mProgressDialog.show();
	}
	/**
	 * 关闭ProgressDialog
	 */
	public static final void dismissDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	/**
	 * dp to px
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

}
