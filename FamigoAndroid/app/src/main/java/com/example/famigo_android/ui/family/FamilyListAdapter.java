package com.example.famigo_android.ui.family;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famigo_android.R;
import com.example.famigo_android.data.family.FamilyOut;

import java.util.List;

public class FamilyListAdapter extends RecyclerView.Adapter<FamilyListAdapter.FamilyViewHolder> {

    private List<FamilyOut> families;
    private OnFamilyClickListener listener;

    public interface OnFamilyClickListener {
        void onFamilyClick(FamilyOut family);
    }

    public FamilyListAdapter(List<FamilyOut> families, OnFamilyClickListener listener) {
        this.families = families;
        this.listener = listener;
    }

    public void setFamilies(List<FamilyOut> families) {
        this.families = families;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FamilyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_family, parent, false);
        return new FamilyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyViewHolder holder, int position) {
        FamilyOut family = families.get(position);
        
        String familyName = (family.name != null && !family.name.isEmpty())
                ? family.name
                : "Unnamed family";
        holder.familyName.setText(familyName);
        
        int memberCount = (family.members != null) ? family.members.size() : 0;
        holder.membersCount.setText(memberCount + " member" + (memberCount != 1 ? "s" : ""));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFamilyClick(family);
            }
        });
    }

    @Override
    public int getItemCount() {
        return families == null ? 0 : families.size();
    }

    static class FamilyViewHolder extends RecyclerView.ViewHolder {
        TextView familyName;
        TextView membersCount;

        FamilyViewHolder(View itemView) {
            super(itemView);
            familyName = itemView.findViewById(R.id.familyName);
            membersCount = itemView.findViewById(R.id.familyMembersCount);
        }
    }
}

