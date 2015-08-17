package com.seeyuan.logistics.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mesada.nj.pubcontrols.controls.RemoteImageView;
import com.seeyuan.logistics.R;
import com.seeyuan.logistics.application.ConstantPool;
import com.seeyuan.logistics.entity.GoodsDto;
import com.seeyuan.logistics.entity.OrderDto;
import com.seeyuan.logistics.util.CommonUtils;

public class OrderInfoAdapter extends BaseAdapter {

	private static final String ISCANCEL = "N";
	private Context context;
	private List<OrderDto> mDataList;
	private Handler myHandler;

	public OrderInfoAdapter(Context context, List<OrderDto> mDataList,
			Handler handler) {
		this.context = context;
		this.mDataList = mDataList;
		this.myHandler = handler;
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		if (mDataList.get(position).getIsCancel()!= null && mDataList.get(position).getIsCancel().equals("Y")) {
		
			return false;
		} 
		return true;
	}
	@Override
	public View getView(final int position, View convertView,
			ViewGroup viewGroup) {
		ViewHolder holder;
		final OrderDto orderDto = mDataList.get(position);
		if (null != mDataList) {

			if (null == convertView) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.item_order_info, null);
				holder.main1 = (RelativeLayout) convertView.findViewById(R.id.main1);
				holder.main2 = (RelativeLayout) convertView.findViewById(R.id.main2);
				holder.main3 = (LinearLayout) convertView.findViewById(R.id.main3);
				holder.orderID = (TextView) convertView
						.findViewById(R.id.order_id);
				holder.orderCurretType = (TextView) convertView
						.findViewById(R.id.order_current_type);
				holder.orderGoodsType = (TextView) convertView
						.findViewById(R.id.order_goods_type);
				holder.orderWeight = (TextView) convertView
						.findViewById(R.id.order_weight);
				holder.orderPrice = (TextView) convertView
						.findViewById(R.id.order_price);
				holder.orderFrom = (TextView) convertView
						.findViewById(R.id.order_from);
				holder.orderTo = (TextView) convertView
						.findViewById(R.id.order_to);
				holder.leftBtn = (Button) convertView
						.findViewById(R.id.order_btn_left);
				holder.rightBtn = (Button) convertView
						.findViewById(R.id.order_btn_right);
				holder.midBtn = (Button) convertView
						.findViewById(R.id.order_btn_mid);
				holder.orderGoodsList = (RemoteImageView) convertView
						.findViewById(R.id.order_goodslist);
				holder.orderPeceipt = (RemoteImageView) convertView
						.findViewById(R.id.order_peceipt);
				if (mDataList.get(position).getIsCancel()!= null && mDataList.get(position).getIsCancel().equals("Y")) {
					
					holder.main1.setBackgroundColor(Color.GRAY);
					holder.main2.setBackgroundColor(Color.GRAY);
					holder.main3.setVisibility(View.GONE);
				} 
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			
			GoodsDto goodsDto = orderDto.getGoods();
			holder.orderID
					.setText(TextUtils.isEmpty(orderDto.getOrderNo()) ? "未知"
							: String.format(
									context.getResources().getString(
											R.string.order_id_hint),
									orderDto.getOrderNo()));
			holder.orderCurretType.setText(TextUtils.isEmpty(orderDto
					.getOrderStatus()) ? "未知" : CommonUtils
					.getOrderType(Integer.parseInt(orderDto.getOrderStatus())));
			holder.orderGoodsType.setText(TextUtils.isEmpty(goodsDto
					.getGoodsName()) ? "未知" : goodsDto.getGoodsName());
			holder.orderWeight.setText(null == goodsDto.getGoodsWeight() ? "未知"
					: String.format(
							context.getResources().getString(
									R.string.weight_hint),
							goodsDto.getGoodsWeight()));
			holder.orderPrice.setText(null == orderDto.getOrderAmount() ? "未知"
					: String.format(
							context.getResources().getString(
									R.string.price_hint),
							orderDto.getOrderAmount()));
			holder.orderFrom
					.setText(TextUtils.isEmpty(goodsDto.getSetout()) ? "未知"
							: goodsDto.getSetout());
			holder.orderTo
					.setText(TextUtils.isEmpty(goodsDto.getDestination()) ? "未知"
							: goodsDto.getDestination());
			// holder.rightBtn.setUserInfo(orderDto);
			holder.rightBtn.setText(getShowText(orderDto , holder.rightBtn));
			holder.rightBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message msg = myHandler.obtainMessage();
					msg.what = v.getId();
					msg.obj = orderDto;
					msg.sendToTarget();
				}
			});
			holder.midBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message msg = myHandler.obtainMessage();
					msg.what = v.getId();
					msg.obj = orderDto;
					msg.sendToTarget();
				}
			});
			holder.leftBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message msg = myHandler.obtainMessage();
					msg.what = v.getId();
					msg.obj = orderDto;
					msg.sendToTarget();
				}
			});
			holder.orderGoodsList.draw(null == orderDto.getGoodsList() ? "未知"
					: orderDto.getGoodsList().getHeaderImgURL(),
					ConstantPool.DEFAULT_ICON_PATH, false, false);
			holder.orderGoodsList.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message msg = myHandler.obtainMessage();
					msg.what = v.getId();
					msg.obj = orderDto;
					msg.sendToTarget();
				}
			});
			holder.orderPeceipt.draw(null == orderDto.getPeceipt() ? "未知"
					: orderDto.getPeceipt().getHeaderImgURL(),
					ConstantPool.DEFAULT_ICON_PATH, false, false);
			holder.orderPeceipt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message msg = myHandler.obtainMessage();
					msg.what = v.getId();
					msg.obj = orderDto;
					msg.sendToTarget();
				}
			});
			setBtnVisible(mDataList.get(position), holder.leftBtn,
					holder.orderGoodsList, holder.orderPeceipt);
			setMidBtnVisible(holder.midBtn, mDataList.get(position));
		}

		return convertView;
	}

	public class ViewHolder {
		LinearLayout main3;
		RelativeLayout main1;
		RelativeLayout main2;
		private TextView orderID;
		private TextView orderCurretType;
		private TextView orderGoodsType;
		private TextView orderWeight;
		private TextView orderPrice;
		private TextView orderFrom;
		private TextView orderTo;
		private Button leftBtn;
		private Button rightBtn;
		private Button midBtn;
		private RemoteImageView orderGoodsList;
		private RemoteImageView orderPeceipt;
	}

	private void setBtnVisible(OrderDto orderDto, Button lefeButton,
			RemoteImageView goodsList, RemoteImageView peceipt) {
		int orderType = Integer.parseInt(orderDto.getOrderStatus());
		int memberType = Integer.parseInt(CommonUtils.getMemberType(context));
		switch (memberType) {
		case 2:// 个人车主
			setDriver(lefeButton, goodsList, peceipt, orderType);
			break;
		case 3:// 企业
		case 1:// 货主
			setGoods(lefeButton, goodsList, peceipt, orderType);
			break;

		default:
			break;
		}
	}

	/**
	 * 设置订单跟踪按钮显示状态
	 * 
	 * @param midButton
	 * @param orderDto
	 */
	private void setMidBtnVisible(Button midButton, OrderDto orderDto) {
		int orderType = Integer.parseInt(orderDto.getOrderStatus());
		switch (orderType) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			midButton.setVisibility(View.GONE);
			break;
		case 4:
			midButton.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	/**
	 * @param lefeButton
	 * @param goodsList
	 *            货物清单
	 * @param peceipt
	 *            回执
	 * @param orderType
	 *            订单状态
	 */
	private void setDriver(Button lefeButton, RemoteImageView goodsList,
			RemoteImageView peceipt, int orderType) {
		switch (orderType) {
		case 0:
		case 1:
			lefeButton.setVisibility(View.VISIBLE);
			break;
		case 3:
			lefeButton.setVisibility(View.GONE);
			goodsList.setVisibility(View.GONE);
			peceipt.setVisibility(View.GONE);
			break;
		case 4:
		case 5:
		case 6:
			lefeButton.setVisibility(View.GONE);
			goodsList.setVisibility(View.VISIBLE);
			peceipt.setVisibility(View.GONE);
			break;
		case 7:
		case 8:
		case 9:
			lefeButton.setVisibility(View.GONE);
			goodsList.setVisibility(View.VISIBLE);
			peceipt.setVisibility(View.VISIBLE);

			break;
		default:
			lefeButton.setVisibility(View.GONE);
			break;
		}
	}

	/**
	 * 
	 * @param lefeButton
	 * @param goodsList
	 *            货物清单
	 * @param peceipt
	 *            回执
	 * @param orderType
	 *            订单状态
	 */
	private void setGoods(Button lefeButton, RemoteImageView goodsList,
			RemoteImageView peceipt, int orderType) {
		switch (orderType) {
		case 0:
		case 2:
			lefeButton.setVisibility(View.VISIBLE);
			break;
		case 3:
			lefeButton.setVisibility(View.GONE);
			goodsList.setVisibility(View.GONE);
			peceipt.setVisibility(View.GONE);
			break;
		case 4:
		case 5:
		case 6:
			lefeButton.setVisibility(View.GONE);
			goodsList.setVisibility(View.VISIBLE);
			peceipt.setVisibility(View.GONE);
			break;
		case 7:
		case 8:
		case 9:
			lefeButton.setVisibility(View.GONE);
			goodsList.setVisibility(View.VISIBLE);
			peceipt.setVisibility(View.VISIBLE);
			break;
		default:
			lefeButton.setVisibility(View.GONE);
			break;
		}

	}

	private String getShowText(OrderDto orderDto ,Button button) {
		String result = "";
		int orderStatus = Integer.parseInt(TextUtils.isEmpty(orderDto
				.getOrderStatus()) ? "-1" : orderDto.getOrderStatus());
		int memberType = Integer.parseInt(CommonUtils.getMemberType(context));
		int payStatus = Integer.parseInt(TextUtils.isEmpty(orderDto
				.getPayStatus()) ? "0" : orderDto.getPayStatus());
		switch (memberType) {
		case 2:// 个人车主
			result = doDriverStatus(orderStatus,payStatus ,button);
			break;
		case 3:// 物流企业
		case 1:// 货主
			result = doGoodsStatus(orderStatus, payStatus);
			break;
		case 0:// 异常
			result = "未知";
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 个人车主，状态显示控制
	 * 
	 * @param orderStatus
	 */
	private String doDriverStatus(int orderStatus,int payStatus ,Button button) {
		String result = "";
		switch (orderStatus) {
		case 0:
			result = "报价";
			break;
		case 1:
			result = "改价";
			break;
		case 3:
			if (payStatus == 1 || payStatus == 2) {
				
				result = "发货";
				button.setVisibility(View.VISIBLE);
			} else {
				button.setVisibility(View.GONE);
			}
			break;
		case 4:
		case 5:
		case 6:
			// 此状态设置，是否需要先提示签收，然后点击签收，状态改变为上传回单，待定
			result = "上传回单";
			break;
		case 7:
		case 8:
		case 2:
		case 9:
			result = "查看订单";
			break;
		default:
			result = "异常";
			break;
		}
		return result;
	}

	/**
	 * 企业，货主，状态显示控制
	 * 
	 * @param orderStatus
	 */
	private String doGoodsStatus(int orderStatus, int payStatus) {
		String result = "";
		switch (orderStatus) {
		case 0:
			result = "报价";
			break;
		case 2:
			result = "改价";
			break;
		case 3:
			// result = "去付款";
			result = doPayStatus(payStatus);
			break;
		case 4:
		case 5:
		case 6:
			result = "查看订单";
			break;
		case 7:
			result = "回单确认";
			break;
		case 8:
			result = "查看订单";
			break;
		case 1:
		case 9:
			result = "查看订单";
			break;
		default:
			result = "异常";
			break;
		}
		return result;
	}

	/**
	 * 换算订单付款状态
	 * 
	 * @param payStatus
	 * @return
	 */
	private String doPayStatus(int payStatus) {
		// 0 未付款
		// 1 部分付款
		// 2 已付款
		// -1 申请退款
		// -2 已部分退款
		// -3 已退款
		String result = "";
		switch (payStatus) {
		case 0:
		case 1:
			result = "去付款";
			break;
		case 2:
			result = "查看订单";
			break;
		case -1:
		case -2:
			result = "申请退款";
			break;
		case -3:
			result = "查看订单";
			break;

		default:
			break;
		}
		return result;
	}
}