package com.unpad.trashcare.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unpad.trashcare.R;
import com.unpad.trashcare.models.Pemberitahuan;

import java.util.ArrayList;

//import com.unpad.trashcarepetugas.models.Warga;

public class PemberitahuanAdapter extends RecyclerView.Adapter<PemberitahuanAdapter.ViewHolder>{

    private ArrayList<Pemberitahuan> mPemberitahuan = new ArrayList<>();


    public PemberitahuanAdapter(ArrayList<Pemberitahuan> pemberitahuans) {
        this.mPemberitahuan = pemberitahuans;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pemberitahuan_item_layout, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


//        ((ViewHolder)holder).headNotif.setText(mPemberitahuan.get(position).getPenerima());
//        ((ViewHolder)holder).timeNotif.set
//        ((ViewHolder)holder).timeNotif.setText(mPemberitahuan.get(position).getWaktu());

    }

    @Override
    public int getItemCount() {
        return mPemberitahuan.size();
//        return (mWarga != null) ? mWarga.size() : 0;

    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView headNotif, timeNotif;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            headNotif = itemView.findViewById(R.id.headNotif);
            timeNotif = itemView.findViewById(R.id.waktuNotif);
            cardView = itemView.findViewById(R.id.card_notif);
        }
    }
}
















