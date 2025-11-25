package com.example.famigo_android.ui.rewards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famigo_android.R;
import com.example.famigo_android.data.rewards.RewardOut;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    public interface OnRewardActionListener {
        void onRedeemClick(RewardOut reward);
    }

    private List<RewardOut> rewards;
    private final OnRewardActionListener listener;

    public RewardAdapter(List<RewardOut> rewards, OnRewardActionListener listener) {
        this.rewards = rewards;
        this.listener = listener;
    }

    public void setRewards(List<RewardOut> rewards) {
        this.rewards = rewards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        RewardOut r = rewards.get(position);
        holder.title.setText(r.title);
        holder.cost.setText(r.cost_points + " pts");

        holder.redeemBtn.setOnClickListener(v -> {
            if (listener != null) listener.onRedeemClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return rewards == null ? 0 : rewards.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView title, cost;
        Button redeemBtn;
        RewardViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rewardTitle);
            cost = itemView.findViewById(R.id.rewardCost);
            redeemBtn = itemView.findViewById(R.id.redeemBtn);
        }
    }
}
