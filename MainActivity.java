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


    //Método para verificar os dados que o usuário inseriu -> btnSet click
    private void verifyInput(String minutos,  String segundos){
        long millisInputSegundos, millisInputMinutos;

        if (minutos.length() == 0 && segundos.length() == 0) {
            Toast.makeText(MainActivity.this, "Os campos não podem ser vazio!", Toast.LENGTH_LONG).show();
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
            Toast.makeText(MainActivity.this, "Digite um valor válido!", Toast.LENGTH_LONG).show();
            return;
        }

        setTime(millisInputMinutos, millisInputSegundos);
        edtInputMinutos.setText(null);
        edtInputSegundos.setText(null);
    }


    //Método para definir quanto tempo falta para o alarme
    private void setTime(long milliSecondsMinutes, long milliSeconds) {
        mStartTimeInMillis = milliSecondsMinutes + milliSeconds;
        resetTimer();
        closeKeyboard();
    }

    //Método para iniciar o contador
    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        setNotificationAlarm();

        //Método que faz o "desconto" do tempo a cada segundo (não precisa ser necessariamente 1s, o programador que define)
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
        //Cálculos para conversão de Milissegundo para horas, minutos e segundos
        //Divisão por 1000 -> 1 segundo - 1000 milissegundos
        //Divisão por 3600 -> 1 hora - 3600 segundos
        //Divisão por 60 -> 1 minuto - 60 segundos
        //Módulo de 3600 -> O resto equivale a quantidade de minutos já descontando as horas
        //Módulo de 60 -> O resto equivale a quantidade de segundos, já descontando os minutos
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        //String para apresentação na tela
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

    //Método para atualizar os botões na tela, quando o timer está sendo executado o comportamento
    //dos botões será diferente, por isso esse método irá atualiza-los
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

    //Para criar a notificação é necessário criar um "canal" para ela antes de criar a notificação
    public void createNotificationChannel(){

        //A partir de uma determinada versão do android se tornou obrigatório a
        //criação de canais para as notificações, por isso é necessário fazer esse if
        //e passar as informações referentes as notificações que serão apresentadas
        //nesse canal específico. Podem ser criados diversos canais diferentes
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

    //Método que define quando a notificação irá aparecer
    private void setNotificationAlarm() {

        Intent it = new Intent(this, AlarmReceiver.class);
        it.putExtra("autoIsChecked", autoIsChecked);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, it, 0);

        //A notificação e o timer são processos paralelos. Um não irá intereferir
        //no funcionamento do outro, porém foram programados para que no fim do timer
        //a notificação seja enviada, mas são 2 processos separados.
        AlarmManager  alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Parametros para definir o alarme
        //Definir o alarme a partir daquele momento, passar o tempo em milissegundos em que o alarme
        //irá executar sua ação e ação que ele irá executar
        alarmManager.set(AlarmManager.RTC_WAKEUP, mEndTime,
                pendingIntent);

        Toast.makeText(this, "Alarme definido.", Toast.LENGTH_SHORT).show();
    }

    //Método para cancelar a notificação
    private void cancelNotificationAlarm(){

        //Para cancelar a notificação é preciso "criar" ela novamente com
        //os mesmos parametros e usar o método .cancel, passando a ação que ele executaria
        Intent it = new Intent(this, AlarmReceiver.class);
        it.putExtra("autoIsChecked", autoIsChecked);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, it, 0);

        AlarmManager  alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "Alarme cancelado.", Toast.LENGTH_SHORT).show();
    }

    //Método para fechar o teclado após a digitação e confirmação
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //Quando o aplicativo for fechado esse método será executado e irá salvar
    //as informações que forem especificadas, dessa forma o timer não fica executando
    //em segundo, mas suas informações ficarão salvas para que ele continue sua execução
    //corretamente na próxima vez que o aplicativo for aberto
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


    //Método que é executado quando o aplicativo é iniciado, o timer fará a contagem
    //correta mesmo que ele não esteja rodando em segundo plano porque a informação de
    //quando ele deveria terminar está na variável mEndTime, então caso o timer estivesse
    //sendo executado quando a aplicação foi fechada, assim que for iniciada será feito
    //o cálculo para saber onde o timer deve estar no momento, ou se ele já não foi finalizado
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