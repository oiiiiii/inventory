package com.example.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 物品列表适配器
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private Context mContext;
    private List<Item> mItemList;
    private OnItemClickListener mOnItemClickListener;

    // 日期格式化器（解析有效期）
    private SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

    // 临期天数阈值（7天内算临期）
    private static final int NEAR_EXPIRE_DAYS = 7;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemAdapter(Context context, List<Item> itemList, OnItemClickListener listener) {
        this.mContext = context;
        this.mItemList = itemList;
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = mItemList.get(position);
        if (item == null) return;

        // 绑定基础数据
        holder.tvItemName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory() == null || item.getCategory().isEmpty() ? "未分类" : item.getCategory());
        holder.tvLocation.setText(item.getLocation() == null || item.getLocation().isEmpty() ? "未指定" : item.getLocation());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvCreateTime.setText(item.getCreateTime());
        holder.tvUpdateTime.setText(item.getUpdateTime());

        // 处理有效期显示和临期提醒
        String expiryDate = item.getExpiryDate();
        if (expiryDate == null || expiryDate.isEmpty()) {
            holder.tvExpiryDate.setText("无");
            holder.tvExpireTip.setVisibility(View.GONE);
        } else {
            try {
                // 格式化有效期显示（yyyy-MM-dd → yyyy.MM.dd）
                Date expiryDateObj = expiryFormat.parse(expiryDate);
                holder.tvExpiryDate.setText(displayFormat.format(expiryDateObj));

                // 判断是否临期/过期
                Calendar today = Calendar.getInstance();
                Calendar expiryCal = Calendar.getInstance();
                expiryCal.setTime(expiryDateObj);

                long diff = expiryCal.getTimeInMillis() - today.getTimeInMillis();
                long diffDays = diff / (1000 * 60 * 60 * 24);

                if (diffDays < 0) {
                    // 过期
                    holder.tvExpireTip.setText("过期");
                    holder.tvExpireTip.setBackgroundColor(mContext.getResources().getColor(R.color.warning_expired));
                    holder.tvExpireTip.setVisibility(View.VISIBLE);
                } else if (diffDays <= NEAR_EXPIRE_DAYS) {
                    // 临期
                    holder.tvExpireTip.setText("临期");
                    holder.tvExpireTip.setBackgroundColor(mContext.getResources().getColor(R.color.warning_near_expire));
                    holder.tvExpireTip.setVisibility(View.VISIBLE);
                } else {
                    // 正常
                    holder.tvExpireTip.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                holder.tvExpiryDate.setText(expiryDate);
                holder.tvExpireTip.setVisibility(View.GONE);
            }
        }

        // 条目点击事件
        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    // 更新列表数据
    public void updateData(List<Item> newList) {
        this.mItemList = newList;
        notifyDataSetChanged();
    }

    // 追加列表数据（分页加载）
    public void addData(List<Item> newData) {
        int startPos = mItemList.size();
        mItemList.addAll(newData);
        notifyItemRangeInserted(startPos, newData.size());
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName;
        TextView tvExpireTip;
        TextView tvCategory;
        TextView tvLocation;
        TextView tvQuantity;
        TextView tvExpiryDate;
        TextView tvCreateTime;
        TextView tvUpdateTime;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvExpireTip = itemView.findViewById(R.id.tv_expire_tip);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            tvUpdateTime = itemView.findViewById(R.id.tv_update_time);
        }
    }
}