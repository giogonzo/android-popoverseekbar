package popoverseekbar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class PopoverSeekBarPopover extends View {
  private static final int SIDE_PAD = 17; // dp
  private static final int TEXT_PAD = 8; // dp
  private float maxWidthRatio;
  private Paint textPaint;
  private int width, height;
  private CharSequence[] content;
  private int sidePadding, textPadding, indHeight, indWidth, maxWidth, minWidth;
  private float lineSpacing;
  private int progress, ratio;
  private Drawable indicator, bg;
  private boolean below;

  public PopoverSeekBarPopover(Context context, AttributeSet attrs) {
    super(context, attrs);
    Resources r = getResources();
    textPaint = new Paint();
    textPaint.setColor(Color.GRAY);
    textPaint.setAntiAlias(true);
    sidePadding =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SIDE_PAD,
            r.getDisplayMetrics());
    textPadding =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_PAD,
            r.getDisplayMetrics());
  }

  public void setBelow(boolean below) {
    this.below = below;
  }

  public void setContent(CharSequence[] content) {
    this.content = content;
  }

  public void setIndicator(Drawable indicator) {
    this.indicator = indicator;
    indHeight = indicator.getIntrinsicHeight();
    indWidth = indicator.getIntrinsicWidth();
  }

  public void setBg(Drawable bg) {
    this.bg = bg;
  }

  public void setRatio(int ratio) {
    this.ratio = ratio;
  }

  public void setProgress(int progress) {
    this.progress = progress;
    invalidate();
  }

  public void setTextSize(float ts) {
    textPaint.setTextSize(ts);
    invalidate();
  }

  public void setLineSPacing(float ls) {
    this.lineSpacing = ls;
    invalidate();
  }

  public void setMaxWidthRatio(float mwr) {
    this.maxWidthRatio = mwr;
    invalidate();
  }

  public void setMinWidth(float mw) {
    this.minWidth = (int) mw;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    drawContent(canvas);
    drawIndicator(canvas);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    width = w;
    measureHeight();
    setMeasuredDimension(width, height);
    invalidate();
    requestLayout();
  }

  private float paddedWidth() {
    return width - 2 * sidePadding;
  }

  private int getProgress(int progress) {
    int index = progress / ratio;
    if (progress % ratio >= ratio / 2) index += 1;
    return index;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureHeight();
    setMeasuredDimension(width, height);
  }

  private void measureHeight() {
    int lines, maxLines = 0;
    for (int i = 0; i < content.length; i++) {
      lines = getLines(i, false).size();
      if (lines > maxLines) maxLines = lines;
    }
    height =
        (int) (indHeight + 2 * textPadding + maxLines * textPaint.getTextSize() + (maxLines - 1)
            * lineSpacing)
            + (int) (textPaint.getTextSize() / 4);
  }

  private void drawContent(Canvas canvas) {
    if (content == null) return;
    int p = getProgress(progress);
    if (content[p].length() == 0) return;

    List<String> lines = getLines(p, true);

    int popWidth = (int) Math.max(maxWidth + 2 * textPadding, minWidth);
    int popHeight =
        (int) (2 * textPadding + lines.size() * textPaint.getTextSize() + (lines.size() - 1)
            * lineSpacing)
            + (int) (textPaint.getTextSize() / 4);

    int popLeft =
        (int) Math.max(0,
            Math.min(width - popWidth, sidePadding + unit() * progress - popWidth / 2));
    int popTop = (int) (below ? indHeight : (height - popHeight - indHeight));
    bg.setBounds(popLeft, popTop, popLeft + popWidth, popTop + popHeight);
    bg.draw(canvas);
    for (int i = 0; i < lines.size(); i++)
      canvas.drawText(lines.get(i), popLeft + (popWidth - maxWidth) / 2, popTop + textPadding
          + (i + 1) * textPaint.getTextSize() + i * lineSpacing, textPaint);
  }

  private List<String> getLines(int p, boolean single) {
    int currentWidth = 0, wordWidth = 0, maxWidth = 0;
    String currentLine = "";
    String[] words = ((String) content[p]).split(" ");
    List<String> lines = new ArrayList<String>();

    for (int i = 0; i < words.length; i++) {
      wordWidth = (int) textPaint.measureText(" " + words[i]);
      if (currentWidth + wordWidth + 2 * textPadding <= width * maxWidthRatio) {
        currentWidth += wordWidth;
        currentLine += currentLine.equals("") ? words[i] : " " + words[i];
        if (currentWidth > maxWidth) maxWidth = currentWidth;
      } else {
        lines.add(new String(currentLine));
        currentLine = new String(words[i]);
        currentWidth = (int) (wordWidth - textPaint.measureText(" "));
      }
    }
    lines.add(new String(currentLine));
    if (single || maxWidth > this.maxWidth) this.maxWidth = maxWidth;
    return lines;
  }

  private void drawIndicator(Canvas canvas) {
    int indTop = (int) (below ? 0 : height - indHeight);
    int indLeft =
        (int) Math.max(0,
            Math.min(width, sidePadding + unit() * progress - indicator.getIntrinsicWidth() / 2));
    indicator.setBounds(indLeft, indTop, (int) (indLeft + indWidth), (int) (indTop + indHeight));
    indicator.draw(canvas);
  }

  private float unit() {
    return paddedWidth() / ((content.length - 1) * ratio);
  }
}
