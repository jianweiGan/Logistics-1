package com.seeyuan.logistics.activity;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mesada.nj.pubcontrols.controls.RemoteImageView;
import com.seeyuan.logistics.R;
import com.seeyuan.logistics.application.ApplicationPool;
import com.seeyuan.logistics.application.ConstantPool;
import com.seeyuan.logistics.application.NetWork;
import com.seeyuan.logistics.customview.NumberImageView;
import com.seeyuan.logistics.datacenter.DataHandler;
import com.seeyuan.logistics.datacenter.OnDataReceiveListener;
import com.seeyuan.logistics.datahandler.GetCurrentOrderDetailHandler;
import com.seeyuan.logistics.datahandler.GetUserInfoHandler;
import com.seeyuan.logistics.entity.MemberDto;
import com.seeyuan.logistics.entity.PdaRequest;
import com.seeyuan.logistics.entity.PdaResponse;
import com.seeyuan.logistics.entity.StatisticInfoDto;
import com.seeyuan.logistics.entity.UserInfo;
import com.seeyuan.logistics.jsonparser.CurrentOrderMessageJsonParser;
import com.seeyuan.logistics.jsonparser.LoginJsonParser;
import com.seeyuan.logistics.provider.DBOperate;
import com.seeyuan.logistics.service.BDLocationService;
import com.seeyuan.logistics.service.CheckUpdateService;
import com.seeyuan.logistics.service.LoginIMServerService;
import com.seeyuan.logistics.util.CommonUtils;
import com.seeyuan.logistics.util.ToastUtil;

/**
 * 个人信息中心,个人车主
 * 
 * @author zhazhaobao
 * 
 */
