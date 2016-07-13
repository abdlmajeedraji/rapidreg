package org.unicef.rapidreg.model;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.Table;

import org.unicef.rapidreg.db.PrimeroDB;

@Table(database = PrimeroDB.class)
@ModelContainer
public class CasePhoto extends RecordPhoto {

    @ForeignKey(references = {@ForeignKeyReference(
            columnName = "case_id",
            columnType = long.class,
            foreignKeyColumnName = "id"
    )})
    Case childCase;

    public Case getCase() {
        return childCase;
    }

    public void setCase(Case childCase) {
        this.childCase = childCase;
    }

    @Override
    public String toString() {
        return "CasePhoto{" +
                "childCase=" + childCase +
                "} " + super.toString();
    }
}
