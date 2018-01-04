package com.liquor.kiiru.liquorglassmerchant;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.liquor.kiiru.liquorglassmerchant.Common.Common;
import com.liquor.kiiru.liquorglassmerchant.Model.MyResponse;
import com.liquor.kiiru.liquorglassmerchant.Model.Notification;
import com.liquor.kiiru.liquorglassmerchant.Model.Request;
import com.liquor.kiiru.liquorglassmerchant.Model.Sender;
import com.liquor.kiiru.liquorglassmerchant.Model.Token;
import com.liquor.kiiru.liquorglassmerchant.Remote.APIService;
import com.liquor.kiiru.liquorglassmerchant.ViewHolder.OrderDetailsAdapter;
import com.liquor.kiiru.liquorglassmerchant.ViewHolder.OrderViewHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.liquor.kiiru.liquorglassmerchant.Home.customerId;

public class OrderDetails extends AppCompatActivity {

    TextView order_id, order_phone, customer_name, order_status, order_total, order_address;
    String order_id_value = "";
    RecyclerView listDrinksOrdered;
    RecyclerView.LayoutManager layoutManager;
    MaterialSpinner spinner;
    DatabaseReference orderDatabaseRef;
    FirebaseDatabase orderDatabase;
    APIService mService;

    OrderDetailsAdapter adapter = new OrderDetailsAdapter(Common.currentRequest.getDrinks());

    @Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_order_details);

        order_id = (TextView) findViewById(R.id.order_id);
        order_phone = (TextView) findViewById(R.id.order_phone);
        customer_name = (TextView) findViewById(R.id.customer_name);
        order_status = (TextView) findViewById(R.id.order_status);
        order_total = (TextView) findViewById(R.id.order_total);
        order_address = (TextView) findViewById(R.id.order_address);

        mService = Common.getFCMService();
        orderDatabase = FirebaseDatabase.getInstance();
        orderDatabaseRef =orderDatabase.getReference("customerRequest").child(customerId).child("orderDetails");

        listDrinksOrdered = (RecyclerView) findViewById(R.id.listDrinksOrdered);
        listDrinksOrdered.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listDrinksOrdered.setLayoutManager(layoutManager);

        if (getIntent()!=null)
            order_id_value = getIntent().getStringExtra("OrderId");

        order_id.setText(order_id_value);
        order_phone.setText(Common.currentRequest.getPhone());
        customer_name.setText(Common.currentRequest.getFname()+" "+Common.currentRequest.getLname());
        order_status.setText(Common.convertCodeToStatus(Common.currentRequest.getStatus()));
        order_total.setText(Common.currentRequest.getTotal());
        order_address.setText(Common.currentRequest.getAddress());



        adapter.notifyDataSetChanged();
        listDrinksOrdered.setAdapter(adapter);


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE))
            showUpdateDialog(order_id_value, Common.currentRequest );
        else if (item.getTitle().equals(Common.DELETE))
            deleteOrder(order_id_value);
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderDetails.this);
        alertDialog.setTitle("Update order");
        alertDialog.setMessage("Please update status of order");


        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);

        spinner = (MaterialSpinner) view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "Coming your way", "Delivered");

        alertDialog.setView(view);

        final String localkey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                orderDatabaseRef.child(localkey).setValue(item);

                sendOrderStatusToCustomer(localkey, item);

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void deleteOrder(String key) {
        orderDatabaseRef.child(key).removeValue();
    }

    private void sendOrderStatusToCustomer(final String key,  Request item) {
        DatabaseReference tokens = orderDatabase.getReference("Tokens");
        tokens.orderByKey().equalTo(customerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                            Token token = postSnapshot.getValue(Token.class);
                            //Creating a  raw payload to send

                            Notification notification = new Notification("LIQUOR GLASS","Your order " +key +" was updated");
                            Sender content = new Sender(notification, token.getToken());

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                            if (response.body().success == 1){
                                                Toast.makeText(OrderDetails.this, "Order was updated", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else {
                                                Toast.makeText(OrderDetails.this, "Order was updated but failed to send notification to customer", Toast.LENGTH_LONG).show();

                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }
}
