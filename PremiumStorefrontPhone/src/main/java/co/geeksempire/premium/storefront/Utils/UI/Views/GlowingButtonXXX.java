/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 4/27/21 5:48 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.Utils.UI.Views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import java.util.Arrays;

import co.geeksempire.premium.storefront.R;

public class GlowingButtonXXX extends AppCompatButton implements View.OnTouchListener {

    private int mBackgroundColor;

    private int mGlowColor;

    private int mUnpressedGlowSize;

    private int mPressedGlowSize;

    private int mCornerRadius;

    public GlowingButtonXXX(final Context context) {
        super(context);
        this.setStateListAnimator(null);
        setOnTouchListener(this);
        initDefaultValues();
    }

    public GlowingButtonXXX(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.setStateListAnimator(null);
        setOnTouchListener(this);
        initDefaultValues();
        parseAttrs(context, attrs);
    }

    public GlowingButtonXXX(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setStateListAnimator(null);
        setOnTouchListener(this);
        initDefaultValues();
        parseAttrs(context, attrs);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackground(getBackgroundWithGlow(this, mBackgroundColor,
                        mGlowColor, mCornerRadius, mUnpressedGlowSize, mPressedGlowSize));
                break;
            case MotionEvent.ACTION_UP:
                setBackground(getBackgroundWithGlow(this, mBackgroundColor,
                        mGlowColor, mCornerRadius, mUnpressedGlowSize, mUnpressedGlowSize));
                break;
        }
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateButtonGlow();
    }

    private void parseAttrs(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GlowButton);
        if (typedArray == null) {
            return;
        }
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.GlowButton_backgroundColor) {
                mBackgroundColor = typedArray.getColor(attr, Color.BLACK);
            } else if (attr == R.styleable.GlowButton_glowColor) {
                mGlowColor = typedArray.getColor(attr, Color.BLACK);
            } else if (attr == R.styleable.GlowButton_unpressedGlowSize) {
                mUnpressedGlowSize = typedArray.getDimensionPixelSize(attr, R.dimen.default_unpressed_glow_size);
            } else if (attr == R.styleable.GlowButton_pressedGlowSize) {
                mPressedGlowSize = typedArray.getDimensionPixelSize(attr, R.dimen.default_pressed_glow_size);
            }
        }
        typedArray.recycle();
    }

    private void updateButtonGlow() {
        setBackground(getBackgroundWithGlow(this, mBackgroundColor,
                mGlowColor, mCornerRadius, mUnpressedGlowSize, mUnpressedGlowSize));
    }

    private void initDefaultValues() {

        Resources resources = getResources();
        if (resources == null) {
            return;
        }
        mBackgroundColor = resources.getColor(R.color.default_color_bright);
        mGlowColor = resources.getColor(R.color.default_color_game);
        mCornerRadius = resources.getDimensionPixelSize(R.dimen.default_corner_radius);
        mUnpressedGlowSize = resources.getDimensionPixelSize(R.dimen.default_unpressed_glow_size);
        mPressedGlowSize = resources.getDimensionPixelSize(R.dimen.default_pressed_glow_size);

    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        updateButtonGlow();
    }



    public static Drawable getBackgroundWithGlow(View view, int backgroundColor,
            int glowColor,
            int cornerRadius,
            int unPressedGlowSize,
            int pressedGlowSize) {

        float[] outerRadius = new float[8];
        Arrays.fill(outerRadius, cornerRadius);

        Rect shapeDrawablePadding = new Rect();

        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.setPadding(shapeDrawablePadding);

        shapeDrawable.getPaint().setColor(backgroundColor);
        shapeDrawable.getPaint().setShadowLayer(pressedGlowSize, 0f, 0f, glowColor);

        view.setLayerType(LAYER_TYPE_SOFTWARE, shapeDrawable.getPaint());

        shapeDrawable.setShape(new RoundRectShape(outerRadius, null, null));

        LayerDrawable drawable = new LayerDrawable(new Drawable[]{shapeDrawable});
        drawable.setLayerInset(0, unPressedGlowSize, unPressedGlowSize, unPressedGlowSize, unPressedGlowSize);

        return drawable;

    }
}
