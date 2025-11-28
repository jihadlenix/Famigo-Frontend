package com.example.famigo_android.ui.family;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.famigo_android.R;
import com.example.famigo_android.data.family.FamilyMemberOut;
import com.example.famigo_android.data.network.ApiClient;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    public interface OnMemberClickListener {
        void onMemberClick(FamilyMemberOut member);
    }

    private List<FamilyMemberOut> members;
    private OnMemberClickListener listener;
    private boolean isParent;  // Whether current user is a parent

    public MemberAdapter(List<FamilyMemberOut> members) {
        this.members = members;
        this.isParent = false;
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void setIsParent(boolean isParent) {
        this.isParent = isParent;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        FamilyMemberOut member = members.get(position);
        
        // Determine primary display name: prefer username with @, then display_name, then full_name, then email, then role
        String primaryName = "Member";
        if (member.username != null && !member.username.trim().isEmpty()) {
            primaryName = "@" + member.username.trim();
        } else if (member.display_name != null && !member.display_name.trim().isEmpty()) {
            primaryName = member.display_name.trim();
        } else if (member.full_name != null && !member.full_name.trim().isEmpty()) {
            primaryName = member.full_name.trim();
        } else if (member.email != null && !member.email.trim().isEmpty()) {
            primaryName = member.email.trim();
        } else if (member.role != null && !member.role.isEmpty()) {
            primaryName = member.role.substring(0, 1).toUpperCase() + member.role.substring(1).toLowerCase();
        }
        
        holder.memberName.setText(primaryName);
        
        // Show role as secondary text
        String role = (member.role != null && !member.role.isEmpty())
                ? member.role.substring(0, 1).toUpperCase() + member.role.substring(1).toLowerCase()
                : "Member";
        holder.memberRole.setText(role);
        
        // Set coins/points
        int coins = member.wallet_balance;
        holder.memberCoins.setText(coins + " pts");
        
        // Determine initial for avatar: prefer display_name, then full_name, then username, then email, then role
        String initial = "?";
        if (member.display_name != null && !member.display_name.trim().isEmpty()) {
            // Use first letter of display_name
            initial = member.display_name.trim().substring(0, 1).toUpperCase();
        } else if (member.full_name != null && !member.full_name.trim().isEmpty()) {
            // Use first letter of full_name
            initial = member.full_name.trim().substring(0, 1).toUpperCase();
        } else if (member.username != null && !member.username.trim().isEmpty()) {
            // Use first letter of username
            initial = member.username.trim().substring(0, 1).toUpperCase();
        } else if (member.email != null && !member.email.trim().isEmpty()) {
            // Use first letter of email
            initial = member.email.trim().substring(0, 1).toUpperCase();
        } else if (member.role != null && !member.role.isEmpty()) {
            // Fallback to role (but this shouldn't happen often)
            initial = member.role.substring(0, 1).toUpperCase();
        }
        
        // Load profile picture if available, otherwise show initial
        if (member.profile_pic != null && !member.profile_pic.isEmpty()) {
            String baseUrl = ApiClient.getBaseUrl();
            String imageUrl = baseUrl + "static/" + member.profile_pic;
            
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.color.famigo_green)
                    .error(R.color.famigo_green)
                    .circleCrop()
                    .into(holder.avatarImage);
            
            holder.avatarImage.setVisibility(View.VISIBLE);
            holder.avatarInitial.setVisibility(View.GONE);
        } else {
            // Show avatar initial
            holder.avatarInitial.setText(initial);
            holder.avatarImage.setVisibility(View.GONE);
            holder.avatarInitial.setVisibility(View.VISIBLE);
        }

        // Enable click if parent viewing a child
        boolean isClickable = isParent && "CHILD".equalsIgnoreCase(member.role);
        holder.itemView.setClickable(isClickable);
        holder.itemView.setFocusable(isClickable);
        holder.itemView.setAlpha(isClickable ? 1.0f : 1.0f);
        
        holder.itemView.setOnClickListener(v -> {
            if (isClickable && listener != null) {
                listener.onMemberClick(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members == null ? 0 : members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;
        TextView memberRole;
        TextView memberCoins;
        ImageView avatarImage;
        TextView avatarInitial;

        MemberViewHolder(View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberName);
            memberRole = itemView.findViewById(R.id.memberRole);
            memberCoins = itemView.findViewById(R.id.memberCoins);
            avatarImage = itemView.findViewById(R.id.avatarImage);
            avatarInitial = itemView.findViewById(R.id.avatarInitial);
        }
    }
}

