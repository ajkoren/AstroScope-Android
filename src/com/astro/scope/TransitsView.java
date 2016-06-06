package com.astro.scope;

import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import swisseph.TCPlanet;
import swisseph.TransitCalculator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;

public class TransitsView extends SurfaceView implements SurfaceHolder.Callback 
{
	private static final int numXscreens = 10;
	
	private Paint textPaint = new Paint();
	
	private int width, height;
	private int initWidth;
	private float screenScale;
	private float chartScale;
	
	private int[] x = new int[2];
	private int[] y = new int[2];
	private int downX0, downX1;
	private int downY0, downY1;
	private int gridX0, gridX1;
	private int gridY0, gridY1;
	private int initGridX0, initGridX1; 
	private int initGridY0, initGridY1;
	private int xStart, yStart, xEnd, yEnd;
	
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	private String[] personalPlanets = 
		{"Sun", "Moon", "Merc", "Venus", "Mars"};

	public TransitsView(Context context, 
		int byear, int bmonth, int bday, int syear, int smonth, int sday) 
	{
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setFocusable(true); // make sure we get key events
		setFocusableInTouchMode(true); // make sure we get touch events
		init();
		
		//TransitionsCalc tcalc = 
		//	new TransitionsCalc(byear, bmonth, bday, syear, smonth, sday);
		//tcalc.run();
	}
	
	private void init() {
		textPaint.setColor(Color.WHITE);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pointerCount = event.getPointerCount();
		int id;

		Canvas c = getHolder().lockCanvas();
		if (c != null) {

			c.drawColor(Color.BLACK);
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
	      	case MotionEvent.ACTION_DOWN:
				id = event.getPointerId(0);
				gridX0 = xStart;
				gridY0 = yStart;
				downX0 = (int) event.getX(id);
				downY0 = (int) event.getY(id);
		        mode = DRAG;
				drawGrid(chartScale, 2000, 
						xStart, yStart, xEnd, yEnd, textPaint, c);
				Log.i("Astro", "ACTION_DOWN");
				break;
			case MotionEvent.ACTION_UP:
	      		initWidth = width;
				drawGrid(chartScale, 2000, 
					xStart, yStart, xEnd, yEnd, textPaint, c);
				downX0 = x[0];
				downX1 = x[1];
				downY0 = y[0];
				downY1 = y[1];
				mode = NONE;
				Log.i("Astro", "ACTION_UP");
				break;
	      	case MotionEvent.ACTION_POINTER_DOWN:
				if (pointerCount > 1) {
					for (int i = 0; i < 2; i++) {
						id = event.getPointerId(i);
						x[i] = (int) event.getX(id);
						y[i] = (int) event.getY(id);
					}
					downX0 = x[0];
					downX1 = x[1];
					downY0 = y[0];
					downY1 = y[1];
					mode = ZOOM;
	    	   }
				Log.i("Astro", "ACTION_POINTER_DOWN");
	    	   break;
			case MotionEvent.ACTION_POINTER_UP:
	      		initWidth = width;
				drawGrid(chartScale, 2000, 
						xStart, yStart, xEnd, yEnd, textPaint, c);
	      		break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					id = event.getPointerId(0);
					x[0] = (int) event.getX(id);
					y[0] = (int) event.getY(id);
					xStart = gridX0 + (x[0] - downX0);
					xEnd = xStart + numXscreens * width - initGridX0;
					yStart = initGridY0;
					yEnd = initGridY1;
					drawGrid(chartScale, 2000, 
						xStart, yStart, xEnd, yEnd, textPaint, c);
				}
				else if (mode == ZOOM) {
					if (pointerCount > 1) {
						for (int i = 0; i < 2; i++) {
							id = event.getPointerId(i);
							x[i] = (int) event.getX(id);
							y[i] = (int) event.getY(id);
						}
												
						int widthDelta =  2 * (Math.abs(x[0] - x[1]) - Math.abs(downX0 - downX1));
						int dragDelta = - widthDelta / 2; 
						xStart = gridX0 + dragDelta;
						width = initWidth + widthDelta;
						xEnd = xStart + numXscreens * width - initGridX0;
						yStart = initGridY0;
						yEnd = initGridY1;
						drawGrid(chartScale, 2000, 
							xStart, yStart, xEnd, yEnd, textPaint, c);
					}
				}
				Log.i("Astro", "ACTION_MOVE, ACTION_POINTER_UP");
		        break;
			}
			
			Log.i("Astro", "Mode = " + mode);
			getHolder().unlockCanvasAndPost(c);
		}
		return true;
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.width = width;
		this.initWidth = width;
		this.height = height;
		
		if (width > height) {
			this.screenScale = width / 480f;
		} else {
			this.screenScale = height / 480f;
		}
		
		initGridX0 = gridX0 = downX0 = 2*width/10;
		initGridY0 = gridY0 = downY0 = height/10;
		initGridX1 = gridX1 = downX1 = 9*width/10;
		initGridY1 = gridY1 = downY1 = 9*height/10;
		
		xStart = gridX0;
		xEnd = xStart + numXscreens * width - initGridX0;

		textPaint.setTextSize(14 * screenScale);
		Canvas c = getHolder().lockCanvas();
		if (c != null) {
			// clear screen
			c.drawColor(Color.BLACK);
			drawGrid(chartScale, 2000, xStart, gridY0, xEnd, gridY1, textPaint, c);
			getHolder().unlockCanvasAndPost(c);
		}
	}
	
	private void drawGrid(float chartScale, int startYear,
		int x0, int y0, int x1, int y1, Paint paint, Canvas c) 
	{		
		// draw horizontal lines
				
		int xStart = x0;
		int xEnd = x1;
		int yStart = y0;
		int yEnd = y1;

		final int numLines = 50;
		
		int deltaX = (xEnd - xStart) / (numLines - 1);
		for (int i = 0; i < numLines; i++) {
			int xTick = xStart + i * deltaX;
			String year = Integer.toString(startYear + i);
			c.drawText(year, xTick, 50, paint);
			c.drawLine(xTick, yStart, xTick, yEnd, paint);
		}

		// draw vertical lines
		
		int numPlanets = personalPlanets.length;
		int deltaY = (yEnd - yStart) / (numPlanets - 1);
		for (int j = 0; j < numPlanets; j++) {
			int yTick = yStart + j * deltaY;
			c.drawText(personalPlanets[j], 10, yTick, paint);
			c.drawLine(xStart, yTick, xEnd, yTick, paint);
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
} 
