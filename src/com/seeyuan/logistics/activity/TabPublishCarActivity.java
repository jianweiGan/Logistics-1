package com.seeyuan.logistics.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.seeyuan.logistics.R;
import com.seeyuan.logistics.application.NetWork;
import com.seeyuan.logistics.customview.MuInputEditText;
import com.seeyuan.logistics.customview.ProgressAlertDialog;
import com.seeyuan.logistics.customview.SingleSelectAlertDlialog;
import com.seeyuan.logistics.datacenter.DataHandler;
import com.seeyuan.logistics.datacenter.OnDataReceiveListener;
import com.seeyuan.logistics.datahandler.PublishCarSourceHandler;
import com.seeyuan.logistics.entity.CarTypeInfo;
import com.seeyuan.logistics.entity.CarsDto;
import com.seeyuan.logistics.entity.DriverDto;
import com.seeyuan.logistics.entity.PdaRequest;
import com.seeyuan.logistics.entity.PdaResponse;
import com.seeyuan.logistics.entity.RouteDto;
import com.seeyuan.logistics.jsonparser.CarSourceJsonParser;
import com.seeyuan.logistics.jsonparser.ResultCodeJsonParser;
import com.seeyuan.logistics.util.CommonUtils;
import com.seeyuan.logistics.util.ToastUtil;

/**
 * 发布车源
 * 
 * @author zhazhaobao
 * 
 */
@SuppressLint("HandlerLeak")
public class TabPublishCarActivity extends BaseActivity implements
		OnClickListener, OnDataReceiveListener {

	/**
	 * 车牌号码
	 */
	private Button publishCar_CarNumber;

	/**
	 * 司机姓名
	 */
	private Button publish_car_driver;

	/**
	 * 始发地
	 */
	private Button publish_car_from;

	/**
	 * 目的地
	 */
	private Button publish_car_to;

	/**
	 * 路线状态
	 */
	private Button PublishCar_line_type;

	/**
	 * 有效期限
	 */
	private Button PublishCar_vaild_time;

	/**
	 * 途经地
	 */
	private Button PublishCar_through_to;

	/**
	 * 司机姓名，数据返回
	 */
	private final int REFRESH_DRIVER_NAME = 1000;

	/**
	 * 始发地，数据返回
	 */
	private final int REFRESH_CAR_FROM = 1001;

	/**
	 * 目的地，数据返回
	 */
	private final int REFRESH_CAR_TO = 1002;

	/**
	 * 路线状态，数据返回
	 */
	private final int REFRESH_LINE_TYPE = 1003;

	/**
	 * 有效期限，数据返回
	 */
	private final int REFRESH_VALID_TIME = 1004;

	/**
	 * 途经地，数据返回
	 */
	private final int REFRESH_THROUGH_TO = 1005;

	private Context context;

	private DriverDto driverDto;

	private RouteDto mRouteDto = new RouteDto();

	private static final int SHOW_DATAPICK = 0;
	private static final int DATE_DIALOG_ID = 1;
	private static final int SHOW_TIMEPICK = 2;
	private static final int TIME_DIALOG_ID = 3;

	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;

	private String currentTime;
	private String selectTime;

	/**
	 * 显示进度条
	 */
	private final int SHOW_PROGRESS = 2000;
	/**
	 * 关闭进度条
	 */
	private final int CLOSE_PROGRESS = 2001;
	
	private final int SHOW_TOAST = 2002;

	private ProgressAlertDialog progressDialog;

	/**
	 * 单价:元/吨
	 */
	private MuInputEditText publish_car_pricet;

	/**
	 * 单价:元/方
	 */
	private MuInputEditText publish_car_pricem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_publish_car); // 软件activity的布局
		context = getApplicationContext();
		initView();
		setDateTime();
		setTimeOfDay();
	}

	@Override
	public void initView() {
		publishCar_CarNumber = (Button) findViewById(R.id.PublishCar_CarNumber);
		publish_car_driver = (Button) findViewById(R.id.publish_car_driver);
		publish_car_from = (Button) findViewById(R.id.publish_car_from);
		publish_car_to = (Button) findViewById(R.id.publish_car_to);
		PublishCar_line_type = (Button) findViewById(R.id.PublishCar_line_type);
		PublishCar_vaild_time = (Button) findViewById(R.id.PublishCar_vaild_time);
		PublishCar_through_to = (Button) findViewById(R.id.PublishCar_through_to);
		publish_car_pricet = (MuInputEditText) findViewById(R.id.publish_car_pricet);
		publish_car_pricem = (MuInputEditText) findViewById(R.id.publish_car_pricem);

	}

	private Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_DRIVER_NAME:
				publish_car_driver.setText(msg.obj.toString());
				break;
			case REFRESH_CAR_FROM:
				publish_car_from.setText(msg.obj.toString() + "米");
				break;
			case REFRESH_CAR_TO:
				publish_car_to.setText(msg.obj.toString());
				break;
			case REFRESH_LINE_TYPE:
				String lineType = msg.obj.toString();
				if (lineType.equalsIgnoreCase("常跑线路")) {
					PublishCar_vaild_time.setText("不限期");
					PublishCar_vaild_time.setEnabled(false);
				} else {
					PublishCar_vaild_time.setText("");
					PublishCar_vaild_time.setEnabled(true);
				}
				PublishCar_line_type.setText(lineType);
				break;
			case SHOW_PROGRESS:
				showProgress();
				break;
			case CLOSE_PROGRESS:
				dismissProgress();
				break;
			case SHOW_TOAST:
				ToastUtil.show(context, msg.obj.toString());
				break;

			default:
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
		case R.id.PublishCar_CarNumber:
