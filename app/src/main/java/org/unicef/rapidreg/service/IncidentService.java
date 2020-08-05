package org.unicef.rapidreg.service;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.SQLOperator;

import org.unicef.rapidreg.PrimeroAppConfiguration;
import org.unicef.rapidreg.model.Incident;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.repository.IncidentDao;
import org.unicef.rapidreg.repository.impl.IncidentDaoImpl;
import org.unicef.rapidreg.service.cache.ItemValuesMap;
import org.unicef.rapidreg.utils.TextUtils;
import org.unicef.rapidreg.utils.Utils;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import static org.unicef.rapidreg.model.RecordModel.EMPTY_AGE;
import static org.unicef.rapidreg.service.CaseService.CASE_ID;

public class IncidentService extends RecordService {
    public static final String TAG = IncidentService.class.getSimpleName();
    public static final String INCIDENT_DISPLAY_ID = "incident_id_display";
    public static final String INCIDENT_ID = "incident_id";
    public static final String INCIDENT_PRIMARY_ID = "incident_primary_id";
    public static final String EMPTY_ID = "";

    private IncidentDao incidentDao = new IncidentDaoImpl();

    public IncidentService() {
    }

    public IncidentService(IncidentDao incidentDao) {
        this.incidentDao = incidentDao;
    }

    public Incident getById(long incidentId) {
        return incidentDao.getIncidentById(incidentId);
    }

    public Incident getByUniqueId(String uniqueId) {
        return incidentDao.getIncidentByUniqueId(uniqueId);
    }

    public List<Incident> getAll() {
        return incidentDao.getAll(PrimeroAppConfiguration.getCurrentUsername(), TextUtils
                .lintUrl(PrimeroAppConfiguration.getApiBaseUrl()));
    }

    public List<Long> getAllOrderByDateASC() {
        return extractIds(incidentDao.getAllIncidentsOrderByDate(true, PrimeroAppConfiguration.getCurrentUsername(),
                TextUtils.lintUrl(PrimeroAppConfiguration.getApiBaseUrl())));
    }

    public List<Long> getAllOrderByDateDES() {
        return extractIds(incidentDao.getAllIncidentsOrderByDate(false, PrimeroAppConfiguration.getCurrentUsername(),
                TextUtils.lintUrl(PrimeroAppConfiguration.getApiBaseUrl())));
    }

    public List<Long> getAllOrderByAgeASC() {
        return extractIds(incidentDao.getAllIncidentsOrderByAge(true, PrimeroAppConfiguration.getCurrentUsername(),
                TextUtils.lintUrl(PrimeroAppConfiguration.getApiBaseUrl())));
    }

    public List<Long> getAllOrderByAgeDES() {
        return extractIds(incidentDao.getAllIncidentsOrderByAge(false, PrimeroAppConfiguration.getCurrentUsername(),
                TextUtils.lintUrl(PrimeroAppConfiguration.getApiBaseUrl())));
    }

    public List<Long> getSearchResult(String uniqueId, String survivorCode, int ageFrom, int ageTo, String
            typeOfViolence, String location) {
        OperatorGroup searchCondition = getSearchCondition(uniqueId, survivorCode, ageFrom, ageTo, typeOfViolence,
                location);
        return extractIds(incidentDao.getIncidentListByOperatorGroup(PrimeroAppConfiguration.getCurrentUsername(),
                PrimeroAppConfiguration.getApiBaseUrl(),
                searchCondition));
    }

    private OperatorGroup getSearchCondition(String shortId, String survivorCode, int ageFrom, int ageTo, String
            typeOfViolence, String location) {
        OperatorGroup operatorGroup = OperatorGroup.clause();

        operatorGroup.and(Operator.op(NameAlias.builder(RecordModel.COLUMN_OWNED_BY).build())
                .eq(PrimeroAppConfiguration.getCurrentUser().getUsername()));

        SQLOperator ageSearchCondition = generateAgeSearchCondition(ageFrom, ageTo);
        if (ageSearchCondition != null) {
            operatorGroup.and(ageSearchCondition);
        }

        if (!TextUtils.isEmpty(shortId)) {
            operatorGroup.and(Operator.op(NameAlias.builder(RecordModel.COLUMN_SHORT_ID).build())
                    .like(getWrappedCondition(shortId)));
        }
        if (!TextUtils.isEmpty(typeOfViolence)) {
            operatorGroup.and(Operator.op(NameAlias.builder(Incident.COLUMN_TYPE_OF_VIOLENCE).build())
                    .eq(typeOfViolence));
        }
        if (!TextUtils.isEmpty(location)) {
            operatorGroup.and(Operator.op(NameAlias.builder(Incident.COLUMN_LOCATION).build())
                    .eq(location));
        }
        if (!TextUtils.isEmpty(survivorCode)) {
            operatorGroup.and(Operator.op(NameAlias.builder(Incident.COLUMN_SURVIVOR_CODE).build())
                    .like(getWrappedCondition(survivorCode)));
        }
        return operatorGroup;
    }

