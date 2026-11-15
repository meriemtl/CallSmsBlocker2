package com.example.callsmsblocker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReceiver";
    private static final String PREFS_NAME = "CallBlockerPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "=========================================");
        Log.d(TAG, "📞 CallReceiver DÉCLENCHÉ !");

        if (!intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            return;
        }

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d(TAG, "État de l'appel: " + state);

        if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            return;
        }

        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        Log.d(TAG, "📞 Appel entrant de: " + number);

        if (number == null || number.isEmpty()) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        FileManager fileManager = new FileManager(context);

        // Vérifier si le numéro est autorisé
        boolean isAllowed = MainActivity.isAllowed(number, prefs, fileManager);
        Log.d(TAG, "Autorisé? " + isAllowed);
        Log.d(TAG, "Mode Whitelist? " + prefs.getBoolean("whitelist_mode", false));

        // Afficher les listes pour débogage
        Log.d(TAG, "Blacklist: " + fileManager.loadBlacklist().toString());
        Log.d(TAG, "Whitelist: " + fileManager.loadWhitelist().toString());

        if (!isAllowed) {
            Log.d(TAG, "🚫 Appel BLOQUÉ de: " + number);
            Toast.makeText(context, "Appel bloqué: " + number, Toast.LENGTH_SHORT).show();
            rejectCall(context);
        } else {
            Log.d(TAG, "✅ Appel autorisé de: " + number);
        }
    }

    private void rejectCall(Context context) {
        Log.d(TAG, "Tentative de rejet d'appel...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                    == PackageManager.PERMISSION_GRANTED) {

                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager != null) {
                    telecomManager.endCall();
                    Log.d(TAG, "✅ Appel rejeté (Android 9+)");
                }
            } else {
                Log.d(TAG, "Permission ANSWER_PHONE_CALLS non accordée");
            }
        } else {
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                java.lang.reflect.Method method = tm.getClass().getMethod("endCall");
                method.invoke(tm);
                Log.d(TAG, "✅ Appel rejeté (Android <9)");
            } catch (Exception e) {
                Log.e(TAG, "Erreur rejet appel", e);
            }
        }
    }
}