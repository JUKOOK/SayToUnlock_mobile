package com.example.administrator.saytounlock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.naver.speech.clientapi.SpeechRecognitionResult;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CLIENT_ID = "95oychpq0k";
    private static final String CLIENT_SECRET = "Enter your Client Secret ID of application key";
    private RecognitionHandler Nhandler;
    private NaverRecognizer naverRecognizer;
    private TextView TV_process, TV_state, TV_keyword;
    private Button btn_start, btn_open, btn_temp;
    private String Result; // Speech Recognizer 용 결과문
    private AudioWriterPCM writer;
    private RandomKeyword RK;
//    private String [] Keywords = {"House", "Elephant", "Intelligence", "Language", "Smart", "Plastic", "Concrete", "Alaska", "Tempest", "Segment"}; // Random selected keywords
    private String MyKeyword = ""; // Selected one keyword

    private File open_file;

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
//    private final String ip = "10.64.148.71";
   // private final String ip = "192.168.0.3"; // JUKOOK_5G
    private final String ip = "192.168.0.37"; // Anthouse_5G
    private final int port = 8887;
    private MyHandler myHandler;
    private MyThread myThread;
    private String mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test.wav";

    // First condition to Unlock : True -> Voice analysis
    boolean IsSameKeyword = false; // Speech Recognition result

    class MyThread extends Thread {
        @Override
        public void run() {
            try {
                clientSocket = new Socket(ip, port);
                socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                socketOut = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                open_file = new File(mPath);
                dataInputStream = new DataInputStream(new FileInputStream(open_file));
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            }
            catch (IOException e) {
                e.printStackTrace();
//                Log.e("RUN .CLOSE()", e.toString());
            }

            String data;
            try {
                while((data = socketIn.readLine()) != null) {
                    Message msg = myHandler.obtainMessage();
                    msg.obj = data;
                    myHandler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
//                Log.e("MESSAGE .CLOSE()", e.toString());
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj.toString().equals("success")) {
                TV_state.setText("Unlock Success");
                Result = "Keyword Matching ... Clear\nVoice Matching ... Clear";
                btn_start.setEnabled(false);
                btn_open.setVisibility(View.VISIBLE);
                btn_open.setText(R.string.btn_open);
                btn_open.bringToFront();
            } else if (msg.obj.toString().equals("fail")) {
                TV_state.setText("Deny");
                Result = "Keyword Matching ... Clear\nVoice Matching ... Deny";
                btn_start.setText("Retry");
                btn_start.setEnabled(true);
                myThread.interrupt();
            }
//            TV_process.setText(msg.obj.toString());
            TV_process.setText(Result);
        }
    }

   // Handle speech recognition Messages.
    private void myhandleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady: // 음성인식 준비 가능
                TV_state.setText("Connected, Ready");
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;
            case R.id.audioRecording:
                TV_state.setText("AudioRecording");
                writer.write((short[]) msg.obj);
                break;
            case R.id.partialResult:
                TV_state.setText("PartialResult");
                break;
            case R.id.finalResult:
                TV_state.setText("FinalResult");
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();
                for(String result : results) {
                    if(result.toLowerCase().equals(MyKeyword.toLowerCase()) && !IsSameKeyword) {
                        IsSameKeyword = true;
                    }
                }
                if (IsSameKeyword) { // 단어 판별 통과
                    Result = "Keyword Matching ... Clear\nVoice Matching ... Ready";
                } else {
                    Result = "Keyword Matching ... Deny\nVoice Matching ... Deny";
                }
                TV_process.setText(Result);
                break;
            case R.id.recognitionError:
                TV_state.setText("RecognitionError");
                if (writer != null) {
                    writer.close();
                }
                Result = "Error code : " + msg.obj.toString();
                TV_process.setText(Result);
                IsSameKeyword = false;
                btn_start.setText("Start");
                btn_start.setEnabled(true);
                break;
            case R.id.clientInactive:
                TV_state.setText("Client Inactive");
                if (writer != null) {
                    writer.close();
                }
                if(IsSameKeyword) {
                    File f1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest/Test.pcm");
                    // The location of your PCM file
                    File f2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test.wav");
                    // The location where you want your WAV file

                    // pcm -> wav
                    try {
                        writer.rawToWave(f1, f2); // pcm -> wav
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // File transfer to Server
                    try {
                        byte[] buf = new byte[1024];

                        while(dataInputStream.read(buf) > 0) {
                            dataOutputStream.write(buf);
                            dataOutputStream.flush();
                        }
                        socketOut.println("end");
                        socketOut.flush();
                    } catch (IOException e) {
                        TV_process.setText(e.toString());
                    }
                } else {
                    TV_state.setText("Deny");
                    btn_start.setText("Retry");
                    btn_start.setEnabled(true);
                }

                // TV_process.setText("Keyword Matching ... Ready\nVoice Matching ... Ready"); // Process Restart.
                break;
        }
    }

    // Declare handler for handling SpeechRecognizer thread's Messages.
    static class RecognitionHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        RecognitionHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.myhandleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 승인
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        TV_keyword = findViewById(R.id.TV_keyword);
        TV_keyword.setText("ㆍ ㆍ ㆍ"); // Selecting
        TV_state = findViewById(R.id.TV_state);
        TV_state.setText("Normal"); // Normal state
        TV_process = findViewById(R.id.TV_process);
        TV_process.setText("Keyword Matching ... Ready\nVoice Matching ... Ready"); // Process Start.
        btn_start = findViewById(R.id.btn_check);
        btn_start.setText("Start");
        btn_temp = findViewById(R.id.btn_check2);
        btn_open = findViewById(R.id.btn_open);

        // Thread Start, Socket Open, Initialize condition
        try {
            myHandler = new MyHandler();
            myThread = new MyThread();
            myThread.start();
        } catch (Exception e){
            e.printStackTrace();
        }

        Nhandler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, Nhandler, CLIENT_ID);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize condition
                IsSameKeyword = false;
                if (!naverRecognizer.getSpeechRecognizer().isRunning()) {
                    //  TV_keyword select randomly
//                    Random random = new Random();
//                    MyKeyword = Keywords[random.nextInt(Keywords.length)];
                    RK = new RandomKeyword();
                    MyKeyword = RK.getKeyword();
                    TV_state.setText("Connecting...");
                    TV_keyword.setText(MyKeyword);
                    TV_process.setText("Keyword Matching ... Ready\nVoice Matching ... Ready"); // Process Restart.
                    Result = "";
                    btn_start.setText("Stop");

                    if(!myThread.isAlive()) {
                        try {
                            myHandler = new MyHandler();
                            myThread = new MyThread();
                            myThread.start();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    naverRecognizer.recognize();
                } else {
                    Log.d(TAG, "stop and wait Final Result");
                    btn_start.setEnabled(false);

                    naverRecognizer.getSpeechRecognizer().stop();
                }
            }
        });
        btn_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_open.setVisibility(View.VISIBLE);
                btn_open.setText(R.string.btn_open);
                btn_open.bringToFront();
            }
        });
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        naverRecognizer.getSpeechRecognizer().initialize(); // 음성인식 서버 초기화
    }

    @Override
    protected void onResume() {
        super.onResume();
        Result = "";
        btn_start.setEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        naverRecognizer.getSpeechRecognizer().release(); // 음성인식 서버 종료
        try {
            dataInputStream.close();
            dataOutputStream.close();
            clientSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            dataInputStream.close();
            dataOutputStream.close();
            clientSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
