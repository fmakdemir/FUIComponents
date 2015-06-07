package com.fmakdemir.fuic;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SplitView extends LinearLayout {
	private final static String TAG = "SplitView"; // for debuggin purposes

	final int SENSITIVITY_MARGIN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14,
			getResources().getDisplayMetrics());

	private LinearLayout rootView; // root view of our views
	private FrameLayout firstDiv, secondDiv, holderView; // first, second, and holder views
	private int firstWeight, secondWeight; // height given as attribute
	private int holderSize;

	private int contentWidth, contentHeight;
	private double ratio;

	private LayoutParams firstLayoutParams, secondLayoutParams;
	private static final double MIN_RATIO = 0.1, MAX_RATIO = 0.9;

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

	private void init(AttributeSet attrs, int defStyle) {
		Log.v(TAG, "Init");

		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.SplitView, defStyle, 0);

		if (a.hasValue(R.styleable.SplitView_firstWeight)) {
			firstWeight = a.getInt(R.styleable.SplitView_firstWeight, 0);
		}

		if (a.hasValue(R.styleable.SplitView_secondWeight)) {
			secondWeight = a.getInt(R.styleable.SplitView_secondWeight, 0);
		}

		if (a.hasValue(R.styleable.SplitView_holderSize)) {
			setHolderSize(a.getDimensionPixelSize(R.styleable.SplitView_holderSize,
					R.dimen.DEFAULT_HOLDER_SIZE));
		}
		int defSize = (int) getResources().getDimension(R.dimen.DEFAULT_HOLDER_SIZE);
		if (getHolderSize() < defSize) {
			setHolderSize(defSize);
		}

		a.recycle();

//		Log.v(TAG + "_attrs", "F, S: " + firstWeight + ", " + secondWeight);

		Log.v(TAG, "Add view");
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		rootView = (LinearLayout) inflater.inflate(R.layout.layout_split_view, null, false);

		firstDiv = (FrameLayout) rootView.findViewById(R.id.split_first);
		secondDiv = (FrameLayout) rootView.findViewById(R.id.split_second);
		holderView = (FrameLayout) rootView.findViewById(R.id.split_holder);

		firstLayoutParams = (LayoutParams) firstDiv.getLayoutParams();
		secondLayoutParams = (LayoutParams) secondDiv.getLayoutParams();

		ratio = (double)firstWeight/(firstWeight+secondWeight);
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
//			Log.i(getClass().getSimpleName(), "" + event.getAction() + "|" + event.getX() + ", " + event.getY());
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

			int x, y, topMargin;
			x = (int) event.getX();
			y = (int) event.getY();

//			Log.i(getClass().getSimpleName(), "" + event.getAction() + "|" + x + ", " + y + " | " +
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
					ratio = (double)topMargin/contentHeight;
					if (ratio < MIN_RATIO) {
						ratio = MIN_RATIO;
					}
					if (ratio > MAX_RATIO) {
						ratio = MAX_RATIO;
					}
					updateSplits(contentWidth, contentHeight+getHolderSize());
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

		Log.i(TAG + "_get", "W, H: " + getWidth() + ", " + getHeight()+" | "+parentWidth + ", "+parentHeight);
		LayoutParams params = (LayoutParams) holderView.getLayoutParams();
		params.height = getHolderSize();
		holderView.setLayoutParams(params);

		if (parentHeight != 0) {
			contentWidth = parentWidth - getPaddingLeft() - getPaddingRight();
			contentHeight = parentHeight - getPaddingTop() - getPaddingBottom() - getHolderSize();
		}
		updateSplits(parentWidth, parentHeight);

		Log.i(TAG + "_sizes", "CW: " + contentWidth + " |CH: " + contentHeight + " |FH: " + firstLayoutParams.height + " |SH: " + secondLayoutParams.height);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	void updateSplits(int parentWidth, int parentHeight) {
		Log.i(TAG + "_content", "W, H: " + contentWidth + ", " + contentHeight);

		firstLayoutParams.height = (int)(contentHeight * ratio);
		secondLayoutParams.height = contentHeight - firstLayoutParams.height;

		Log.i(TAG + "_first", "W, H: " + firstLayoutParams.width + ", " + firstLayoutParams.height);
		Log.i(TAG + "_second", "W, H: " + secondLayoutParams.width + ", " + secondLayoutParams.height);

		firstDiv.setLayoutParams(firstLayoutParams);
		secondDiv.setLayoutParams(secondLayoutParams);
	}

/*	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// TODO: consider storing these as member variables to reduce
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

	public static class SplitPlaceHolderFragment extends Fragment {
		static int id;

		public static SplitPlaceHolderFragment newInstance() {
			return new SplitPlaceHolderFragment();
		}

		public SplitPlaceHolderFragment() {
			++id;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			LinearLayout v = (LinearLayout)inflater.inflate(R.layout.split_frag_test, container, false);
			TextView tv = (TextView)v.findViewById(R.id.test_frag_text);
//			TextView tv = (TextView) getView().findViewById(R.id.test_frag_text);
			tv.setText("TestFrag" + id);
			return v;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
		}
	}
}
