package com.liquor.kiiru.liquorglassmerchant.ViewHolder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liquor.kiiru.liquorglassmerchant.Interface.ItemShortClickListener;
import com.liquor.kiiru.liquorglassmerchant.Model.Order;
import com.liquor.kiiru.liquorglassmerchant.R;

import java.util.List;

/**
 * Created by Kiiru on 11/22/2017.
 */


class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

    public TextView productName, productPrice, productQuantity;

    public MyViewHolder(View itemView) {
        super(itemView);
        productName = (TextView) itemView.findViewById(R.id.product_name);
        productPrice = (TextView) itemView.findViewById(R.id.product_price);
        productQuantity = (TextView) itemView.findViewById(R.id.product_quantity);

        itemView.setOnCreateContextMenuListener(this);

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Choose an action");

        menu.add(0,0, getAdapterPosition(), "Update");
        menu.add(0,1, getAdapterPosition(), "Delete");
    }
}

public class OrderDetailsAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> myOrders;

    public OrderDetailsAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_details_layout, parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Order order = myOrders.get(position);
        holder.productName.setText(String.format("PRODUCT NAME: %s", order.getProductName()));
        holder.productPrice.setText(String.format("PRODUCT PRICE: %s", order.getPrice()));
        holder.productQuantity.setText(String.format("QUANTITY: %s", order.getQuantity()));



    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }


}
