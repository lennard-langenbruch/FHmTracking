package com.example.myapplication22;

import android.util.Log;
import android.widget.TextView;
import org.apache.commons.lang3.time.StopWatch;

public class StopWatchHelper {

    private final StopWatch stopWatch;
    private final TextView textView;
    private boolean running;
    private Thread updateThread;

    private String totalFormattedTime;
    private long elapsedSecondsGlobal;


    public StopWatchHelper(TextView textView) {
        this.textView = textView;
        stopWatch = new StopWatch();
        running = false;
    }
    public String getTotalFormattedTime() {
        return totalFormattedTime;
    }

    public boolean isRunning() {
        return running;
    }

    public long getElapsedSecondsGlobal() {
        return elapsedSecondsGlobal;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void start() {
        if (!running) {
            running = true;
            stopWatch.start();

            // Create a new thread for updating the UI
            updateThread = new Thread(() -> {
                while (running) {
                    update();
                    try {
                        Thread.sleep(100); // Update every 100 milliseconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            updateThread.start(); // Start the thread
        }
    }

    public void stop() {
        if (running) {
            running = false;
            stopWatch.stop();
            try {
                updateThread.join(); // Wait for the update thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        if (stopWatch.isStarted()) {
            long elapsedMilliseconds = stopWatch.getTime();

            long elapsedSeconds = elapsedMilliseconds / 1000;
            long elapsedMinutes = elapsedSeconds / 60;

            elapsedSeconds %= 60;

            elapsedSecondsGlobal = elapsedSeconds;

            Log.d(String.valueOf(StopWatchHelper.class), String.valueOf(elapsedSecondsGlobal));

            elapsedMilliseconds %= 1000;

            totalFormattedTime = String.format("Elapsed Time: %02d:%02d:%02d", elapsedMinutes, elapsedSeconds, elapsedMilliseconds / 10);

            textView.post(() -> textView.setText(totalFormattedTime));
        }
    }
}
