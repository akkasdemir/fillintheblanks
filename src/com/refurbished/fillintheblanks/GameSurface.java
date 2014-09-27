package com.refurbished.fillintheblanks;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class GameSurface extends SurfaceView {

	public MainActivity parentActivity;
	Context context;
	SurfaceHolder holder;
	boolean surfaceReady = false, startSign = false;

	public GameSurface(Context context) {
		super(context);
		this.context = context;
		initialize();
	}

	public GameSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initialize();
	}

	public GameSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initialize();
	}

	public void initialize() {
		holder = getHolder();
		holder.addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				surfaceReady = true;
				onStart();
			}

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		switch (arg0.getAction()) {
		case MotionEvent.ACTION_DOWN:
			onMouDown(arg0.getX(), arg0.getY());
			break;
		case MotionEvent.ACTION_UP:
			onMouUp(arg0.getX(), arg0.getY());
			break;
		}
		return true;
	}

	public void startSignFromActivity(int startN, MainActivity mainActivity) {
		startSign = true;
		this.startN = startN;
		parentActivity = mainActivity;
		onStart();
	}

	// GAME

	// constants
	float left, right, top, bottom, startR, rRate, startV;

	Canvas canvas;

	ArrayList<Blob> blobList = new ArrayList<GameSurface.Blob>();
	ArrayList<DeadBlob> deadBlobs = new ArrayList<GameSurface.DeadBlob>();

	int mouHold = 0;
	float mouX, mouY;
	Blob newBlob;

	Timer timer;
	TimerTask timerTask;

	int numDeath = 0;
	int startN;

	Paint textPaint;
	Bitmap bmp;
	int bubbleColor = 0;

	boolean nextLevelDialogShown = false;

	public void onStart() {
		if (!startSign || !surfaceReady)
			return;
		left = 0;
		right = getWidth();
		top = (getHeight() - getWidth()) / 2;
		bottom = (getHeight() + getWidth()) / 2;
		startR = (right - left) / 20;
		rRate = (right - left) / 150;
		startV = (right - left) / 200;

		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		switch (new Random().nextInt(7)) {
		case 0:
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a1);
			break;
		case 1:
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a2);
			break;
		case 2:
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a3);
			break;
		case 3:
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a4);
			break;
		case 4:
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a5);
			break;
		case 5:
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a6);
			break;
		case 6:
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a7);
			break;
		}
		switch (new Random().nextInt(4)) {
		case 0:
			bubbleColor = 0x8fffa050;
			break;
		case 1:
			bubbleColor = 0x8fa0ff50;
			break;
		case 2:
			bubbleColor = 0x8fb0ffb0;
			break;
		case 3:
			bubbleColor = 0x7fffff85;
			break;
		}
		bmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * ((bottom - top) / bmp.getHeight())),
				(int) (bottom - top), false);

		for (int i = 0; i < startN; i++)
			randomBlob(startR, startV * 3);

		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				physics();
				paint();
			}
		};
		timer.schedule(timerTask, 100, 40);
	}

	public void onMouDown(float x, float y) {
		if (y >= top && y <= bottom && mouHold == 0 && numDeath < 10) {
			mouHold = 1;
			newBlob = new Blob(x, y, startR);
		}
	}

	public void onMouUp(float x, float y) {
		if (mouHold == 1)
			mouHold = 2;
	}

	public void physics() {
		for (Blob blob : blobList) {
			blob.physics();
		}
		for (Blob blob1 : blobList)
			for (Blob blob2 : blobList)
				if (blob1 != blob2 && blob1.hitTest(blob2))
					blob1.collision(blob2);
		if (mouHold == 1) {
			newBlob.r += rRate;
			if (newBlob.x - newBlob.r < left)
				newBlob.x = left + newBlob.r;
			if (newBlob.x + newBlob.r > right)
				newBlob.x = right - newBlob.r;
			if (newBlob.y - newBlob.r < top)
				newBlob.y = top + newBlob.r;
			if (newBlob.y + newBlob.r > bottom)
				newBlob.y = bottom - newBlob.r;
			for (Blob blob : blobList)
				if (blob.hitTest(newBlob)) {
					deadBlobs.add(new DeadBlob(newBlob));
					mouHold = 0;
					numDeath++;
					break;
				}
		} else if (mouHold == 2) {
			float ang = 2 * (float) Math.PI * (new Random().nextFloat());
			newBlob.vx = startV * (float) Math.cos(ang);
			newBlob.vy = startV * (float) Math.sin(ang);
			blobList.add(newBlob);
			float perc = 0;
			for (Blob blob : blobList)
				perc += blob.r * blob.r;
			perc *= Math.PI;
			perc /= (right - left) * (bottom - top);
			SharedPreferences sP = context.getSharedPreferences("variables", 0);
			SharedPreferences.Editor editor = sP.edit();
			if ((int) (perc * 100) > sP.getInt("l" + (startN - 1) + "p", -1))
				editor.putInt("l" + (startN - 1) + "p", (int) (perc * 100));
			if (perc * 100 > 60 && !nextLevelDialogShown) {
				editor.putBoolean("l" + (startN) + "b", true);
				nextLevelDialogShown = true;
				parentActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						parentActivity.nextLevelDialog(startN);
					}
				});
			}
			editor.commit();
			mouHold = 0;
		}
	}

	public void paint() {
		canvas = holder.lockCanvas();
		if (canvas == null)
			return;
		canvas.drawColor(Color.rgb(235, 235, 235));
		Paint backp = new Paint();
		backp.setColor(Color.WHITE);
		canvas.drawRect(left, top, right, bottom, backp);
		backp.setAlpha(180);
		canvas.drawBitmap(bmp, left, top, backp);
		backp.setAlpha(255);
		for (DeadBlob blob : deadBlobs)
			blob.paint();
		for (Blob blob : blobList)
			blob.paint();
		if (mouHold != 0)
			newBlob.paint();
		backp.setStyle(Style.FILL);
		backp.setColor(Color.rgb(235, 235, 235));
		canvas.drawRect(0, 0, getWidth(), top, backp);
		canvas.drawRect(0, bottom, getWidth(), getHeight(), backp);
		backp.setStyle(Style.STROKE);
		backp.setColor(Color.DKGRAY);
		canvas.drawRect(left, top, right - 1, bottom, backp);

		float perc = 0;
		if (mouHold != 0)
			perc += newBlob.r * newBlob.r;
		for (Blob blob : blobList)
			perc += blob.r * blob.r;
		perc *= Math.PI;
		perc /= (right - left) * (bottom - top);
		final float finalperc = perc;
		parentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				parentActivity.setTexts("Percentage: " + (int) (finalperc * 1000) / 10.0, "Bubbles: " + blobList.size()
						+ ", Deaths: " + numDeath + "/10");
			}
		});

		if (numDeath >= 10) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTextAlign(Align.CENTER);
			paint.setColor(Color.rgb(255, 230, 200));
			paint.setStyle(Style.STROKE);
			paint.setTextSize((right - left) / 15);
			paint.setStrokeWidth(3);
			canvas.drawText("You're out of lives", (right - left) / 2, (bottom - top) / 2 + (right - left) / 20, paint);
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize((right - left) / 15);
			paint.setColor(Color.RED);
			canvas.drawText("You're out of lives", (right - left) / 2, (bottom - top) / 2 + (right - left) / 20, paint);
		}
		holder.unlockCanvasAndPost(canvas);
	}

	public void randomBlob(float R, float v) {
		Random random = new Random();
		Blob newB = new Blob(random.nextFloat() * (right - left - 2 * R) + R + left, random.nextFloat()
				* (bottom - top - 2 * R) + R + top, R);
		float ang = random.nextFloat() * (float) Math.PI * 2;
		newB.vx = v * (float) Math.cos(ang);
		newB.vy = v * (float) Math.sin(ang);
		blobList.add(newB);
	}

	public class Blob {
		public float x, y, vx, vy, r;
		Paint paintIn, paintOut;

		public Blob(float x, float y, float r) {
			if (x < left + r)
				x = left + r;
			if (x > right - r)
				x = right - r;
			if (y < top + r)
				y = top + r;
			if (y > bottom - r)
				y = bottom - r;
			this.x = x;
			this.y = y;
			this.r = r;
			vx = 0;
			vy = 0;
			paintIn = new Paint();
			paintOut = new Paint();
			paintIn.setAntiAlias(true);
			paintOut.setAntiAlias(true);
			paintIn.setColor(bubbleColor);
			paintOut.setColor(Color.DKGRAY);
			paintIn.setStyle(Style.FILL);
			paintOut.setStyle(Style.STROKE);
		}

		public boolean hitTest(Blob b) {
			return Math.sqrt((x - b.x) * (x - b.x) + (y - b.y) * (y - b.y)) < r + b.r;
		}

		public void collision(Blob b) {
			double alph = Math.atan2(b.y - y, b.x - x);
			float sin = (float) Math.sin(alph);
			float cos = (float) Math.cos(alph);
			float v1x = vx * cos + vy * sin;
			float v1y = -vx * sin + vy * cos;
			float v2x = b.vx * cos + b.vy * sin;
			float v2y = -b.vx * sin + b.vy * cos;
			float m1 = r;
			float m2 = b.r;
			if (v2x > v1x)
				return;
			float v1x_ = (m1 * v1x + 2 * m2 * v2x - m2 * v1x) / (m1 + m2);
			float v2x_ = (m2 * v2x + 2 * m1 * v1x - m1 * v2x) / (m1 + m2);
			vx = v1x_ * cos - v1y * sin;
			vy = v1x_ * sin + v1y * cos;
			b.vx = v2x_ * cos - v2y * sin;
			b.vy = v2x_ * sin + v2y * cos;
		}

		public void physics() {
			x += vx;
			y += vy;
			if (x - r < left)
				vx = (float) Math.abs(vx);
			if (x + r > right)
				vx = -(float) Math.abs(vx);
			if (y - r < top)
				vy = (float) Math.abs(vy);
			if (y + r > bottom)
				vy = -(float) Math.abs(vy);
		}

		public void paint() {
			canvas.drawCircle(x, y, r, paintIn);
			canvas.drawCircle(x, y, r, paintOut);
		}
	}

	public class DeadBlob {
		public float x, y, r;
		int alpha;
		Paint paintIn, paintOut;

		public DeadBlob(Blob b) {
			x = b.x;
			y = b.y;
			r = b.r;
			alpha = 50;
			paintIn = new Paint();
			paintOut = new Paint();
			paintIn.setAntiAlias(true);
			paintOut.setAntiAlias(true);
			paintIn.setColor(Color.WHITE);
			paintOut.setColor(Color.DKGRAY);
			paintIn.setStyle(Style.FILL);
			paintOut.setStyle(Style.STROKE);
		}

		public void paint() {
			paintIn.setARGB((int) alpha, 255, 0, 0);
			paintOut.setARGB((int) alpha, 68, 68, 68);
			alpha--;
			if (alpha < 0)
				alpha = 0;
			canvas.drawCircle(x, y, r, paintIn);
			canvas.drawCircle(x, y, r, paintOut);
		}
	}
}
