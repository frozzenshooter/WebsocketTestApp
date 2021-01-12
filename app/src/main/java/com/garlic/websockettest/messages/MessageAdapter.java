package com.garlic.websockettest.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.garlic.websockettest.R;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Double[] messages;

    public MessageAdapter(Double[] messages){
        this.messages = messages;
    }

    @Override
    public int getItemCount() {
        return messages.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout view;

        public ViewHolder(ConstraintLayout v){
            super(v);
            view = v;

        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        ConstraintLayout view = holder.view;

        TextView textView = (TextView) view.findViewById(R.id.message_view_text_view);
        textView.setText(String.valueOf(messages[position]));

    }
}
