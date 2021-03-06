package com.seeyuan.logistics.activity;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mesada.nj.pubcontrols.controls.RemoteImageView;
import com.seeyuan.logistics.R;
import com.seeyuan.logistics.application.ApplicationPool;
import com.seeyuan.logistics.application.ConstantPool;
import com.seeyuan.logistics.application.NetWork;
import com.seeyuan.logistics.customview.MuInputEditText;
import com.seeyuan.logistics.customview.ProgressAlertDialog;
import com.seeyuan.logistics.datacenter.DataHandler;
import com.seeyuan.logistics.datacenter.OnDataReceiveListener;
import com.seeyuan.logistics.datahandler.LoginHandler;
import com.seeyuan.logistics.entity.MemberDto;
import com.seeyuan.logistics.entity.PdaRequest;
import com.seeyuan.logistics.entity.PdaResponse;
import com.seeyuan.logistics.entity.UserInfo;
import com.seeyuan.logistics.jsonparser.LoginJsonParser;
import com.seeyuan.logistics.provider.DBOperate;
import com.seeyuan.logistics.service.CarBDLocationService;
import com.seeyuan.logistics.service.CheckUpdateService;
import com.seeyuan.logistics.service.LoginIMServerService;
import com.seeyuan.logistics.util.CommonUtils;
import com.seeyuan.logistics.util.MD5Util;
import com.seeyuan.logistics.util.ToastUtil;

@SuppressLint("HandlerLeak")
public class LoginActivity extends BaseActivity implements OnClickListener, OnDataReceiveListener {

	/**
	 * 返回按钮
	 */
	private ImageView maintitle_back_iv;

	/**
	 * 标题title
	 */
	private TextView defaulttitle_title_tv;

	private Context context;

	/**
	 * 登录按钮
	 */
	private Button login_submit;

	/**
	 * 账号
	 */
	private MuInputEditText login_userID;

	/**
	 * 密码
	 */
	private MuInputEditText login_password;

	/**
	 * 登录成功
	 */
	private final int LOGIN_CODE_SUCCESS = 100;
	/**
	 * 请求参数不能为空
	 */
	private final int LOGIN_CODE_PARAMETER_NULL = 101;
	/**
	 * 参数中用户名不能为空
	 */
	private final int LOGIN_CODE_USERNAME_NULL = 102;
	/**
	 * 参数中密码不能为空
	 */
	private final int LOGIN_CODE_PASSWORD_NULL = 103;
	/**
	 * 用户名不存在
	 */
	private final int LOGIN_CODE_USERNAME_UNINVAILED = 104;
	/**
	 * 密码不正确，请重新输入
	 */
	private final int LOGIN_CODE_PASSWORD_WRONG = 105;
	/**
	 * 登录验证失败
	 */
	private final int LOGIN_CODE_INVAILED = 106;

	/**
	 * 显示进度条
	 */
	private final int SHOW_PROGRESS = 1000;
	/**
	 * 关闭进度条
	 */
	private final int CLOSE_PROGRESS = 1001;

	private SharedPreferences sPreferences;

	private DBOperate dbOperate;

	private ProgressAlertDialog progressDialog;

