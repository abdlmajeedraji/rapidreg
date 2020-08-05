package org.unicef.rapidreg.childcase.caseregister;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicef.rapidreg.IntentSender;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.Feature;
import org.unicef.rapidreg.base.record.RecordActivity;
import org.unicef.rapidreg.base.record.recordregister.RecordRegisterAdapter;
import org.unicef.rapidreg.base.record.recordregister.RecordRegisterFragment;
import org.unicef.rapidreg.childcase.CaseActivity;
import org.unicef.rapidreg.childcase.CaseFeature;
import org.unicef.rapidreg.childcase.casephoto.CasePhotoAdapter;
import org.unicef.rapidreg.event.CreateIncidentThruGBVCaseEvent;
import org.unicef.rapidreg.event.SaveCaseEvent;
import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.service.CaseService;
import org.unicef.rapidreg.service.RecordService;
import org.unicef.rapidreg.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.OnClick;

import static org.unicef.rapidreg.childcase.CaseFeature.ADD_CP_FULL;
import static org.unicef.rapidreg.childcase.CaseFeature.ADD_GBV_FULL;
import static org.unicef.rapidreg.childcase.CaseFeature.DETAILS_CP_FULL;
import static org.unicef.rapidreg.childcase.CaseFeature.DETAILS_CP_MINI;
import static org.unicef.rapidreg.childcase.CaseFeature.DETAILS_GBV_FULL;
import static org.unicef.rapidreg.childcase.CaseFeature.DETAILS_GBV_MINI;
import static org.unicef.rapidreg.childcase.CaseFeature.EDIT_FULL;
import static org.unicef.rapidreg.childcase.caseregister.CaseRegisterPresenter.MODULE_CASE_CP;
import static org.unicef.rapidreg.service.CaseService.CASE_ID;
import static org.unicef.rapidreg.service.RecordService.MODULE;

public class CaseMiniFormFragment extends RecordRegisterFragment {
    public static final String TAG = CaseMiniFormFragment.class.getSimpleName();

    @Inject
    CaseRegisterPresenter caseRegisterPresenter;

    @Inject
    CasePhotoAdapter casePhotoAdapter;

    @NonNull
    @Override
    public CaseRegisterPresenter createPresenter() {
        if (getArguments() != null) {
            if (getArguments().containsKey(MODULE)) {
                caseRegisterPresenter.setCaseType(getArguments().getString(MODULE));
            }
        }
        return caseRegisterPresenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getComponent().inject(this);
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((RecordActivity)getActivity()).setShowHideSwitcherToShowState();
        this.initTopWarning();
    }

    @Override
    public void onInitViewContent() {
        super.onInitViewContent();
        formSwitcher.setText(R.string.show_more_details);

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

    protected void initTopWarning() {
        if (this.caseIsInvalidated()) {
            topInfoMessage.setVisibility(View.VISIBLE);
        } else {
            topInfoMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected RecordRegisterAdapter createRecordRegisterAdapter() {
        List<Field> fields = caseRegisterPresenter.getValidFields();

        addProfileFieldForDetailsPage(0, fields);

        RecordRegisterAdapter recordRegisterAdapter = new RecordRegisterAdapter(getActivity(),
                fields,
                caseRegisterPresenter.getDefaultItemValues(),
                caseRegisterPresenter.getFieldValueVerifyResult(),
                true);
        casePhotoAdapter.setItems(caseRegisterPresenter.getDefaultPhotoPaths());
        recordRegisterAdapter.setPhotoAdapter(casePhotoAdapter);

        return recordRegisterAdapter;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void createIncidentThruGBVCase(CreateIncidentThruGBVCaseEvent event) {
        String caseId = getRecordRegisterData().getAsString(CASE_ID);
        Bundle extra = new Bundle();
        extra.putString(CASE_ID, caseId);
        extra.putSerializable(RecordService.ITEM_VALUES, caseRegisterPresenter.filterGBVRelatedItemValues(getRecordRegisterData()));
        new IntentSender().showIncidentActivity(getActivity(), true, extra);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCase(SaveCaseEvent event) {
        caseRegisterPresenter.saveRecord(getRecordRegisterData(), getPhotoPathsData(), this);
    }

    @Override
    public void onSaveSuccessful(long recordId) {
        Utils.showMessageByToast(getActivity(), R.string.save_success, Toast.LENGTH_SHORT);
        Bundle args = new Bundle();

        String moduleId = caseRegisterPresenter.getCaseType();
        Feature feature = moduleId.equals(MODULE_CASE_CP) ? DETAILS_CP_MINI : DETAILS_GBV_MINI;

        args.putString(MODULE, moduleId);
        args.putLong(CaseService.CASE_PRIMARY_ID, recordId);
        ((RecordActivity)getActivity()).turnToFeature(feature, args, null);
    }

    @OnClick(R.id.edit)
    public void onEditClicked() {
        Bundle args = new Bundle();
        args.putString(MODULE, caseRegisterPresenter.getCaseType());
        args.putSerializable(RecordService.ITEM_VALUES, getRecordRegisterData());
        args.putSerializable(RecordService.VERIFY_MESSAGE, getFieldValueVerifyResult());
        args.putStringArrayList(RecordService.RECORD_PHOTOS, (ArrayList<String>) getPhotoPathsData());
        ((CaseActivity) getActivity()).turnToFeature(CaseFeature.EDIT_MINI, args, null);
    }

    @OnClick(R.id.form_switcher)
    public void onSwitcherChecked() {
        Bundle args = new Bundle();
        args.putSerializable(RecordService.ITEM_VALUES, getRecordRegisterData());
        args.putSerializable(RecordService.VERIFY_MESSAGE, getFieldValueVerifyResult());
        args.putString(MODULE, caseRegisterPresenter.getCaseType());
        args.putStringArrayList(RecordService.RECORD_PHOTOS, (ArrayList<String>) getPhotoPathsData());

        CaseFeature currentFeature = (CaseFeature) ((CaseActivity) getActivity()).getCurrentFeature();

        Feature feature = currentFeature.isDetailMode() ?
                (currentFeature.isCPCase() ? DETAILS_CP_FULL : DETAILS_GBV_FULL) : currentFeature.isAddMode() ?
                (currentFeature.isCPCase() ? ADD_CP_FULL : ADD_GBV_FULL) : EDIT_FULL;
        ((RecordActivity) getActivity()).turnToFeature(feature, args, ANIM_TO_FULL);
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
