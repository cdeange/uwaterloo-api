package com.deange.uwaterlooapi.sample.ui.flow;

import android.content.Context;
import android.util.AttributeSet;
import com.deange.uwaterlooapi.sample.R;
import com.deange.uwaterlooapi.sample.dagger.scoping.MortarContextFactory;
import flow.path.Path;

public class MortarScreenSwitcherFrame extends FramePathContainerView {
  public MortarScreenSwitcherFrame(Context context, AttributeSet attrs) {
    super(context, attrs, new SimplePathContainer(
        R.id.screen_switcher_tag, Path.contextFactory(new MortarContextFactory())));
  }
}
