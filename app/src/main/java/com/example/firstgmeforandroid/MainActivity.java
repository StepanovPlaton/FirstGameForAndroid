package com.example.firstgmeforandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
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
import android.widget.Toast;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    public float width = 0;
    public int height = 0;
    public float R_ball = 0;
    TextView tv;
    TextView IP_print;
    ImageView im;
    ImageView im2;
    SensorManager sensorManager;
    Sensor sensor;
    EditText input;
    Button button_conn;
    Button serv;
    Socket client;
    Vibrator vibrate;
    PrintWriter out;
    String IP = "";

    Boolean toast_server = false;
    Boolean view = false;
    Boolean restart = false;

    float start_x;
    float start_y;
    Boolean set_start = false;

    Boolean array_upload = false;

    int[][] playing_field = new int[15][23];

    RelativeLayout.LayoutParams lp;
    RelativeLayout.LayoutParams lp_x;
    RelativeLayout.LayoutParams lp_y;
    RelativeLayout.LayoutParams lp_xy;

    public class player {
        float x = 75;
        float y = 75;
        float x_old = 75;
        float y_old = 75;
        int x_cell = 2;
        int y_cell = 2;
        float speed_x = 0;
        float speed_y = 0;

        player(int x, int y) {
            this.x = x;
            this.y = y;
            this.x_old = x;
            this.y_old = y;
        }
    }

    player first_player = new player(75, 75);
    player second_player = new player(-100, -100);

    SensorEventListener listenerLight = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];

            if (!set_start || start_y == 0 && start_x == 0) {
                start_x = x;
                start_y = y;
                set_start = true;
            }

            String Out = "";

            if (x - start_x >= 0.5) {
                Out += "Left ";
                first_player.speed_x -= (width / 720.0f) * (float) control_angle_phone(Math.abs(x - start_x));
            }
            if (x - start_x <= -0.5) {
                Out += "Right ";
                first_player.speed_x += (width / 720.0f) * (float) control_angle_phone(Math.abs(x - start_x));
            }

            if (y - start_y >= 0.5) {
                Out += "Down ";
                first_player.speed_y += (width / 720.0f) * (float) control_angle_phone(Math.abs(y - start_y));
            }
            if (y - start_y <= -0.5) {
                Out += "Up ";
                first_player.speed_y -= (width / 720.0f) * (float) control_angle_phone(Math.abs(y - start_y));
            }

            boolean move_x = false;
            boolean move_y = false;

            first_player.x_cell = (int) (first_player.x) / (int) (width / playing_field.length);
            first_player.y_cell = (int) (first_player.y) / (int) (width / playing_field.length);

            float to_wall_x = 0;
            float to_wall_y = 0;

            if (first_player.speed_x > 0) {
                if (playing_field[first_player.x_cell + 1][first_player.y_cell] != 1 || first_player.x + first_player.speed_x + (R_ball / 2f) < (first_player.x_cell + 1) * (float) (width / playing_field.length)) {
                    move_x = true;
                } else if (playing_field[first_player.x_cell + 1][first_player.y_cell] != 1 || first_player.x + (R_ball / 2f) < (first_player.x_cell + 1) * (float) (width / playing_field.length)) {
                    to_wall_x = first_player.x + (R_ball / 2f) - (float) (first_player.x_cell + 1) * (float) (width / playing_field.length);
                } else {
                    first_player.speed_x = 0;
                }
            }
            if (first_player.speed_x < 0) {
                if (playing_field[first_player.x_cell - 1][first_player.y_cell] != 1 || first_player.x + first_player.speed_x - (R_ball / 2f) > first_player.x_cell * (float) (width / playing_field.length)) {
                    move_x = true;
                } else if (playing_field[first_player.x_cell - 1][first_player.y_cell] != 1 || first_player.x - (R_ball / 2f) > first_player.x_cell * (float) (width / playing_field.length)) {
                    to_wall_x = first_player.x - (R_ball / 2f) - (float) first_player.x_cell * (float) (width / playing_field.length);
                } else {
                    first_player.speed_x = 0;
                }
            }
            if (first_player.speed_y > 0) {
                if (playing_field[first_player.x_cell][first_player.y_cell + 1] != 1 || first_player.y + first_player.speed_y + (R_ball / 2f) < (first_player.y_cell + 1) * (float) (width / playing_field.length)) {
                    move_y = true;
                } else if (playing_field[first_player.x_cell][first_player.y_cell + 1] != 1 || first_player.y + (R_ball / 2f) < (first_player.y_cell + 1) * (float) (width / playing_field.length)) {
                    to_wall_y = first_player.y + (R_ball / 2f) - (float) (first_player.y_cell + 1) * (float) (width / playing_field.length);
                } else {
                    first_player.speed_y = 0;
                }

            }
            if (first_player.speed_y < 0) {
                if (playing_field[first_player.x_cell][first_player.y_cell - 1] != 1 || first_player.y + first_player.speed_y - (R_ball / 2f) > first_player.y_cell * (float) (width / playing_field.length)) {
                    move_y = true;
                } else if (playing_field[first_player.x_cell][first_player.y_cell - 1] != 1 || first_player.y - (R_ball / 2f) > first_player.y_cell * (float) (width / playing_field.length)) {
                    to_wall_y = first_player.y - (R_ball / 2f) - (float) first_player.y_cell * (float) (width / playing_field.length);
                } else {
                    first_player.speed_y = 0;
                }
            }

            if (move_x && !move_y) {
                first_player.x += first_player.speed_x;
                lp_x = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp_x.setMargins((int) first_player.x - (int) (R_ball / 2), (int) first_player.y - (int) (R_ball / 2), 0, 0);
                im.setLayoutParams(lp_x);
            } else if (!move_x && move_y) {
                first_player.y += first_player.speed_y;
                lp_y = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp_y.setMargins((int) first_player.x - (int) (R_ball / 2), (int) first_player.y - (int) (R_ball / 2), 0, 0);
                im.setLayoutParams(lp_y);
            } else if (move_x && move_y) {
                first_player.x += first_player.speed_x;
                first_player.y += first_player.speed_y;
                lp_xy = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp_xy.setMargins((int) first_player.x - (int) (R_ball / 2), (int) first_player.y - (int) (R_ball / 2), 0, 0);
                im.setLayoutParams(lp_xy);
            }

            if (to_wall_x != 0 || to_wall_y != 0) {
                first_player.x += -1 * to_wall_x;
                first_player.y += -1 * to_wall_y;

                lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
                lp.setMargins((int) first_player.x - (int) (R_ball / 2), (int) first_player.y - (int) (R_ball / 2), 0, 0);
                im.setLayoutParams(lp);

                vibrate.vibrate(50);
            }

            lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
            lp.setMargins((int) second_player.x - 15, (int) second_player.y - 15, 0, 0);
            im2.setLayoutParams(lp);

            if (first_player.speed_x > 0) {
                first_player.speed_x -= 0.5f * (width / 720.0f);
            } else if (first_player.speed_x < 0) {
                first_player.speed_x += 0.5f * (width / 720.0f);
            }

            if (first_player.speed_y > 0) {
                first_player.speed_y -= 0.5f * (width / 720.0f);
            } else if (first_player.speed_y < 0) {
                first_player.speed_y += 0.5f * (width / 720.0f);
            }

            if (Math.abs(first_player.speed_x) < 0.5f * (width / 720.0f)) {
                first_player.speed_x = 0;
            }
            if (Math.abs(first_player.speed_y) < 0.5f * (width / 720.0f)) {
                first_player.speed_y = 0;
            }

            //tv.setText(String.valueOf(second_player.x) + " " + String.valueOf(second_player.y) + " " + String.valueOf(Math.abs(x - start_x)) + " " + String.valueOf(Math.abs(y - start_y)));
            if(toast_server) { WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE); IP_print.setText("Ваш IP - "+Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress()));}
            if(view) { View(); view = false; IP_print.setText("");}
        }
    };

    public static int[] removeElement(int[] original, int element) {
        int[] n = new int[original.length - 1];
        int el = 0;
        for (int i = 0; i < original.length; i++) {
            if (original[i] != element) {
                n[el] = original[i];
                el++;
            }
        }
        return n;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int x = 0; x < playing_field.length; x++) {
            for (int y = 0; y < playing_field[0].length; y++) {
                if (x == 0 || x == playing_field.length - 1 || y == 0 || y == playing_field[0].length - 1) {
                    playing_field[x][y] = 1;
                } else {
                    playing_field[x][y] = 0;
                }
            }
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        R_ball = (int) (width / playing_field.length) / 2;
        first_player.x = (width / playing_field.length) + (R_ball / 2);
        first_player.y = (width / playing_field.length) + (R_ball / 2);
        first_player.x_old = (width / playing_field.length) + (R_ball / 2);
        first_player.y_old = (width / playing_field.length) + (R_ball / 2);

        second_player.x = (width / playing_field.length) * (-2);
        second_player.y = (width / playing_field.length) * (-2);

        vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        for (int x = 0; x < playing_field.length; x++) {
            for (int y = 0; y < playing_field[0].length; y++) {
                if (playing_field[x][y] == 1) {
                    final ImageView imageView = new ImageView(this);
                    lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) width / playing_field.length + 1, (int) width / playing_field.length + 1));
                    lp.setMargins((int) x * (int) width / playing_field.length, (int) y * (int) width / playing_field.length, 0, 0);
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
        IP_print = (TextView) findViewById(R.id.IP);
        serv = findViewById(R.id.button_server);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
        lp.setMargins((int) second_player.x - (int) (R_ball / 2), (int) second_player.y - (int) (R_ball / 2), 0, 0);
        im2.setLayoutParams(lp);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
        lp.setMargins((int) first_player.x - (int) (R_ball / 2), (int) first_player.y - (int) (R_ball / 2), 0, 0);
        im.setLayoutParams(lp);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listenerLight, sensor, SensorManager.SENSOR_DELAY_GAME);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        button_conn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IP = input.getText().toString();
                Client_or_Server(-1);
                View();
            }
        });
    }

    public void client(View view) {
        Button button = findViewById(R.id.button_server);
        ViewGroup containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);
        button = findViewById(R.id.button_client);
        containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        lp.setMargins((int) (150 * (width / 720.0f)), (int) (250 * (width / 720.0f)), 0, 0);
        input.setLayoutParams(lp);

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        lp.setMargins((int) (250 * (width / 720.0f)), (int) (120 * (width / 720.0f)), 0, 0);
        button_conn.setLayoutParams(lp);
    }

    public void server(View view) {
        Client_or_Server(1);
        Button button = findViewById(R.id.button_server);
        ViewGroup containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);
        button = findViewById(R.id.button_client);
        containerThatHoldsCheckBox = (ViewGroup) button.getParent();
        containerThatHoldsCheckBox.removeView(button);
        toast_server = true;
    }

    public void upload_plating_field(int i, int x, int y) {
        playing_field[x][y] = i;
    }

    public Boolean Client_or_Server(int client_or_server) {
        if (client_or_server == -1) {
            new Thread(new client()).start();
            while (!array_upload) {
                if (restart) {
                    restart = false;
                    return false;
                }
                ;
            }
            client_im();
            Log.d("CREATION", "Client");
        } else if (client_or_server == 1) {
            for (int x = 0; x < playing_field.length; x++) {
                for (int y = 0; y < playing_field[0].length; y++) {
                    playing_field[x][y] = 1;
                }
            }

            Random random = new Random();

            for (int i = 0; i < (int) playing_field[0].length; i++) {
                int[] jumps = {1, 2, 3, 4};


                int random_x = ((random.nextInt((int) playing_field.length / 2 - 1) + 1) * 2) - 1;
                int random_y = ((random.nextInt((int) playing_field[0].length / 2 - 1) + 1) * 2) - 1;

                if (i == 0) {
                    random_x = 1;
                    random_y = 1;
                    playing_field[1][1] = 0;
                }
                if (i == 1) {
                    random_x = playing_field.length - 2;
                    random_y = playing_field[0].length - 2;
                    playing_field[playing_field.length - 2][playing_field[0].length - 2] = 0;
                }

                while (true) {
                    if (jumps.length == 0) {
                        break;
                    }

                    int random_angle = jumps[random.nextInt(jumps.length)];

                    if (random_angle == 1) { //1
                        int next_x = random_x;
                        int next_y = random_y - 2;

                        if (next_y > 0) {
                            if (playing_field[next_x][next_y] != 0) {
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x][next_y + 1] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if (random.nextInt(100) < 30) {
                                    jumps = removeElement(jumps, random_angle);
                                }
                            } else {
                                playing_field[next_x][next_y + 1] = 0;
                                jumps = removeElement(jumps, random_angle);
                            }
                        } else {
                            jumps = removeElement(jumps, random_angle);
                        }
                    }

                    if (random_angle == 2) { //2
                        int next_x = random_x + 2;
                        int next_y = random_y;

                        if (next_x < playing_field.length - 1) {
                            if (playing_field[next_x][next_y] != 0) {
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x - 1][next_y] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if (random.nextInt(100) < 30) {
                                    jumps = removeElement(jumps, random_angle);
                                }
                            } else {
                                playing_field[next_x - 1][next_y] = 0;
                                jumps = removeElement(jumps, random_angle);
                            }
                        } else {
                            jumps = removeElement(jumps, random_angle);
                        }
                    }

                    if (random_angle == 3) { //3
                        int next_x = random_x;
                        int next_y = random_y + 2;

                        if (next_y < playing_field.length - 1) {
                            if (playing_field[next_x][next_y] != 0) {
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x][next_y - 1] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if (random.nextInt(100) < 30) {
                                    jumps = removeElement(jumps, random_angle);
                                }
                            } else {
                                playing_field[next_x][next_y - 1] = 0;
                                jumps = removeElement(jumps, random_angle);
                            }
                        } else {
                            jumps = removeElement(jumps, random_angle);
                        }
                    }

                    if (random_angle == 4) { //4
                        int next_x = random_x - 2;
                        int next_y = random_y;

                        if (next_x > 0) {
                            if (playing_field[next_x][next_y] != 0) {
                                playing_field[next_x][next_y] = 0;
                                playing_field[next_x + 1][next_y] = 0;
                                random_x = next_x;
                                random_y = next_y;
                                if (random.nextInt(100) < 30) {
                                    jumps = removeElement(jumps, random_angle);
                                }
                            } else {
                                playing_field[next_x + 1][next_y] = 0;
                                jumps = removeElement(jumps, random_angle);
                            }
                        } else {
                            jumps = removeElement(jumps, random_angle);
                        }
                    }
                }

            }

            new Thread(new server()).start();
        }

        return true;
    }

    public void View() {
        first_player.x = (width / playing_field.length) + R_ball;
        first_player.y = (width / playing_field.length) + R_ball;
        first_player.x_old = (width / playing_field.length) + R_ball;
        first_player.y_old = (width / playing_field.length) + R_ball;

        lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) R_ball, (int) R_ball));
        lp.setMargins((int) first_player.x - (int) (R_ball / 2), (int) first_player.y - (int) (R_ball / 2), 0, 0);
        im.setLayoutParams(lp);


        Log.d("CREATION", "VIEW");
        for (int x = 1; x < playing_field.length - 1; x++) {
            for (int y = 1; y < playing_field[0].length - 1; y++) {
                if (playing_field[x][y] == 1) {
                    final ImageView imageView = new ImageView(this);
                    lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams((int) width / playing_field.length + 1, (int) width / playing_field.length + 1));
                    lp.setMargins((int) x * (int) width / playing_field.length, (int) y * (int) width / playing_field.length, 0, 0);
                    imageView.setLayoutParams(lp);
                    imageView.setImageResource(R.drawable.wall);
                    RelativeLayout RL = findViewById(R.id.RL);
                    RL.addView(imageView);
                }
            }
        }
    }

    public void clear_EditText_IP() {
        runOnUiThread(new Runnable() {
            public void run() {
                ViewGroup containerThatHoldsCheckBox = (ViewGroup) button_conn.getParent();
                containerThatHoldsCheckBox.removeView(button_conn);

                containerThatHoldsCheckBox = (ViewGroup) input.getParent();
                containerThatHoldsCheckBox.removeView(input); }
        });
    }

    public void TOAST(final String text) {
        Log.d("CREATION", "TOAST");
        try {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CREATION", "TOAST!!!");
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void client_im() {
        im = (ImageView) findViewById(R.id.ball2);
        im2 = (ImageView) findViewById(R.id.ball);
    }

    public float control_angle_phone(float x) {
        if (x > 3) {
            return 3f;
        } else {
            return (float) x;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listenerLight, sensor);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(listenerLight, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public class client implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("CREATION", "CREATE");
                Socket socket = new Socket(InetAddress.getByName(IP), 2048);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                clear_EditText_IP();

                new Thread(new send_xy()).start();
                //SystemClock.sleep(5000);
                for (int x = 0; x < playing_field.length; x++) {
                    for (int y = 0; y < playing_field[0].length; y++) {
                        String str = input.readLine();
                        upload_plating_field(Integer.parseInt(str), x, y);
                    }
                }
                array_upload = true;
                //start_start_xy(out);
                Log.d("CREATION", "start send");
                while (true) {
                    String str = input.readLine();
                    Log.d("CREATION", "OK SEND   " + str);
                    if (str != null) {
                        String[] tmp = str.split(":");
                        second_player.x = (float) (((float) Integer.parseInt(tmp[0]) / 1000f) * (float) (width / playing_field.length)) + 4f;
                        second_player.y = (float) (((float) Integer.parseInt(tmp[1]) / 1000f) * (float) (width / playing_field.length)) + 4f;
                    }
                }
            } catch (IOException e) {
                TOAST("Ошибка подключения!");
                restart = true;
                //e.printStackTrace();
            }
        }
    }

    public class server implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(2048, 3);
                Log.d("CREATION", "CREATE SERVER");
                while (true) {
                    client = server.accept();
                    Log.d("CREATION", "NEW CLIENT");
                    new Thread(new Conn()).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class send_xy implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    while (true) {
                        Log.d("CREATION", "SEND XY");

                        out.println(String.valueOf((int) ((first_player.x / (float) (width / playing_field.length) * 0.8 + first_player.x_old / (float) (width / playing_field.length) * 0.2) * 1000)) + ":" +
                                String.valueOf((int) ((first_player.y / (float) (width / playing_field.length) * 0.8 + first_player.y_old / (float) (width / playing_field.length) * 0.2) * 1000)));
                        first_player.x_old = first_player.x;
                        first_player.y_old = first_player.y;
                        SystemClock.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class Conn implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("CREATION", "CONNECT");
                toast_server = false; view = true;
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                for (int x = 0; x < playing_field.length; x++) {
                    for (int y = 0; y < playing_field[0].length; y++) {
                        out.println(playing_field[x][y]);
                        SystemClock.sleep(100);
                    }
                }
                //start_start_xy(out);
                new Thread(new send_xy()).start();
                while (true) {

                    String str = input.readLine();
                    Log.d("CREATION", "OK SEND   " + str);
                    if (str != null) {
                        String[] tmp = str.split(":");
                        second_player.x = (float) (((float) Integer.parseInt(tmp[0]) / 1000f) * (float) (width / playing_field.length));
                        second_player.y = (float) (((float) Integer.parseInt(tmp[1]) / 1000f) * (float) (width / playing_field.length));
                    }
                    //out.println(String.valueOf((int)(x_ball*0.8 + x_ball_old*0.2)) + ":" + String.valueOf((int)(y_ball*0.8 + y_ball_old*0.2))); x_ball_old = x_ball; y_ball_old = y_ball;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

