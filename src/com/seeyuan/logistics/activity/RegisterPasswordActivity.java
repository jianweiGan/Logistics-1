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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.seeyuan.logistics.R;
import com.seeyuan.logistics.application.ApplicationPool;
import com.seeyuan.logistics.application.ConstantPool;
import com.seeyuan.logistics.application.NetWork;
import com.seeyuan.logistics.customview.MuInputEditText;
import com.seeyuan.logistics.customview.ProgressAlertDialog;
import com.seeyuan.logistics.datacenter.DataHandler;
import com.seeyuan.logistics.datacenter.OnDataReceiveListener;
import com.seeyuan.logistics.datahandler.SubmitRegisterPasswordHandler;
import com.seeyuan.logistics.entity.MemberDto;
import com.seeyuan.logistics.entity.PdaRequest;
import com.seeyuan.logistics.entity.PdaResponse;
import com.seeyuan.logistics.entity.UserInfo;
import com.seeyuan.logistics.jsonparser.LoginJsonParser;
import com.seeyuan.logistics.provider.DBOperate;
import com.seeyuan.logistics.service.LoginIMServerService;
import com.seeyuan.logistics.util.CommonUtils;
import com.seeyuan.logistics.util.MD5Util;
import com.seeyuan.logistics.util.ToastUtil;

/**
 * 注册密码
 * 
 * @author zhazhaobao
 * 
 */
