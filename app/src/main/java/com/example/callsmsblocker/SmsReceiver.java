package com.example.callsmsblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static final String PREFS_NAME = "CallBlockerPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "📨 SMS reçu !");

        SmsMessage[] messages;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        } else {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }

        if (messages == null || messages.length == 0) return;

        String sender = messages[0].getDisplayOriginatingAddress();
        String messageBody = messages[0].getMessageBody();

        Log.d(TAG, "Expéditeur: " + sender);
        Log.d(TAG, "Message: " + messageBody);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        FileManager fileManager = new FileManager(context);

        // Vérifier si le numéro est autorisé
        boolean isAllowed = MainActivity.isAllowed(sender, prefs, fileManager);
        Log.d(TAG, "SMS autorisé? " + isAllowed);

        if (!isAllowed) {
            Log.d(TAG, "🚫 SMS bloqué de: " + sender);
            Toast.makeText(context, "SMS bloqué de: " + sender, Toast.LENGTH_SHORT).show();

            // Démarrer l'alerte
            Intent serviceIntent = new Intent(context, AlertService.class);
            serviceIntent.putExtra("sender", sender);
            serviceIntent.putExtra("message", messageBody);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

            // Bloquer le SMS (tentative)
            abortBroadcast();

        } else {
            Log.d(TAG, "✅ SMS autorisé de: " + sender);
        }
    }
}