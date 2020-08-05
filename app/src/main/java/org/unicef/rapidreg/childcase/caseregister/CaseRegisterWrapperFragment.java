package org.unicef.rapidreg.childcase.caseregister;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicef.rapidreg.IntentSender;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.Feature;
import org.unicef.rapidreg.base.record.RecordActivity;
import org.unicef.rapidreg.base.record.recordphoto.RecordPhotoAdapter;
import org.unicef.rapidreg.base.record.recordregister.RecordRegisterWrapperFragment;
import org.unicef.rapidreg.childcase.CaseActivity;
import org.unicef.rapidreg.childcase.casephoto.CasePhotoAdapter;
import org.unicef.rapidreg.event.CreateIncidentThruGBVCaseEvent;
import org.unicef.rapidreg.event.SaveCaseEvent;
import org.unicef.rapidreg.forms.Section;
import org.unicef.rapidreg.service.CaseService;
import org.unicef.rapidreg.service.RecordService;
import org.unicef.rapidreg.service.cache.ItemValuesMap;
import org.unicef.rapidreg.utils.Utils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.OnClick;

import static org.unicef.rapidreg.childcase.CaseFeature.DETAILS_CP_FULL;
import static org.unicef.rapidreg.childcase.CaseFeature.DETAILS_GBV_FULL;
import static org.unicef.rapidreg.childcase.CaseFeature.EDIT_FULL;
import static org.unicef.rapidreg.childcase.caseregister.CaseRegisterPresenter.MODULE_CASE_CP;
import static org.unicef.rapidreg.service.CaseService.CASE_ID;
import static org.unicef.rapidreg.service.RecordService.MODULE;

public class CaseRegisterWrapperFragment extends RecordRegisterWrapperFragment {
    public static final String TAG = CaseRegisterWrapperFragment.class.getSimpleName();

    @Inject
    CaseRegisterPresenter caseRegisterPresenter;

    @Inject
    CasePhotoAdapter casePhotoAdapter;

    @Override
    public CaseRegisterPresenter createPresenter() {
        if (getArguments() != null && getArguments().containsKey(MODULE)) {
            caseRegisterPresenter.setCaseType(getArguments().getString(MODULE));
        }
        return caseRegisterPresenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        getComponent().inject(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initFloatingActionButton() {
        if (((RecordActivity) getActivity()).getCurrentFeature().isDetailMode()) {
            if (this.caseIsInvalidated()) {
                editButton.setVisibility(View.GONE);
            } else {
                editButton.setVisibility(View.VISIBLE);
            }
        } else {
            editButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initTopWarning() {
        if (this.caseIsInvalidated()) {
            this.topInfoMessage.setVisibility(View.VISIBLE);
        } else {
            this.topInfoMessage.setVisibility(View.GONE);
        }
    }

    @Override
    protected RecordPhotoAdapter createRecordPhotoAdapter() {
        casePhotoAdapter.setItems(getArguments().getStringArrayList(RecordService.RECORD_PHOTOS));
        return casePhotoAdapter;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCase(SaveCaseEvent event) {
        caseRegisterPresenter.saveRecord(getRecordRegisterData(), getPhotoPathsData(), this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void createIncidentThruGBVCase(CreateIncidentThruGBVCaseEvent event) {
        String caseId = getRecordRegisterData().getAsString(CASE_ID);
        Bundle extra = new Bundle();
        extra.putString(CASE_ID, caseId);
        extra.putSerializable(RecordService.ITEM_VALUES, caseRegisterPresenter.filterGBVRelatedItemValues
                (getRecordRegisterData()));

        new IntentSender().showIncidentActivity(getActivity(), true, extra);
    }

    @OnClick(R.id.edit)
    public void onEditClicked() {
        Bundle args = new Bundle();
        args.putString(MODULE, caseRegisterPresenter.getCaseType());
        args.putSerializable(RecordService.ITEM_VALUES, getRecordRegisterData());
        args.putSerializable(RecordService.VERIFY_MESSAGE, new ItemValuesMap());
        args.putStringArrayList(RecordService.RECORD_PHOTOS, (ArrayList<String>) getCurrentPhotoAdapter().getAllItems());

        ((CaseActivity) getActivity()).turnToFeature(EDIT_FULL, args, null);
    }

    @Override
    protected void initItemValues() {
        if (getArguments() != null) {
            setRecordRegisterData((ItemValuesMap) getArguments().getSerializable(RecordService.ITEM_VALUES));
            setFieldValueVerifyResult((ItemValuesMap) getArguments().getSerializable(RecordService.VERIFY_MESSAGE));
        }
    }

    @Override
    protected void initFormData() {
        form = caseRegisterPresenter.getTemplateForm();
        sections = form.getSections();
    }

    @NonNull
    protected FragmentPagerItems getPages() {
        FragmentPagerItems pages = new FragmentPagerItems(getActivity());
        for (Section section : sections) {
            String[] values = section.getName().values().toArray(new String[0]);
            Bundle args = new Bundle();
            args.putString(MODULE, caseRegisterPresenter.getCaseType());
            args.putSerializable(RecordService.ITEM_VALUES, getRecordRegisterData());
            args.putSerializable(RecordService.VERIFY_MESSAGE, getFieldValueVerifyResult());
            args.putStringArrayList(RecordService.RECORD_PHOTOS, (ArrayList<String>) getCurrentPhotoAdapter().getAllItems());
            pages.add(FragmentPagerItem.of(values[0], CaseRegisterFragment.class, args));
        }
        return pages;
    }

    @Override
    public void onSaveSuccessful(long recordId) {
        Utils.showMessageByToast(getActivity(), R.string.save_success, Toast.LENGTH_SHORT);
        String moduleId = caseRegisterPresenter.getCaseType();
        Feature feature = moduleId.equals(MODULE_CASE_CP) ? DETAILS_CP_FULL : DETAILS_GBV_FULL;

        Bundle args = new Bundle();
        args.putLong(CaseService.CASE_PRIMARY_ID, recordId);
        args.putString(MODULE, caseRegisterPresenter.getCaseType());
        args.putSerializable(RecordService.ITEM_VALUES, getRecordRegisterData());
        args.putStringArrayList(RecordService.RECORD_PHOTOS, (ArrayList<String>) getPhotoPathsData());

        ((RecordActivity) getActivity()).turnToFeature(feature, args, null);
    }

    // case_id comes as part of itemValues
    // then is safer than passing recordId around in Bundle
    private boolean caseIsInvalidated() {
        String caseId = getRecordRegisterData().getAsString(CASE_ID);
        if (caseId != null) {
            return caseRegisterPresenter.getCaseIsInvalidated(caseId);
        } else {
            return false;
        }
    }
}
