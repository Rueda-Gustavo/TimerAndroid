package com.example.timer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    //Esse método será executado mesmo se o aplicativo tiver sido fechado
    @Override
    public void onReceive(Context context, Intent intent) {

        String message = "Hora de beber água!!!";
        String title = "Atenção!";

        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, activityIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "drinkWaterApp")
                .setSmallIcon(R.drawable.ic_one) //Definir o ícone da notificação
                .setContentTitle(title) //Definir o título da notificação
                .setContentText(message) //Definir o conteúdo no corpo da notificação
                .setPriority(NotificationCompat.PRIORITY_HIGH) //Definir a prioridade da notificação
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true) //Fazer com que a notificação seja fechada após o evento de click nela e a execução da tarefa determinada
                .setOnlyAlertOnce(true) //A notificação só irá notificar uma vez
                .setContentIntent(contentIntent); //Ação que a notificação irá fazer quando for clicada

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200, builder.build());
    }
}
