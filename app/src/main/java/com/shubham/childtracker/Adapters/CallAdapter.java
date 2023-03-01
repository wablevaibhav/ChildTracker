package com.shubham.childtracker.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shubham.childtracker.R;
import com.shubham.childtracker.models.Call;
import com.shubham.childtracker.utils.BackgroundGenerator;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.DateUtils;

import java.util.ArrayList;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.CallAdapterViewHolder> {
	private static final String TAG = "CallAdapterTAG";
	private final Context context;
	private final ArrayList<Call> calls;

	public CallAdapter(Context context, ArrayList<Call> calls) {
		this.context = context;
		this.calls = calls;
	}
	
	@NonNull
	@Override
	public CallAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_call, viewGroup, false);
		return new CallAdapterViewHolder(view);
	}
	
	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull CallAdapterViewHolder callAdapterViewHolder, int i) {
		Call call = calls.get(i);
		callAdapterViewHolder.txtCallerPhoneNumber.setText(call.getPhoneNumber());
		callAdapterViewHolder.txtContactName.setText(call.getContactName());
		callAdapterViewHolder.txtCallTime.setText(DateUtils.getFormattedDate(call.getCallTime(), context));
		callAdapterViewHolder.txtCallDuration.setText(call.getCallDurationInSeconds() + "s");
		
		if (call.getCallType().equals(Constant.INCOMING_CALL)) {
			callAdapterViewHolder.imgCallType.setBackgroundResource(R.drawable.ic_call_received);
		} else {
			callAdapterViewHolder.imgCallType.setBackgroundResource(R.drawable.ic_call_made);
		}
		
		if (call.getContactName().equals(Constant.UNKNOWN_NUMBER)) {
			callAdapterViewHolder.txtCallBackground.setText("#");
			callAdapterViewHolder.txtCallBackground.setBackground(BackgroundGenerator.getBackground(context));
		} else {
			callAdapterViewHolder.txtCallBackground.setText(BackgroundGenerator.getFirstCharacters(call.getContactName()));
			callAdapterViewHolder.txtCallBackground.setBackground(BackgroundGenerator.getBackground(context));
		}
	}
	
	@Override
	public int getItemCount() {
		return calls.size();
	}
	
	public static class CallAdapterViewHolder extends RecyclerView.ViewHolder {
		private final ImageView imgCallType;
		private final TextView txtCallerPhoneNumber;
		private final TextView txtContactName;
		private final TextView txtCallTime;
		private final TextView txtCallDuration;
		private final TextView txtCallBackground;


		public CallAdapterViewHolder(@NonNull final View itemView) {
			super(itemView);
			imgCallType = itemView.findViewById(R.id.imgCallType);
			txtCallerPhoneNumber = itemView.findViewById(R.id.txtCallerPhoneNumber);
			txtContactName = itemView.findViewById(R.id.txtContactName);
			txtCallTime = itemView.findViewById(R.id.txtCallTime);
			txtCallDuration = itemView.findViewById(R.id.txtCallDuration);
			txtCallBackground = itemView.findViewById(R.id.txtCallBackground);
		}
	}


	
}
