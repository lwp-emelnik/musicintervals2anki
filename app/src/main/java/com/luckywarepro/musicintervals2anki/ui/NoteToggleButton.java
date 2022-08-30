package com.luckywarepro.musicintervals2anki.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ToggleButton;

import com.luckywarepro.musicintervals2anki.R;

public class NoteToggleButton extends ToggleButton {
    private static final int[] STATE_HINTED = {R.attr.state_hinted};

    public static final String SELECTED_TEXT = "1";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private String hintFor;

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(32);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        hintPaint.setStyle(Paint.Style.FILL);
        hintPaint.setColor(Color.BLACK);
        hintPaint.setTextSize(24);
    }

    public NoteToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public NoteToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NoteToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoteToggleButton(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean sharp = this.getText().toString().endsWith("#");
        if (isChecked()) {
            canvas.drawText(SELECTED_TEXT, getWidth() / 2f, dpToPx(sharp ? 92 : 160), paint);
        }
        if (hintFor != null) {
            canvas.drawText(hintFor, getWidth() / 2f, dpToPx(sharp ? 50 : 119), hintPaint);
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (hintFor != null) {
            mergeDrawableStates(drawableState, STATE_HINTED);
        }
        return drawableState;
    }

    public void setHintFor(String hintFor) {
        this.hintFor = hintFor;
        refreshDrawableState();
        invalidate();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
