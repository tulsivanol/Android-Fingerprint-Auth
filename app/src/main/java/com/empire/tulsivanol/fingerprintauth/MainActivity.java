package com.empire.tulsivanol.fingerprintauth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.Image;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevalpatel.passcodeview.PinView;
import com.kevalpatel.passcodeview.authenticator.PasscodeViewPinAuthenticator;
import com.kevalpatel.passcodeview.indicators.CircleIndicator;
import com.kevalpatel.passcodeview.interfaces.AuthenticationListener;
import com.kevalpatel.passcodeview.keys.KeyNamesBuilder;
import com.kevalpatel.passcodeview.keys.RoundKey;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.Manifest.permission.USE_FINGERPRINT;

public class MainActivity extends AppCompatActivity {

    private static final Object KEY_NAME = "AndroidKey";
    TextView title, para;
    ImageView finger;

    Cipher cipher;
    KeyGenerator keyGenerator;
    KeyStore keyStore;

    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        PinView pinView = (PinView) findViewById(R.id.pin_view);
//        final int[] correctPin = new int[]{1, 2, 3, 4};
//        pinView.setPinAuthenticator(new PasscodeViewPinAuthenticator(correctPin));
//        pinView.setPinLength(PinView.DYNAMIC_PIN_LENGTH);
//
//        pinView.setKey(new RoundKey.Builder(pinView)
//                .setKeyPadding(R.dimen.key_padding)
//                .setKeyStrokeColorResource(R.color.colorAccent)
//                .setKeyStrokeWidth(R.dimen.key_stroke_width)
//                .setKeyTextColorResource(R.color.colorAccent)
//                .setKeyTextSize(R.dimen.key_text_size));
//
//        pinView.setIndicator(new CircleIndicator.Builder(pinView)
//                .setIndicatorRadius(R.dimen.indicator_radius)
//                .setIndicatorFilledColorResource(R.color.colorAccent)
//                .setIndicatorStrokeColorResource(R.color.colorAccent)
//                .setIndicatorStrokeWidth(R.dimen.indicator_stroke_width));
//
//        pinView.setKeyNames(new KeyNamesBuilder()
//                .setKeyOne(this, R.string.key_1)
//                .setKeyTwo(this, R.string.key_2)
//                .setKeyThree(this, R.string.key_3)
//                .setKeyFour(this, R.string.key_4)
//                .setKeyFive(this, R.string.key_5)
//                .setKeySix(this, R.string.key_6)
//                .setKeySeven(this, R.string.key_7)
//                .setKeyEight(this, R.string.key_8)
//                .setKeyNine(this, R.string.key_9)
//                .setKeyZero(this, R.string.key_0));
//
//        pinView.setAuthenticationListener(new AuthenticationListener() {
//            @Override
//            public void onAuthenticationSuccessful() {
//
//                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//                //User authenticated successfully.
//                //Navigate to next screens.
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                //Calls whenever authentication is failed or user is unauthorized.
//                //Do something if you want to handle unauthorized user.
//            }
//        });

        title = findViewById(R.id.title);
        para = findViewById(R.id.para);
        finger = (ImageView) findViewById(R.id.image_view);

//        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setCancelable(false)
//                .setIcon(R.mipmap.action_fingerprint)
//                .setTitle("FingerprintScanner")
//                .setMessage("Put your finger on fingerprint scanner");
//        alertDialog.create();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if (ContextCompat.checkSelfPermission(this, USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {

                para.setText("Fingerprint scanner permission not granted!!!");

            } else if (!fingerprintManager.isHardwareDetected()) {

                para.setText("There is no fingerprint scanner in your device");

            } else if (!fingerprintManager.hasEnrolledFingerprints()) {

                para.setText("Ensure that you have fingerprint enrolled");

            } else if (!keyguardManager.isKeyguardSecure()) {

                para.setText("Ensure that that the device is secured with atleast one lock");

            } else {

//                alertDialog.show();
//                alertDialog.setMessage("Put your finger on fingerprint scanner");

                para.setText("Put your finger in the scanner");

                generateKey();

                if (cipherInit()) {

                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject);

                }

//                startActivity(new Intent(MainActivity.this, HomeActivity.class)
//                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
//                finish();
//                finishAffinity();


            }

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder((String) KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey((String) KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }

}
