package com.liquor.kiiru.liquorglassmerchant.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.liquor.kiiru.liquorglassmerchant.Model.User;

/**
 * Created by Kiiru on 10/17/2017.
 */

public class Common {
    public static User currentUser;

    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static boolean isConnectedToInternet (Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager!=null){

            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null)
            {
                for (int i=0;i<info.length;i++)
                {
                    if (info [i].getState() ==NetworkInfo.State.CONNECTED)
                        return  true;
                }

            }

        }

        return  false;

    }
}
