package com.luckywarepro.musicintervals2anki.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.luckywarepro.musicintervals2anki.R;

public class IntervalToggleButton extends ToggleButton {
    private static final int[] STATE_HIGHLIGHTED = {R.attr.state_highlighted};

    private static final int TEXT_SIZE = 24;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean isHighlighted;

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
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isHighlighted) {
            mergeDrawableStates(drawableState, STATE_HIGHLIGHTED);
        }
        return drawableState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isHighlighted) {
            canvas.drawText(NoteToggleButton.SELECTED_TEXT, getWidth() / 2f, TEXT_SIZE + 24 , paint);
        }
    }

    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
        refreshDrawableState();
    }
}
