package com.seeyuan.logistics.datahandler;

import java.util.List;

import android.content.Context;

import com.google.gson.Gson;
import com.seeyuan.logistics.application.NetWork;
import com.seeyuan.logistics.datacenter.DataHandler;
import com.seeyuan.logistics.entity.DriverDto;
import com.seeyuan.logistics.entity.PdaRequest;
import com.seeyuan.logistics.net.http.HttpAction;
import com.seeyuan.logistics.util.CommonUtils;

public class DeleteDriverInfoHandler extends DataHandler {
	private Context mContext;
	private String server_url;
	private PdaRequest<List<DriverDto>> driverInfo;

	public DeleteDriverInfoHandler(Context context,
			PdaRequest<List<DriverDto>> driverInfo) {
		this.mContext = context;
		this.server_url = NetWork.DELETE_DRIVER_INFO_ACTION;
		this.driverInfo = driverInfo;
	}

	public void startNetWork() {
		HttpAction httpAction = new HttpAction(HttpAction.REQUEST_TYPE_POST);
		httpAction.setUri(server_url);
		driverInfo.setUuId(CommonUtils.getUUID(mContext));
		driverInfo.setMemberType(CommonUtils.getMemberType(mContext));
		driverInfo.setOriginApp("ANDROID");
		String jsonString = new Gson().toJson(driverInfo);
		httpAction.addBodyParam("jsonString", jsonString);

		startNetwork(httpAction);
	}

	@Override
	protected void onNetReceiveOk(byte[] receiveBody) {
		sendResult(NetWork.DELETE_DRIVER_INFO_OK, receiveBody);
	}

	@Override
	protected void onNetReceiveError(int errorCode) {
		sendResult(NetWork.DELETE_DRIVER_INFO_ERROR, errorCode);
	}

	@Override
	protected void onNetReceiveTimeout(int errorCode) {
		sendResult(NetWork.DELETE_DRIVER_INFO_ERROR, errorCode);
	}

}