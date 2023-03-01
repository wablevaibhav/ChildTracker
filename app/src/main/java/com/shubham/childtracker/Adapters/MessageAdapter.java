package com.shubham.childtracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shubham.childtracker.R;
import com.shubham.childtracker.models.Message;
import com.shubham.childtracker.utils.BackgroundGenerator;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.DateUtils;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageAdapterViewHolder> {
	private final Context context;
	private final ArrayList<Message> messages;

	public MessageAdapter(Context context, ArrayList<Message> messages) {
		this.context = context;
		this.messages = messages;
	}
	
	@NonNull
	@Override
	public MessageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_message, viewGroup, false);
		return new MessageAdapterViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MessageAdapterViewHolder messageAdapterViewHolder, int i) {
		Message message = messages.get(i);
		messageAdapterViewHolder.txtSenderPhoneNumber.setText(message.getSenderPhoneNumber());
		messageAdapterViewHolder.txtMessageBody.setText(message.getMessageBody());
		messageAdapterViewHolder.txtTimeReceived.setText(DateUtils.getFormattedDate(message.getTimeReceived(), context));
		messageAdapterViewHolder.txtSenderName.setText(message.getContactName());
		if (message.getContactName().equals(Constant.UNKNOWN_NUMBER)) {
			messageAdapterViewHolder.txtMessageBackground.setText("#");
			messageAdapterViewHolder.txtMessageBackground.setBackground(BackgroundGenerator.getBackground(context));
		} else {
			messageAdapterViewHolder.txtMessageBackground.setText(BackgroundGenerator.getFirstCharacters(message.getContactName()));
			messageAdapterViewHolder.txtMessageBackground.setBackground(BackgroundGenerator.getBackground(context));
		}
		
	}
	
	@Override
	public int getItemCount() {
		return messages.size();
	}
	
	public static class MessageAdapterViewHolder extends RecyclerView.ViewHolder {
		private final TextView txtSenderPhoneNumber;
		private final TextView txtMessageBody;
		private final TextView txtTimeReceived;
		private final TextView txtSenderName;
		private final TextView txtMessageBackground;
		
		
		public MessageAdapterViewHolder(@NonNull View itemView) {
			super(itemView);
			txtSenderPhoneNumber = itemView.findViewById(R.id.txtSenderPhoneNumber);
			txtMessageBody = itemView.findViewById(R.id.txtMessageBody);
			txtTimeReceived = itemView.findViewById(R.id.txtTimeReceived);
			txtSenderName = itemView.findViewById(R.id.txtSenderName);
			txtMessageBackground = itemView.findViewById(R.id.txtMessageBackground);
			
		}
	}
	
}
