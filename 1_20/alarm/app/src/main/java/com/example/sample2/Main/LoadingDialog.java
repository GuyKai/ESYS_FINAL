package com.example.sample2.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.example.sample2.R;

public class LoadingDialog {

    private Activity mActivity;
    private AlertDialog mDialog;

    public LoadingDialog(Activity activity){
        mActivity = activity;
    }

    public void startLoading(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        LayoutInflater layoutInflater = mActivity.getLayoutInflater();
        builder.setView(View.inflate(mActivity,R.layout.dialog,null));
        builder.setCancelable(true);

        mDialog = builder.create();
        mDialog.show();

    }

    public void dismissDialog(){
        mDialog.dismiss();
    }


}