	private RemoteImageView userHeadImg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_login);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.deflaut_titlebar); // titlebar为自己标题栏的布局
		context = getApplicationContext();
		sPreferences = getSharedPreferences(ConstantPool.LOGISTICS_PREFERENCES, Context.MODE_PRIVATE);
		dbOperate = DBOperate.getInstance(context);
		startCheckUpdateService();
		initView();
		ToastUtil.show(context, getIntent().getStringExtra("toast"));
	}

	@Override
	public void initView() {
		maintitle_back_iv = (ImageView) findViewById(R.id.maintitle_back_iv);
		maintitle_back_iv.setVisibility(View.GONE);
		maintitle_back_iv.setOnClickListener(this);

		defaulttitle_title_tv = (TextView) findViewById(R.id.defaulttitle_title_tv);
		defaulttitle_title_tv.setText(R.string.login_title);

		login_submit = (Button) findViewById(R.id.login_submit);
		// login_submit.setEnabled(false);

		login_userID = (MuInputEditText) findViewById(R.id.login_userID);
		String userName = sPreferences.getString("userName", "");
		login_userID.setText(userName);
		login_userID.addTextChangedListener(textWatcherListener);

		login_password = (MuInputEditText) findViewById(R.id.login_password);
		String password = sPreferences.getString("password", "");
		login_password.setText(password);
		// login_password.addTextChangedListener(textWatcherListener);

		userHeadImg = (RemoteImageView) findViewById(R.id.userHeadImg);
		doCheckUserInfo(userName);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.maintitle_back_iv:
			CommonUtils.finishActivity(this);
			break;

		default:
			break;
		}
	}

	private void startCheckUpdateService() {
		Intent intent = new Intent(LoginActivity.this, CheckUpdateService.class);
		startService(intent);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService(new Intent(LoginActivity.this, CheckUpdateService.class));
	}

	@Override
	public void onClickListener(View view) {
		switch (view.getId()) {
		case R.id.login_submit:
			doLoginSubmit();
			break;
		case R.id.registBtn:
			doRegister();
			break;
		case R.id.getPassWordBtn:
			doResetPassword();
			break;

		default:
			break;
		}
	}

	private Context mContext;

	private void doJump2PersonalCenter() {

		String uuid = CommonUtils.getUUID(mContext);
		UserInfo userInfo = null;
		try {
			userInfo = dbOperate.getUesrInfoByUUID(uuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null == userInfo || userInfo.getIsLogin().equalsIgnoreCase("N")) {
			myHandler.sendEmptyMessage(1);
			return;
		}
		if (userInfo.getMemberType() == 2) {
			Intent selectIntent = new Intent(LoginActivity.this, SelectCarActivity.class);
			selectIntent.putExtra("isLogin", true);
			startActivity(selectIntent);
		} else {
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra("myTag", 2);
			startActivity(intent);
		}
	}

	private Handler myHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			dismissProgress();
			switch (msg.what) {
			case LOGIN_CODE_SUCCESS:
				Intent imIntent = new Intent(LoginActivity.this, LoginIMServerService.class);
				startService(imIntent);
				doJump2PersonalCenter();
				// Intent intent = new Intent(LoginActivity.this,
				// MainActivity.class);
				// startActivity(intent);
				finish();
				break;
			case LOGIN_CODE_PARAMETER_NULL:
			case LOGIN_CODE_USERNAME_NULL:
			case LOGIN_CODE_PASSWORD_NULL:
			case LOGIN_CODE_INVAILED:
				ToastUtil.show(context, msg.obj.toString());
				break;
			case LOGIN_CODE_USERNAME_UNINVAILED:
				login_userID.showPopWindow(LoginActivity.this, msg.obj.toString());
				break;
			case LOGIN_CODE_PASSWORD_WRONG:
				login_password.showPopWindow(LoginActivity.this, msg.obj.toString());
				break;
			case SHOW_PROGRESS:
				showProgress();
				break;
			case CLOSE_PROGRESS:
				dismissProgress();
				break;

			default:
				break;
			}
		};
	};

	/**
	 * 重置密码
	 */
	private void doResetPassword() {
		CommonUtils.addActivity(this);
		Intent intent = new Intent(LoginActivity.this, RetrievePasswordAuthcodeActivity.class);
		startActivity(intent);
	}

	/**
	 * 执行注册
	 */
	private void doRegister() {
		CommonUtils.addActivity(this);
		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(intent);

	}

	/**
	 * 执行登录操作
	 */
	private void doLoginSubmit() {

		if (login_userID.getText().length() == 0) {
			login_userID.showPopWindow(LoginActivity.this, "请输入正确的账号");
			return;
		}

		if (!CommonUtils.isPasswordTypeCorrect(login_password.getText().toString())) {
			login_password.showPopWindow(LoginActivity.this, getResources().getString(R.string.psw_number_format));
			return;
		}
		myHandler.sendEmptyMessage(SHOW_PROGRESS);
		CommonUtils.closeKeyboard(context, login_password);
		CommonUtils.closeKeyboard(context, login_userID);
		MemberDto loginInfo = new MemberDto();
		loginInfo.setUserName(login_userID.getText().toString());
		loginInfo.setPassword((MD5Util.getMD5String(login_password.getText().toString())).toLowerCase());
		PdaRequest<MemberDto> request = new PdaRequest<MemberDto>();
		request.setData(loginInfo);
		LoginHandler loginHandler = new LoginHandler(context, request);
		loginHandler.setOnDataReceiveListener(this);
		loginHandler.startNetWork();

	}

	@Override
	public void onDataReceive(DataHandler dataHandler, int resultCode, Object data, int type) {
		switch (resultCode) {
		case NetWork.LOGIN_OK:

			doLoginSuccess(data);
			break;
		case NetWork.LOGIN_ERROR:
			myHandler.sendEmptyMessage(CLOSE_PROGRESS);
			ToastUtil.show(context, getResources().getString(R.string.network_error_hint));
			break;

		default:

			break;
		}
	}

	/**
	 * 登录成功
	 */
	private void doLoginSuccess(Object data) {
		String dataString = null;
		try {
			dataString = new String((byte[]) data, "UTF-8");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			PdaResponse<MemberDto> response = LoginJsonParser.parserLoginJson(dataString);
			Log.i("返回结果", dataString.toString());
			if (null != response && response.isSuccess()) {// 登录成功
				MemberDto result = response.getData();
				// 保存登录信息
				Editor editor = sPreferences.edit();
				editor.putString("uuId", result.getUuId());
				editor.putString("memberType", result.getMemberType().toString());
				editor.putString("password", login_password.getText().toString());
				editor.putString("userName", result.getUserName());
				editor.putString("mobile", result.getMobile());
				editor.commit();
				ApplicationPool.setUUID(result.getUuId());
				ApplicationPool.setMemberType(result.getMemberType().toString());
				ApplicationPool.setPassword(login_password.getText().toString());
				ApplicationPool.setUserName(result.getUserName());
				UserInfo userInfo = new UserInfo();
				userInfo.setUuId(result.getUuId());
				userInfo.setUSER_NAME(result.getUserName());
				userInfo.setMemberType(result.getMemberType());
				// 密码使用明文保存
				userInfo.setPASSWORD(login_password.getText().toString());
				userInfo.setMOBILE(result.getMobile());
				userInfo.setEMAIL(result.getEmail());
				userInfo.setIS_EMAIL_PROVEN(result.getIsEmailProven());
				userInfo.setNICKNAME(result.getNickName());
				userInfo.setSEX(result.getSex());
				userInfo.setBIRTHDAY(null == result.getBirthday() ? "" : CommonUtils.parserData(result.getBirthday()));
				userInfo.setPROVINCE(result.getProvince());
				userInfo.setCITY(result.getCity());
				userInfo.setREGION(result.getRegion());
				userInfo.setADDRESS(result.getAddress());
				userInfo.setZIP(result.getZip());
				userInfo.setTEL(result.getTel());
				userInfo.setQQ(result.getQq());
				userInfo.setWECHAT(result.getWechat());
				userInfo.setINFO_COMPL(result.getInfoCompl());
				userInfo.setFACE(null == result.getFace() ? "" : result.getFace().getHeaderImgURL());
				userInfo.setLAST_LAT(Double.isNaN(result.getLast_lat()) ? "" : String.valueOf(result.getLast_lat()));
				userInfo.setLAST_LNG(Double.isNaN(result.getLast_lng()) ? "" : String.valueOf(result.getLast_lng()));
				userInfo.setLAST_LOCATION(result.getLast_location());
				userInfo.setADDRESS(result.getAddress());
				userInfo.setIsLogin("Y");
				dbOperate.updateUserInfo(userInfo);
				myHandler.sendEmptyMessage(LOGIN_CODE_SUCCESS);
			} else {// 登录失败
				try {
					String result = response.getMessage();
					int messageCode = Integer.parseInt(result.substring(0, result.indexOf("#")));
					String message = result.substring(result.indexOf("#") + 1, result.length());
					Message msg = myHandler.obtainMessage();
					msg.what = messageCode;
					msg.obj = message;
					myHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(CLOSE_PROGRESS);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			myHandler.sendEmptyMessage(CLOSE_PROGRESS);
		}
	}

	private TextWatcher textWatcherListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// 登录按钮是否可点击操作判断
			// login_userID.dismissPopWindow();
			// login_password.dismissPopWindow();
			// if (login_userID.length() > 0 && login_password.length() > 0) {
			// login_submit.setEnabled(true);
			// login_submit
			// .setBackgroundResource(R.drawable.confirm_back_button_select);
			// } else {
			// login_submit.setEnabled(false);
			// login_submit
			// .setBackgroundResource(R.drawable.submint_btn_unfocaus);
			// }

			doCheckUserInfo(arg0.toString());
		}
	};

	private void showProgress() {
		if (progressDialog == null) {
			progressDialog = new ProgressAlertDialog(this);
		} else {
			progressDialog.show();
		}
	}

	/**
	 * 匹配头像
	 * 
	 * @param arg0
	 */
	protected void doCheckUserInfo(String userName) {
		UserInfo userInfo = dbOperate.getUesrInfoByUserName(userName);
		if (null == userInfo) {
			userHeadImg.setImageDrawable(getResources().getDrawable(R.drawable.default_header_img));
		} else {
			userHeadImg.draw(userInfo.getFACE(), ConstantPool.DEFAULT_ICON_PATH, false, true);
		}
	}

	private void dismissProgress() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
}
