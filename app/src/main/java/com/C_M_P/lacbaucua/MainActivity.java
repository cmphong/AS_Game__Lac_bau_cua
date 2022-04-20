package com.C_M_P.lacbaucua;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button btn_shuffle;
//    Button btn_open_close;
    ImageView iv_1, iv_2, iv_3,
                iv_plate,
                iv_light;
    RelativeLayout rl_dice;

    Random random;

    MediaPlayer mediaPlayer_start,
            mediaPlayer_finish_1,
            mediaPlayer_finish_2,
            mediaPlayer_finish_3;

    Sensor sensor;
    SensorManager sensorManager;
    boolean isAccelerometerSensorAvailable,
            itIsNotFirstTime = false;
    float currentX, currentY, currentZ,
            lastX, lastY, lastZ,
            xDifference, yDifference, zDifference,
            shakeThreshold = 5f;

    VibratorManager vibratorManager;
    Vibrator vibrator;

    boolean isOpen = false,
            isLight = false;
    Animation openAnimation, closeAnimation;




    // Gesture Drag =========================
    float widthParent, heightParent;
    float xDown = 0, yDown = 0,
            movedX, movedY,
            density;
    boolean isCollision         = false,
            isCollisionTop      = false,
            isCollisionRight    = false,
            isCollisionBottom   = false,
            isCollisionLeft     = false,
            isCollisionTL       = false,
            isCollisionTR       = false,
            isCollisionBL       = false,
            isCollisionBR       = false;
    float collisionTop, collisionRight, collisionBottom, collisionLeft;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        initial();

        density = getResources().getDisplayMetrics().density;

        random = new Random();
        mediaPlayer_start = MediaPlayer.create(this, R.raw.spin_prize_wheel_sound_effect);
        mediaPlayer_finish_1 = MediaPlayer.create(this, R.raw.bell_ding);
        mediaPlayer_finish_2 = MediaPlayer.create(this, R.raw.bell_ding);
        mediaPlayer_finish_3 = MediaPlayer.create(this, R.raw.bell_ding);

        // START - SHAKE DEVICE =====================================================
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        }else{
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerSensorAvailable = true;
        }else{
            isAccelerometerSensorAvailable = false;
        }
        // END - SHAKE DEVICE ======================================================

        btn_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer_start.start();

                btn_shuffle.setBackgroundColor(getResources().getColor(R.color.grey400));
                btn_shuffle.setEnabled(false);
//                btn_open_close.setBackgroundColor(getResources().getColor(R.color.grey400));
//                btn_open_close.setEnabled(false);
                iv_plate.setClickable(false);

                randomDiceRotate(iv_1);
                randomDiceRotate(iv_2);
                randomDiceRotate(iv_3);

                randomAnimal(iv_1, 3000, mediaPlayer_finish_1);
                randomAnimal(iv_2, 4500, mediaPlayer_finish_2);
                randomAnimal(iv_3, 6000, mediaPlayer_finish_3);
            }
        });

//        openAnimation = AnimationUtils.loadAnimation(this, R.anim.open_animation);
//        closeAnimation = AnimationUtils.loadAnimation(this, R.anim.close_animation);
//        btn_open_close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openCloseThePlate();
//            }
//        });
//        iv_plate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openCloseThePlate();
//            }
//        });

        // HANLER TRANSLATE ANIMATION EVENT ==============================
