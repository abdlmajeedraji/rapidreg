package org.unicef.rapidreg.service;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLOperator;

import org.unicef.rapidreg.PrimeroAppConfiguration;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.model.Tracing;
import org.unicef.rapidreg.repository.TracingDao;
import org.unicef.rapidreg.repository.TracingPhotoDao;
import org.unicef.rapidreg.repository.impl.TracingDaoImpl;
import org.unicef.rapidreg.repository.impl.TracingPhotoDaoImpl;
import org.unicef.rapidreg.service.cache.ItemValuesMap;
import org.unicef.rapidreg.utils.StreamUtil;
import org.unicef.rapidreg.utils.TextUtils;
import org.unicef.rapidreg.utils.Utils;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import static org.unicef.rapidreg.model.RecordModel.EMPTY_AGE;
import static org.unicef.rapidreg.utils.Utils.getRegisterDateByYyyyMmDd;

public class TracingService extends RecordService {
    public static final String TAG = TracingService.class.getSimpleName();
    public static final String TRACING_DISPLAY_ID = "tracing_request_id_display";
    public static final String TRACING_ID = "tracing_request_id";
    public static final String TRACING_PRIMARY_ID = "tracing_primary_id";

    private TracingDao tracingDao = new TracingDaoImpl();
    private TracingPhotoDao tracingPhotoDao = new TracingPhotoDaoImpl();

    public TracingService(TracingDao tracingDao, TracingPhotoDao tracingPhotoDao) {
        this.tracingDao = tracingDao;
        this.tracingPhotoDao = tracingPhotoDao;
    }

    public Tracing getById(long tracingId) {
        return tracingDao.getTracingById(tracingId);
    }

    public Tracing getByUniqueId(String uniqueId) {
        return tracingDao.getTracingByUniqueId(uniqueId);
    }

    public List<Tracing> getAll() {
        return tracingDao.getAll(PrimeroAppConfiguration.getCurrentUsername(),
                PrimeroAppConfiguration.getApiBaseUrl());
    }

    public List<Long> getAllOrderByDateASC() {
        return extractIds(tracingDao.getAllTracingsOrderByDate(true, PrimeroAppConfiguration.getCurrentUsername(),
                PrimeroAppConfiguration.getApiBaseUrl()));
    }

    public List<Long> getAllOrderByDateDES() {
        return extractIds(tracingDao.getAllTracingsOrderByDate(false, PrimeroAppConfiguration.getCurrentUsername(),
                PrimeroAppConfiguration.getApiBaseUrl()));
    }

    public List<Long> getSearchResult(String uniqueId, String name, int ageFrom, int ageTo, Date
            date) {
        OperatorGroup searchCondition = getSearchCondition(uniqueId, name, ageFrom, ageTo, date);
        return extractIds(tracingDao.getAllTracingsByOperatorGroup(PrimeroAppConfiguration.getCurrentUsername(),
                PrimeroAppConfiguration.getApiBaseUrl(),
                searchCondition));
    }

    private OperatorGroup getSearchCondition(String shortId, String name, int ageFrom, int
            ageTo, Date date) {
        OperatorGroup operatorGroup = OperatorGroup.clause();
        operatorGroup.and(Operator.column(NameAlias.builder(RecordModel.COLUMN_CREATED_BY).build())
                .eq(PrimeroAppConfiguration.getCurrentUser().getUsername()));

        SQLOperator ageSearchCondition = generateAgeSearchCondition(ageFrom, ageTo);
        if (!TextUtils.isEmpty(shortId)) {
            operatorGroup.and(Operator.column(NameAlias.builder(RecordModel.COLUMN_SHORT_ID).build())
                    .like(getWrappedCondition(shortId)));
        }
        if (ageSearchCondition != null) {
            operatorGroup.and(ageSearchCondition);
        }
        if (!TextUtils.isEmpty(name)) {
            operatorGroup.and(Operator.column(NameAlias.builder(RecordModel.COLUMN_NAME).build())
                    .like(getWrappedCondition(name)));
        }
        if (date != null) {
            operatorGroup.and(Operator.column(
                    NameAlias.builder(RecordModel.COLUMN_REGISTRATION_DATE).build()).eq(date));
        }
        return operatorGroup;
    }

    public Tracing saveOrUpdate(ItemValuesMap itemValues, List<String> photoPaths) throws
            IOException {
        if (itemValues.getAsString(TRACING_ID) == null) {
            return save(itemValues, photoPaths);
        } else {
            return update(itemValues, photoPaths);
        }
    }

    public Tracing save(ItemValuesMap itemValues, List<String> photoPaths) throws IOException {
        Tracing tracing = generateTracingFromItemValues(itemValues,
                generateUniqueId());

        tracing.setNoteAlerts("");

        tracingDao.save(tracing);
        tracingPhotoDao.save(tracing, photoPaths);
        clearImagesCache();
        return tracing;
    }

