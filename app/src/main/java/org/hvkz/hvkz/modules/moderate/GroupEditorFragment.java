package org.hvkz.hvkz.modules.moderate;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.BaseWindow;

public class GroupEditorFragment extends AAH_FabulousFragment implements BaseWindow<GroupEditorPresenter>
{
    public static final String KEY = "groupname";
    private static final String CLOSED = "closed";

    private BaseWindow parent;
    private View contentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.contentView = View.inflate(getContext(), R.layout.admin_layout, null);

        parent = (BaseWindow) getActivity();
        GroupEditorPresenter presenter = getPresenter();
        presenter.init();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        RelativeLayout rl_content = (RelativeLayout) contentView.findViewById(R.id.rl_content);
        setAnimationDuration(300);
        setPeekHeight(300);
        setViewMain(rl_content);
        setMainContentView(contentView);
        super.setupDialog(dialog, style);
    }

    public void closeWindow() {
        closeFilter(CLOSED);
    }

    public BaseWindow getParent() {
        return parent;
    }

    public static GroupEditorFragment newInstance(Group group) {
        GroupEditorFragment fragment = new GroupEditorFragment();

        if (group != null) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY, group.getGroupName());
            fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    public void showProgress(String message) {
        parent.showProgress(message);
    }

    @Override
    public void hideProgress() {
        parent.hideProgress();
    }

    @Override
    public void dialogMessage(String title, String message) {
        parent.dialogMessage(title, message);
    }

    @Override
    public void onBackPressed() {
        parent.onBackPressed();
    }

    @Override
    public GroupEditorPresenter getPresenter() {
        return new GroupEditorPresenter(this);
    }

    @Override
    public Object getViewFinder() {
        return contentView;
    }
}
