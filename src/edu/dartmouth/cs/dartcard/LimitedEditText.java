package edu.dartmouth.cs.dartcard;

/**
 * LimitedEditText (we didn't write this code)
 *
 * Copyright (C) 2013  Khaled Bakhit
 * 
 * 
 * LimitedEditText is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LimitedEditText code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Check <http://www.gnu.org/licenses/> for more details.
 */
 
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.renderscript.Float2;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
 
public class LimitedEditText extends EditText
{
    /**
     * Paint Object used to paint limit text.
     */
    private Paint limit_text_paint;
         
    /**
     * Current size of text displayed in the EditView.
     */
    private int current_text_size;
    /**
     * Maximum text size allowed.
     */
    private int maximum_text_size;
    /**
     * Default limit indicator X margin used.
     */
    private final int DEFAULT_X_MARGIN= 10;
    /**
     * Default limit indicator Y margin used.
     */
    private final int DEFAULT_Y_MARGIN= 20;
    /**
     * Flag indicating text size limit is unlimited.
     */
    public static final int UNLIMITED= -100;
     
    /**
     * LimitedEditText constructor.
     * @param context Context that will display this view.
     * @param attrs Set of attributes defined for this view.
     * @param defStyle Style defined for this view.
     */
    public LimitedEditText(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        initComponents(attrs);
    }
    /**
     * LimitedEditText constructor.
     * @param context Context that will display this view.
     * @param attrs Set of attributes defined for this view.
     */
    public LimitedEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initComponents(attrs);
    }
    /**
     * LimitedEditText constructor.
     * @param context Context that will display this view.
     */
    public LimitedEditText(Context context)
    {
        super(context);
        initComponents(null);
    }
     
    /**
     * Initialize the UI components.
     * @param attrs Set of attributes defined for this view.
     */
    private void initComponents(AttributeSet attrs)
    {
        limit_text_paint= new Paint();
        limit_text_paint.setColor(Color.GRAY);
        limit_text_paint.setTextSize(20);
        maximum_text_size= UNLIMITED;
        current_text_size= 0;
        if(attrs!=null) //android:maxLength 
            maximum_text_size= attrs.getAttributeIntValue("android", "maxLength", UNLIMITED);
         
        super.addTextChangedListener(new  TextWatcher(){
 
            @Override
            public void afterTextChanged(Editable s) 
            {
                if(maximum_text_size != UNLIMITED)
                    current_text_size= getText().toString().length();
                invalidate();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });
    }
     
    /**
     * Set maximum text size for given EditText.
     * @param max Maximum number of characters allowed.
     */
    public void setMaxTextSize(int max)
    {
        maximum_text_size= max;
        if(max== UNLIMITED)
            setFilters(null);
        else
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});
    }
    /**
     * Remove text size limit. 
     * This will stop drawing the amount of characters left.
     */
    public void removeTextSizeLimit()
    {
        setMaxTextSize(UNLIMITED);
    }
    /**
     * Get the X position of limit indicator.
     * @param text Text to be drawn on the limit indicator.
     * @return X position of limit indicator.
     */
    protected float getLimitIndicatorX(String text)
    {
        float widths[]= new float[maximum_text_size + 5];
        
        limit_text_paint.getTextWidths(text, widths);
         
        float sum= 0;
        for(float w: widths)
            sum+= w;
        return getWidth() + getScrollX() - sum - DEFAULT_X_MARGIN;
    }
    /**
     * Get the Y position of limit indicator.
     * @param text Text to be drawn on the limit indicator.
     * @return Y position of limit indicator.
     */
    protected float getLimitIndicatorY(String text)
    {
        return DEFAULT_Y_MARGIN + getScrollY();
    }
     
     
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if(maximum_text_size== UNLIMITED)
            return;
         
        String text= current_text_size+"/"+maximum_text_size;
        canvas.drawText(text, getLimitIndicatorX(text), getLimitIndicatorY(text), limit_text_paint);
    }
     
}