    public Incident saveOrUpdate(ItemValuesMap itemValues) throws IOException {
        if (itemValues.getAsString(INCIDENT_ID) == null) {
            return save(itemValues);
        } else {
            return update(itemValues);
        }
    }

    public Incident save(ItemValuesMap itemValues) throws IOException {
        if (!itemValues.has(DATE_OF_INTERVIEW)) {
            itemValues.addStringItem(DATE_OF_INTERVIEW, getCurrentRegistrationDateAsString());
        }
        String uniqueId = generateUniqueId();
        itemValues.addStringItem(INCIDENT_DISPLAY_ID, getShortUUID(uniqueId));
        itemValues.addStringItem(INCIDENT_ID, uniqueId);
        String username = PrimeroAppConfiguration.getCurrentUser().getUsername();
        itemValues.addStringItem(MODULE, MODULE_GBV_CASE);
        itemValues.addStringItem(RECORD_OWNED_BY, username);
        itemValues.addStringItem(RECORD_CREATED_BY, username);
        itemValues.addStringItem(PREVIOUS_OWNER, username);
        itemValues.addStringItem(ALERTS, "");

        Gson gson = new Gson();
        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        Blob tracingBlob = new Blob(gson.toJson(itemValues.getValues()).getBytes());

        Incident incident = new Incident();
        incident.setUniqueId(uniqueId);
        incident.setInternalId(generateUniqueId(true));
        incident.setShortId(getShortUUID(uniqueId));
        incident.setUniqueIdentifier(uniqueId);
        incident.setCreateDate(date);
        incident.setLastUpdatedDate(date);
        incident.setContent(tracingBlob);
        incident.setName(getName(itemValues));
        incident.setSurvivorCode(getSurvivorCode(itemValues));
        incident.setTypeOfViolence(getTypeOfViolence(itemValues));
        incident.setLocation(getLocation(itemValues));
        incident.setServerUrl(TextUtils.lintUrl(PrimeroAppConfiguration.getApiBaseUrl()));
        int age = itemValues.getAsInt(AGE) != null ? itemValues.getAsInt(AGE) : EMPTY_AGE;
        incident.setAge(age);
        incident.setCaregiver(getCaregiverName(itemValues));
        incident.setRegistrationDate(Utils.getRegisterDateByYyyyMmDd(itemValues.getAsString(DATE_OF_INTERVIEW)));
        incident.setCreatedBy(username);
        incident.setOwnedBy(username);

        incident.setNoteAlerts("");

        String caseId = itemValues.has(CASE_ID) ? itemValues.getAsString(CASE_ID) : EMPTY_ID;
        incident.setCaseUniqueId(caseId);
        incidentDao.save(incident);
        return incident;
    }

    public Incident update(ItemValuesMap itemValues) throws IOException {
        itemValues.addStringItem(ALERTS, "");

        Gson gson = new Gson();
        Blob blob = new Blob(gson.toJson(itemValues.getValues()).getBytes());

        Incident incident = incidentDao.getIncidentByUniqueId(itemValues.getAsString(INCIDENT_ID));
        incident.setLastUpdatedDate(new Date(Calendar.getInstance().getTimeInMillis()));
        incident.setContent(blob);
        incident.setName(getName(itemValues));
        incident.setSurvivorCode(getSurvivorCode(itemValues));
        incident.setTypeOfViolence(getTypeOfViolence(itemValues));
        incident.setLocation(getLocation(itemValues));
        int age = itemValues.getAsInt(AGE) != null ? itemValues.getAsInt(AGE) : EMPTY_AGE;
        incident.setAge(age);
        incident.setCaregiver(getCaregiverName(itemValues));
        incident.setSynced(false);

        incident.setNoteAlerts("");

        if (itemValues.has(DATE_OF_INTERVIEW)) {
            incident.setRegistrationDate(Utils.getRegisterDateByYyyyMmDd(itemValues.getAsString(DATE_OF_INTERVIEW)));
        }
        return incidentDao.update(incident);
    }

    public Incident deleteByRecordId(long recordId) {
        Incident deleteIncident = incidentDao.getIncidentById(recordId);
        if (deleteIncident != null && !deleteIncident.isSynced()) {
            return null;
        }
        incidentDao.delete(deleteIncident);
        return deleteIncident;
    }

    private String getName(ItemValuesMap values) {
        return values.concatMultiStringsWithBlank(RELATION_NAME, RELATION_AGE, RELATION_NICKNAME);
    }

    public Incident getByInternalId(String id) {
        return incidentDao.getByInternalId(id);
    }

    public boolean hasSameRev(String id, String rev) {
        Incident incident = incidentDao.getByInternalId(id);
        return incident != null && rev.equals(incident.getInternalRev());
    }

    public List<Long> getAllSyncedRecordsId() {
        return extractIds(incidentDao.getALLSyncedRecords(PrimeroAppConfiguration.getCurrentUsername()));
    }
}
