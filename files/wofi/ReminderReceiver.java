package com.example.wofi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.app.PendingIntent;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String NEW_PROFESSIONAL_ACTION = "com.example.wofi.NEW_PROFESSIONAL";
    private static final String DAILY_REMINDER_ACTION = "com.example.wofi.DAILY_REMINDER";
    private static final String NOTIFICATION_CHANNEL_ID = "wofi_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "WOFI Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        String title;
        String message;
        Intent contentIntent;

        if (DAILY_REMINDER_ACTION.equals(intent.getAction())) {
            title = "תזכורת יומית מ-WOFI";
            message = "בדוק אם יש בעלי מקצוע חדשים שמתאימים לך!";
            contentIntent = new Intent(context, MainActivity.class);
            contentIntent.putExtra("navigate_to", "professionals");
        } else if (NEW_PROFESSIONAL_ACTION.equals(intent.getAction())) {
            String professionalName = intent.getStringExtra("professional_name");
            title = "בעל מקצוע חדש הצטרף!";
            message = "המשתמש " + professionalName + " נוסף לאפליקציה.";
            contentIntent = new Intent(context, MainActivity.class);
            contentIntent.setAction(Intent.ACTION_VIEW);
            contentIntent.putExtra("navigate_to", "professionals");
        } else {
            return; // Unknown action
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
