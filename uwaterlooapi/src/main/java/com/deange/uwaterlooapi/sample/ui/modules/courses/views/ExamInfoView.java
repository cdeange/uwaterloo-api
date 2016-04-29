package com.deange.uwaterlooapi.sample.ui.modules.courses.views;

import android.content.Context;
import android.widget.ListView;

import com.deange.uwaterlooapi.model.courses.ExamInfo;
import com.deange.uwaterlooapi.model.courses.ExamSection;
import com.deange.uwaterlooapi.sample.R;
import com.deange.uwaterlooapi.sample.model.CombinedCourseInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;

public class ExamInfoView extends BaseCourseView {

    @Bind(R.id.exam_list_view) ListView mListView;

    public ExamInfoView(final Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_exam_info;
    }

    @Override
    public void bind(final CombinedCourseInfo info) {
        final ExamInfo examInfo = info.getExams();

        final List<ExamSection> sections = new ArrayList<>(examInfo.getSections());
        Collections.sort(sections, new Comparator<ExamSection>() {
            @Override
            public int compare(final ExamSection lhs, final ExamSection rhs) {
                return lhs.getSection().compareToIgnoreCase(rhs.getSection());
            }
        });

        mListView.setAdapter(new ExamAdapter(getContext(), sections));
    }
}
