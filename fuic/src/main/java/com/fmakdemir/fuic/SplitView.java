package com.fmakdemir.fuic;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SplitView extends LinearLayout {
	private final static String TAG = "SplitView"; // for debuggin purposes

	final int SENSITIVITY_MARGIN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14,
			getResources().getDisplayMetrics());

	private LinearLayout rootView; // root view of our views
	private FrameLayout firstDiv, secondDiv, holderView; // first, second, and holder views
	private int holderSize;

	private int contentWidth, contentHeight;
	private float ratio;

	private LayoutParams firstLayoutParams, secondLayoutParams;
	private float minRatio, maxRatio;

	private static final int BASE = 100;

	public SplitView(Context context) {
		super(context);
		init(null, 0);
	}

	public SplitView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public SplitView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private float getFloatFraction(int id) {
		return getResources().getFraction(id, BASE, BASE)/BASE;
	}

	private void init(AttributeSet attrs, int defStyle) {
		Log.v(TAG, "Init");

		int firstWeight = getResources().getInteger(R.integer.DEFAULT_SPLIT_WEIGHT);
		int secondWeight = getResources().getInteger(R.integer.DEFAULT_SPLIT_WEIGHT);

		float defaultMinRatio = getFloatFraction(R.fraction.DEFAULT_MIN_SPLIT_RATIO);
		float defaultMaxRatio = getFloatFraction(R.fraction.DEFAULT_MAX_SPLIT_RATIO);
		minRatio = defaultMinRatio;
		maxRatio = defaultMaxRatio;

		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.SplitView, defStyle, 0);

		if (a.hasValue(R.styleable.SplitView_firstWeight)) {
			firstWeight = a.getInt(R.styleable.SplitView_firstWeight, 0);
		}

		if (a.hasValue(R.styleable.SplitView_secondWeight)) {
			secondWeight = a.getInt(R.styleable.SplitView_secondWeight, 0);
		}

		Log.d(TAG+"_Weights", ""+firstWeight+ ", "+secondWeight);

		// get values from attributes or default values
		minRatio = a.getFraction(R.styleable.SplitView_minSplitRatio, BASE, BASE, minRatio*BASE);
		minRatio /= BASE;
		maxRatio = a.getFraction(R.styleable.SplitView_maxSplitRatio, BASE, BASE, maxRatio*BASE);
		maxRatio /= BASE;

		// check for invalid ratios
		if (minRatio > maxRatio || minRatio < 0) {
			minRatio = defaultMinRatio;
		}
		if (minRatio > maxRatio || maxRatio > 1) {
			maxRatio = defaultMaxRatio;
		}

		Log.d(TAG+"_minMaxRatios", ""+minRatio+ ", "+maxRatio);

		if (a.hasValue(R.styleable.SplitView_holderSize)) {
			setHolderSize(a.getDimensionPixelSize(R.styleable.SplitView_holderSize,
					R.dimen.DEFAULT_HOLDER_SIZE));
		}
		// don't allow any smaller than default size
		int defSize = (int) getResources().getDimension(R.dimen.DEFAULT_HOLDER_SIZE);
		if (getHolderSize() < defSize) {
			setHolderSize(defSize);
		}

		Log.d(TAG+"_holderSize", ""+getHolderSize());

		a.recycle();

		Log.d(TAG, "Add default views");
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		// having a known layout is easier for resize of others
		rootView = (LinearLayout) inflater.inflate(R.layout.layout_split_view, null, false);

		firstDiv = (FrameLayout) rootView.findViewById(R.id.split_first);
		secondDiv = (FrameLayout) rootView.findViewById(R.id.split_second);
		holderView = (FrameLayout) rootView.findViewById(R.id.split_holder);

		firstLayoutParams = (LayoutParams) firstDiv.getLayoutParams();
		secondLayoutParams = (LayoutParams) secondDiv.getLayoutParams();

		ratio = (float) firstWeight /(firstWeight + secondWeight);
		Log.d("TTT", "ratio: "+ratio);

		TextView tv;

		tv = new TextView(getContext());
		tv.setText("Split 1");
		firstDiv.addView(tv);

		tv = new TextView(getContext());
		tv.setText("Split 2");
		secondDiv.addView(tv);

		rootView.setOnGenericMotionListener(HolderMotionListener.getInstance());
		rootView.setOnTouchListener(holderTouchListener);

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		addView(rootView);
		Log.v(TAG, "View added");
	}

	public void setHolderSize(int holderSize) {
		this.holderSize = holderSize;
	}

	public int getHolderSize() {
		return holderSize;
	}

	private static class HolderMotionListener implements OnGenericMotionListener {

		public static HolderMotionListener getInstance() {
			return new HolderMotionListener();
		}

		private HolderMotionListener() {
		}

		@Override
		public boolean onGenericMotion(View view, MotionEvent event) {
//			Log.d(getClass().getSimpleName(), "" + event.getAction() + "|" + event.getX() + ", " + event.getY());
			return false;
		}
	}

	enum State {HOLDER_DOWN, IDLE, SPLIT_DOWN}

	private final OnTouchListener holderTouchListener = new OnTouchListener() {

//		private static int currentState = State.E;
		State state = State.IDLE;

		@Override
		public boolean onTouch(View view, MotionEvent event) {
//			if (currentState != State.EDIT_MOVE) return false;

			int y, topMargin;
			y = (int) event.getY();

//			Log.d(getClass().getSimpleName(), "" + event.getAction() + "|" + x + ", " + y + " | " +
//					(double)y/contentHeight);

/*			for (int i=0; i<rootView.getChildCount(); ++i) {
				FrameLayout frm = (FrameLayout) rootView.getChildAt(i);
				if (frm.getTop() <= y && y < frm.getBottom()) {
					Log.d(TAG + "_ontouch", "Touched: "+i + "  Action: "+event.getAction());
				}
 			}
*/
			switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if (state == State.HOLDER_DOWN) {
						state = State.IDLE;
						break;
					}
				case MotionEvent.ACTION_MOVE:
					if (state != State.HOLDER_DOWN) {
						break;
					}
					topMargin = y - getHolderSize()/2;
					ratio = (float)topMargin/contentHeight;
					if (ratio < minRatio) {
						ratio = minRatio;
					}
					if (ratio > maxRatio) {
						ratio = maxRatio;
					}
					updateSplits();
					Log.d("TTT", "ratio: "+ratio);
//					leftMargin = (int) event.getRawX() - (view.getWidth() / 2);
					break;

				case MotionEvent.ACTION_DOWN:
					int top, bottom;
					top = (int) holderView.getY() - SENSITIVITY_MARGIN;
					bottom = top + getHolderSize() + SENSITIVITY_MARGIN;
					if (top <= y && y < bottom) {
						state = State.HOLDER_DOWN;
					}
					break;
			}

			return true;
		}
	};
