package com.absoluteguitar.mob;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;


public class ABSMainActivity extends ActionBarActivity {


    private final static int RATE = 8000*2;
    private final static int CHANNEL_MODE = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final static int BUFFER_SIZE_IN_MS = 3000;
    private final static int CHUNK_SIZE_IN_SAMPLES = 4096*2;
    private final static int CHUNK_SIZE_IN_MS = 1000 * CHUNK_SIZE_IN_SAMPLES / RATE;
    private final static int BUFFER_SIZE_IN_BYTES = RATE * BUFFER_SIZE_IN_MS / 1000 * 2;
    private final static int CHUNK_SIZE_IN_BYTES = RATE * CHUNK_SIZE_IN_MS / 1000 * 2;
    private final static int MIN_FREQUENCY = 60;
    private final static int MAX_FREQUENCY = 1200;
    private final static String APP_TAG = "AbsolutePitch";

    private boolean listening;
    private String results;
    private ToggleButton toggleButton;
    private TextView display;
    private TextView display3;
    private ImageView fretsImage;
    private Handler handler = new Handler();
    private NumberFormat nf = NumberFormat.getInstance();
    private Thread listenerThread;
    private LinkedHashSet<String> possibleKeys;
    private LinkedHashMap<Double, Double> possibleFrequencies;



    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absmain);
        toggleButton = (ToggleButton) findViewById( R.id.toggleButton1 );
        display = (TextView) findViewById(R.id.display);
        display3 = (TextView) findViewById(R.id.display3);
        possibleKeys = new LinkedHashSet<String>();
        possibleFrequencies = new LinkedHashMap<Double, Double>();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        Bitmap frets = BitmapFactory.decodeResource(getResources(), R.drawable.frets);
        Bitmap scaledFrets = Bitmap.createScaledBitmap( frets, 800, 300, true );
        fretsImage = (ImageView) findViewById( R.id.frets );
        fretsImage.setImageBitmap(scaledFrets);
    }




    public void listen( View view ) {
        Object obj = view.getTag();
        if( listening ) {
            stopListening();
        } else {
            startListening();
        }
    }



    private void startListening() {
        listening = true;
        listenerThread = new Thread( new Listener() );
        listenerThread.start();
    }



    private void stopListening() {
        listening = false;
        listenerThread.interrupt();
        listenerThread = null;
    }



    private double getFrequency( short [] audio_data, int bufferSize ) {

        double [] data = new double[CHUNK_SIZE_IN_SAMPLES * 2];
        AbsoluteGuitarFFT fft = new AbsoluteGuitarFFT();

        for( int i = 0; i < CHUNK_SIZE_IN_SAMPLES; i++ ) {
            data[i * 2] = audio_data[i];
            data[i * 2 + 1] = 0;
        }

        fft.doFFTInternal( data, CHUNK_SIZE_IN_SAMPLES );
        double bestFrequency = MIN_FREQUENCY;
        double bestAmplitude = MAX_FREQUENCY;
        results = "";
        possibleKeys.clear();

        for( int i=MIN_FREQUENCY; i<=MAX_FREQUENCY; i++ ) {

            double currentFrequency = i * 1.0;
            double currentAmplitude = Math.pow( data[ i * 2], 2 ) + Math.pow( data[ i * 2 + 1], 2 );
            double normalizedAmplitude = currentAmplitude * Math.pow( MIN_FREQUENCY * MAX_FREQUENCY, 0.5 ) / currentFrequency;

            if( normalizedAmplitude > bestAmplitude ) {
                bestFrequency = currentFrequency;
                bestAmplitude = normalizedAmplitude;
            }

            final String key = getPitch(currentFrequency);

            if( normalizedAmplitude > 2.0E10 || key==null || key.equals("") ) {
                possibleFrequencies.put(currentFrequency, normalizedAmplitude);
                possibleKeys.add(key);
            }

        }

        String key = getPitch(bestFrequency);
        if( bestAmplitude < 2.0E10 || key==null || key.equals("") ) {
            bestFrequency = 0.0;
        }
//		else {
//			Iterator<Double> it = possibleFrequencies.keySet().iterator();
//			while( it.hasNext() ) {
//				double freq = it.next();
//				double amp = possibleFrequencies.get(freq);
//				System.out.println(freq + " | " + amp);
//			}
//			System.out.println("-----------------------------------------------------");
//		}

        return bestFrequency;
    }



    private String getPitch( double frequency ) {

        String key = "";

        if ( frequency >= 65.41 && frequency < 69.30 ) key = "C2";
        else if ( frequency >= 69.30 && frequency < 73.42 ) key = "C2#";
        else if ( frequency >= 73.42 && frequency < 77.78 )	key = "D2";
        else if ( frequency >= 77.78 && frequency < 82.41 ) key = "D2#";
        else if ( frequency >= 82.41 && frequency < 87.31 ) key = "E2";
        else if ( frequency >= 87.31 && frequency < 92.5 ) key = "F2";
        else if ( frequency >= 92.5 && frequency < 98.0 ) key = "F2#";
        else if ( frequency >= 98.0 && frequency < 103.83 ) key = "G2";
        else if ( frequency >= 103.83 && frequency < 110.0 ) key = "G2#";
        else if ( frequency >= 110.0 && frequency < 116.54 ) key = "A2";
        else if ( frequency >= 116.54  && frequency < 123.47 ) key = "A2#";
        else if ( frequency >= 123.47 && frequency < 130.81 ) key = "B2";
        else if ( frequency >= 130.81 && frequency < 138.59 ) key = "C3";
        else if ( frequency >= 138.59 && frequency < 146.83 ) key = "C3#";
        else if ( frequency >= 146.83 && frequency < 155.56 ) key = "D3";
        else if ( frequency >= 155.56 && frequency < 164.81 ) key = "D3#";
        else if ( frequency >= 164.81 && frequency < 174.61 ) key = "E3";
        else if ( frequency >= 174.61 && frequency < 185.0 ) key = "F3";
        else if ( frequency >= 185.0 && frequency < 196.0 ) key = "F3#";
        else if ( frequency >= 196.0 && frequency < 207.65 ) key = "G3";
        else if ( frequency >= 207.65 && frequency < 220.0 ) key = "G3#";
        else if ( frequency >= 220.0 && frequency < 233.08 ) key = "A3";
        else if ( frequency >= 233.08 && frequency < 246.94 ) key = "A3#";
        else if ( frequency >= 246.94 && frequency < 261.63 ) key = "B3";
        else if ( frequency >= 261.63 && frequency < 277.18 ) key = "MC";
        else if ( frequency >= 277.18 && frequency < 293.66 ) key = "C4#";
        else if ( frequency >= 293.66 && frequency < 311.13 ) key = "D4";
        else if ( frequency >= 311.13 && frequency < 329.63 ) key = "D4#";
        else if ( frequency >= 329.63 && frequency < 349.23 ) key = "E4";
        else if ( frequency >= 349.23 && frequency < 369.99 ) key = "F4";
        else if ( frequency >= 369.99 && frequency < 392.0 ) key = "F4#";
        else if ( frequency >= 392.0 && frequency < 415.30 ) key = "G4";
        else if ( frequency >= 415.30 && frequency < 440.0 ) key = "G4#";
        else if ( frequency >= 440.0 && frequency < 466.16 ) key = "A4";
        else if ( frequency >= 466.16 && frequency < 493.88 ) key = "A4#";
        else if ( frequency >= 493.88 && frequency < 523.25 ) key = "B4";
        else if ( frequency >= 523.25 && frequency < 554.37 ) key = "C5";
        else if ( frequency >= 554.37 && frequency < 587.33 ) key = "C5#";
        else if ( frequency >= 587.33 && frequency < 622.25 ) key = "D5";
        else if ( frequency >= 622.25 && frequency < 659.26 ) key = "D5#";
        else if ( frequency >= 659.26 && frequency < 698.46 ) key = "E5";
        else if ( frequency >= 698.46 && frequency < 739.99 ) key = "F5";
        else if ( frequency >= 739.99 && frequency < 783.99 ) key = "F5#";
        else if ( frequency >= 783.99 && frequency < 830.61 ) key = "G5";
        else if ( frequency >= 830.61 && frequency < 880.00 ) key = "G5#";
        else if ( frequency >= 880.00 && frequency < 932.33 ) key = "A5";
        else if ( frequency >= 932.33 && frequency < 987.77 ) key = "A5#";
        else if ( frequency >= 987.77 && frequency < 1046.50 ) key = "B5";
        else if ( frequency >= 1046.50 && frequency < 1108.73 ) key = "C6";

        return key;
    }




    private class Listener implements Runnable {

        public void run() {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int bufferSize = AudioRecord.getMinBufferSize(RATE, CHANNEL_MODE, ENCODING); //get the buffer size to use with this audio record
            AudioRecord recorder = new AudioRecord (MediaRecorder.AudioSource.MIC, RATE, CHANNEL_MODE, ENCODING, 6144 ); //instantiate the AudioRecorder

            while(listening) {  //loop while recording is needed

                if( recorder.getState() == android.media.AudioRecord.STATE_INITIALIZED ) { // check to see if the recorder has initialized yet.

                    if( recorder.getRecordingState() == android.media.AudioRecord.RECORDSTATE_STOPPED ) {
                        recorder.startRecording();  //check to see if the Recorder has stopped or is not recording, and make it record.
                    }

                    short[] audio_data = new short[BUFFER_SIZE_IN_BYTES / 2];
                    recorder.read( audio_data, 0, CHUNK_SIZE_IN_BYTES / 2 );
                    final double frequency = getFrequency( audio_data, bufferSize );
                    final String key = getPitch( frequency );

                    // post to UI
                    handler.post(
                            new Runnable() {
                                public void run() {
                                    if( frequency > 0.0 ) {

                                        display.setText( key + "  " + nf.format(frequency) + "Hz" );
                                        results = "Possible Keys: \n";
                                        for( String s: possibleKeys) {
                                            results += s + "\n";
                                        }
                                        Bitmap frets = BitmapFactory.decodeResource( getResources(), getFretImageId( key ) );
                                        Bitmap scaledFrets = Bitmap.createScaledBitmap( frets, 800, 300, true );
                                        fretsImage.setImageBitmap(scaledFrets);
                                        display3.setText( results );
                                    }
                                }
                            }
                    );

                }
            } //while recording

            if ( recorder.getState() == android.media.AudioRecord.RECORDSTATE_RECORDING ) {
                recorder.stop(); //stop the recorder before ending the thread
            }
            recorder.release(); //release the recorders resources
            recorder = null; //set the recorder to be garbage collected.

            Log.i(APP_TAG, "Process completed...");
        }

    }


    private int getFretImageId( String key ) {

        if( key.contains("C") )	return R.drawable.c;
        else if( key.contains("C#") ) return R.drawable.c_sharp;
        else if( key.contains("D") ) return R.drawable.d;
        else if( key.contains("D#") ) return R.drawable.d_sharp;
        else if( key.contains("E") ) return R.drawable.e;
        else if( key.contains("F") ) return R.drawable.f;
        else if( key.contains("F#") ) return R.drawable.f_sharp;
        else if( key.contains("G") ) return R.drawable.g;
        else if( key.contains("G#") ) return R.drawable.g_sharp;
        else if( key.contains("A") ) return R.drawable.a;
        else if( key.contains("A#") ) return R.drawable.a_sharp;
        else if( key.contains("B") ) return R.drawable.b;
        return R.drawable.frets;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_absmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
