package com.liquor.kiiru.liquorglassmerchant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.liquor.kiiru.liquorglassmerchant.Common.Common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserProfile extends AppCompatActivity {

    private EditText firstNameField, lastNameField, emailField, passwordField, phoneField;

    private Button backButton, saveButton;

    private ImageView profileImageView;
    private DatabaseReference mCustomerDatabase;

    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;

    private String mProfileImageUrl;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firstNameField = (EditText) findViewById(R.id.profileFirstName);
        lastNameField = (EditText) findViewById(R.id.profileLastName);
        emailField = (EditText) findViewById(R.id.profileEmail);
        phoneField = (EditText) findViewById(R.id.profilePhone);
        passwordField = (EditText) findViewById(R.id.profilePassword);

        profileImageView = (ImageView) findViewById(R.id.profileImage);

        backButton = (Button) findViewById(R.id.profileBack);
        saveButton = (Button) findViewById(R.id.profileSave);

        userID = Common.currentUser.getPhone();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Merchants").child(userID);

        getUserInfo();

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileImageIntent = new Intent(Intent.ACTION_PICK);
                profileImageIntent.setType("image/*");
                startActivityForResult(profileImageIntent, 1);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo(){
        final ProgressDialog mProgressDialog = new ProgressDialog(UserProfile.this);
        mProgressDialog.setTitle("Fetching your details...");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("email") != null) {
                        email = map.get("email").toString();
                        emailField.setText(email);
                    }
                    if (map.get("fName") != null) {
                        firstName = map.get("fName").toString();
                        firstNameField.setText(firstName);
                    }
                    if (map.get("lName") != null) {
                        lastName = map.get("lName").toString();
                        lastNameField.setText(lastName);
                    }
                    if (map.get("phone") != null) {
                        phone = map.get("phone").toString();
                        phoneField.setText(phone);
                    }
                    if (map.get("password") != null) {
                        password = map.get("password").toString();
                        passwordField.setText(password);
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(profileImageView);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProgressDialog.dismiss();

    }

    private void saveUserInformation() {
        final ProgressDialog mProgressDialog = new ProgressDialog(UserProfile.this);
        mProgressDialog.setMessage("Saving your details...");
        mProgressDialog.show();

        email = emailField.getText().toString();
        firstName = firstNameField.getText().toString();
        lastName = lastNameField.getText().toString();
        phone = phoneField.getText().toString();
        password = passwordField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("email", email);
        userInfo.put("fName", firstName);
        userInfo.put("lName", lastName);
        userInfo.put("phone", phone);
        userInfo.put("password", password);
        mCustomerDatabase.updateChildren(userInfo);

        if(resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressDialog.dismiss();
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    mCustomerDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImageView.setImageURI(resultUri);
        }
    }
}
