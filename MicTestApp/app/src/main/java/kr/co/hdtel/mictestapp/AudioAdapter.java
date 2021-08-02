package kr.co.hdtel.mictestapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter {
    ArrayList<Uri> mDataModels;
    Context mContext;
    private OnIconClickListener mListener = null;

    public AudioAdapter(Context mContext, ArrayList<Uri> mDataModels) {
        this.mDataModels = mDataModels;
        this.mContext = mContext;
    }

    public interface OnIconClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnIconClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mDataModels.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        String uriName = String.valueOf(mDataModels.get(position));
        File file = new File(uriName);

        myViewHolder.audioTitle.setText(file.getName());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageButton audioBtn;
        TextView audioTitle;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            audioBtn = itemView.findViewById(R.id.btn_play);
            audioTitle = itemView.findViewById(R.id.tv_title);

            audioBtn.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (mListener != null) {
                        mListener.onItemClick(view, pos);
                    }
                }
            });
        }
    }
}