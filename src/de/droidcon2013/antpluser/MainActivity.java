package de.droidcon2013.antpluser;

import java.math.BigDecimal;
import java.util.Timer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dsi.ant.plugins.AntPluginMsgDefines;
import com.dsi.ant.plugins.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataTimestampReceiver;

public class MainActivity extends Activity {

    final String TAG = "Pulse";

    AntPlusHeartRatePcc hrPcc = null;

    TextView tvBpm;
    TextView tvLastTime;
    RelativeLayout background;

    Timer timer = new Timer();

    int computedHeartRate = -1;
    BigDecimal lastEvent;
    boolean stillRunning = false;

    boolean firstRun = true;

    boolean lastColorRed = false;

    CountDownTimer counter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        background = (RelativeLayout) findViewById(R.id.background);
        tvBpm = (TextView) findViewById(R.id.bpm);

        tvLastTime = (TextView) findViewById(R.id.lastTime);

        if (hrPcc != null) {
            hrPcc.releaseAccess();
            hrPcc = null;
        }

        counter = new MyCounter(Integer.MAX_VALUE, 500).start();


        AntPlusHeartRatePcc.requestAccess(this, this, false,
                new IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                    @Override
                    public void onResultReceived(AntPlusHeartRatePcc result,
                            int resultCode, int initialDeviceStateCode) {
                        switch (resultCode) {
                        case AntPluginMsgDefines.MSG_REQACC_RESULT_whatSUCCESS:
                            hrPcc = result;
                            subscribeToEvents();
                            break;
                        case AntPluginMsgDefines.MSG_REQACC_RESULT_whatCHANNELNOTAVAILABLE:
                            break;
                        case AntPluginMsgDefines.MSG_REQACC_RESULT_whatOTHERFAILURE:
                            break;
                        case AntPluginMsgDefines.MSG_REQACC_RESULT_whatDEPENDENCYNOTINSTALLED:
                            break;
                        case AntPluginMsgDefines.MSG_REQACC_RESULT_whatUSERCANCELLED:
                            break;
                        default:
                            break;
                        }
                    }

                    private void subscribeToEvents() {

                        hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver() {
                            @Override
                            public void onNewHeartRateData(
                                    final int currentMessageCount,
                                    final int computedHeartRate,
                                    final long heartBeatCounter) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
                                        MainActivity.this.computedHeartRate = computedHeartRate;
                                        tvBpm.setText(String
                                                .valueOf(computedHeartRate));
                                        // tv_heartBeatCounter.setText(String.valueOf(heartBeatCounter));
                                        /*
                                         * if (MainActivity.this.firstRun) {
                                         * MainActivity.this.timer.schedule(
                                         * changeColorTask,
                                         * MainActivity.this.getHeartRate() );
                                         * MainActivity.this.firstRun = false; }
                                         */
                                    }
                                });
                            }
                        });

                        hrPcc.subscribeHeartRateDataTimestampEvent(new IHeartRateDataTimestampReceiver() {
                            @Override
                            public void onNewHeartRateDataTimestamp(
                                    final int currentMessageCount,
                                    final BigDecimal timestampOfLastEvent) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!timestampOfLastEvent
                                                .equals(MainActivity.this.lastEvent)) {
                                            MainActivity.this.stillRunning = true;
                                            if (computedHeartRate > 0 && firstRun ) {
                                                Log.d( TAG, "Starting counter" );
//                                                counter = new MyCounter(Integer.MAX_VALUE, getHeartRate()).start();
                                                firstRun = false;
                                            }
                                        } else {
                                            MainActivity.this.stillRunning = false;
                                        }

                                        MainActivity.this.lastEvent = timestampOfLastEvent;
                                        tvLastTime.setText(String
                                                .valueOf(timestampOfLastEvent));

                                        // tv_timestampOfLastEvent.setText(String.valueOf(timestampOfLastEvent));
                                    }
                                });
                            }
                        });

                    }
                }, new IDeviceStateChangeReceiver() {
                    @Override
                    public void onDeviceStateChange(final int newDeviceState) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // tv_status.setText(sdmPcc.getDeviceName() +
                                // ": " +
                                // AntPlusStrideSdmPcc.statusCodeToPrintableString(newDeviceState));
                            }
                        });
                    }
                });
    }

    class MyCounter extends CountDownTimer {
        int i = 0;

        public MyCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onFinish() {
            this.start();
        }

        @Override
        public void onTick(long arg0) {
            i++;
            Log.d( TAG, "Changing color" );
            background.setBackgroundColor(132 * i + 50);
            background.invalidate();
//            background.setBackgroundColor(getNextColor());
        }
    }

    protected long getHeartRate() {
        return 60000 / this.computedHeartRate;
    }

    /*
     * public TimerTask changeColorTask = new TimerTask() {
     *
     * @Override public void run() { MainActivity.this.runOnUiThread( new
     * Runnable() {
     *
     * @Override public void run() {
     * MainActivity.this.background.setBackgroundColor(
     * MainActivity.this.getNextColor() ); // TimerTask.this.cancel();
     * MainActivity.this.timer.purge(); MainActivity.this.timer.schedule(
     * changeColorTask, MainActivity.this.getHeartRate() ); } } ); }
     *
     * };
     */




//    public class ChangeColorTask extends TimerTask {
//
//        Activity act;
//
//        public ChangeColorTask( Activity act) {
//            this.act = act;
//        }
//
//        @Override
//        public void run() {
//            MainActivity.this.runOnUiThread( new Runnable() {
//
//                @Override
//                public void run() {
//                    MainActivity.this.background.setBackgroundResource( MainActivity.this.getNextColor() );
//                    MainActivity.this.scheduleTask();
//                }
//            });
//        }
//    }


//    private void scheduleTask() {
//        MainActivity.this.timer = new Timer();
//        ChangeColorTask newTask = new ChangeColorTask( this );
//        MainActivity.this.timer.schedule( newTask, MainActivity.this.getHeartRate() );
//    }

    private int getNextColor() {
        if (lastColorRed) {
            lastColorRed = false;
            return R.color.yellow;
        } else {
            lastColorRed = true;
            return R.color.white;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( hrPcc != null ) {
            hrPcc.releaseAccess();
        }
        //sdmPcc.releaseAccess();
    }
}
