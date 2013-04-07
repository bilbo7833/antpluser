package de.droidcon2013.antpluser;

import java.math.BigDecimal;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dsi.ant.plugins.AntPluginMsgDefines;
import com.dsi.ant.plugins.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataTimestampReceiver;

public class MainActivity extends Activity
{

    AntPlusHeartRatePcc hrPcc = null;

    TextView tvBpm;
    TextView tvLastTime;
    RelativeLayout background;

    int computedHeartRate;
    BigDecimal lastEvent;
    boolean stillRunning = false;




    boolean lastColorRed = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        background = (RelativeLayout) findViewById( R.id.background );
        tvBpm = (TextView)findViewById(R.id.bpm);
        tvLastTime = (TextView)findViewById(R.id.lastTime);


        if(hrPcc != null)
        {
            hrPcc.releaseAccess();
            hrPcc = null;
        }

        AntPlusHeartRatePcc.requestAccess(this, this, false,
                new IPluginAccessResultReceiver<AntPlusHeartRatePcc>()
                {
                    @Override
                    public void onResultReceived(AntPlusHeartRatePcc result, int resultCode,
                            int initialDeviceStateCode)
                    {
                        switch(resultCode)
                        {
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

                    private void subscribeToEvents()
                    {
//                        sdmPcc.subscribeInstantaneousSpeedEvent(
//                            new IInstantaneousSpeedReceiver()
//                            {
//                                @Override
//                                public void onNewInstantaneousSpeed(
//                                    final int currentMessageCount,
//                                    final BigDecimal instantaneousSpeed)
//                                {
//                                    runOnUiThread(
//                                        new Runnable()
//                                        {
//                                            @Override
//                                            public void run()
//                                            {
//
//                                                tvBpm.setText(String.valueOf(instantaneousSpeed));
//                                            }
//                                        });
//                                }
//
//                            });

                        hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver()
                        {
                            @Override
                            public void onNewHeartRateData(final int currentMessageCount,
                                    final int computedHeartRate, final long heartBeatCounter)
                            {
                                runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                //tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
                                                MainActivity.this.computedHeartRate = computedHeartRate;
                                                tvBpm.setText(String.valueOf(computedHeartRate));
                                                //tv_heartBeatCounter.setText(String.valueOf(heartBeatCounter));
                                            }
                                        });
                            }
                        });

                        hrPcc.subscribeHeartRateDataTimestampEvent(new IHeartRateDataTimestampReceiver()
                        {
                            @Override
                            public void onNewHeartRateDataTimestamp(final int currentMessageCount, final BigDecimal timestampOfLastEvent)
                            {
                                runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                if (!timestampOfLastEvent.equals( MainActivity.this.lastEvent )) {
                                                    MainActivity.this.stillRunning = true;
                                                } else {
                                                    MainActivity.this.stillRunning = false;
                                                }
                                                MainActivity.this.lastEvent = timestampOfLastEvent;
                                                tvLastTime.setText(String.valueOf(timestampOfLastEvent));

//                                                tv_timestampOfLastEvent.setText(String.valueOf(timestampOfLastEvent));
                                            }
                                        });
                            }
                        });

//                        sdmPcc.subscribeInstantaneousCadenceEvent(
//                            new IInstantaneousCadenceReceiver()
//                            {
//                                @Override
//                                public void onNewInstantaneousCadence(
//                                    final int currentMessageCount,
//                                    final BigDecimal instantaneousCadence)
//                                {
//                                    runOnUiThread(
//                                        new Runnable()
//                                        {
//                                            @Override
//                                            public void run()
//                                            {
//
//                                                tvLastTime.setText(String.valueOf(instantaneousCadence));
//                                            }
//                                        });
//                                }
//                            });







//                        sdmPcc.subscribeProductInformationEvent(
//                            new IProductInformationReceiver()
//                            {
//                                @Override
//                                public void onNewProductInformation(final int currentMessageCount, final int softwareRevision,
//                                        final long serialNumber)
//                                {
//                                    runOnUiThread(
//                                        new Runnable()
//                                        {
//                                            @Override
//                                            public void run()
//                                            {
//                                                //tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
//
//                                                //tv_softwareRevision.setText(String.valueOf(softwareRevision));
//                                                //tv_serialNumber.setText(String.valueOf(serialNumber));
//                                            }
//                                        });
//                                }
//                            });
                    }
                },
            new IDeviceStateChangeReceiver()
                {
                    @Override
                    public void onDeviceStateChange(final int newDeviceState)
                    {
                        runOnUiThread(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                  //tv_status.setText(sdmPcc.getDeviceName() + ": " + AntPlusStrideSdmPcc.statusCodeToPrintableString(newDeviceState));
                                }
                            });
                    }
                }
            );

    }

    public TimerTask changeColorTask = new TimerTask() {

    }

    private int getNextColor() {
        if (lastColorRed) {
            lastColorRed = false;
            return android.R.color.black;
        } else {
            lastColorRed = true;
            return android.R.color.white;
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
