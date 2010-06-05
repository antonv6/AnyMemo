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
package org.liberty.android.fantastischmemo;

import org.amr.arabic.ArabicUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.util.Log;
import android.os.SystemClock;
import android.graphics.Typeface;


public abstract class MemoScreenBase extends Activity{
    //protected ArrayList<Item> learnQueue;
	protected DatabaseHelper dbHelper = null;
	protected String dbName;
	protected String dbPath;
	protected boolean showAnswer;
	protected Item currentItem;
    /* prevItem is used to undo */
    //private Item prevItem = null;
    private int prevScheduledItemCount;
    private int prevNewItemCount;
    /* How many words to learn at a time (rolling) */
	//private final int WINDOW_SIZE = 10;
	//private boolean queueEmpty;
	//private int idMaxSeen;
	//private int scheduledItemCount;
	//private int newItemCount;
	protected double questionFontSize = 23.5;
	protected double answerFontSize = 23.5;
	protected String questionAlign = "center";
	protected String answerAlign = "center";
	protected String questionLocale = "US";
	protected String answerLocale = "US";
	protected String htmlDisplay = "none";
	protected String qaRatio = "50%";
    protected String questionTextColor = "Default";
    protected String answerTextColor = "Default";
    protected String questionBgColor = "Default";
    protected String answerBgColor = "Default";
    protected boolean btnOneRow = false;
	protected boolean autoaudioSetting = true;
    protected ProgressDialog mProgressDialog = null;
	protected boolean questionUserAudio = false;
	protected boolean answerUserAudio = false;
    protected boolean copyClipboard = true;
    protected String questionTypeface = "";
    protected String answerTypeface = "";
	//private SpeakWord mSpeakWord = null;
    //private Context mContext;
    //private Handler mHandler;
    //private AlertDialog.Builder mAlert;
    /* Six grading buttons */
	//private Button[] btns = {null, null, null, null, null, null}; 

	protected int returnValue = 0;
	//private boolean initFeed;

    private final static String TAG = "org.liberty.android.fantastischmemo.MemoScreenBase";

	abstract protected boolean prepare();

	abstract protected int feedData();
	
	abstract public boolean onCreateOptionsMenu(Menu menu);
	
	abstract public boolean onOptionsItemSelected(MenuItem item);
	
    abstract protected void createButtons();

	abstract protected void buttonBinding();

    protected abstract boolean fetchCurrentItem();

    abstract protected void restartActivity();	

    abstract protected void refreshAfterEditItem();

    abstract protected void refreshAfterDeleteItem();

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

	public void onResume(){
        super.onResume();
        /* Refresh depending on where it returns. */
		if(returnValue == 1){
			prepare();
			returnValue = 0;
		}
		else{
			returnValue = 0;
		}
    }


	public void onDestroy(){
        super.onDestroy();
    }

