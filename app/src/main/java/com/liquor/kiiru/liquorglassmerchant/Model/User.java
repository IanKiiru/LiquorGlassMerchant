package com.liquor.kiiru.liquorglassmerchant.Model;

/**
 * Created by Kiiru on 10/17/2017.
 */

public class User {

    private String Phone;
    private String fName;
    private String lName;
    private String Email;
    private String Password;

    public User() {
    }


    public User(String phone, String fname, String lname, String email, String password) {
        Phone = phone;
        fName = fname;
        lName = lname;
        Email = email;
        Password = password;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fname) {
        fName = fname;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lname) {
        lName = lname;
    }

    public String getPhone() {
        return Phone;
    }
    public void setPhone(String phone){
        Phone = phone;
    }
    public String getEmail() {
        return Email;
    }
    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
