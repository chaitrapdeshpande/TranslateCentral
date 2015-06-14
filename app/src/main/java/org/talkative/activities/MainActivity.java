package org.talkative.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.gtranslate.Language;
import com.gtranslate.Translator;

import org.languageutils.LanguageDescriptor;

import java.util.Locale;
import android.os.StrictMode;




public class MainActivity extends Activity implements TextToSpeech.OnInitListener
{

    private float pitchc=1.0f,speechRate=1.0f;
    private TextToSpeech tts;
    private EditText translateFromText;
    private EditText translateToText;
    private SeekBar sbPitch;
    private SeekBar sbSpeed;
    private Button okbutton;
    private ImageButton speakButton;
    private ImageButton clearButton;
    private CheckBox checkPitchSpeed;
    private TextView pitch;
    private TextView speed;
    private Button ratingButton;

    private TextView description;
    private TextView tdescription;
    private TextView feature;
    private TextView tfeature;
    private TextView howtouse;
    private TextView thowtouse;
    private TextView note;
    private TextView tnote;
    private Spinner fromDropdown;
    private Spinner toDropdown;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);

        //Show Icon in all screens
        ActionBar actionBar =getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffff9034")));

        fromDropdown = (Spinner)findViewById(R.id.fromSpinner);


        LanguageDescriptor[] supportedLanguages = Language.getInstance().getAllSupportedLanguages().toArray(new LanguageDescriptor[]{});



        ArrayAdapter<LanguageDescriptor> supportedLanguageAdapter =
                new ArrayAdapter<LanguageDescriptor>(this, android.R.layout.simple_spinner_item, supportedLanguages);

        fromDropdown.setAdapter(supportedLanguageAdapter);


        toDropdown = (Spinner)findViewById(R.id.toSpinner);
        String[] toitems = new String[]{"English(US)", "English(UK)", "Hindi(India)", "French(Canada)", "Italian", "German", "Korean"};


        toDropdown.setAdapter(supportedLanguageAdapter);

        checkPitchSpeed=(CheckBox)findViewById(R.id.checkBox_ps);
        speakButton=(ImageButton)findViewById(R.id.imageButton1_speakout);
        clearButton=(ImageButton)findViewById(R.id.imageButton2_clear);
        translateFromText=(EditText)findViewById(R.id.fromMultiline);
        translateToText=(EditText)findViewById(R.id.toMultiline);
        ratingButton = (Button)findViewById(R.id.button_Rate);

        description=(TextView)findViewById(R.id.textView1);
        tdescription=(TextView)findViewById(R.id.dtext);
        feature=(TextView)findViewById(R.id.textView2);
        tfeature=(TextView)findViewById(R.id.ftext);
        howtouse=(TextView)findViewById(R.id.textView3);
        thowtouse=(TextView)findViewById(R.id.htext);
        note=(TextView)findViewById(R.id.textView4);
        tnote=(TextView)findViewById(R.id.ntext);

        ratingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingButtonOnClick(v);
            }
        });

        translateToText.setEnabled(false);

        initializeSpeakClear();//Setting the Speak and Clear Buttons
        initializeAlertDialog();//Popping up the Alert Dialog

        //Setting up of TAB1,TAB2 and TAB3
        TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("",getResources().getDrawable(R.drawable.aap_home));

        TabHost.TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("",getResources().getDrawable(R.drawable.app_help));

        description.setTypeface(Typeface.SANS_SERIF);
        tdescription.setTypeface(Typeface.SANS_SERIF);
        feature.setTypeface(Typeface.SANS_SERIF);
        tfeature.setTypeface(Typeface.SANS_SERIF);
        howtouse.setTypeface(Typeface.SANS_SERIF);
        thowtouse.setTypeface(Typeface.SANS_SERIF);
        note.setTypeface(Typeface.SANS_SERIF);
        tnote.setTypeface(Typeface.SANS_SERIF);


        TabHost.TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("",getResources().getDrawable(R.drawable.app_rating));

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);

    }

    //Setting the Speak and Clear Buttons

    public void initializeSpeakClear()
    {
        speakButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                speakOut();
            }
        });

        clearButton=(ImageButton)findViewById(R.id.imageButton2_clear);
        clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                translateFromText.setText("");
                translateToText.setText("");
                //Toast.makeText(MainActivity.this, "Clearing the Message", Toast.LENGTH_LONG).show();
            }
        });
        tts = new TextToSpeech(getApplicationContext(), this);

    }

    @Override
    public void onInit(int status)
    {
        if(status!=TextToSpeech.ERROR_INVALID_REQUEST)
        {
            int outcome= tts.setLanguage(Locale.US);

            if(outcome==TextToSpeech.LANG_MISSING_DATA || outcome==TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Toast.makeText(MainActivity.this,"Sorry,This Language is not Supported",Toast.LENGTH_LONG).show();
            }

        }
        else
        {
            speakButton.setEnabled(true);
            Toast.makeText(this,"Ready to Speak",Toast.LENGTH_LONG).show();
            speakOut();
        }
    }

    private  void speakOut()
    {
        translateToText.setText("");
        LanguageDescriptor inputLanguage = (LanguageDescriptor)fromDropdown.getSelectedItem();
        LanguageDescriptor outputLanguage = (LanguageDescriptor)toDropdown.getSelectedItem();

        Log.i("LanguageDescriptor", String.format("Input language : %s - > Output language : %s", inputLanguage.getPrefix(), outputLanguage.getPrefix()));

        //set the output locale
        tts.setLanguage(Locale.forLanguageTag(outputLanguage.getPrefix()));

        String gettext=translateFromText.getText().toString();


        if(gettext.length()==0)
        {
            new TranslateTextTask().execute("Please enter the text to convert.", "en", inputLanguage.getPrefix());
            //tts.speak("Please Enter the Text",TextToSpeech.QUEUE_FLUSH,null);
            return;
        }

       // String settext=translateToText.setText("").toString();



        //translateToText.setText(translatedText);

        //TODO : If pitch and rate are 0.0 then do not set them.
        tts.setPitch((float) pitchc);
        tts.setSpeechRate((float) speechRate);

        new TranslateTextTask().execute(gettext, inputLanguage.getPrefix(), outputLanguage.getPrefix());




    }
    protected void onPause()
    {

        super.onPause();
        shutdownTTS();
    }
    protected void onDestroy()
    {
        super.onDestroy();
        shutdownTTS();
    }

    private void shutdownTTS()
    {
        if(tts!=null)
        {
            tts.stop();
            tts.shutdown();
        }
    }

        public void ratingButtonOnClick(View v)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Try Google play
        intent.setData(Uri.parse("market://details?id=org.mobileappsforfun.talkative"));
        if (!launchRatingActivity(intent)) {
            //Market (Google play) app seems not installed, let's try to open a webbrowser
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=org.mobileappsforfun.talkative"));
            if (!launchRatingActivity(intent)) {
                //Well if this also fails, we have run out of options, inform the user.
                Toast.makeText(this, "Could not open Android market, please install the market app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean launchRatingActivity(Intent intent)
    {
        try
        {
            startActivity(intent);
            return true;
        }
        catch (ActivityNotFoundException e)
        {
            return false;
        }
    }
    //Popping up the Alert Dialog
    public void initializeAlertDialog()
    {
        checkPitchSpeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setTitle("Pitch-Speed Selection");
                    dialog.setContentView(R.layout.alertdialog_layout);
                    dialog.show();

                    pitch = (TextView) dialog.findViewById(R.id.textView1_pitch);
                    sbPitch = (SeekBar) dialog.findViewById(R.id.seekBar_pitch);
                    speed = (TextView) dialog.findViewById(R.id.textView2_speed);
                    sbSpeed = (SeekBar) dialog.findViewById(R.id.seekBar_speed);
                    okbutton=(Button)dialog.findViewById(R.id.button_ok);

                    sbPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            pitch.setText("Pitch Range : " + progress);
                            pitchc = ((float) progress + 1) / 10;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                    sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            speed.setText("Speed Range : " + progress);
                            speechRate = ((float) progress + 1) / 10;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    okbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }
                    });
                } else {
                    pitchc = 1.0f;
                    speechRate = 1.0f;
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //TTS Engine for Speech Output
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        LinearLayout relative =(LinearLayout)findViewById(R.id.linear_main);

        switch (item.getItemId())
        {
            case R.id.langUS: tts.setLanguage(Locale.US);
                Toast.makeText(MainActivity.this,"English(US) Language is Selected",Toast.LENGTH_LONG).show();
                return true;

            case R.id.langFrenchCanada: tts.setLanguage(Locale.CANADA_FRENCH);
                Toast.makeText(MainActivity.this,"French(Canada) Language is Selected",Toast.LENGTH_LONG).show();
                return true;

            case R.id.langFrench: tts.setLanguage(Locale.FRENCH);
                Toast.makeText(MainActivity.this,"French Language is Selected",Toast.LENGTH_LONG).show();
                return true;
            case R.id.langGerman: tts.setLanguage(Locale.GERMAN);
                Toast.makeText(MainActivity.this,"German Language is Selected",Toast.LENGTH_LONG).show();
                return true;

            case R.id.langItalian: tts.setLanguage(Locale.ITALIAN);
                Toast.makeText(MainActivity.this,"Italian Language is Selected",Toast.LENGTH_LONG).show();
                return true;

            case R.id.langJapan: tts.setLanguage(Locale.JAPAN);
                Toast.makeText(MainActivity.this,"Japanese Language is Selected",Toast.LENGTH_LONG).show();
                return true;
            case R.id.langKorean: tts.setLanguage(Locale.KOREAN);
                Toast.makeText(MainActivity.this,"Korean Language is Selected",Toast.LENGTH_LONG).show();
                return true;

            case R.id.langUK: tts.setLanguage(Locale.UK);
                Toast.makeText(MainActivity.this,"English(UK) Language is Selected",Toast.LENGTH_LONG).show();
                return true;

            case R.id.langHindi: tts.setLanguage(new Locale("hi"));
                Toast.makeText(MainActivity.this,"Hindi(India) Language is Selected",Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //aync class to handle network activity
    private class TranslateTextTask extends AsyncTask<String,Void,String>
    {
        private Exception exception;
        @Override
        protected String doInBackground(String... params)
        {
            try{
            String translatedText = Translator.getInstance().translate(params[0], params[1], params[2]);

            Log.i("Translation Activity","Translation completed");

            Log.i("TranslationActivityText", translatedText);
            return translatedText;
        } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        protected  void onPostExecute(String result)
        {
            translateToText.setText(result);
            tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);
        }

    }
}