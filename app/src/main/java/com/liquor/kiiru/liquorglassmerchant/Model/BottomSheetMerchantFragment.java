package com.liquor.kiiru.liquorglassmerchant.Model;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liquor.kiiru.liquorglassmerchant.R;

/**
 * Created by Kiiru on 11/21/2017.
 */

public class BottomSheetMerchantFragment extends BottomSheetDialogFragment {
    String mTag;

    public static BottomSheetMerchantFragment newInstance(String tag){
        BottomSheetMerchantFragment f = new BottomSheetMerchantFragment();
        Bundle args = new Bundle();
        args.putString("TAG", tag);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString("TAG");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_info_window, container, false);
        return view;


    }
}
