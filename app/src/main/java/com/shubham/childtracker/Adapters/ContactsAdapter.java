package com.shubham.childtracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnContactClickListener;
import com.shubham.childtracker.models.Contact;
import com.shubham.childtracker.utils.BackgroundGenerator;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsAdapterViewHolder> {
	private final Context context;
	private final ArrayList<Contact> contacts;
	private OnContactClickListener onContactClickListener;
	
	public ContactsAdapter(ArrayList<Contact> contacts, Context context) {
		this.contacts = contacts;
		this.context = context;
	}
	
	public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
		this.onContactClickListener = onContactClickListener;
	}
	
	@NonNull
	@Override
	public ContactsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_contact, viewGroup, false);
		return new ContactsAdapterViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ContactsAdapterViewHolder contactsAdapterViewHolder, int i) {
		Contact contact = contacts.get(i);
		contactsAdapterViewHolder.txtContactName.setText(contact.getContactName());
		contactsAdapterViewHolder.txtContactNumber.setText(contact.getContactNumber());
		contactsAdapterViewHolder.txtContactBackground.setText(BackgroundGenerator.getFirstCharacters(contact.getContactName()));
		contactsAdapterViewHolder.txtContactBackground.setBackground(BackgroundGenerator.getBackground(context));
	}
	
	@Override
	public int getItemCount() {
		return contacts.size();
		
	}
	
	public class ContactsAdapterViewHolder extends RecyclerView.ViewHolder {
		private final TextView txtContactName;
		private final TextView txtContactNumber;
		private final TextView txtContactBackground;
		
		public ContactsAdapterViewHolder(@NonNull View itemView) {
			super(itemView);
			txtContactName = itemView.findViewById(R.id.txtContactName);
			txtContactNumber = itemView.findViewById(R.id.txtContactNumber);
			txtContactBackground = itemView.findViewById(R.id.txtContactBackground);
			ImageButton imgBtnCall = itemView.findViewById(R.id.imgBtnCall);
			imgBtnCall.setOnClickListener(view -> onContactClickListener.onCallClick(contacts.get(getAdapterPosition()).getContactNumber()));

			ImageButton imgBtnMessage = itemView.findViewById(R.id.imgBtnMessage);
			imgBtnMessage.setOnClickListener(view -> onContactClickListener.onMessageClick(contacts.get(getAdapterPosition()).getContactNumber()));
			
		}
	}
	
	
}
