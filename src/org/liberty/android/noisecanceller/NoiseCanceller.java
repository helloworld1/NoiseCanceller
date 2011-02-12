/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package org.liberty.android.noisecanceller;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.TextView;
import android.widget.SeekBar;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class NoiseCanceller extends Activity implements SeekBar.OnSeekBarChangeListener
{
    NoiseProcessor np = null;
    SeekBar mAmpSeekBar;
    SeekBar mAngleSeekBar;
    TextView mAmpText;
    TextView mAngleText;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = (TextView)findViewById(R.id.main_text);
        mAmpSeekBar = (SeekBar)findViewById(R.id.ampseek);
        mAngleSeekBar = (SeekBar)findViewById(R.id.angleseek);

        mAmpText = (TextView)findViewById(R.id.main_amp);
        mAngleText = (TextView)findViewById(R.id.main_phase);

        mAmpSeekBar.setOnSeekBarChangeListener(this);
        mAngleSeekBar.setOnSeekBarChangeListener(this);

        boolean success = true;
        String errStr = "";
        try{
            np = new NoiseProcessor();
        }
        catch(Exception e){
            np = null;
            success = false;
            errStr = e.toString();
        }
        if(success == true){

            np.start();
            mAngleText.setText("Phase shifting angle: " + np.getAngle() + " degree");
            mAmpText.setText("Amplitude: " + np.getAmp());
        }
        else{
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Error");
                alert.setMessage("Can't initial audio device: " + errStr);
                alert.setPositiveButton("Quit", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which ){
                            finish();
                        }
                        });
                alert.show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(np == null){
            return;
        }
        if(np.getStatus() == false){
            np.start();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(np != null){
            np.destroy();
        }
    }
        

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return true;
    }



    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
    {
        if(seekBar == mAngleSeekBar){
            np.setAngle(progress);
            mAngleText.setText("Phase shifting angle: " + np.getAngle() + " degree");
        }
        if(seekBar == mAmpSeekBar){
            np.setAmp((float)progress / 100.0f);
            mAmpText.setText("Amplitude: " + np.getAmp());
        }
    }
    
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()){
            case R.id.start_sampling:
                np.pauseRecord();
                return true;

            case R.id.stop_sampling:
                np.resumeRecord();
                return true;
            case R.id.about:
                
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("About NoiseCanceller");
                alert.setMessage("Ver: 1.0\nAuthor: Haowen Ning\nHow it works: This application will try to generate an phase-inversed wave of the noise so it can cancel out the noise. But android API makes some delay between recording and playing, so the user needs to adjust the phase accordingly");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which ){
                        }
                        });
                alert.show();
                return true;
        }
        return false;
    }

        


}
