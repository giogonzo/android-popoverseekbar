package popoverseekbar;

import com.popoverseekbar.popoverseekbar.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PopoverSeekBar extends LinearLayout implements OnSeekBarChangeListener {
  private static final int RATIO = 100;
  private static final int LABELS_OFFSET = 10; // dp
  private static final int TEXT_SIZE = 18; // sp
  private static final int LINE_SPACING = 4; // sp
  private int snap;
  private SeekBar bar;
  private CharSequence[] content;
  private CharSequence[] contentLabels;
  private int progress;
  private PopoverSeekBarLabels popoverLabels;
  private PopoverSeekBarPopover popover;
  private boolean popoverBelow;
  private Drawable indicator, bg;
  private int labelsOffset;
  private float popoverTextSize, popoverLineSpacing, popoverMaxWidthRatio, popoverMinWidth;

  public PopoverSeekBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PopoverSeekBar);
    Resources r = getResources();

    content = a.getTextArray(R.styleable.PopoverSeekBar_content);
    contentLabels = a.getTextArray(R.styleable.PopoverSeekBar_contentLabels);
    progress = a.getInteger(R.styleable.PopoverSeekBar_progress, 0);
    indicator = a.getDrawable(R.styleable.PopoverSeekBar_popoverIndicator);
    popoverBelow = a.getBoolean(R.styleable.PopoverSeekBar_popoverBelow, false);
    bg = a.getDrawable(R.styleable.PopoverSeekBar_popoverBg);
    popoverTextSize =
        a.getDimensionPixelSize(
            R.styleable.PopoverSeekBar_popoverTextSize,
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE,
                r.getDisplayMetrics()));
    popoverLineSpacing =
        a.getDimensionPixelSize(
            R.styleable.PopoverSeekBar_popoverLineSpacing,
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, LINE_SPACING,
                r.getDisplayMetrics()));
    popoverMaxWidthRatio =
        a.getFloat(R.styleable.PopoverSeekBar_popoverMaxWidthRatio,
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1, r.getDisplayMetrics()));
    popoverMinWidth =
        a.getDimensionPixelSize(R.styleable.PopoverSeekBar_popoverMinWidth,
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 0, r.getDisplayMetrics()));

    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.popover_seekbar, this, true);
    a.recycle();

    init();
  }

  public int getProgress() {
    return progress;
  }

  public void setProgress(int prog) {
    progress = prog;
    bar.setProgress(progress * RATIO);
    updatePopover();
    invalidate();
  }

  private void init() {
    setOrientation(LinearLayout.VERTICAL);
    bar = (SeekBar) getChildAt(0);

    // setup labels
    if (contentLabels != null) {
      labelsOffset =
          (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LABELS_OFFSET,
              getResources().getDisplayMetrics());
      popoverLabels = new PopoverSeekBarLabels(getContext(), null);
      addView(popoverLabels, 0);
      popoverLabels.setLabels(contentLabels);
      popoverLabels.setBelow(!popoverBelow);
    }

    // setup popover
    popover = new PopoverSeekBarPopover(getContext(), null);
    if (popoverBelow)
      addView(popover);
    else
      addView(popover, 0);

    popover.setContent(content);
    popover.setIndicator(indicator);
    popover.setBg(bg);
    popover.setRatio(RATIO);
    popover.setBelow(popoverBelow);
    popover.setTextSize(popoverTextSize);
    popover.setLineSPacing(popoverLineSpacing);
    popover.setMaxWidthRatio(popoverMaxWidthRatio);
    popover.setMinWidth(popoverMinWidth);
    bar.setMax((content.length - 1) * RATIO);

    // TODO:
    snap = 20;
    bar.setOnSeekBarChangeListener(this);
    bar.setProgress(progress * RATIO);

    updatePopover();
    if (listener != null) listener.onChange(progress);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int count = getChildCount();
    int y = getPaddingTop();

    if (!popoverBelow) {
      popover.layout(getPaddingLeft(), y, getWidth() - getPaddingRight(),
          y + popover.getMeasuredHeight());
      y += popover.getMeasuredHeight();
    }

    if (popoverBelow && popoverLabels != null) {
      popoverLabels.layout(getPaddingLeft(), y, getWidth() - getPaddingRight(),
          y + popoverLabels.getMeasuredHeight());
      y += popoverLabels.getMeasuredHeight() - labelsOffset;
    }

    bar.layout(getPaddingLeft(), y, getWidth() - getPaddingRight(), y + bar.getMeasuredHeight());
    y += bar.getMeasuredHeight();

    if (!popoverBelow && popoverLabels != null) {
      popoverLabels.layout(getPaddingLeft(), y - labelsOffset, getWidth() - getPaddingRight(), y
          - labelsOffset + popoverLabels.getMeasuredHeight());
      y += popoverLabels.getMeasuredHeight() - labelsOffset;
    }

    if (popoverBelow) {
      popover.layout(getPaddingLeft(), y, getWidth() - getPaddingRight(),
          y + popover.getMeasuredHeight());
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int count = getChildCount();
    int height = getPaddingTop(), width = getWidth();
    for (int i = 0; i < count; i++) {
      View child = getChildAt(i);
      measureChild(child, widthMeasureSpec, heightMeasureSpec);
      height += child.getMeasuredHeight();
    }
    height += getPaddingBottom();
    height -= labelsOffset;
    setMeasuredDimension(resolveSize(width, widthMeasureSpec), height);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
    progress = getProgress(prog);
    if (progress * RATIO - snap < prog && prog < progress * RATIO + snap)
      bar.setProgress(progress * RATIO);
    updatePopover();
    if (listener != null) listener.onChange(progress);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {}

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    bar.setProgress(progress * RATIO);
    if (listener != null) listener.onStopTracking(progress);
  }

  private void updatePopover() {
    popover.setProgress(bar.getProgress());
  }

  private int getProgress(int progress) {
    int index = progress / RATIO;
    if (progress % RATIO >= RATIO / 2) index += 1;
    return index;
  }

  private OnChangeListener listener;

  public void setOnChangeListener(OnChangeListener l) {
    listener = l;
  }

  public interface OnChangeListener {
    void onChange(int progress);

    void onStopTracking(int progress);
  }
}
