package com.example.timer;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText edtInputMinutos, edtInputSegundos;
    private TextView txtCountDown;
    private Button btnStart_Pause, btnReset, btnSet;
    private Switch switch1;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;
    private boolean autoIsChecked;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        edtInputMinutos = findViewById(R.id.edtInputMinutos);
        edtInputSegundos = findViewById(R.id.edtInputSegundos);
        txtCountDown = findViewById(R.id.txtCountDown);
        btnStart_Pause = findViewById(R.id.btnStart_Pause);
        btnReset = findViewById(R.id.btnReset);
        btnSet = findViewById(R.id.btnSet);

        switch1 = findViewById(R.id.switch1);

        btnSet.setOnClickListener(view -> {
            String inputMinutos = edtInputMinutos.getText().toString();
            String inputSegundos = edtInputSegundos.getText().toString();
            verifyInput(inputMinutos, inputSegundos);
        });

        btnStart_Pause.setOnClickListener(view -> {
            if (mTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        switch1.setOnClickListener(view -> {
            if (switch1.isChecked())
                autoIsChecked = true;
            else
                autoIsChecked = false;
        });

        btnReset.setOnClickListener(view -> resetTimer());
    }


    //M??todo para verificar os dados que o usu??rio inseriu -> btnSet click
    private void verifyInput(String minutos,  String segundos){
        long millisInputSegundos, millisInputMinutos;

        if (minutos.length() == 0 && segundos.length() == 0) {
            Toast.makeText(MainActivity.this, "Os campos n??o podem ser vazio!", Toast.LENGTH_LONG).show();
            return;
        }

        if (segundos.length() == 0){
            millisInputSegundos = 0;
        } else {
            millisInputSegundos = Long.parseLong(segundos) * 1000;
        }

        if (minutos.length() == 0){
            millisInputMinutos = 0;
        } else {
            millisInputMinutos = Long.parseLong(minutos) * 60000;
        }

        if (millisInputMinutos < 0 || millisInputSegundos < 0) {
            Toast.makeText(MainActivity.this, "Digite um valor v??lido!", Toast.LENGTH_LONG).show();
            return;
        }

        setTime(millisInputMinutos, millisInputSegundos);
        edtInputMinutos.setText(null);
        edtInputSegundos.setText(null);
    }


    //M??todo para definir quanto tempo falta para o alarme
    private void setTime(long milliSecondsMinutes, long milliSeconds) {
        mStartTimeInMillis = milliSecondsMinutes + milliSeconds;
        resetTimer();
        closeKeyboard();
    }

    //M??todo para iniciar o contador
    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        setNotificationAlarm();

        //M??todo que faz o "desconto" do tempo a cada segundo (n??o precisa ser necessariamente 1s, o programador que define)
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateWatchInterface();
                if(autoIsChecked)
                    restartAll();
            }
        }.start();

        mTimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateWatchInterface();
        cancelNotificationAlarm();
    }

    private void resetTimer() {
        //pauseTimer();
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }

    private void restartAll(){
        resetTimer();
        mTimeLeftInMillis += 1000;
        startTimer();
    }

    private void updateCountDownText() {
        //C??lculos para convers??o de Milissegundo para horas, minutos e segundos
        //Divis??o por 1000 -> 1 segundo - 1000 milissegundos
        //Divis??o por 3600 -> 1 hora - 3600 segundos
        //Divis??o por 60 -> 1 minuto - 60 segundos
        //M??dulo de 3600 -> O resto equivale a quantidade de minutos j?? descontando as horas
        //M??dulo de 60 -> O resto equivale a quantidade de segundos, j?? descontando os minutos
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        //String para apresenta????o na tela
        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        txtCountDown.setText(timeLeftFormatted);
    }

    //M??todo para atualizar os bot??es na tela, quando o timer est?? sendo executado o comportamento
    //dos bot??es ser?? diferente, por isso esse m??todo ir?? atualiza-los
    private void updateWatchInterface() {
        if (mTimerRunning) {
            edtInputSegundos.setVisibility(View.INVISIBLE);
            edtInputMinutos.setVisibility(View.INVISIBLE);
            btnSet.setVisibility(View.INVISIBLE);
            btnReset.setVisibility(View.INVISIBLE);
            btnStart_Pause.setText("Pause");
        } else {
            edtInputSegundos.setVisibility(View.VISIBLE);
            edtInputMinutos.setVisibility(View.VISIBLE);
            btnSet.setVisibility(View.VISIBLE);
            btnStart_Pause.setText("Start");

            if (mTimeLeftInMillis < 1000) {
                btnStart_Pause.setVisibility(View.INVISIBLE);
            } else {
                btnStart_Pause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                btnReset.setVisibility(View.VISIBLE);
            } else {
                btnReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    //Para criar a notifica????o ?? necess??rio criar um "canal" para ela antes de criar a notifica????o
    public void createNotificationChannel(){

        //A partir de uma determinada vers??o do android se tornou obrigat??rio a
        //cria????o de canais para as notifica????es, por isso ?? necess??rio fazer esse if
        //e passar as informa????es referentes as notifica????es que ser??o apresentadas
        //nesse canal espec??fico. Podem ser criados diversos canais diferentes
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "drinkWaterAppReminderChannel";
            String description = "Channel to notify the time to drink water";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("drinkWaterApp", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //M??todo que define quando a notifica????o ir?? aparecer
    private void setNotificationAlarm() {

        Intent it = new Intent(this, AlarmReceiver.class);
        it.putExtra("autoIsChecked", autoIsChecked);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, it, 0);

        //A notifica????o e o timer s??o processos paralelos. Um n??o ir?? intereferir
        //no funcionamento do outro, por??m foram programados para que no fim do timer
        //a notifica????o seja enviada, mas s??o 2 processos separados.
        AlarmManager  alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Parametros para definir o alarme
        //Definir o alarme a partir daquele momento, passar o tempo em milissegundos em que o alarme
        //ir?? executar sua a????o e a????o que ele ir?? executar
        alarmManager.set(AlarmManager.RTC_WAKEUP, mEndTime,
                pendingIntent);

        Toast.makeText(this, "Alarme definido.", Toast.LENGTH_SHORT).show();
    }

    //M??todo para cancelar a notifica????o
    private void cancelNotificationAlarm(){

        //Para cancelar a notifica????o ?? preciso "criar" ela novamente com
        //os mesmos parametros e usar o m??todo .cancel, passando a a????o que ele executaria
        Intent it = new Intent(this, AlarmReceiver.class);
        it.putExtra("autoIsChecked", autoIsChecked);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, it, 0);

        AlarmManager  alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "Alarme cancelado.", Toast.LENGTH_SHORT).show();
    }

    //M??todo para fechar o teclado ap??s a digita????o e confirma????o
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //Quando o aplicativo for fechado esse m??todo ser?? executado e ir?? salvar
    //as informa????es que forem especificadas, dessa forma o timer n??o fica executando
    //em segundo, mas suas informa????es ficar??o salvas para que ele continue sua execu????o
    //corretamente na pr??xima vez que o aplicativo for aberto
    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //editor.clear();
        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }


    //M??todo que ?? executado quando o aplicativo ?? iniciado, o timer far?? a contagem
    //correta mesmo que ele n??o esteja rodando em segundo plano porque a informa????o de
    //quando ele deveria terminar est?? na vari??vel mEndTime, ent??o caso o timer estivesse
    //sendo executado quando a aplica????o foi fechada, assim que for iniciada ser?? feito
    //o c??lculo para saber onde o timer deve estar no momento, ou se ele j?? n??o foi finalizado
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 10000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        } else {
            //resetTimer();
        }
    }
}