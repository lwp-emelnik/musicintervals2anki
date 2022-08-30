package com.luckywarepro.musicintervals2anki.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.luckywarepro.musicintervals2anki.R;

import java.util.ArrayList;

public class IntervalToggleButton extends ToggleButton {
    private static final int TEXT_SIZE = 24;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean isHighlighted;
    private String hintFor;

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(TEXT_SIZE);
    }

    public IntervalToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public IntervalToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public IntervalToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IntervalToggleButton(Context context) {
        super(context);
        init();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        ArrayList<Integer> drawableStates = new ArrayList<>();
        if (isHighlighted) {
            drawableStates.add(R.attr.state_highlighted);
        }
        if (hintFor != null) {
            drawableStates.add(R.attr.state_hinted);
        }
        final int[] drawableState = super.onCreateDrawableState(extraSpace + drawableStates.size());
        mergeDrawableStates(drawableState, drawableStates.stream().mapToInt(i -> i).toArray());
        return drawableState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isHighlighted) {
            canvas.drawText(NoteToggleButton.SELECTED_TEXT, getWidth() / 2f, TEXT_SIZE + 24, paint);
        }
        if (hintFor != null) {
            canvas.drawText(hintFor, getWidth() / 2f, TEXT_SIZE + 84, paint);
        }
    }

    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
        refreshDrawableState();
    }

    public void setHintFor(String hintFor) {
        this.hintFor = hintFor;
        refreshDrawableState();
        invalidate();
    }
}
