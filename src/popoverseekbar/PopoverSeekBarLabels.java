package popoverseekbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class PopoverSeekBarLabels extends View {
	private static final int SIDE_PAD = 17; // dp
	private static final int STROKE_WIDTH = 1; // dp
	private static final int TEXT_SIZE = 12; // sp
	private static final int HEIGHT = 30; // dp
	private static final int TEXT_PAD_BOTTOM = 4; // dp
	private Paint textPaint, linesPaint;
	private int width, height;
	private CharSequence[] labels;
	private int sidePadding;
	private int textPadding;
	private boolean below;

	public PopoverSeekBarLabels(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources r = getResources();
		textPaint = new Paint();
		textPaint.setColor(Color.GRAY);
		textPaint.setTextSize(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE, r.getDisplayMetrics()));
		textPaint.setAntiAlias(true);
		linesPaint = new Paint();
		linesPaint.setColor(Color.GRAY);
		linesPaint
				.setStrokeWidth(TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_SP, STROKE_WIDTH,
						r.getDisplayMetrics()));
		sidePadding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, SIDE_PAD, r.getDisplayMetrics());
		height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				HEIGHT, r.getDisplayMetrics());
		textPadding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, TEXT_PAD_BOTTOM,
				r.getDisplayMetrics());
	}

	public void setBelow(boolean below) {
		this.below = below;
	}

	public void setLabels(CharSequence[] contentLabels) {
		this.labels = contentLabels;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawLabels(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w;
		invalidate();
	}

	private float paddedWidth() {
		return width - 2 * sidePadding;
	}

	private void drawLabels(Canvas canvas) {
		if (labels == null)
			return;
		float w = paddedWidth();
		float unit = w / (labels.length - 1);
		for (int i = 0; i < labels.length; i++) {
			if (labels[i].length() == 0)
				continue;
			float textWidth = textPaint.measureText((String) labels[i]);
			float textLeft = Math.max(
					0,
					Math.min(width - textWidth, sidePadding + unit * i
							- textWidth / 2));
			if (!below) {
				canvas.drawLine(sidePadding + unit * i, textPaint.getTextSize()
						+ textPadding, sidePadding + unit * i, height,
						linesPaint);
				canvas.drawText((String) labels[i], textLeft,
						textPaint.getTextSize(), textPaint);
			} else {
				canvas.drawLine(sidePadding + unit * i, textPadding,
						sidePadding + unit * i,
						height - textPaint.getTextSize(), linesPaint);
				canvas.drawText((String) labels[i], textLeft, height, textPaint);
			}
		}
	}
}
