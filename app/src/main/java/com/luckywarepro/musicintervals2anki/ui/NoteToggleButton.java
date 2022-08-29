package com.luckywarepro.musicintervals2anki.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class NoteToggleButton extends ToggleButton {
    public static final String SELECTED_TEXT = "1";

    private static final int TEXT_SIZE = 32;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(NoteToggleButton.TEXT_SIZE);
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
    }
}
