package com.example.game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import android.os.Vibrator;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    ImageView im;
    ImageView im2;
    SensorManager sensorManager;
    Sensor sensor;
    EditText input;
    Button button_conn;
    Socket client;

    Vibrator vibrate;

    PrintWriter out;

    String IP = "";

    public float width = 0;
    public int height = 0;

    public float R_ball = 0;

    int x_ball_cell = 2;
    int y_ball_cell = 2;
    float x_ball = 75;
    float y_ball = 75;
    float x_ball_old = 75;
    float y_ball_old = 75;
    float start_x;
    float start_y;
    float speed_x;
    float speed_y;
    Boolean set_start = false;

    float x_two_player = 350;
    float y_two_player = 1000;

    boolean array_upload = false;

    int[][] playing_field = new int[15][23];

    RelativeLayout.LayoutParams lp;
    RelativeLayout.LayoutParams lp_x;
    RelativeLayout.LayoutParams lp_y;
    RelativeLayout.LayoutParams lp_xy;

    public static int[] removeElement(int[] original, int element){
        int[] n = new int[original.length - 1];
        int el = 0;
        for(int i = 0; i < original.length; i++) {
            if(original[i] != element) { n[el] = original[i]; el++;}
        }
        return n;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int x = 0; x < playing_field.length; x++){
            for(int y = 0; y < playing_field[0].length; y++) {
                if (x == 0 || x == playing_field.length-1 || y == 0 || y == playing_field[0].length-1) { playing_field[x][y] = 1; } else { playing_field[x][y] = 0; } }
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        R_ball = (int) (width/playing_field.length)/2;
        x_ball = (width/playing_field.length)+(R_ball/2);
        y_ball = (width/playing_field.length)+(R_ball/2);
        x_ball_old = (width/playing_field.length)+(R_ball/2);
        y_ball_old = (width/playing_field.length)+(R_ball/2);

        float x_two_player = (width/playing_field.length)*5;
        float y_two_player = (width/playing_field.length)*12;

        vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        for(int x=0; x < playing_field.length; x++) {
            for (int y = 0; y < playing_field[0].length; y++) {
                if(playing_field[x][y] == 1) {
                    final ImageView imageView = new ImageView(this);
                    lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) width/playing_field.length+1, (int) width/playing_field.length+1));
                    lp.setMargins((int) x * (int) width/playing_field.length, (int) y * (int) width/playing_field.length, 0, 0);
                    imageView.setLayoutParams(lp);
                    imageView.setImageResource(R.drawable.wall);
                    RelativeLayout RL = findViewById(R.id.RL);
                    RL.addView(imageView);
                }
            }
        }

        im = (ImageView) findViewById(R.id.ball);
        im2 = (ImageView) findViewById(R.id.ball2);
        tv = (TextView) findViewById(R.id.textView);
        input = (EditText) findViewById(R.id.input);
        button_conn = findViewById(R.id.button_connect);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
        lp.setMargins((int) x_two_player - (int) (R_ball/2), (int) y_two_player - (int) (R_ball/2), 0, 0);
        im2.setLayoutParams(lp);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
        lp.setMargins((int) x_ball-(int) (R_ball/2), (int) y_ball-(int) (R_ball/2), 0, 0);
        im.setLayoutParams(lp);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listenerLight, sensor, SensorManager.SENSOR_DELAY_GAME);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        button_conn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IP = input.getText().toString();

                ViewGroup containerThatHoldsCheckBox = (ViewGroup) button_conn.getParent();
                containerThatHoldsCheckBox.removeView(button_conn);

                containerThatHoldsCheckBox = (ViewGroup) input.getParent();
                containerThatHoldsCheckBox.removeView(input);

                Client_or_Server(-1);
            }
        });
    }

    public void client(View view){
        Button button = findViewById(R.id.button_server);
        ViewGroup containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);
        button = findViewById(R.id.button_client);
        containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        lp.setMargins((int) 150* (int) (width/720.0f), (int) 250* (int) (width/720.0f), 0, 0);
        input.setLayoutParams(lp);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        lp.setMargins((int) 250* (int) (width/720.0f), (int) 120* (int) (width/720.0f), 0, 0);
        button_conn.setLayoutParams(lp);
    }

    public void server(View view){
        Client_or_Server(1);
        Button button = findViewById(R.id.button_server);
        ViewGroup containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);
        button = findViewById(R.id.button_client);
        containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);
    }
    public void upload_plating_field(int i, int x, int y){ playing_field[x][y]=i; }


    public void Client_or_Server(int client_or_server) {
        if(client_or_server == -1) { new Thread(new client()).start(); while(!array_upload){}; client_im(); Log.d("CREATION", "Client");}
        else if(client_or_server == 1) {
            for(int x = 0; x < playing_field.length; x++){ for(int y = 0; y < playing_field[0].length; y++){ playing_field[x][y] = 1; } }

            Random random = new Random();

            for(int i = 0; i < (int)playing_field[0].length; i++){
                int[] jumps = {1, 2, 3, 4};


                int random_x = ((random.nextInt((int)playing_field.length/2-1)+1)*2)-1;
                int random_y = ((random.nextInt((int)playing_field[0].length/2-1)+1)*2)-1;

                if(i == 0) { random_x = 1; random_y = 1; playing_field[1][1] = 0; }
                if(i == 1) { random_x = playing_field.length-2; random_y = playing_field[0].length-2; playing_field[playing_field.length-2][playing_field[0].length-2] = 0; }

                while (true){
                    if(jumps.length == 0){ break; }

                    int random_angle = jumps[random.nextInt(jumps.length)];

                    if(random_angle == 1){ //1
                        int next_x = random_x;
                        int next_y = random_y-2;

                        if(next_y > 0){
                            if(playing_field[next_x][next_y] != 0){
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x][next_y+1] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if(random.nextInt(100) < 30) { jumps = removeElement(jumps, random_angle); }
                            }else { playing_field[next_x][next_y+1] = 0; jumps = removeElement(jumps, random_angle); }
                        }else { jumps = removeElement(jumps, random_angle); }
                    }

                    if(random_angle == 2){ //2
                        int next_x = random_x+2;
                        int next_y = random_y;

                        if(next_x < playing_field.length-1){
                            if(playing_field[next_x][next_y] != 0){
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x-1][next_y] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if(random.nextInt(100) < 30) { jumps = removeElement(jumps, random_angle); }
                            }else { playing_field[next_x-1][next_y] = 0; jumps = removeElement(jumps, random_angle); }
                        }else { jumps = removeElement(jumps, random_angle); }
                    }

                    if(random_angle == 3){ //3
                        int next_x = random_x;
                        int next_y = random_y+2;

                        if(next_y < playing_field.length-1){
                            if(playing_field[next_x][next_y] != 0){
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x][next_y-1] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if(random.nextInt(100) < 30) { jumps = removeElement(jumps, random_angle); }
                            }else { playing_field[next_x][next_y-1] = 0; jumps = removeElement(jumps, random_angle); }
                        }else { jumps = removeElement(jumps, random_angle); }
                    }

                    if(random_angle == 4){ //4
                        int next_x = random_x-2;
                        int next_y = random_y;

                        if(next_x > 0){
                            if(playing_field[next_x][next_y] != 0){
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x+1][next_y] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if(random.nextInt(100) < 30) { jumps = removeElement(jumps, random_angle); }
                            }else { playing_field[next_x+1][next_y] = 0; jumps = removeElement(jumps, random_angle); }
                        }else { jumps = removeElement(jumps, random_angle); }
                    }
                }

            }
            new Thread(new server()).start();
        }

        x_ball = (width/playing_field.length)+R_ball;
        y_ball = (width/playing_field.length)+R_ball;
        x_ball_old = (width/playing_field.length)+R_ball;
        y_ball_old = (width/playing_field.length)+R_ball;

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
        lp.setMargins((int) x_ball-(int) (R_ball/2), (int) y_ball-(int) (R_ball/2), 0, 0);
        im.setLayoutParams(lp);


        Log.d("CREATION", "VIEW");
        for(int x=1; x < playing_field.length-1; x++) {
            for (int y = 1; y < playing_field[0].length-1; y++) {
                if(playing_field[x][y] == 1) {
                    final ImageView imageView = new ImageView(this);
                    lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) width/playing_field.length+1, (int) width/playing_field.length+1));
                    lp.setMargins((int) x * (int) width/playing_field.length, (int) y * (int) width/playing_field.length, 0, 0);
                    imageView.setLayoutParams(lp);
                    imageView.setImageResource(R.drawable.wall);
                    RelativeLayout RL = findViewById(R.id.RL);
                    RL.addView(imageView);
                }
            }
        }

    }

    public class client implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("CREATION", "CREATE");
                Socket socket = new Socket(InetAddress.getByName(IP), 2048);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                new Thread(new send_xy()).start();
                //SystemClock.sleep(5000);
                for(int x = 0; x < playing_field.length; x++) {
                    for (int y = 0; y < playing_field[0].length; y++) {
                        String str = input.readLine();
                        upload_plating_field(Integer.parseInt(str), x, y);
                    }
                }
                array_upload=true;
                //start_start_xy(out);
                Log.d("CREATION", "start send");
                while(true) {
                    String str = input.readLine();
                    Log.d("CREATION", "OK SEND   "+str);
                    if(str != null) {
                        String[] tmp = str.split(":");
                        x_two_player = (float) (((float) Integer.parseInt(tmp[0])/1000f) * (float) (width/playing_field.length)) +4f;
                        y_two_player = (float) (((float) Integer.parseInt(tmp[1])/1000f) * (float) (width/playing_field.length)) +4f;
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public class server implements Runnable{
        @Override
        public void run () {
            try {
                ServerSocket server = new ServerSocket(2048, 3);
                Log.d("CREATION", "CREATE SERVER");
                while(true) {
                    client = server.accept();
                    Log.d("CREATION", "NEW CLIENT");
                    new Thread(new Conn()).start();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public class send_xy implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    while (true) {
                        Log.d("CREATION", "SEND XY");

                        out.println(String.valueOf((int) ((x_ball / (float) (width / playing_field.length) * 0.8 + x_ball_old / (float) (width / playing_field.length) * 0.2)*1000)) + ":" +
                                    String.valueOf((int) ((y_ball / (float) (width / playing_field.length) * 0.8 + y_ball_old / (float) (width / playing_field.length) * 0.2)*1000)));
                        x_ball_old = x_ball;
                        y_ball_old = y_ball;
                        SystemClock.sleep(50);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    public class Conn implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("CREATION", "CONNECT");
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                for(int x = 0; x < playing_field.length; x++){ for(int y = 0; y < playing_field[0].length; y++){  out.println(playing_field[x][y]); SystemClock.sleep(100); } }
                //start_start_xy(out);
                new Thread(new send_xy()).start();
                while(true) {

                    String str = input.readLine();
                    Log.d("CREATION", "OK SEND   "+str);
                    if(str != null) {
                        String[] tmp = str.split(":");
                        x_two_player = (float) (((float) Integer.parseInt(tmp[0])/1000f) * (float) (width/playing_field.length));
                        y_two_player = (float) (((float) Integer.parseInt(tmp[1])/1000f) * (float) (width/playing_field.length));
                    }
                    //out.println(String.valueOf((int)(x_ball*0.8 + x_ball_old*0.2)) + ":" + String.valueOf((int)(y_ball*0.8 + y_ball_old*0.2))); x_ball_old = x_ball; y_ball_old = y_ball;
                }
            }catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void client_im() { im = (ImageView) findViewById(R.id.ball2); im2 = (ImageView) findViewById(R.id.ball); }

    public float control_angle_phone(float x) {
        if(x > 3) { return 3f; }
        else { return (float) x; }
    }

    @Override
    protected void onPause() { super.onPause(); sensorManager.unregisterListener(listenerLight, sensor); }
    @Override
    protected void onStart() { super.onStart(); sensorManager.registerListener(listenerLight, sensor, SensorManager.SENSOR_DELAY_GAME); }

    SensorEventListener listenerLight = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];

            if(!set_start || start_y == 0 && start_x == 0) { start_x = x; start_y = y; set_start = true; }

            String Out = "";

            if(x-start_x >= 0.5){ Out += "Left "; speed_x -= (width/720.0f)*(float) control_angle_phone(Math.abs(x-start_x)); }
            if(x-start_x <= -0.5) { Out += "Right "; speed_x += (width/720.0f)*(float) control_angle_phone(Math.abs(x-start_x)); }

            if(y-start_y >= 0.5) { Out += "Down "; speed_y += (width/720.0f)*(float) control_angle_phone(Math.abs(y-start_y)); }
            if(y-start_y <= -0.5) { Out += "Up "; speed_y -= (width/720.0f)*(float) control_angle_phone(Math.abs(y-start_y)); }

            boolean move_x = false;
            boolean move_y = false;

            x_ball_cell = (int) (x_ball)/(int) (width/playing_field.length);
            y_ball_cell = (int) (y_ball)/(int) (width/playing_field.length);

            float to_wall_x = 0;
            float to_wall_y = 0;

            if(speed_x > 0) {
                if(playing_field[x_ball_cell+1][y_ball_cell] != 1 || x_ball+speed_x+(R_ball/2f) < (x_ball_cell+1)*(float) (width/playing_field.length)) { move_x = true; }
                else if(playing_field[x_ball_cell+1][y_ball_cell] != 1 || x_ball+(R_ball/2f) < (x_ball_cell+1)*(float) (width/playing_field.length)) { to_wall_x = x_ball+(R_ball/2f) - (float) (x_ball_cell+1)*(float) (width/playing_field.length); }
                else { speed_x = 0; }
            }
            if(speed_x < 0) {
                if(playing_field[x_ball_cell-1][y_ball_cell] != 1 || x_ball+speed_x-(R_ball/2f) > x_ball_cell*(float) (width/playing_field.length)) { move_x = true; }
                else if(playing_field[x_ball_cell-1][y_ball_cell] != 1 || x_ball-(R_ball/2f) > x_ball_cell*(float) (width/playing_field.length)) { to_wall_x = x_ball-(R_ball/2f) -(float)  x_ball_cell*(float) (width/playing_field.length); }
                else { speed_x = 0; }
            }
            if(speed_y > 0) {
                if(playing_field[x_ball_cell][y_ball_cell+1] != 1 || y_ball+speed_y+(R_ball/2f) < (y_ball_cell+1)*(float) (width/playing_field.length)) { move_y = true; }
                else if(playing_field[x_ball_cell][y_ball_cell+1] != 1 || y_ball+(R_ball/2f) < (y_ball_cell+1)*(float) (width/playing_field.length)) { to_wall_y = y_ball+(R_ball/2f) - (float) (y_ball_cell+1)*(float) (width/playing_field.length); }
                else { speed_y = 0; }

            }
            if(speed_y < 0) {
                if(playing_field[x_ball_cell][y_ball_cell-1] != 1 || y_ball+speed_y-(R_ball/2f) > y_ball_cell*(float) (width/playing_field.length)) { move_y = true; }
                else if(playing_field[x_ball_cell][y_ball_cell-1] != 1 || y_ball-(R_ball/2f) > y_ball_cell*(float) (width/playing_field.length)) { to_wall_y = y_ball-(R_ball/2f) - (float) y_ball_cell*(float) (width/playing_field.length); }
                else { speed_y = 0; }
            }

            if(move_x && !move_y) {
                x_ball += speed_x;
                lp_x = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp_x.setMargins((int) x_ball-(int) (R_ball/2), (int) y_ball-(int) (R_ball/2), 0, 0);
                im.setLayoutParams(lp_x);
            }
            else if(!move_x && move_y) {
                y_ball += speed_y;
                lp_y = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp_y.setMargins((int) x_ball-(int) (R_ball/2), (int) y_ball-(int) (R_ball/2), 0, 0);
                im.setLayoutParams(lp_y);
            }
            else if(move_x && move_y) {
                x_ball += speed_x;
                y_ball += speed_y;
                lp_xy = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp_xy.setMargins((int) x_ball-(int) (R_ball/2), (int) y_ball-(int) (R_ball/2), 0, 0);
                im.setLayoutParams(lp_xy);
            }

            if(to_wall_x != 0 || to_wall_y != 0) {
                x_ball += -1*to_wall_x;
                y_ball += -1*to_wall_y;

                lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp.setMargins((int) x_ball-(int) (R_ball/2), (int) y_ball-(int) (R_ball/2), 0, 0);
                im.setLayoutParams(lp);

                vibrate.vibrate(50);
            }

            lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
            lp.setMargins((int) x_two_player-15, (int) y_two_player-15, 0, 0);
            im2.setLayoutParams(lp);
            
            if(speed_x > 0) { speed_x -= 0.5f*(width/720.0f); }
            else if(speed_x < 0) { speed_x += 0.5f*(width/720.0f); }

            if(speed_y > 0) { speed_y -= 0.5f*(width/720.0f); }
            else if(speed_y < 0) { speed_y += 0.5f*(width/720.0f); }

            if(Math.abs(speed_x) < 0.5f*(width/720.0f)) {speed_x = 0;}
            if(Math.abs(speed_y) < 0.5f*(width/720.0f)) {speed_y = 0;}

            tv.setText(String.valueOf(x_two_player)  + " " + String.valueOf(y_two_player) + " " + String.valueOf(Math.abs(x-start_x))  + " " + String.valueOf(Math.abs(y-start_y)));

        }
    };
}
