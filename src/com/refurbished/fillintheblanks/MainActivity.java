package com.refurbished.fillintheblanks;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends Activity {

	GameSurface gameSurface;
	final int numOfLevels = 10;
	boolean isOnMainMenu = true;
	Boolean[] levels = new Boolean[numOfLevels + 1];
	Integer[] percents = new Integer[numOfLevels + 1];
	int screenWidth = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_menu);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (screenWidth == -1) {
			screenWidth = ((RelativeLayout) findViewById(R.id.main_menu)).getWidth();
			mainMenu();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (!isOnMainMenu) {
				mainMenu();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	boolean ispaused = false;

	@Override
	protected void onPause() {
		super.onPause();
		ispaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ispaused)
			mainMenu();
		ispaused = true;
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	public void mainMenu() {
		setContentView(R.layout.main_menu);
		SharedPreferences sP = this.getSharedPreferences("variables", 0);
		levels[1] = sP.getBoolean("l1b", true);
		for (int i = 2; i <= numOfLevels; i++)
			levels[i] = sP.getBoolean("l" + i + "b", false);
		for (int i = 1; i <= numOfLevels; i++)
			percents[i] = sP.getInt("l" + i + "p", -1);

		LinearLayout elo = (LinearLayout) findViewById(R.id.elo);
		elo.setPadding((int) (screenWidth * 0.36), 0, (int) (screenWidth * 0.36), 0);
		for (int i = 1; i <= numOfLevels; i++) {
			LinearLayout vi = new LinearLayout(this);
			LinearLayout vi2 = new LinearLayout(this);
			vi2.setPadding((int) (screenWidth * 0.03), (int) (screenWidth * 0.01), (int) (screenWidth * 0.03),
					(int) (screenWidth * 0.01));
			vi.setOrientation(LinearLayout.VERTICAL);
			TextView text = new TextView(this);
			text.setText("");
			if (percents[i] != -1)
				text.setText("" + percents[i] + "%");
			text.setGravity(Gravity.CENTER);
			text.setTextColor(Color.rgb(80, 80, 80));
			Button button = new Button(this);
			button.setText("" + i);
			button.setTypeface(null, Typeface.BOLD);
			button.setTextColor(Color.WHITE);
			button.setShadowLayer(30, 0, 0, Color.GRAY);
			if (levels[i]) {
				if (percents[i] >= 60) {
					button.setBackgroundColor(Color.rgb(100, 255, 100));
				} else {
					button.setBackgroundColor(Color.rgb(255, 160, 80));
				}
			} else {
				button.setBackgroundColor(Color.rgb(192, 192, 192));
			}
			if (levels[i]) {
				final int index = i;
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						startGame(index);
					}
				});
			}
			vi.addView(text);
			vi2.addView(button, (int) (screenWidth * 0.22), (int) (screenWidth * 0.22));
			vi.addView(vi2);
			elo.addView(vi);
		}
		isOnMainMenu = true;
	}

	public void startGame(int level) {
		setContentView(R.layout.game_layout);
		gameSurface = (GameSurface) findViewById(R.id.gameView);
		gameSurface.parentActivity = this;
		gameSurface.startSignFromActivity(level + 1, this);
		Button b = new Button(this);
		b.setBackgroundColor(Color.rgb(190, 190, 190));
		b.setTextColor(Color.WHITE);
		b.setTypeface(null, Typeface.BOLD);
		b.setText("Menu");
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gameSurface.timer.cancel();
				mainMenu();
			}
		});
		((LinearLayout) findViewById(R.id.game_layout_button_layout)).addView(b, screenWidth / 5, screenWidth / 9);
		isOnMainMenu = false;
	}

	public void setTexts(String str1, String str2) {
		if (((TextView) findViewById(R.id.textView3)) == null || ((TextView) findViewById(R.id.textView3)) == null)
			return;
		((TextView) findViewById(R.id.textView3)).setText(str1);
		((TextView) findViewById(R.id.textView4)).setText(str2);
	}

	public void nextLevelDialog(final int level) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.nextlevel_layout);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.rgb(198, 198, 198)));
		((Button) dialog.findViewById(R.id.b_keep)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		((Button) dialog.findViewById(R.id.b_next)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				startGame(level);
			}
		});
		dialog.show();
	}
}
