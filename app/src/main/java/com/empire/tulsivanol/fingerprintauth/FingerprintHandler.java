package com.empire.tulsivanol.fingerprintauth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context mCtx;

    public FingerprintHandler(Context mCtx) {

        this.mCtx = mCtx;

    }

    public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {

        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);

    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

        this.update("Error:" + errString, false);
    }

    @Override
    public void onAuthenticationFailed() {

        this.update("Authentication Failed", false);

    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

        this.update(helpString + "", true);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        this.update("You can now access the  app", true);

        Intent intent  = new Intent(mCtx,HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mCtx.startActivity(intent);

    }

    private void update(String s, boolean b) {

        TextView textView = (TextView) ((Activity) mCtx).findViewById(R.id.para);
        ImageView imageView = (ImageView) ((Activity) mCtx).findViewById(R.id.image_view);

        textView.setText(s);

//        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);
//        alertDialog.setCancelable(false)
//                .setIcon(R.mipmap.action_fingerprint)
//                .setTitle("FingerprintScanner")
//                .setMessage("Put your finger on fingerprint scanner");
//        alertDialog.create();

        if (b == false) {

//            alertDialog.setMessage(s);
//
            textView.setTextColor(ContextCompat.getColor(mCtx, R.color.colorAccent));

        } else {

//            alertDialog.setMessage(s);
//            alertDialog.setIcon(R.mipmap.action_done);
            textView.setTextColor(ContextCompat.getColor(mCtx, R.color.colorPrimary));
            imageView.setImageResource(R.mipmap.action_done);
        }

    }


}