//			doSelectCarNumber();
			Intent intent = new Intent(this, SelectCarActivity.class);
			intent.putExtra("isLogin", false);
			startActivityForResult(intent, 1);
			break;
		case R.id.publish_car_driver:
			doSearchCarDriver();
			break;
		case R.id.publish_car_from:
			doSearchCarFrom();
			break;
		case R.id.publish_car_to:
			doSearchCarTo();
			break;
		case R.id.PublishCar_line_type:
			doSelectCarLineType();
			break;
		case R.id.PublishCar_vaild_time:
			doSelectDayData();
			break;
		case R.id.PublishCar_ensure:
			String result = isCanSubmitCarSource();
			if (result.equalsIgnoreCase("成功")) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						doSubmitPublishCar();
					}
				}).start();
			} else {
				ToastUtil.show(context, result);
			}
			break;
		case R.id.PublishCar_cancel:
			CommonUtils.finishActivity(getParent());
			break;
		case R.id.PublishCar_through_to:
			doThroughTo();
			break;

		default:
			break;
		}
	}

	/**
	 * 途经地
	 */
	private void doThroughTo() {
		Intent intent = new Intent(TabPublishCarActivity.this,
				SearchCityActivity.class);
		intent.putExtra("isCanMultipleChoice", true);
		intent.putExtra("mulitpleChoiceMaxNum", 7);
		startActivityForResult(intent, REFRESH_THROUGH_TO);
	}

	/**
	 * 日期选择
	 */
	private void doSelectDayData() {
		Message msg = new Message();
		msg.what = SHOW_DATAPICK;
		dateandtimeHandler.sendMessage(msg);
	}

	/**
	 * 提交，发布车源
	 */
	private void doSubmitPublishCar() {
		myHandler.sendEmptyMessage(SHOW_PROGRESS);
		mRouteDto.setVehicleNum(publishCar_CarNumber.getText().toString());
		mRouteDto.setDriverId(driverDto.getDriverId());
		mRouteDto.setDriverName(driverDto.getDriverName());
		mRouteDto.setSetout(publish_car_from.getText().toString());
		mRouteDto.setDestination(publish_car_to.getText().toString());
		mRouteDto
				.setType(getLineType(PublishCar_line_type.getText().toString()));
		mRouteDto.setValidDeadline(PublishCar_vaild_time.getText().toString()
				.equalsIgnoreCase("不限期") ? null : CommonUtils
				.parserTimestamp(PublishCar_vaild_time.getText().toString()));
		mRouteDto.setPath(PublishCar_through_to.getText().toString());
		mRouteDto.setPriceM(TextUtils.isEmpty(publish_car_pricem.getText()
				.toString()) ? 0 : Integer.parseInt(publish_car_pricem
				.getText().toString()));
		mRouteDto.setPriceT(TextUtils.isEmpty(publish_car_pricet.getText()
				.toString()) ? 0 : Integer.parseInt(publish_car_pricet
				.getText().toString()));
		PdaRequest<RouteDto> request = new PdaRequest<RouteDto>();
		request.setData(mRouteDto);
		PublishCarSourceHandler dataHandler = new PublishCarSourceHandler(
				context, request);
		dataHandler.setOnDataReceiveListener(this);
		dataHandler.startNetWork();
	}

	private String getLineType(String lineType) {
		if (lineType.equalsIgnoreCase("常跑线路")) {
			return "2";
		} else if (lineType.equalsIgnoreCase("即时空车")) {
			return "1";
		}
		return "0";
	}

	private String isCanSubmitCarSource() {

		Filter carPlate = new CarPlateFilter1();
		Filter driver = new DriverFilter1();
		Filter from = new FromFilter1();
		Filter to = new ToFilter1();
		Filter lineType = new LineTypeFilter1();
		Filter validTime = new ValidTimeFilter1();

		carPlate.setNext(driver);
		driver.setNext(from);
		from.setNext(to);
		to.setNext(lineType);
		lineType.setNext(validTime);

		String result = carPlate.doFilter(publishCar_CarNumber.getText()
				.toString(), publish_car_driver.getText().toString(),
				publish_car_from.getText().toString(), publish_car_to.getText()
						.toString(), PublishCar_line_type.getText().toString(),
				PublishCar_vaild_time.getText().toString());
		return result;
	}

	abstract class Filter {

		Filter next = null;

		public Filter getNext() {

			return next;

		}

		public void setNext(Filter next) {

			this.next = next;

		}

		public String doFilter(String carPlate, String driver, String from,
				String to, String lineType, String validTime) {

			if (next == null) {
				return "成功";
			} else
				return next.doFilter(carPlate, driver, from, to, lineType,
						validTime);

		}

	}

	class CarPlateFilter1 extends Filter {

		public String doFilter(String carPlate, String driver, String from,
				String to, String lineType, String validTime) {

			if (TextUtils.isEmpty(carPlate)
				/*	|| !CommonUtils.isVehiclePlate(carPlate)*/) {
				return "请输入正确的车牌号码";
			} else
				return super.doFilter(carPlate, driver, from, to, lineType,
						validTime);
		}
	}

	class DriverFilter1 extends Filter {

		public String doFilter(String carPlate, String driver, String from,
				String to, String lineType, String validTime) {

			if (TextUtils.isEmpty(driver)) {
				return "请输入正确的司机信息";
			} else
				return super.doFilter(carPlate, driver, from, to, lineType,
						validTime);
		}
	}

	class FromFilter1 extends Filter {

		public String doFilter(String carPlate, String driver, String from,
				String to, String lineType, String validTime) {

			if (TextUtils.isEmpty(from)) {
				return "请输入正确的始发地";
			} else
				return super.doFilter(carPlate, driver, from, to, lineType,
						validTime);
		}
	}

	class ToFilter1 extends Filter {

		public String doFilter(String carPlate, String driver, String from,
				String to, String lineType, String validTime) {

			if (TextUtils.isEmpty(to)) {
				return "请输入正确的目的地";
			} else
				return super.doFilter(carPlate, driver, from, to, lineType,
						validTime);
		}
	}

	class LineTypeFilter1 extends Filter {

		public String doFilter(String carPlate, String driver, String from,
				String to, String lineType, String validTime) {

			if (TextUtils.isEmpty(lineType)) {
				return "请输入正确的路线状态";
			} else
				return super.doFilter(carPlate, driver, from, to, lineType,
						validTime);
		}
	}

	class ValidTimeFilter1 extends Filter {

		public String doFilter(String carPlate, String driver, String from,
				String to, String lineType, String validTime) {

			if (TextUtils.isEmpty(validTime)) {
				return "请输入正确的有效期限";
			} else
				return super.doFilter(carPlate, driver, from, to, lineType,
						validTime);
		}
	}

	/**
	 * 路线状态
	 */
	private void doSelectCarLineType() {
		CommonUtils.selectCarInfo(TabPublishCarActivity.this, myHandler,
				REFRESH_LINE_TYPE, R.array.line_type, R.string.line_type_hint2);
	}

	@Override
	public void onClick(View view) {

	}

	/**
	 * 车型选择
	 */
	private void doSelectCarNumber() {

		List<CarTypeInfo> mDataList = new ArrayList<CarTypeInfo>();
		TypedArray typedArray = getResources().obtainTypedArray(
				R.array.CarType_Str);
		int size = typedArray.length();
		for (int i = 0; i < size; i++) {
			CarTypeInfo indexInfo = new CarTypeInfo();
			indexInfo.setCar_type(typedArray.getString(i));
			mDataList.add(indexInfo);
		}

		final SingleSelectAlertDlialog ad = new SingleSelectAlertDlialog(
				TabPublishCarActivity.this);
		ad.setTitle(getResources()
				.getString(R.string.PublishInfo_CarTypes_Hint));
		ad.setListContentForCarType(mDataList);
		ad.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				if (null == view)
					return;
				ad.dismiss();
				Message message = myHandler.obtainMessage();
				message.what = REFRESH_DRIVER_NAME;
				message.obj = ((TextView) view.findViewById(R.id.item_car_type))
						.getText();
				myHandler.sendMessage(message);

			}
		});
		typedArray.recycle();
	}

	/**
	 * 司机选择
	 */
	private void doSearchCarDriver() {
		Intent intent = new Intent(TabPublishCarActivity.this,
				SelectDriverManagerActivity.class);
		startActivityForResult(intent, REFRESH_DRIVER_NAME);
	}

	/**
	 * 始发地
	 */
	private void doSearchCarFrom() {
		Intent intent = new Intent(TabPublishCarActivity.this,
				SearchCityActivity.class);
		startActivityForResult(intent, REFRESH_CAR_FROM);

	}

	/**
	 * 目的地
	 */
	private void doSearchCarTo() {
		Intent intent = new Intent(TabPublishCarActivity.this,
				SearchCityActivity.class);
		startActivityForResult(intent, REFRESH_CAR_TO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case REFRESH_CAR_FROM:
			publish_car_from.setText(data.getStringExtra("place"));
			break;
		case REFRESH_CAR_TO:
			publish_car_to.setText(data.getStringExtra("place"));
			break;
		case REFRESH_DRIVER_NAME:
			driverDto = (DriverDto) data.getSerializableExtra("driverInfo");
			publish_car_driver.setText(driverDto.getDriverName());
			break;
		case REFRESH_THROUGH_TO:
			PublishCar_through_to.setText(data.getStringExtra("place"));
			mRouteDto.setPath(data.getStringExtra("place"));
			break;
		case 1:
			publishCar_CarNumber.setText(data.getExtras().getString("carNum"));
			break;
			
		default:
			break;
		}
	}

	@Override
	public void onDataReceive(DataHandler dataHandler, int resultCode,
			Object data, int type) {
		myHandler.sendEmptyMessage(CLOSE_PROGRESS);
		switch (resultCode) {
		case NetWork.PUBLISH_CAR_SOURCE_OK:
			doPublishCarSuccess(data);
			break;
		case NetWork.PUBLISH_CAR_SOURCE_ERROR:
			ToastUtil.show(context,
					getResources().getString(R.string.network_error_hint));
			break;

		default:
			break;
		}
	}

	private void doPublishCarSuccess(Object data) {
		String dataString = null;
		try {
			dataString = new String((byte[]) data, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			PdaResponse<String> response = ResultCodeJsonParser
					.parserResultCodeJson(dataString);
			String result = response.getMessage();
			int messageCode = Integer.parseInt(result.substring(0,
					result.indexOf("#")));
			String message = "";
			message = result.substring(result.indexOf("#") + 1,
					result.length());
			if (!response.isSuccess()) {
				Message msg = myHandler.obtainMessage();
				msg.what = SHOW_TOAST;
				msg.obj = message;
				myHandler.sendMessage(msg);
				return;
			} else {
				ToastUtil.show(context, "发布车源成功");
				Intent intent = new Intent(this,
						CarManagerActivity.class);
				intent.putExtra("isNomalGetIn", false);
				startActivity(intent);
				CommonUtils.finishActivity(this);
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理日期和时间控件的Handler
	 */
	Handler dateandtimeHandler = new Handler() {

		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_DATAPICK:
				showDialog(DATE_DIALOG_ID);
				break;
			case SHOW_TIMEPICK:
				showDialog(TIME_DIALOG_ID);
				break;
			}
		}

	};

	/**
	 * 设置时间
	 */
	private void setTimeOfDay() {
		final Calendar c = Calendar.getInstance();
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
		// updateTimeDisplay();
	}

	/**
	 * 更新时间显示
	 */
	private void updateTimeDisplay() {
		selectTime = new StringBuilder().append(mYear).append("-")
				.append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1))
				.append("-").append((mDay < 10) ? "0" + mDay : mDay)
				.append("   ").append(mHour).append(":")
				.append((mMinute < 10) ? "0" + mMinute : mMinute).toString();
		if (CommonUtils.compare_date(currentTime, selectTime)) {
			ToastUtil.show(context, "请选择正确的时间");
		} else {
			PublishCar_vaild_time.setText(selectTime);
		}

	}

	/**
	 * 设置日期
	 */
	private void setDateTime() {
		final Calendar c = Calendar.getInstance();

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		currentTime = new StringBuilder().append(mYear).append("-")
				.append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1))
				.append("-").append((mDay < 10) ? "0" + mDay : mDay).toString();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute,
					true);
		}

		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
			break;
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mHour, mMinute);
			break;
		}
	}

	/**
	 * 日期控件的事件
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;

			updateDateDisplay();
			// doSelectTimeData();
		}
	};
	/**
	 * 时间控件事件
	 */
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;

			updateTimeDisplay();
		}
	};

	

	/**
	 * 更新日期显示
	 */
	private void updateDateDisplay() {
		selectTime = new StringBuilder().append(mYear).append("-")
				.append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1))
				.append("-").append((mDay < 10) ? "0" + mDay : mDay).toString();
		if (CommonUtils.compare_date(currentTime, selectTime)) {
			ToastUtil.show(context, "请选择正确的时间");
		} else {
			PublishCar_vaild_time.setText(selectTime);
		}

	}

	/**
	 * 时间选择
	 */
	private void doSelectTimeData() {
		Message msg = new Message();
		msg.what = SHOW_TIMEPICK;
		dateandtimeHandler.sendMessage(msg);
	}

}
