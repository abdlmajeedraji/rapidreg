package org.unicef.rapidreg.db.impl;

import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.unicef.rapidreg.db.IncidentDao;
import org.unicef.rapidreg.model.Incident;
import org.unicef.rapidreg.model.Incident_Table;
import org.unicef.rapidreg.model.RecordModel;

import java.util.ArrayList;
import java.util.List;

public class IncidentDaoImpl implements IncidentDao {
    @Override
    public Incident getIncidentByUniqueId(String uniqueId) {
        return SQLite.select().from(Incident.class).where(Incident_Table.unique_id.eq(uniqueId))
                .querySingle();
    }

    @Override
    public List<Incident> getAllIncidentsOrderByDate(boolean isASC, String createdBy) {
        return isASC ? getIncidentsByDateASC(createdBy) : getIncidentsByDateDES(createdBy);
    }

    @Override
    public List<Incident> getAllIncidentsOrderByAge(boolean isASC, String createdBy) {
        return isASC ? getIncidentsByAgeASC(createdBy) : getIncidentsByAgeDES(createdBy);

    }

    @Override
    public List<Incident> getIncidentListByConditionGroup(ConditionGroup conditionGroup) {
        return SQLite.select().from(Incident.class)
                .where(conditionGroup)
                .orderBy(Incident_Table.registration_date, false)
                .queryList();
    }

    @Override
    public Incident getIncidentById(long incidentId) {
        return SQLite.select().from(Incident.class).where(Incident_Table.id.eq(incidentId))
                .querySingle();
    }

    @Override
    public Incident getByInternalId(String id) {
        return SQLite.select().from(Incident.class).where(Incident_Table._id.eq(id)).querySingle();
    }

    @Override
    public Incident getFirst() {
        return SQLite.select().from(Incident.class).querySingle();
    }

    @Override
    public List<Long> getAllIds(String createdBy) {
        List<Long> result = new ArrayList<>();
        FlowQueryList<Incident> incidents = SQLite.select().from(Incident.class).flowQueryList();
        for (Incident aIncident : incidents) {
            result.add(aIncident.getId());
        }
        return result;
    }

    @Override
    public Incident save(Incident incident) {
        incident.save();
        return incident;
    }

    @Override
    public Incident update(Incident incident) {
        incident.update();
        return incident;
    }

    private List<Incident> getIncidentsByAgeASC(String createdBy) {
        return SQLite
                .select()
                .from(Incident.class)
                .where(ConditionGroup.clause().and(Condition.column(NameAlias.builder(RecordModel.COLUMN_CREATED_BY).build())
                        .eq(createdBy)))
                .orderBy(Incident_Table.age, true)
                .queryList();
    }

    private List<Incident> getIncidentsByAgeDES(String createdBy) {
        return SQLite
                .select()
                .from(Incident.class)
                .where(ConditionGroup.clause().and(Condition.column(NameAlias.builder(RecordModel.COLUMN_CREATED_BY).build())
                        .eq(createdBy)))
                .orderBy(Incident_Table.age, false)
                .queryList();
    }

    private List<Incident> getIncidentsByDateASC(String createdBy) {
        return SQLite
                .select()
                .from(Incident.class)
                .where(ConditionGroup.clause().and(Condition.column(NameAlias.builder(RecordModel.COLUMN_CREATED_BY).build())
                        .eq(createdBy)))
                .orderBy(Incident_Table.registration_date, true)
                .queryList();
    }

    private List<Incident> getIncidentsByDateDES(String createdBy) {
        return SQLite
                .select()
                .from(Incident.class)
                .where(ConditionGroup.clause().and(Condition.column(NameAlias.builder(RecordModel.COLUMN_CREATED_BY).build())
                        .eq(createdBy)))
                .orderBy(Incident_Table.registration_date, false)
                .queryList();
    }
}
