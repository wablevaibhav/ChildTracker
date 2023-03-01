package com.shubham.childtracker.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shubham.childtracker.R;
import com.shubham.childtracker.interfaces.OnChildClickListener;
import com.shubham.childtracker.models.Child;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildAdapterViewHolder> {
	private final Context context;
	private final ArrayList<Child> childs;
	private OnChildClickListener onChildClickListener;
	
	
	public ChildAdapter(Context context, ArrayList<Child> childs) {
		this.context = context;
		this.childs = childs;
	}
	
	public void setOnChildClickListener(OnChildClickListener listener) {
		this.onChildClickListener = listener;
	}
	
	@NonNull
	@Override
	public ChildAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_child, viewGroup, false);
		return new ChildAdapterViewHolder(view);
	}
	
	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull final ChildAdapterViewHolder childAdapterViewHolder, int i) {
		Child child = childs.get(i);
		childAdapterViewHolder.txtChildName.setText(child.getName());
		
		if (child.getScreenLock() != null) {
			childAdapterViewHolder.switchLockPhone.setChecked(child.getScreenLock().isLocked());
		}
		Picasso.get().load(child.getProfileImage()).placeholder(R.drawable.ic_profile_image).error(R.drawable.ic_profile_image).into(childAdapterViewHolder.imgChild);
		if (child.isAppDeleted()) {
			childAdapterViewHolder.layoutDeletedApp.setVisibility(View.VISIBLE);
			childAdapterViewHolder.txtDeletedApp.setText(child.getName() + " " + context.getResources().getString(R.string.deleted_the_app));
			childAdapterViewHolder.imgChild.setEnabled(false);
			childAdapterViewHolder.txtChildName.setEnabled(false);
			childAdapterViewHolder.switchLockPhone.setEnabled(false);
			childAdapterViewHolder.switchLockPhone.setClickable(false);
		}
	}
	
	@Override
	public int getItemCount() {
		return childs.size();
	}
	
	public class ChildAdapterViewHolder extends RecyclerView.ViewHolder {
		private final CircleImageView imgChild;
		private final TextView txtChildName;
		@SuppressLint("UseSwitchCompatOrMaterialCode")
		private final Switch switchLockPhone;
		private final LinearLayout layoutDeletedApp;
		private final TextView txtDeletedApp;
		
		public ChildAdapterViewHolder(@NonNull View itemView) {
			super(itemView);
			imgChild = itemView.findViewById(R.id.imgChild);
			txtChildName = itemView.findViewById(R.id.txtChildName);
			@SuppressLint("UseSwitchCompatOrMaterialCode")
			Switch switchWebFilter = itemView.findViewById(R.id.switchWebFilter);
			switchWebFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
				if (buttonView.isPressed()) {
					int position = getAdapterPosition();
					onChildClickListener.onWebFilterClick(isChecked, childs.get(position));
				}

			});
			
			itemView.setOnClickListener(v -> {
				if (onChildClickListener != null) {
					int position = getAdapterPosition();
					if (position != RecyclerView.NO_POSITION)
						onChildClickListener.onItemClick(position);
				}
			});
			
			switchLockPhone = itemView.findViewById(R.id.switchLockPhone);
			switchLockPhone.setOnCheckedChangeListener((buttonView, isChecked) -> {
				if (buttonView.isPressed()) {
					int position = getAdapterPosition();
					onChildClickListener.onBtnLockClick(isChecked, childs.get(position));
				}
			});
			
			layoutDeletedApp = itemView.findViewById(R.id.layoutDeletedApp);
			layoutDeletedApp.setVisibility(View.GONE);
			txtDeletedApp = itemView.findViewById(R.id.txtDeletedApp);
			
		}
	}
	
	
}
