package com.example.joem.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    ExecutorService executorServiceName;//used to create threadpools below

    //construct that acts like a mailbox which the child thread can deposit messages that arrive at and read by main thread by accessing said mailbox
    Handler handlerName;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //progressBar = new ProgressBar(this);//still need to figure out how progressBar works
        //progressBar.setMax(100);

        executorServiceName = Executors.newFixedThreadPool(4); //initializer initializing threadpools, creating 4

        handlerName = new Handler(new Handler.Callback() { //instantiates handler, uses a callback interface that we implement
            @Override //method handleMessage
            public boolean handleMessage(Message message) {
                //prints message below, here it's what iteration of outer loop of doWork
                //can use: message.what OR message.obj if sending object (both declared below)
                Log.d("demo", "Message received....." + message.obj);
                switch (message.what){//switch statement showing where in the process when we are communicating threads

                    case DoWork.STATUS_START:
                        //progressBar.setProgress(0);//starts at 0% when doWork begins
                        Log.d("demo", "Starting.....");
                        break;
                    case DoWork.STATUS_STOP:
                        Log.d("demo", "Stopping.....");
                        break;
                    case DoWork.STATUS_PROGRESS:
                        //progressBar.setProgress(message.getData().getInt(DoWork.PROGRESS_KEY));//setProgress=value from message
                        //can use: message.obj OR message.getData().getInt(keyHere)
                        Log.d("demo", "Progress....." + message.getData().getInt(DoWork.PROGRESS_KEY));
                        break;
                }
                return false; //false=not handled by mainActivity and uses default behavior of handler to be executed
                                //true=done handling messages at this level and will not be trickled down to main handler to handle again
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//thread takes in runnable target
                //Thread threadName = new Thread(new DoWork(), "Worker 1");//'runnable' to run fake work below
                //threadName.start();//starts fake work
                executorServiceName.execute(new DoWork());//executes fake work in thread pool
            }
        });
        //new Thread(new DoWork()).start();//starts doWork on creation

        Log.d("demo", "onCreate thread ID is " + Thread.currentThread().getId());//prints thread ID to logcat debug onCreate

        new DoWorkAsync().execute("Bob", "Alice");//runs DoWorkAsync; string array that is passed to doInBackground
    }
    class DoWorkAsync extends AsyncTask<String, Integer, Double>{

        @Override
        protected void onPreExecute() {//executes in main thread
            Log.d("demo", "onPreExecute thread ID is " + Thread.currentThread().getId());
        }

        @Override
        protected void onPostExecute(Double aDouble) {//whatever 'doInBackground' does appears here in post execute; executes in main thread
            Log.d("demo", "onPostExecute thread ID is " + Thread.currentThread().getId());
            Log.d("demo", "onPostExecute aDouble is " + aDouble);//showing that value returned by doInBackground goes to onPostExecute
        }

        @Override
        protected void onProgressUpdate(Integer... values) {//executes in main thread
            Log.d("demo", "onProgressUpdate thread ID is " + Thread.currentThread().getId());
        }

        @Override
        protected Double doInBackground(String... strings) {//background method that executes in child thread; '...'=array
            Log.d("demo", "doInBackground strings params is " + strings);//prints string params
            Log.d("demo", "doInBackground thread ID is " + Thread.currentThread().getId());
            publishProgress(100);//publishProgress triggers onProgressUpdate to run
            return 333.33;//double that is returned
        }
    }

    class DoWork implements Runnable{
        //status integers created below to send integers
        static final int STATUS_START = 0x00;
        static final int STATUS_PROGRESS = 0x01;
        static final int STATUS_STOP = 0x02;
        //string created to send bundle
        static final String PROGRESS_KEY = "PROGRESS";

        @Override//implements interface method
        public void run() {

            //sends message when method starts
            Message startMessage = new Message();
            startMessage.what = STATUS_START;
            handlerName.sendMessage(startMessage);

            Log.d("demo", "Started work.....");//printing to logcat to see what we're doing
            for (int i=0; i<100; i++){//creating fake work to run in background or another thread
                for(int j=0; j<1000; j++){
                }
                Message messageName = new Message();
                messageName.what = i;//what=(integer) status parameter indicated status, here it's how far we're into the outer loop of doWork
                messageName.what = STATUS_PROGRESS; //same as '.what' above but using STATUS_PROGRESS instead of i
                messageName.obj = (Integer)i;//sends 'i' as an (integer) object

                Bundle bundleName = new Bundle();//creating bundle to send via thread
                bundleName.putInt(PROGRESS_KEY, (Integer)i);
                messageName.setData(bundleName);

                //handlerName.sendEmptyMessage(100); //empty message showing message is being sent
                handlerName.sendMessage(messageName);
            }
            Log.d("demo", "Ended work.....");//printing to logcat to see what we're doing
            //sends message when method stops
            Message stopMessage = new Message();
            stopMessage.what = STATUS_STOP;
            handlerName.sendMessage(stopMessage);
        }
    }
}
