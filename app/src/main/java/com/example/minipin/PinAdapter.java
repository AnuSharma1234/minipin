package com.example.minipin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PinAdapter extends RecyclerView.Adapter<PinAdapter.PinViewHolder> {
    private List<Pin> pins;
    private Context context;
    private OnPinClickListener onPinClickListener;
    private OnPinLongClickListener onPinLongClickListener;

    public interface OnPinClickListener {
        void onPinClick(Pin pin);
    }

    public interface OnPinLongClickListener {
        void onPinLongClick(Pin pin, int position);
    }

    public PinAdapter(List<Pin> pins, Context context) {
        this.pins = pins;
        this.context = context;
    }

    public void setOnPinClickListener(OnPinClickListener listener) {
        this.onPinClickListener = listener;
    }

    public void setOnPinLongClickListener(OnPinLongClickListener listener) {
        this.onPinLongClickListener = listener;
    }

    @NonNull
    @Override
    public PinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pin_item, parent, false);
        return new PinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PinViewHolder holder, int position) {
        Pin pin = pins.get(position);
        holder.pinTitle.setText(pin.getTitle());
        holder.pinDescription.setText(pin.getDescription());

        // Load image using Glide if imagePath is available
        if (pin.getImagePath() != null && !pin.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(pin.getImagePath())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.pinImage);
        } else {
            holder.pinImage.setImageResource(R.drawable.placeholder_image);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (onPinClickListener != null) {
                onPinClickListener.onPinClick(pin);
            }
        });

        // Long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (onPinLongClickListener != null) {
                onPinLongClickListener.onPinLongClick(pin, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return pins.size();
    }

    public void updateList(List<Pin> newPins) {
        this.pins = newPins;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        pins.remove(position);
        notifyItemRemoved(position);
    }

    public void addItem(Pin pin) {
        pins.add(0, pin);
        notifyItemInserted(0);
    }

    public static class PinViewHolder extends RecyclerView.ViewHolder {
        ImageView pinImage;
        TextView pinTitle;
        TextView pinDescription;

        public PinViewHolder(@NonNull View itemView) {
            super(itemView);
            pinImage = itemView.findViewById(R.id.pinImage);
            pinTitle = itemView.findViewById(R.id.pinTitle);
            pinDescription = itemView.findViewById(R.id.pinDescription);
        }
    }
}
