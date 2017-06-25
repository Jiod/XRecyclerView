package com.fishpan.samples;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by yupan on 17/6/22.
 */
public class CustomerAdapter extends RecyclerView.Adapter<CustomerViewHolder> {
    LayoutInflater mInflater;

    public CustomerAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public CustomerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new CustomerViewHolder(mInflater.inflate(R.layout.layout_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(CustomerViewHolder customerViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 20;
    }
}
