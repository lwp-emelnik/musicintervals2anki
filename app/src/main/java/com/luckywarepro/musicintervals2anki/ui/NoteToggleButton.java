package com.luckywarepro.musicintervals2anki.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.luckywarepro.musicintervals2anki.R;

public class NoteToggleButton extends ToggleButton {
    private static final int[] STATE_HINTED = {R.attr.state_hinted};

    public static final String SELECTED_TEXT = "1";

    private static final int TEXT_SIZE = 32;
    private static final int HINT_TEXT_SIZE = 24;


    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private String hintFor;

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(NoteToggleButton.TEXT_SIZE);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        hintPaint.setStyle(Paint.Style.FILL);
        hintPaint.setColor(Color.BLACK);
        hintPaint.setTextSize(NoteToggleButton.HINT_TEXT_SIZE);
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
        if (isChecked()) {
            canvas.drawText(SELECTED_TEXT, getWidth() / 2f, getHeight() - NoteToggleButton.TEXT_SIZE, paint);
        }
        if (hintFor != null) {
            canvas.drawText(hintFor, getWidth() / 2f, HINT_TEXT_SIZE + (this.getText().toString().endsWith("#") ? 114 : 304), hintPaint);
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
}