/*
	private void invalidateTextPaintAndMeasurements() {
		// Set up a default TextPaint object
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);

		mTextPaint.setTextSize(mExampleDimension);
		mTextPaint.setColor(mExampleColor);
		mTextWidth = mTextPaint.measureText(mExampleString);

		Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		mTextHeight = fontMetrics.bottom;
	}
*/
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

		Log.d(TAG + "_get", "W, H: " + getWidth() + ", " + getHeight() + " | " + parentWidth + ", " + parentHeight);
		LayoutParams params = (LayoutParams) holderView.getLayoutParams();
		params.height = getHolderSize();
		holderView.setLayoutParams(params);

		if (parentHeight != 0) {
			contentWidth = parentWidth - getPaddingLeft() - getPaddingRight();
			contentHeight = parentHeight - getPaddingTop() - getPaddingBottom() - getHolderSize();
		}
		updateSplits();

		Log.d(TAG + "_sizes", "CW: " + contentWidth + " |CH: " + contentHeight + " |FH: " + firstLayoutParams.height + " |SH: " + secondLayoutParams.height);
		params = (LayoutParams) rootView.getLayoutParams();
		Log.d(TAG + "_root", "WxH: " + rootView.getWidth() + ", " + rootView.getHeight() + " |WxH: " + params.width + ", " + params.height);

//		setLayoutParams(new LayoutParams(parentWidth, parentHeight)); // we don't know parent LayoutParams type
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	void updateSplits() {
		Log.d(TAG + "_content", "W, H: " + contentWidth + ", " + contentHeight);

		firstLayoutParams.height = (int)(contentHeight * ratio);
		secondLayoutParams.height = contentHeight - firstLayoutParams.height;

		Log.d(TAG + "_first", "W, H: " + firstLayoutParams.width + ", " + firstLayoutParams.height);
		Log.d(TAG + "_second", "W, H: " + secondLayoutParams.width + ", " + secondLayoutParams.height);

		firstDiv.setLayoutParams(firstLayoutParams);
		secondDiv.setLayoutParams(secondLayoutParams);
	}

/*	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// consider storing these as member variables to reduce
		// allocations per draw cycle.
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();

		int contentWidth = getWidth() - paddingLeft - paddingRight;
		int contentHeight = getHeight() - paddingTop - paddingBottom;

		// Draw the text.
		canvas.drawText(mExampleString,
				paddingLeft + (contentWidth - mTextWidth) / 2,
				paddingTop + (contentHeight + mTextHeight) / 2,
				mTextPaint);

		// Draw the example drawable on top of the text.
		if (mExampleDrawable != null) {
			mExampleDrawable.setBounds(paddingLeft, paddingTop,
					paddingLeft + contentWidth, paddingTop + contentHeight);
			mExampleDrawable.draw(canvas);
		}
	}*/

	public void replaceFirst(View v) {
		firstDiv.removeAllViews();
		firstDiv.addView(v);
	}

	public void replaceSecond(View v) {
		secondDiv.removeAllViews();
		secondDiv.addView(v);
	}

}