//        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                iv_plate.setClickable(false);
//                btn_open_close.setClickable(false);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                iv_plate.setClickable(true);
//                btn_open_close.setClickable(true);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        };
//        openAnimation.setAnimationListener(animationListener);
//        closeAnimation.setAnimationListener(animationListener);

        /* DRAG & DROP ==================================== */
        rl_dice.post(new Runnable() {
            @Override
            public void run() {
                widthParent = rl_dice.getWidth();
                heightParent = rl_dice.getHeight();
            }
        });
        iv_plate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    // The user just put his finger down on the image
                    case MotionEvent.ACTION_DOWN:
                        xDown = event.getX();
                        yDown = event.getY();

                        break;
                    // The user moved his finger
                    case MotionEvent.ACTION_MOVE:
                        movedX = event.getX();
                        movedY = event.getY();

                        // Calculate how much the user moved his finger
                        float distanceX = movedX - xDown;
                        float distanceY = movedY - yDown;


                        checkCollision();


                        if(isCollision){

                            if(isCollisionTL){
                                iv_plate.setX(collisionLeft);
                                iv_plate.setY(collisionTop);
                                if(distanceX > 0) iv_plate.setX(iv_plate.getX() + distanceX);
                                if(distanceY > 0) iv_plate.setY(iv_plate.getY() + distanceY);
                                break;
                            }
                            if(isCollisionTR){
                                iv_plate.setX(collisionRight);
                                iv_plate.setY(collisionTop);
                                if(distanceX < 0) iv_plate.setX(iv_plate.getX() + distanceX);
                                if(distanceY > 0) iv_plate.setY(iv_plate.getY() + distanceY);
                                break;
                            }
                            if(isCollisionBL){
                                iv_plate.setX(collisionLeft);
                                iv_plate.setY(collisionBottom);
                                if(distanceX > 0) iv_plate.setX(iv_plate.getX() + distanceX);
                                if(distanceY < 0) iv_plate.setY(iv_plate.getY() + distanceY);
                                break;
                            }
                            if(isCollisionBR){
                                iv_plate.setX(collisionRight);
                                iv_plate.setY(collisionBottom);
                                if(distanceX < 0) iv_plate.setX(iv_plate.getX() + distanceX);
                                if(distanceY < 0) iv_plate.setY(iv_plate.getY() + distanceY);
                                break;
                            }

                            if(isCollisionLeft){
                                iv_plate.setX(collisionLeft);
                                if(distanceX > 0) iv_plate.setX(iv_plate.getX() + distanceX);
                                iv_plate.setY(iv_plate.getY() + distanceY);
                            }

                            if(isCollisionTop){
                                iv_plate.setY(collisionTop);
                                if(distanceY > 0) iv_plate.setY(iv_plate.getY() + distanceY);
                                iv_plate.setX(iv_plate.getX() + distanceX);
                            }

                            if(isCollisionRight){
                                iv_plate.setX(collisionRight);
                                if(distanceX < 0) iv_plate.setX(iv_plate.getX() + distanceX);
                                iv_plate.setY(iv_plate.getY() + distanceY);
                            }

                            if(isCollisionBottom){
                                iv_plate.setY(collisionBottom);
                                if(distanceY < 0) iv_plate.setY(iv_plate.getY() + distanceY);
                                iv_plate.setX(iv_plate.getX() + distanceX);
                            }

                        }else{
                            iv_plate.setX(iv_plate.getX() +distanceX);
                            iv_plate.setY(iv_plate.getY() +distanceY);
                        }


                        break;
                }
                return true;
            }
        });

        iv_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLight = !isLight;
                int color;
                if(isLight){
                    color = Color.YELLOW;
                }else {
                    color = Color.WHITE;

                }
                ColorStateList tint = ColorStateList.valueOf(color);
                iv_light.setImageTintList(tint);
            }
        });
    }



    private void checkCollision(){
        collisionRight = widthParent - (iv_plate.getWidth()*3/4);
        collisionLeft = -(iv_plate.getWidth()*3/4);
        collisionTop = -(iv_plate.getWidth()*3/4);
        collisionBottom = heightParent - (iv_plate.getHeight()*3/4);
        if(
                iv_plate.getX() >= collisionRight ||
                iv_plate.getX() <= collisionLeft ||
                iv_plate.getY() <= collisionTop ||
                iv_plate.getY() >= collisionBottom ||

                (iv_plate.getY() <= collisionTop && iv_plate.getX() <= collisionLeft) ||
                (iv_plate.getY() <= collisionTop && iv_plate.getX() >= collisionRight) ||
                (iv_plate.getY() >= collisionBottom && iv_plate.getX() >= collisionRight) ||
                (iv_plate.getY() >= collisionBottom && iv_plate.getX() <= collisionLeft)
        ){
            isCollision = true;
            isCollisionLeft   = iv_plate.getX() <= collisionLeft;
            isCollisionRight  = iv_plate.getX() >= collisionRight;
            isCollisionTop    = iv_plate.getY() <= collisionTop;
            isCollisionBottom = iv_plate.getY() >= collisionBottom;

            isCollisionTL = isCollisionTop && isCollisionLeft;
            isCollisionTR = isCollisionTop && isCollisionRight;
            isCollisionBR = isCollisionBottom && isCollisionRight;
            isCollisionBL = isCollisionBottom && isCollisionLeft;
        }else{
            isCollision       = false;
            isCollisionRight  = false;
            isCollisionLeft   = false;
            isCollisionBottom = false;
            isCollisionTop    = false;
            isCollisionTL     = false;
            isCollisionTR     = false;
            isCollisionBL     = false;
            isCollisionBR     = false;
        }
    }

    private void openCloseThePlate(){
        if(!isOpen) {
            iv_plate.startAnimation(openAnimation);
            isOpen = !isOpen;
//            btn_open_close.setText(R.string.close);
        }else{
            iv_plate.startAnimation(closeAnimation);
            isOpen = !isOpen;
//            btn_open_close.setText(R.string.open);
        }
    }

    private void randomAnimal(ImageView iv, int timer, MediaPlayer mediaPlayer_finish){
        new CountDownTimer(timer, 40) {
            @Override
            public void onTick(long millisUntilFinished) {
                iv.setImageResource(getResources().getIdentifier(
                        "img_" + (random.nextInt(6) + 1),
                        "drawable",
                        getPackageName()
                ));
            }

            @Override
            public void onFinish() {

                mediaPlayer_finish.start();
                iv.setImageResource(getResources().getIdentifier(
                        "img_" + (random.nextInt(6) + 1),
                        "drawable",
                        getPackageName()
                ));
                if(iv.getId() == R.id.iv_3){

                    if(mediaPlayer_start.isPlaying()) {
                        mediaPlayer_start.stop();
                        try {
                            mediaPlayer_start.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    btn_shuffle.setBackgroundColor(getResources().getColor(R.color.redA200));
                    btn_shuffle.setEnabled(true);
//                    btn_open_close.setBackgroundColor(getResources().getColor(R.color.redA200));
//                    btn_open_close.setEnabled(true);
                    iv_plate.setClickable(true);
                }

            }
        }.start();

    }

    private void randomDiceRotate(ImageView iv){
        iv.setRotation(random.nextInt(34) * 10 + (random.nextInt(6) + 11));
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        currentX = sensorEvent.values[0];
        currentY = sensorEvent.values[1];
        currentZ = sensorEvent.values[2];

        if(itIsNotFirstTime){
            xDifference = Math.abs(lastX - currentX);
            yDifference = Math.abs(lastY - currentY);
            zDifference = Math.abs(lastZ - currentZ);

            if((xDifference > shakeThreshold && yDifference > shakeThreshold)
                    || (xDifference > shakeThreshold && zDifference > shakeThreshold)
                    || (yDifference > shakeThreshold && zDifference > shakeThreshold)
            ){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                }else{
                    vibrator.vibrate(500);
                    // deprecated in API 26
                }

                mediaPlayer_start.start(); // no need to call prepare(); create() does that for you
                btn_shuffle.setBackgroundColor(getResources().getColor(R.color.grey400));
                btn_shuffle.setEnabled(false);
                randomAnimal(iv_1, 3000, mediaPlayer_finish_1);
                randomAnimal(iv_2, 4500, mediaPlayer_finish_2);
                randomAnimal(iv_3, 5500, mediaPlayer_finish_3);
            }
        }

        lastX = currentX;
        lastY = currentY;
        lastZ = currentZ;

        itIsNotFirstTime = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    // ===== LIFECYCLE ACTIVITY ==================
    @Override
    protected void onResume() {
        super.onResume();

        if(isAccelerometerSensorAvailable){
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isAccelerometerSensorAvailable){
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // ENABLE FULL-SCREEN FOR DISPLAY CUTOUT
        setFullScreen();

    }

    public void setFullScreen(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        }else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    private void initial() {
        iv_light        = findViewById(R.id.iv_light);
        btn_shuffle     = findViewById(R.id.btn_shuffle);
//        btn_open_close  = findViewById(R.id.btn_open_close);
        iv_1            = findViewById(R.id.iv_1);
        iv_2            = findViewById(R.id.iv_2);
        iv_3            = findViewById(R.id.iv_3);
        iv_plate        = findViewById(R.id.iv_plate);
        rl_dice         = findViewById(R.id.rl_dice);
    }

    // ====================================================================
    public void Logd(String str){
        Log.d("Log.d", "=== MainActivity.java ==============================\n" + str);
    }
    public void Logdln(String str, int n){
        Log.d("Log.d", "=== MainActivity.java - line: " + n + " ==============================\n" + str);
    }
    public static void LogdStatic(String str){
        Log.d("Log.d", "=== MainActivity.java ==============================\n" + str);
    }
    public static void LogdlnStatic(String str, int n){
        Log.d("Log.d", "=== MainActivity.java - line: " + n + " ==============================\n" + str);
    }

    public void showToast( String str ){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}