@SuppressLint("HandlerLeak")
public class RegisterPasswordActivity extends BaseActivity implements
		OnClickListener, OnDataReceiveListener {

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
	 * 输入密码
	 */
	private MuInputEditText rUserPWEdt;

	/**
	 * 确认密码
	 */
	private MuInputEditText againUserPWEdt;

	/**
	 * 提交，确认
	 */
	private Button rSubmitBtn;

	private MemberDto registerInfo;

	private final int SUBMIT_PASSWORD_SUCCESS = 200;

	private SharedPreferences sPreferences;

	private DBOperate dbOperate;

	/**
	 * 显示进度条
	 */
	private final int SHOW_PROGRESS = 2000;
	/**
	 * 关闭进度条
	 */
	private final int CLOSE_PROGRESS = 2001;

	private ProgressAlertDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_register_password);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.deflaut_titlebar); // titlebar为自己标题栏的布局
		context = getApplicationContext();
		registerInfo = (MemberDto) getIntent().getSerializableExtra(
				"registerInfo");
		sPreferences = getSharedPreferences(ConstantPool.LOGISTICS_PREFERENCES,
				Context.MODE_PRIVATE);
		dbOperate = DBOperate.getInstance(context);
		initView();
	}

	@Override
	public void initView() {
		maintitle_back_iv = (ImageView) findViewById(R.id.maintitle_back_iv);
		maintitle_back_iv.setOnClickListener(this);

		defaulttitle_title_tv = (TextView) findViewById(R.id.defaulttitle_title_tv);
		defaulttitle_title_tv.setText(R.string.register_hint);

		rUserPWEdt = (MuInputEditText) findViewById(R.id.rUserPWEdt);
		// rUserPWEdt.addTextChangedListener(textWatcherListener);

		againUserPWEdt = (MuInputEditText) findViewById(R.id.againUserPWEdt);
		// againUserPWEdt.addTextChangedListener(textWatcherListener);

		rSubmitBtn = (Button) findViewById(R.id.rSubmitBtn);
	}

	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUBMIT_PASSWORD_SUCCESS:
				Intent imIntent = new Intent(RegisterPasswordActivity.this,
						LoginIMServerService.class);
				startService(imIntent);
				Intent intent = new Intent(RegisterPasswordActivity.this,
						SelectCarActivity.class);
				CommonUtils.addActivity(RegisterPasswordActivity.this);
				// 登录成功，保存登录信息
				startActivity(intent);
				CommonUtils.finishAllActivity();
				break;
			case SHOW_PROGRESS:
				showProgress();
				break;
			case CLOSE_PROGRESS:
				dismissProgress();
				break;

			default:
				ToastUtil.show(context, msg.obj.toString());
				break;
			}
		};
	};

	private void showProgress() {
		if (progressDialog == null) {
			progressDialog = new ProgressAlertDialog(this);
		} else {
			progressDialog.show();
		}
	}

	private void dismissProgress() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void onClickListener(View view) {
		switch (view.getId()) {
		case R.id.rSubmitBtn:
			// doSubmitPassword();
			CommonUtils.closeKeyboard(context, rUserPWEdt);
			CommonUtils.closeKeyboard(context, againUserPWEdt);
			doSubmitPassword(rUserPWEdt.getText().toString(), againUserPWEdt
					.getText().toString());
			break;

		default:
			break;
		}
	}

	/**
	 * 提交密码
	 */
	// private void doSubmitPassword() {
	//
	// if (!CommonUtils.isPasswordTypeCorrect(rUserPWEdt.getText().toString()))
	// {
	// rUserPWEdt.showPopWindow(RegisterPasswordActivity.this,
	// getResources().getString(R.string.psw_number_format));
	// return;
	// }
	//
	// if (CommonUtils.isPasswordCorrect(rUserPWEdt.getText().toString(),
	// againUserPWEdt.getText().toString())) {
	//
	// SubmitRegisterPasswordHandler datahHandler = new
	// SubmitRegisterPasswordHandler(
	// context, registerInfo);
	// datahHandler.setOnDataReceiveListener(this);
	// datahHandler.startNetWork();
	// } else {
	// againUserPWEdt.showPopWindow(RegisterPasswordActivity.this,
	// getResources().getString(R.string.entered_psw_differ));
	// }
	//
	// }

	/**
	 * 提交密码
	 * 
	 * @param pass1
	 * @param pass2
	 */
	private void doSubmitPassword(String pass1, String pass2) {

		Filter f1 = new PasswordFilter1();

		Filter f2 = new PasswordFilter2();

		f1.setNext(f2);

		String result = f1.doFilter(pass1, pass2);
		if (result.equalsIgnoreCase("成功")) {
			myHandler.sendEmptyMessage(SHOW_PROGRESS);
			PdaRequest<MemberDto> request = new PdaRequest<MemberDto>();
			registerInfo.setPassword(MD5Util.getMD5String(pass1).toLowerCase());
			request.setData(registerInfo);
			SubmitRegisterPasswordHandler datahHandler = new SubmitRegisterPasswordHandler(
					context, request);
			datahHandler.setOnDataReceiveListener(this);
			datahHandler.startNetWork();
		} else if (result.equalsIgnoreCase("两次密码输入不一致")) {
			againUserPWEdt.showPopWindow(RegisterPasswordActivity.this,
					getResources().getString(R.string.entered_psw_differ));
		} else if (result.equalsIgnoreCase("密码长度必须大于8")) {
			rUserPWEdt.showPopWindow(RegisterPasswordActivity.this,
					getResources().getString(R.string.psw_number_format));
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.maintitle_back_iv:
			if (!RegisterPasswordActivity.this.isFinishing()) {
				finish();
			}
			break;

		default:
			break;
		}
	}

	private TextWatcher textWatcherListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			if (rUserPWEdt.length() > 0 && againUserPWEdt.length() > 0) {
				registerInfo.setPassword(rUserPWEdt.getText().toString());
				rSubmitBtn.setEnabled(true);
				rSubmitBtn
						.setBackgroundResource(R.drawable.confirm_back_button_select);
			} else {
				rSubmitBtn.setEnabled(false);
				rSubmitBtn
						.setBackgroundResource(R.drawable.submint_btn_unfocaus);
			}
		}
	};

	@Override
	public void onDataReceive(DataHandler dataHandler, int resultCode,
			Object data, int type) {
		myHandler.sendEmptyMessage(CLOSE_PROGRESS);
		switch (resultCode) {
		case NetWork.SUBMIT_REGISTER_PASSWORD_OK:
			doSubmitAuthcodeSuccess(data);
			break;
		case NetWork.SUBMIT_REGISTER_PASSWORD_ERROR:
			ToastUtil.show(context,
					getResources().getString(R.string.network_error_hint));
			break;

		default:
			break;
		}
	}

	/**
	 * 执行提交密码
	 */
	private void doSubmitAuthcodeSuccess(Object data) {
		String dataString = null;
		try {
			dataString = new String((byte[]) data, "UTF-8");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			PdaResponse<MemberDto> response = LoginJsonParser
					.parserLoginJson(dataString);
			if (response.isSuccess()) {// 登录成功
				MemberDto result = response.getData();
				// 保存登录信息
				Editor editor = sPreferences.edit();
				editor.putString("uuId", result.getUuId());
				editor.putString("memberType", result.getMemberType()
						.toString());
				editor.putString("password", rUserPWEdt.getText()
						.toString());
				editor.putString("userName", result.getUserName());
				editor.commit();
				ApplicationPool.setUUID(result.getUuId());
				ApplicationPool
						.setMemberType(result.getMemberType().toString());
				ApplicationPool
						.setPassword(rUserPWEdt.getText().toString());
				ApplicationPool.setUserName(result.getUserName());
				UserInfo userInfo = new UserInfo();
				userInfo.setUuId(result.getUuId());
				userInfo.setUSER_NAME(result.getUserName());
				userInfo.setMemberType(result.getMemberType());
				// 密码使用明文保存
				userInfo.setPASSWORD(rUserPWEdt.getText().toString());
				userInfo.setMOBILE(result.getMobile());
				userInfo.setEMAIL(result.getEmail());
				userInfo.setIS_EMAIL_PROVEN(result.getIsEmailProven());
				userInfo.setNICKNAME(result.getNickName());
				userInfo.setSEX(result.getSex());
				userInfo.setBIRTHDAY(null == result.getBirthday() ? ""
						: CommonUtils.parserData(result.getBirthday()));
				userInfo.setPROVINCE(result.getProvince());
				userInfo.setCITY(result.getCity());
				userInfo.setREGION(result.getRegion());
				userInfo.setADDRESS(result.getAddress());
				userInfo.setZIP(result.getZip());
				userInfo.setTEL(result.getTel());
				userInfo.setQQ(result.getQq());
				userInfo.setWECHAT(result.getWechat());
				userInfo.setINFO_COMPL(result.getInfoCompl());
				userInfo.setFACE(null == result.getFace() ? "" : result
						.getFace().getHeaderImgURL());
				userInfo.setLAST_LAT(Double.isNaN(result.getLast_lat()) ? ""
						: String.valueOf(result.getLast_lat()));
				userInfo.setLAST_LNG(Double.isNaN(result.getLast_lng()) ? ""
						: String.valueOf(result.getLast_lng()));
				userInfo.setLAST_LOCATION(result.getLast_location());
				userInfo.setADDRESS(result.getAddress());
				userInfo.setIsLogin("Y");
				dbOperate.updateUserInfo(userInfo);

				myHandler.sendEmptyMessage(SUBMIT_PASSWORD_SUCCESS);
			} else {// 登录失败
				String result = response.getMessage();
				int messageCode = Integer.parseInt(result.substring(0,
						result.indexOf("#")));
				String message = result.substring(result.indexOf("#") + 1,
						result.length());
				Message msg = myHandler.obtainMessage();
				msg.what = messageCode;
				msg.obj = message;
				myHandler.sendMessage(msg);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}

abstract class Filter {

	Filter next = null;

	public Filter getNext() {

		return next;

	}

	public void setNext(Filter next) {

		this.next = next;

	}

	public String doFilter(String pass1, String pass2) {

		if (next == null) {
			return "成功";
		} else
			return next.doFilter(pass1, pass2);

	}

}

class PasswordFilter2 extends Filter {

	public String doFilter(String pass1, String pass2) {

		if (!(pass1.equals(pass2))) {

			return "两次密码输入不一致";
		} else
			return super.doFilter(pass1, pass2);

	}

}

class PasswordFilter1 extends Filter {

	public String doFilter(String pass1, String pass2) {

		if (pass1.length() < 8) {

			return "密码长度必须大于8";
		} else
			return super.doFilter(pass1, pass2);

	}

}