	protected void loadSettings(){
		/* Here is the global settings from the preferences */
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	autoaudioSetting = settings.getBoolean("autoaudio", true);
        btnOneRow = settings.getBoolean("btnonerow", false);
        copyClipboard = settings.getBoolean("copyclipboard", true);
		
		HashMap<String, String> hm = dbHelper.getSettings();
		Set<Map.Entry<String, String>> set = hm.entrySet();
		Iterator<Map.Entry<String, String> > i = set.iterator();
		while(i.hasNext()){
			Map.Entry<String, String> me = i.next();
			if((me.getKey().toString()).equals("question_font_size")){
				this.questionFontSize = new Double(me.getValue().toString());
			}
			if(me.getKey().toString().equals("answer_font_size")){
				this.answerFontSize = new Double(me.getValue().toString());
			}
			if(me.getKey().toString().equals("question_align")){
				this.questionAlign = me.getValue().toString();
			}
			if(me.getKey().toString().equals("answer_align")){
				this.answerAlign = me.getValue().toString();
			}
			if(me.getKey().toString().equals("question_locale")){
				this.questionLocale = me.getValue().toString();
			}
			if(me.getKey().toString().equals("answer_locale")){
				this.answerLocale = me.getValue().toString();
			}
			if(me.getKey().toString().equals("html_display")){
				this.htmlDisplay = me.getValue().toString();
			}
			if(me.getKey().toString().equals("ratio")){
				this.qaRatio = me.getValue().toString();
			}
			if(me.getKey().toString().equals("question_text_color")){
                this.questionTextColor = me.getValue().toString();
            }
			if(me.getKey().toString().equals("answer_text_color")){
                this.answerTextColor = me.getValue().toString();
            }
			if(me.getKey().toString().equals("question_bg_color")){
                this.questionBgColor = me.getValue().toString();
            }
			if(me.getKey().toString().equals("answer_bg_color")){
                this.answerBgColor = me.getValue().toString();
            }
			if(me.getKey().toString().equals("question_typeface")){
                this.questionTypeface = me.getValue().toString();
            }
			if(me.getKey().toString().equals("answer_typeface")){
                this.answerTypeface = me.getValue().toString();
            }
		}
	}

	
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){
        
    	
    	case 1:
    	case 2:
            /* Determine whether to update the screen */
    		if(resultCode == Activity.RESULT_OK){
    			returnValue = 1;
    		}
    		if(resultCode == Activity.RESULT_CANCELED){
    			returnValue = 0;
    		}
    		
    		
    	}
    }
	
			
			

	protected void updateMemoScreen() {
		/* update the main screen according to the currentItem */
		
        /* The q/a ratio is not as whe it seems
         * It displays differently on the screen
         */
		LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
		LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
		float qRatio = Float.valueOf(qaRatio.substring(0, qaRatio.length() - 1));
		float aRatio = 100.0f - qRatio;
		qRatio /= 50.0;
		aRatio /= 50.0;
		layoutQuestion.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, qRatio));
		layoutAnswer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, aRatio));
        /* Set both background and text color */
        setScreenColor();
		feedData();
        fetchCurrentItem();
		if(fetchCurrentItem() == false){
			new AlertDialog.Builder(this)
			    .setTitle(this.getString(R.string.memo_no_item_title))
			    .setMessage(this.getString(R.string.memo_no_item_message))
			    .setNeutralButton(getString(R.string.back_menu_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        /* Finish the current activity and go back to the last activity.
                         * It should be the open screen. */
                        finish();
                    }
                })
                .create()
                .show();
			
		}
        else{
            if(copyClipboard){
                ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                cm.setText(currentItem.getQuestion());
            }
			displayQA(currentItem);
        }
		
	}



	private void displayQA(Item item) {
		/* Display question and answer according to item */
		this.setTitle(this.getTitle() + " / " + this.getString(R.string.memo_current_id) + item.getId() );
        
		TextView questionView = (TextView) findViewById(R.id.question);
		TextView answerView = (TextView) findViewById(R.id.answer);
        /* Set the typeface of question an d answer */
        if(!questionTypeface.equals("")){
            Typeface qt = Typeface.createFromFile(questionTypeface);
            if(qt != null){
                questionView.setTypeface(qt);
            }

        }
        if(!answerTypeface.equals("")){
            Typeface at = Typeface.createFromFile(answerTypeface);
            if(at != null){
                answerView.setTypeface(at);
            }

        }
		
		
		if(this.htmlDisplay.equals("both")){
            /* Use HTML to display */
			CharSequence sq = Html.fromHtml(ArabicUtilities.reshape(item.getQuestion()));
			CharSequence sa = Html.fromHtml(ArabicUtilities.reshape(item.getAnswer()));
			//CharSequence sa = Html.fromHtml(item.getAnswer());

			
			//questionView.setText(ArabicUtilities.reshape(sq.toString()));
			//answerView.setText(ArabicUtilities.reshape(sa.toString()));
            questionView.setText(sq);
            answerView.setText(sa);
			
		}
		else if(this.htmlDisplay.equals("question")){
			CharSequence sq = Html.fromHtml(ArabicUtilities.reshape(item.getQuestion()));
			questionView.setText(sq);
            answerView.setText(ArabicUtilities.reshape(item.getAnswer()));
		}
		else if(this.htmlDisplay.equals("answer")){
            questionView.setText(ArabicUtilities.reshape(item.getQuestion()));
			CharSequence sa = Html.fromHtml(ArabicUtilities.reshape(item.getAnswer()));
			answerView.setText(sa);
		}
		else{
			//questionView.setText(new StringBuilder().append(item.getQuestion()));
			//answerView.setText(new StringBuilder().append(item.getAnswer()));
            questionView.setText(ArabicUtilities.reshape(item.getQuestion()));
            answerView.setText(ArabicUtilities.reshape(item.getAnswer()));
		}
		
        /* Here is tricky to set up the alignment of the text */
		if(questionAlign.equals("center")){
			questionView.setGravity(Gravity.CENTER);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			layoutQuestion.setGravity(Gravity.CENTER);
		}
		else if(questionAlign.equals("right")){
			questionView.setGravity(Gravity.RIGHT);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			layoutQuestion.setGravity(Gravity.NO_GRAVITY);
		}
		else{
			questionView.setGravity(Gravity.LEFT);
			LinearLayout layoutQuestion = (LinearLayout)findViewById(R.id.layout_question);
			layoutQuestion.setGravity(Gravity.NO_GRAVITY);
		}
		if(answerAlign.equals("center")){
			answerView.setGravity(Gravity.CENTER);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutAnswer.setGravity(Gravity.CENTER);
		} else if(answerAlign.equals("right")){
			answerView.setGravity(Gravity.RIGHT);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutAnswer.setGravity(Gravity.NO_GRAVITY);
			
		}
		else{
			answerView.setGravity(Gravity.LEFT);
			LinearLayout layoutAnswer = (LinearLayout)findViewById(R.id.layout_answer);
			layoutAnswer.setGravity(Gravity.NO_GRAVITY);
		}
		questionView.setTextSize((float)questionFontSize);
		answerView.setTextSize((float)answerFontSize);

		buttonBinding();

	}


    
    private void setScreenColor(){
        // Set both text and the background color
		TextView questionView = (TextView) findViewById(R.id.question);
		TextView answerView = (TextView) findViewById(R.id.answer);
        LinearLayout questionLayout = (LinearLayout)findViewById(R.id.layout_question);
        LinearLayout answerLayout = (LinearLayout)findViewById(R.id.layout_answer);
        int[] colorMap = {Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW};
    	String[] colorList = getResources().getStringArray(R.array.color_list);
        /* Default color will do nothing */
        if(!questionTextColor.equals("Default")){
            for(int i = 1; i <= colorMap.length; i++){
                if(questionTextColor.equals(colorList[i])){
                    questionView.setTextColor(colorMap[i - 1]);
                    break;
                }
            }
        }
        if(!answerTextColor.equals("Default")){
            for(int i = 1; i <= colorMap.length; i++){
                if(answerTextColor.equals(colorList[i])){
                    answerView.setTextColor(colorMap[i - 1]);
                    break;
                }
            }
        }
        if(!questionBgColor.equals("Default")){
            for(int i = 0; i <= colorMap.length; i++){
                if(questionBgColor.equals(colorList[i])){
                    questionLayout.setBackgroundColor(colorMap[i - 1]);
                    break;
                }
            }
        }
        if(!answerBgColor.equals("Default")){
            for(int i = 0; i <= colorMap.length; i++){
                if(answerBgColor.equals(colorList[i])){
                    answerLayout.setBackgroundColor(colorMap[i - 1]);
                    break;
                }
            }
        }
    }


    protected void showEditDialog(){
        /* This method will show the dialog after long click 
         * on the screen 
         * */
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.memo_edit_dialog_title))
            .setItems(R.array.memo_edit_dialog_list, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    if(which == 0){
                        /* This is a customized dialog inflated from XML */
                        doEdit();
                    }
                    if(which == 1){
                        /* Delete current card */
                        doDelete();
                    }
                    if(which == 2){
                        /* Skip this card forever */
                        doSkip();
                    }
                }
            })
            .create()
            .show();
    }

    protected void doEdit(){
        /* Edit current card */
        /* This is a customized dialog inflated from XML */
        LayoutInflater factory = LayoutInflater.from(MemoScreenBase.this);
        final View editView = factory.inflate(R.layout.edit_dialog, null);
        EditText eq = (EditText)editView.findViewById(R.id.edit_dialog_question_entry);
        EditText ea = (EditText)editView.findViewById(R.id.edit_dialog_answer_entry);
        eq.setText(currentItem.getQuestion());
        ea.setText(currentItem.getAnswer());
        new AlertDialog.Builder(MemoScreenBase.this)
            .setTitle(getString(R.string.memo_edit_dialog_title))
            .setView(editView)
            .setPositiveButton(getString(R.string.settings_save),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        EditText eq = (EditText)editView.findViewById(R.id.edit_dialog_question_entry);
                        EditText ea = (EditText)editView.findViewById(R.id.edit_dialog_answer_entry);
                        String qText = eq.getText().toString();
                        String aText = ea.getText().toString();
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("question", qText);
                        hm.put("answer", aText);
                        currentItem.setData(hm);
                        //dbHelper.updateQA(currentItem);
                        dbHelper.addOrReplaceItem(currentItem);
                        updateMemoScreen();
                        refreshAfterEditItem();
                    }
                })
            .setNegativeButton(getString(R.string.cancel_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        refreshAfterEditItem();
                    }
                })
            .create()
            .show();
    }

    protected void doDelete(){
        new AlertDialog.Builder(MemoScreenBase.this)
            .setTitle(getString(R.string.detail_delete))
            .setMessage(getString(R.string.delete_warning))
            .setPositiveButton(getString(R.string.yes_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        dbHelper.deleteItem(currentItem);
                        refreshAfterDeleteItem();
                    }
                })
            .setNegativeButton(getString(R.string.no_text), null)
            .create()
            .show();
    }

    protected void doSkip(){

        new AlertDialog.Builder(MemoScreenBase.this)
            .setTitle(getString(R.string.skip_text))
            .setMessage(getString(R.string.skip_warning))
            .setPositiveButton(getString(R.string.yes_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        currentItem.skip();
                        dbHelper.updateItem(currentItem);
                        restartActivity();
                    }
                })
            .setNegativeButton(getString(R.string.no_text), null)
            .create()
            .show();
    }
}