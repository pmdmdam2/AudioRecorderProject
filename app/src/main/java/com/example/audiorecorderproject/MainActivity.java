package com.example.audiorecorderproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaController.MediaPlayerControl{
    private final int RECORDING_AUDIO = 0;
    private final int SAVE_AUDIO = 1;
    private static final String LOG_TAG = "Grabadora";
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean recording;
    private MediaController mediaController;
    private static final String TAG = "AudioPlayer";
    private Handler handler = new Handler();
    private Button btGrabar,btReproducir,btDetener;

    private static String fichero = Environment.
            getExternalStorageDirectory().getAbsolutePath() + "/audio.3gp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.btDetener = findViewById(R.id.btDetener);
        this.btDetener.setEnabled(false);
        this.btGrabar = findViewById(R.id.btGrabar);
        if((new File(fichero).exists()))
            this.btReproducir = findViewById(R.id.btReproducir);
    }
    /**
     * Cuando se detiene la actiidad se detiene la reproducción y se liberan
     * los recursos
     */
    @Override
    protected void onStop() {
        super.onStop();
        if(mediaController==null || mediaPlayer==null) return;

        mediaPlayer.stop();
        mediaPlayer.release();
        this.btReproducir.setEnabled(true);
    }
    /**
     * Evento de toque sobre la UI de la actividad para mostrar el
     * control de reproducción
     * @param event Evento del toque
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //the MediaController will hide after 3 seconds -
        // tap the screen to make it appear again
        mediaController.show();
        return false;
    }
    /**
     * Método de CallBack para el cuadro de diálog con la petición de aceptación
     * del permiso
     * @param requestCode Código de la petición
     * @param permissions Tipo de permiso
     * @param grantResults Concesión del permiso
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // si la solicitud del permiso se ha cancelado el array de resultados estará vacío
        if (grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            // permission concedido, se puede realizar la operación relacionada
            //intent = new Intent(Intent.ACTION_INSERT);
            //se comprueba si el dispositivo tiene posibilidad de realizar la acción solicitada por el intent
            recording = true;
        } else {
            if(requestCode==RECORDING_AUDIO)
                // permiso denegado para grabar audio
                Toast.makeText(this,
                        getResources().getString(R.string.permiso_grabar_denegado), Toast.LENGTH_LONG).show();
            else if(requestCode==SAVE_AUDIO)
                Toast.makeText(this,
                        "Permiso para almacenar el fichero de audio", Toast.LENGTH_LONG).show();
            recording = false;
        }
    }
    /**
     * Comprueba si un permiso dado ha sido concedido, en caso contrario
     * muestra un cuadro de diálogo requiriéndolo
     * @param requestCode Código de la petición
     * @param permission Tipo de permiso
     * @return True si es concedido
     */
    private boolean getPermission(int requestCode, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            /*2. Se comprueba si el permiso fue rechazado, devuelve true
            si el usuario rechazó la solicitud anteriormente y muestra
            false si un usuario rechazó un permiso y seleccionó la opción
            No volver a preguntar en el diálogo de solicitud de permiso, o
            si una política de dispositivo lo prohíbe*/
            if (ActivityCompat.
                    shouldShowRequestPermissionRationale(this,
                            permission)) {
                //Se informa al usuario de que no es posible ejecuta la acción solicitada
                // porque lo prohibe el dispositivo o porque se ha rechazado la solicitud.
                Toast.makeText(this, getResources().getString(R.string.permiso_grabar_denegado),
                        Toast.LENGTH_LONG).show();
                return false;
            } else {
                //3. se requiere el permiso
                ActivityCompat.requestPermissions((AppCompatActivity) this,
                        new String[]{permission},
                        requestCode);
                return this.recording;
            }
        } else {
            return true;
        }
    }
    /**
     * Evento click para el botón de grabación
     * @param view Botón de grabación
     */
    public void grabar(View view) {
        if (!getPermission(RECORDING_AUDIO, Manifest.permission.RECORD_AUDIO) ||
                !getPermission(SAVE_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)) return;
        this.btReproducir.setEnabled(false);
        this.btGrabar.setEnabled(false);
        this.btDetener.setEnabled(true);

        Toast.makeText(this,
                getResources().getString(R.string.comienza_grabacion),Toast.LENGTH_LONG).show();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setOutputFile(fichero);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.
                OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.
                AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Fallo en grabación");
        }
        mediaRecorder.start();
    }
    /**
     * Evento del botón de detención de la grabación
     * @param view Botón para detener la grabación
     */
    public void detenerGrabacion(View view) {
        mediaRecorder.stop();
        mediaRecorder.release();
        Toast.makeText(this,getString(R.string.grabacion_detenida) +
                " sobre el botón reproducir para escucharla",Toast.LENGTH_LONG).show();
        this.btGrabar.setEnabled(true);
        this.btReproducir.setEnabled(true);
        this.btDetener.setEnabled(false);
    }
    /**
     * Evento onClick del botón de reproducción
     * @param view Botón de reproducción
     */
    public void reproducir(View view) {
        this.btReproducir.setEnabled(false);
        this.btDetener.setEnabled(false);
        this.btGrabar.setEnabled(false);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaController = new MediaController(this);
            mediaPlayer.setDataSource(fichero);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Fallo en reproducción");
        }
    }
    /**
     * Una vez se ha cargado el contenido del MediaPlayer
     * se produce la llamada a este método. Se muestra, a través,
     * de un hilo diferente al de la UI el control de reproducción
     * @param mp MediaPlayer que completa la carga y está listo para reproducir
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared");
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.viView));
        handler.post(new Runnable() {
            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }
    @Override
    public void pause() {
        mediaPlayer.stop();
    }
    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }
    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }
    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    @Override
    public int getBufferPercentage() {
        return 0;
    }
    @Override
    public boolean canPause() {
        return true;
    }
    @Override
    public boolean canSeekBackward() {
        return true;
    }
    @Override
    public boolean canSeekForward() {
        return true;
    }
    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /**
     * Método de callback para la interfaz OnCompletionListener de MediaPlayer
     * , es llamado cuando finaliza la reproducción
     * @param mp MediaPlayer que completa la reproducción
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        this.btReproducir.setEnabled(true);
        this.btGrabar.setEnabled(true);
        mediaController.hide();
    }
}