    public Tracing update(ItemValuesMap itemValues,
                          List<String> photoBitPaths) throws IOException {
        Tracing tracing = updateTracingFromItemValues(itemValues);
        tracing.setSynced(false);
        tracing.setNoteAlerts("");

        tracingPhotoDao.update(tracingDao.update(tracing), photoBitPaths);

        clearImagesCache();

        return tracing;
    }

    public Tracing deleteByRecordId(long recordId) {
        Tracing deleteTracing = tracingDao.getTracingById(recordId);
        if (deleteTracing != null && !deleteTracing.isSynced()) {
            return null;
        }
        tracingDao.delete(deleteTracing);
        tracingPhotoDao.deleteByTracingId(recordId);
        return deleteTracing;
    }

    private Tracing generateTracingFromItemValues(ItemValuesMap itemValues, String uniqueId) {
        if (!itemValues.has(INQUIRY_DATE)) {
            itemValues.addStringItem(INQUIRY_DATE, getCurrentRegistrationDateAsString());
            itemValues.addStringItem(ALERTS, "");
        }

        Tracing tracing = new Tracing();
        tracing.setUniqueId(uniqueId);
        tracing.setInternalId(generateUniqueId(true));
        tracing.setShortId(getShortUUID(uniqueId));
        tracing.setUniqueIdentifier(uniqueId);

        String username = PrimeroAppConfiguration.getCurrentUser().getUsername();
        tracing.setCreatedBy(username);
        tracing.setOwnedBy(username);
        tracing.setServerUrl(TextUtils.lintUrl(PrimeroAppConfiguration.getApiBaseUrl()));

        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        tracing.setCreateDate(date);
        tracing.setLastUpdatedDate(date);

        tracing.setName(getName(itemValues));

        int age = itemValues.getAsInt(RELATION_AGE) != null ? itemValues.getAsInt(RELATION_AGE) : EMPTY_AGE;
        tracing.setAge(age);

        tracing.setCaregiver(getCaregiverName(itemValues));
        tracing.setRegistrationDate(getRegisterDateByYyyyMmDd(itemValues.getAsString(INQUIRY_DATE)));
        tracing.setAudio(getAudioBlob());

        tracing.setContent(generateTracingBlob(itemValues, uniqueId, username));

        return tracing;
    }

    private Tracing updateTracingFromItemValues(ItemValuesMap itemValues) {
        itemValues.addStringItem(ALERTS, "");

        Tracing tracing = tracingDao.getTracingByUniqueId(itemValues.getAsString(TRACING_ID));
        String username = PrimeroAppConfiguration.getCurrentUser().getUsername();

        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        tracing.setCreateDate(date);
        tracing.setLastUpdatedDate(date);
        tracing.setName(getName(itemValues));

        int age = itemValues.getAsInt(RELATION_AGE) != null ? itemValues.getAsInt(RELATION_AGE) : EMPTY_AGE;
        tracing.setAge(age);

        tracing.setCaregiver(getCaregiverName(itemValues));
        tracing.setRegistrationDate(Utils.getRegisterDateByYyyyMmDd(itemValues.getAsString(INQUIRY_DATE)));
        tracing.setAudio(getAudioBlob());

        tracing.setContent(generateTracingBlob(itemValues, tracing.getUniqueId(), username));

        return tracing;
    }

    private Blob generateTracingBlob(ItemValuesMap itemValues, String uniqueId, String username) {
        itemValues.addStringItem(TRACING_DISPLAY_ID, getShortUUID(uniqueId));
        itemValues.addStringItem(TRACING_ID, uniqueId);
        itemValues.addStringItem(MODULE, MODULE_CP_CASE);
        itemValues.addStringItem(RECORD_OWNED_BY, username);
        itemValues.addStringItem(RECORD_CREATED_BY, username);
        itemValues.addStringItem(PREVIOUS_OWNER, username);

        if (!itemValues.has(INQUIRY_DATE)) {
            itemValues.addStringItem(INQUIRY_DATE, getCurrentRegistrationDateAsString());
        }

        return new Blob(new Gson().toJson(itemValues.getValues()).getBytes());
    }

    private String getName(ItemValuesMap values) {
        return values.concatMultiStringsWithBlank(RELATION_NAME, RELATION_AGE, RELATION_NICKNAME);
    }

    private Blob getAudioBlob() {
        if (StreamUtil.isFileExists(AUDIO_FILE_PATH)) {
            try {
                return new Blob(StreamUtil.readFile(AUDIO_FILE_PATH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Tracing getByInternalId(String id) {
        return tracingDao.getByInternalId(id);
    }

    public boolean hasSameRev(String id, String rev) {
        Tracing tracing = tracingDao.getByInternalId(id);
        return tracing != null && rev.equals(tracing.getInternalRev());
    }

    public List<Long> getAllSyncedRecordsId() {
        return extractIds(tracingDao.getALLSyncedRecords(PrimeroAppConfiguration.getCurrentUsername()));
    }
}
