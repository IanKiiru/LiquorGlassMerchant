package com.liquor.kiiru.liquorglassmerchant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liquor.kiiru.liquorglassmerchant.Common.Common;
import com.liquor.kiiru.liquorglassmerchant.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private Button btn_login;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private FirebaseAuth auth;
    RelativeLayout rootLayout;

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
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }

        auth = FirebaseAuth.getInstance();
        startService(new Intent(MainActivity.this, onAppKilled.class));

        // Init Paper
        Paper.init(this);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        btn_login = (Button) findViewById(R.id.btn_signIn);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        //Check Remember me

        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);

        if (user !=null && pwd!=null){

            if (!user.isEmpty() && !pwd.isEmpty()){
                login(user,pwd);

            }

        }
    }

    private void showLoginDialog() {
        //Initialize the Firebase Database
        final FirebaseDatabase cDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference table_users = cDatabase.getReference().child("Users").child("Merchants");

        if (Common.isConnectedToInternet(getBaseContext())) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("SIGN IN ");
            dialog.setMessage("Please use your email to sign in");

            LayoutInflater inflater = LayoutInflater.from(this);
            View login_layout = inflater.inflate(R.layout.login_layout, null);
            dialog.setView(login_layout);

            final MaterialEditText edtEmail = (MaterialEditText) login_layout.findViewById(R.id.edtLoginEmail);
            final MaterialEditText edtPassword = (MaterialEditText) login_layout.findViewById(R.id.edtLoginPassword);
            final com.rey.material.widget.CheckBox checkBox = (com.rey.material.widget.CheckBox) login_layout.findViewById(R.id.chkBoxRememberMe);




            dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    final SpotsDialog loginProgressDialog = new SpotsDialog(MainActivity.this);
                    loginProgressDialog.show();
                    if (checkBox.isChecked()) {
                        Paper.book().write(Common.USER_KEY, edtEmail.getText().toString());
                        Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                    }

                    if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                        loginProgressDialog.dismiss();
                        Snackbar.make(rootLayout, "Some fields were left missing...", Snackbar.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                        loginProgressDialog.dismiss();
                        Snackbar.make(rootLayout, "Some fields were left missing...", Snackbar.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if (edtPassword.getText().toString().length() < 6) {
                        loginProgressDialog.dismiss();
                        Snackbar.make(rootLayout, "Password is too short", Snackbar.LENGTH_SHORT)
                                .show();
                        return;
                    } else {

                        auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        loginProgressDialog.dismiss();
                                        final String userId = auth.getCurrentUser().getUid();
                                        table_users.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                //Check if user exists in database
                                                if (dataSnapshot.child(userId).exists()) {
                                                    //Get User Information
                                                    User user = dataSnapshot.child(userId).getValue(User.class);
                                                    user.setEmail(edtEmail.getText().toString()); // setEmail
                                                    Common.currentUser = user;

                                                    Snackbar.make(rootLayout, "Sign in successful", Snackbar.LENGTH_LONG)
                                                            .show();
                                                    startActivity(new Intent(MainActivity.this, Home.class));
                                                }


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                                Snackbar.make(rootLayout, "DatabaseError: "+databaseError.getMessage(), Snackbar.LENGTH_LONG)
                                                        .show();

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loginProgressDialog.dismiss();
                                Snackbar.make(rootLayout, "Sign in failed" +e.getMessage(), Snackbar.LENGTH_LONG)
                                        .show();

                            }
                        });


                    }
                }
            });
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        } else {

            Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
        }

    }


    private void login( final String email, final String pwd) {

        //Initialize the Firebase Database
        final FirebaseDatabase cDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference table_users_merchants = cDatabase.getReference().child("Users").child("Merchants");

        if (Common.isConnectedToInternet(getBaseContext())) {

            final SpotsDialog mProgressDialog = new SpotsDialog(MainActivity.this);
            mProgressDialog.show();

            auth.signInWithEmailAndPassword(email, pwd)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            final String currentUserId = auth.getCurrentUser().getUid();
                            table_users_merchants.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //Check if user exists in database
                                    if (dataSnapshot.child(currentUserId).exists()) {
                                        //Get User Information
                                        User user = dataSnapshot.child(currentUserId).getValue(User.class);
                                        user.setEmail(email);  //setEmail;
                                        Common.currentUser = user;
                                        mProgressDialog.dismiss();
                                        Snackbar.make(rootLayout, "Sign in successful", Snackbar.LENGTH_SHORT)
                                                .show();
                                        startActivity(new Intent(MainActivity.this, Home.class));
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Snackbar.make(rootLayout, "DatabaseError: "+databaseError.getMessage(), Snackbar.LENGTH_LONG)
                                            .show();
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressDialog.dismiss();
                    Snackbar.make(rootLayout, "Sign in failed" +e.getMessage(), Snackbar.LENGTH_LONG)
                            .show();

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();

        }
    }
}