public class PersonalInformationActivity extends BaseActivity implements
		OnClickListener, OnDataReceiveListener {

	private Context context;

	/**
	 * 返回按钮
	 */
	private ImageView maintitle_back_iv;

	/**
	 * 标题title
	 */
	private TextView defaulttitle_title_tv;

	/**
	 * 个人详细资料
	 */
	private RelativeLayout PersonalCenter_InfoManage;

	/**
	 * 充值中心
	 */
	private RelativeLayout PersonalCenter_AccountBalance_But;

	/**
	 * 设置
	 */
	private RelativeLayout PersonalCenter_Setting;

	/**
	 * 充值
	 */
	private RelativeLayout PersonalCenter_MyExpenseCalendar_But;

	/**
	 * 议价中
	 */
	private NumberImageView contingent_car_owner;
	/**
	 * 执行中
	 */
	private NumberImageView contingent_goods_owner;
	/**
	 * 待评价
	 */
	private NumberImageView contingent_evaluate_owner;

	/**
	 * 已完成
	 */
	private NumberImageView contingent_complete_owner;

	/**
	 * 用户信息
	 */
	private UserInfo mUserInfo;

	private DBOperate dbOperate;

	/**
	 * 用户头像
	 */
	private RemoteImageView user_photo;

	/**
	 * 用户名
	 */
	private TextView PersonalCenter_UserName_Text;

	/**
	 * 用户类型
	 */
	private TextView PersonalCenter_UserType;

	/**
	 * 星级
	 */
	private RatingBar rating;

	private SharedPreferences sPreferences;

	private final int REFRESH_HEADER = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_personal_information); // 软件activity的布局
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.deflaut_titlebar); // titlebar为自己标题栏的布局
		context = getApplicationContext();
		sPreferences = getSharedPreferences(ConstantPool.LOGISTICS_PREFERENCES,
				Context.MODE_PRIVATE);
		dbOperate = DBOperate.getInstance(context);
		mUserInfo = getUserInfo(mUserInfo);
		initView();
		CommonUtils.addActivity(this);
	}

	@Override
	public void initView() {
		maintitle_back_iv = (ImageView) findViewById(R.id.maintitle_back_iv);
		maintitle_back_iv.setOnClickListener(this);

		defaulttitle_title_tv = (TextView) findViewById(R.id.defaulttitle_title_tv);
		defaulttitle_title_tv.setText(R.string.tab_personal_information);

		PersonalCenter_InfoManage = (RelativeLayout) findViewById(R.id.PersonalCenter_InfoManage);
		PersonalCenter_InfoManage.setOnClickListener(this);

		// PersonalCenter_AccountBalance_But = (RelativeLayout)
		// findViewById(R.id.PersonalCenter_AccountBalance_But);
		// PersonalCenter_AccountBalance_But.setOnClickListener(this);

		// PersonalCenter_Setting = (RelativeLayout)
		// findViewById(R.id.PersonalCenter_Setting);
		// PersonalCenter_Setting.setOnClickListener(this);

		// PersonalCenter_MyExpenseCalendar_But = (RelativeLayout)
		// findViewById(R.id.PersonalCenter_MyExpenseCalendar_But);
		// PersonalCenter_MyExpenseCalendar_But.setOnClickListener(this);

		contingent_car_owner = (NumberImageView) findViewById(R.id.contingent_car_owner);
		contingent_car_owner.setBackground(R.drawable.bargain);//excute
		contingent_car_owner.setTitle("议价中");
		contingent_car_owner.setNumber("");
		contingent_car_owner.setOnClickListener(this);

		contingent_goods_owner = (NumberImageView) findViewById(R.id.contingent_goods_owner);
		contingent_goods_owner.setBackground(R.drawable.excute);//bargain
		contingent_goods_owner.setTitle("执行中");
		contingent_goods_owner.setNumber("");
		contingent_goods_owner.setOnClickListener(this);

		contingent_evaluate_owner = (NumberImageView) findViewById(R.id.contingent_evaluate_owner);
		contingent_evaluate_owner.setBackground(R.drawable.evaluate_icon);//
		contingent_evaluate_owner.setTitle("已完成");
		contingent_evaluate_owner.setNumber("");
		contingent_evaluate_owner.setOnClickListener(this);

		contingent_complete_owner = (NumberImageView) findViewById(R.id.contingent_complete_owner);
		contingent_complete_owner.setBackground(R.drawable.complete_icon);
		contingent_complete_owner.setTitle("已完成");
		contingent_complete_owner.setNumber("");
		contingent_complete_owner.setOnClickListener(this);

		user_photo = (RemoteImageView) findViewById(R.id.user_photo);
		user_photo.draw(mUserInfo.getFACE(), ConstantPool.DEFAULT_ICON_PATH,
				false, true);
		PersonalCenter_UserName_Text = (TextView) findViewById(R.id.PersonalCenter_UserName_Text);
		PersonalCenter_UserName_Text.setText(mUserInfo.getUSER_NAME());
		PersonalCenter_UserType = (TextView) findViewById(R.id.PersonalCenter_UserType);
		PersonalCenter_UserType.setText(CommonUtils.getUserType(mUserInfo
				.getMemberType()));
		rating = (RatingBar) findViewById(R.id.rating);
		// 后台数据库也没有星级属性
		// rating.setRating(mUserInfo.get)
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.maintitle_back_iv:
			Intent backIntent = new Intent(PersonalInformationActivity.this,
					MainActivity.class);
			startActivity(backIntent);
			CommonUtils.finishActivity(this);
			break;
		case R.id.PersonalCenter_InfoManage:
			Intent intent = new Intent(PersonalInformationActivity.this,
					PersonalOwnerInformationActivity.class);
			intent.putExtra("userInfo", mUserInfo);
			startActivityForResult(intent, REFRESH_HEADER);
			break;
		case R.id.contingent_car_owner:
		case R.id.contingent_goods_owner:
		case R.id.contingent_evaluate_owner:
		case R.id.contingent_complete_owner:
			doMyOrderByID(view.getId());
			break;

		default:
			break;
		}
	}

	/**
	 * 跳转到指定订单界面
	 * 
	 * @param id
	 */
	private void doMyOrderByID(int position) {
		Intent intent = new Intent(PersonalInformationActivity.this,
				MyOrderManagerActivity.class);
		intent.putExtra("position", position);
		startActivity(intent);
	}

	@Override
	public void onClickListener(View view) {
		switch (view.getId()) {
		case R.id.PersonalCenter_Certification:
			doCertification();
			break;
		case R.id.PersonalCenter_car_manage_But:
			doCarManager();
			break;
		case R.id.PersonalCenter_driver_manage_But:
			doDriverManager();
			break;
		case R.id.PersonalCenter_About:
			doAbout();
			break;
		case R.id.PersonalCenter_utils:
			doUtility();
			break;
		case R.id.PersonalCenter_phone_authentication:
			doPhoneAuthentication();
			break;
		case R.id.PersonalCenter_email_authentication:
			doEmailAuthentication();
			break;
		case R.id.PersonalCenter_Check_All_Order:
			doMyOrder();
			break;
		case R.id.PersonalCenter_Certification_Manager:
			doCertificationManager();
			break;
		case R.id.logout_submit:
			doLogout();
			break;
		case R.id.PersonalCenter_pay_manage_But:
			doPayManager();
			break;

		default:
			break;
		}
	}

	/**
	 * 支付管理
	 */
	private void doPayManager() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				PayManagerActivity.class);
		startActivity(intent);
	}

	/**
	 * 登出
	 */
	private void doLogout() {
		UserInfo userInfo = new UserInfo();
		userInfo.setIsLogin("N");
		userInfo.setUuId(CommonUtils.getUUID(context));
		dbOperate.updateUserInfoLoginType(userInfo);
		Intent intent = new Intent(PersonalInformationActivity.this,
				LoginActivity.class);
		startActivity(intent);
		stopService(new Intent(PersonalInformationActivity.this,
				BDLocationService.class));
		stopService(new Intent(PersonalInformationActivity.this,
				CheckUpdateService.class));
		stopService(new Intent(PersonalInformationActivity.this,
				LoginIMServerService.class));
		CommonUtils.finishAllActivity();
	}

	/**
	 * 认证管理
	 */
	private void doCertificationManager() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				CertificationManagerActivity.class);
		startActivity(intent);
	}

	/**
	 * 我的订单
	 */
	private void doMyOrder() {

		Intent intent = new Intent(PersonalInformationActivity.this,
				MyOrderManagerActivity.class);
		startActivity(intent);
	}

	/**
	 * 
	 * 邮箱认证
	 */
	private void doEmailAuthentication() {

		Intent intent = new Intent(PersonalInformationActivity.this,
				EmailAuthenticationActivity.class);
		startActivity(intent);

	}

	/**
	 * 
	 * 手机认证
	 */
	private void doPhoneAuthentication() {

		Intent intent = new Intent(PersonalInformationActivity.this,
				PhoneAuthenticationActivity.class);
		startActivity(intent);

	}

	/**
	 * 实用工具
	 */
	private void doUtility() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				UtilityActivity.class);
		startActivity(intent);
	}

	/**
	 * 关于
	 */
	private void doAbout() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				AboutActivity.class);
		startActivity(intent);
	}

	/**
	 * 我的专线
	 */
	private void doMyLine() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				MyLineSourceActivity.class);
		startActivity(intent);
	}

	/**
	 * 司机管理
	 */
	private void doDriverManager() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				DriverManagerActivity.class);
		startActivity(intent);
	}

	/**
	 * 车辆管理
	 */
	private void doCarManager() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				CarManagerActivity.class);
		startActivity(intent);
	}

	/**
	 * 实名认证
	 */
	private void doCertification() {
		Intent intent = new Intent(PersonalInformationActivity.this,
				CertificationActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new Thread(new Runnable() {

			@Override
			public void run() {
				getUserInfo();
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				getCurrentOrderDetail();
			}
		}).start();
	}

	private UserInfo getUserInfo(UserInfo userInfo) {
		try {
			userInfo = dbOperate
					.getUesrInfoByUUID(CommonUtils.getUUID(context));
		} catch (Exception e) {
		}
		return userInfo;
	}

	private void getUserInfo() {
		PdaRequest<MemberDto> request = new PdaRequest<MemberDto>();
		request.setData(new MemberDto());
		GetUserInfoHandler dataHandler = new GetUserInfoHandler(context,
				request);
		dataHandler.setOnDataReceiveListener(this);
		dataHandler.startNetWork();
	}

	private void getCurrentOrderDetail() {
		PdaRequest<String> request = new PdaRequest<String>();
		request.setData("获取订单数");
		GetCurrentOrderDetailHandler dataHandler = new GetCurrentOrderDetailHandler(
				context, request);
		dataHandler.setOnDataReceiveListener(this);
		dataHandler.startNetWork();
	}

	@Override
	public void onDataReceive(DataHandler dataHandler, int resultCode,
			Object data, int type) {
		switch (resultCode) {
		case NetWork.GET_USERINFO_OK:
			doGetUserInfoSuccess(data);
			break;
		case NetWork.GET_CURRENT_ORDER_MESSAGE_OK:
			doGetCurrentOrderMessageSuccess(data);
			break;
		case NetWork.GET_USERINFO_ERROR:
		case NetWork.GET_CURRENT_ORDER_MESSAGE_ERROR:
			break;
		default:
			break;
		}
	}

	private void doGetCurrentOrderMessageSuccess(Object data) {
		String dataString = null;
		try {
			dataString = new String((byte[]) data, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			PdaResponse<StatisticInfoDto> response = CurrentOrderMessageJsonParser
					.parserCurrentOrderMessageJson(dataString);
			if (null != response && response.isSuccess()) {// 登录成功
				StatisticInfoDto info = response.getData();
				contingent_goods_owner.setNumber(String.valueOf(info
						.getExecuteNum()));
				contingent_car_owner.setNumber(String.valueOf(info
						.getBargainNum()));
				contingent_complete_owner.setNumber(String.valueOf(info
						.getCompleteNum()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取用户信息成功
	 * 
	 * @param data
	 */
	private void doGetUserInfoSuccess(Object data) {
		String dataString = null;
		try {
			dataString = new String((byte[]) data, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			PdaResponse<MemberDto> response = LoginJsonParser
					.parserLoginJson(dataString);
			if (null != response && response.isSuccess()) {// 登录成功
				MemberDto result = response.getData();
				// 保存登录信息
				Editor editor = sPreferences.edit();
				editor.putString("uuId", result.getUuId());
				editor.putString("memberType", result.getMemberType()
						.toString());
				editor.commit();
				ApplicationPool.setUUID(result.getUuId());
				ApplicationPool
						.setMemberType(result.getMemberType().toString());
				UserInfo userInfo = new UserInfo();
				userInfo.setUuId(result.getUuId());
				userInfo.setUSER_NAME(result.getUserName());
				userInfo.setMemberType(result.getMemberType());
				// 密码使用明文保存
				userInfo.setPASSWORD(CommonUtils.getPassword(context));
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
				user_photo.draw(null == result.getFace() ? "" : result
						.getFace().getHeaderImgURL(),
						ConstantPool.DEFAULT_ICON_PATH, false, true);
				mUserInfo = userInfo;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 按下的如果是BACK，同时没有重复
			Intent backIntent = new Intent(PersonalInformationActivity.this,
					MainActivity.class);
			startActivity(backIntent);
			CommonUtils.finishActivity(this);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case REFRESH_HEADER:
			ToastUtil.show(context, "更新资料成功");
			mUserInfo = (UserInfo) data.getSerializableExtra("userInfo");
			if (null == mUserInfo)
				return;
			dbOperate.updateUserInfo(mUserInfo);
			user_photo.setImageBitmap(BitmapFactory.decodeFile(mUserInfo
					.getFACE_LOCATION_URL()));
			break;

		default:
			break;
		}

	}
}
