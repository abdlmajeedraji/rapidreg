package org.unicef.rapidreg.forms;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.unicef.rapidreg.PrimeroAppConfiguration;
import org.unicef.rapidreg.lookups.Option;
import org.unicef.rapidreg.service.cache.GlobalLookupCache;
import org.unicef.rapidreg.utils.Utils;
import org.unicef.rapidreg.widgets.dialog.BaseDialog;
import org.unicef.rapidreg.widgets.dialog.DateDialog;
import org.unicef.rapidreg.widgets.dialog.MultipleSelectDialog;
import org.unicef.rapidreg.widgets.dialog.MultipleTextDialog;
import org.unicef.rapidreg.widgets.dialog.NumericDialog;
import org.unicef.rapidreg.widgets.dialog.SingleSelectDialog;
import org.unicef.rapidreg.widgets.dialog.SingleTextDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Field {
    public static final String TYPE_SELECT_BOX = "select_box";
    public static final String TYPE_SINGLE_SELECT_BOX = "single_select_box";
    public static final String TYPE_SINGLE_LINE_RADIO = "single_line_radio";
    public static final String TYPE_MULTI_SELECT_BOX = "multi_select_box";
    public static final String TYPE_SUBFORM_FIELD = "subform_container";
    public static final String TYPE_TICK_BOX = "tick_box";
    public static final String TYPE_TEXT_AREA = "textarea";
    public static final String TYPE_TEXT_FIELD = "text_field";
    public static final String TYPE_PHOTO_UPLOAD_LAYOUT = "photo_upload_layout";
    public static final String TYPE_AUDIO_UPLOAD_LAYOUT = "audio_item";
    public static final String TYPE_RADIO_BUTTON = "radio_button";
    public static final String TYPE_NUMERIC_FIELD = "numeric_field";
    public static final String TYPE_PHOTO_VIEW_SLIDER = "record_photo_view_slider";
    public static final String TYPE_CUSTOM = "custom";
    public static final String TYPE_DATE_FIELD = "date_field";

    public static final String TYPE_DATE_RANGE = "date_range";
    public static final String TYPE_MINI_FORM_PROFILE = "mini_form_profile";

    public static final String TYPE_INCIDENT_MINI_FORM_PROFILE = "incident_mini_form_profile";

    private static final int INVALID_INDEX = -1;
    public static final String FIELD_NAME_MARK_FOR_MOBILE = "marked_for_mobile";

    public static final String FIELD_NAME_AGE = "age";
    public static final String FIELD_NAME_DATE_OF_BIRTH = "date_of_birth";


    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("disabled")
    @Expose
    private boolean disabled;
    @SerializedName("required")
    @Expose
    private boolean required;
    @SerializedName("multi_select")
    @Expose
    private boolean multiSelect;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("display_name")
    @Expose
    private Map<String, String> displayName;
    @SerializedName("help_text")
    @Expose
    private Map<String, String> helpText;
    @SerializedName("option_strings_text")
    @Expose
    private Map<String, List> optionStringsText;
    @SerializedName("option_strings_source")
    @Expose
    private String optionStringSource;
    @SerializedName("subform")
    @Expose
    private Section subForm;
    @SerializedName("date_validation")
    @Expose
    private String dateValidation;
    @SerializedName("show_on_minify_form")
    @Expose
    private boolean isShowOnMiniForm;

    private String parent;

    private Map<String, String> sectionName;

    private int index = INVALID_INDEX;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateValidation() { return dateValidation; }

    public void setDateValidation(String dateValidation) {
        this.dateValidation = dateValidation;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Map<String, String> displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getHelpText() {
        return helpText;
    }

    public void setHelpText(Map<String, String> helpText) {
        this.helpText = helpText;
    }

    public Map<String, List> getOptionStringsText() {
        return optionStringsText;
    }

    public void setOptionStringsText(Map<String, List> optionStringsText) {
        this.optionStringsText = optionStringsText;
    }

    public String getOptionStringSource() { return optionStringSource; }

    public void setOptionStringSource(String source) {
        this.optionStringSource = source;
    }

    public Section getSubForm() {
        return subForm;
    }

    public void setSubForm(Section subForm) {
        this.subForm = subForm;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Map<String, String> getSectionName() {
        return sectionName;
    }

    public void setSectionName(Map<String, String> sectionName) {
        this.sectionName = sectionName;
    }

    public boolean isSeparator() {
        return FieldType.SEPARATOR.name().equalsIgnoreCase(type);
    }

    public boolean isTickBox() {
        return FieldType.TICK_BOX.name().equalsIgnoreCase(type);
    }

    public boolean isShowOnMiniForm() {
        return isShowOnMiniForm;
    }

    public void setShowOnMiniForm(boolean showOnMiniForm) {
        isShowOnMiniForm = showOnMiniForm;
    }

    public boolean isPhotoUploadBox() {
        return FieldType.PHOTO_UPLOAD_BOX.name().equalsIgnoreCase(type);
    }

    public boolean isAudioUploadBox() {
        return FieldType.AUDIO_UPLOAD_BOX.name().equalsIgnoreCase(type);
    }

    public boolean isSubform() {
        return FieldType.SUBFORM.name().equalsIgnoreCase(type);
    }

    public boolean isCustom() {
        return FieldType.CUSTOM.name().equalsIgnoreCase(type);
    }

    public boolean isTextField() {
        return FieldType.TEXT_FIELD.name().equalsIgnoreCase(type);
    }

    public boolean isSelectField() {
        return type.equals(TYPE_SELECT_BOX);
    }

    public boolean isRadioButton() {
        return type.equals(TYPE_RADIO_BUTTON);
    }

    public boolean isNumericField() {
        return type.equals(TYPE_NUMERIC_FIELD);
    }

    public boolean isTextArea() {
        return type.equals(TYPE_TEXT_AREA);
    }

    public boolean isMiniFormProfile() {
        return TYPE_MINI_FORM_PROFILE.equals(type);
    }

    public boolean isIncidentMiniFormProfile() {
        return TYPE_INCIDENT_MINI_FORM_PROFILE.equals(type);
    }

    public boolean isDateRange() {
        return TYPE_DATE_RANGE.equals(type);
    }

    public boolean hasMoreThanTwoOptions() {
        return getSelectOptions().size() > 2;
    }

    public boolean hasSelectOptions() { return getSelectOptions().size() > 0; }

    public boolean isMarkForMobileField() {
        return TextUtils.equals(this.name, FIELD_NAME_MARK_FOR_MOBILE);
    }

    public boolean isAgeField() {
        return TextUtils.equals(this.name, FIELD_NAME_AGE);
    }

    public boolean isDateOfBirthField() {
        return TextUtils.equals(this.name, FIELD_NAME_DATE_OF_BIRTH);
    }

    public List<Option> getSelectOptions() {
        String language = PrimeroAppConfiguration.getServerLocale();

        List<Option> items = new ArrayList<>();

        if (getOptionStringSource() != null && !getOptionStringSource().equals("")) {
            items = GlobalLookupCache.getLookup(getOptionStringSource().replaceAll("lookup\\s", ""));
        } else {
            List<Map<String, String>> list = getOptionStringsText().get(language);

            for (Map<String, String> option: list) {
                items.add(new Option(option.get("id"), option.get("display_text")));
            }

        }

        return items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<Field>").append("\n");
        sb.append("name: ").append(name).append("\n");
        sb.append("disabled: ").append(disabled).append("\n");
        sb.append("required: ").append(required).append("\n");
        sb.append("multiSelect: ").append(multiSelect).append("\n");
        sb.append("type: ").append(type).append("\n");
        sb.append("displayName: ").append(displayName).append("\n");
        sb.append("helpText: ").append(helpText).append("\n");
        sb.append("optionStringsText: ").append(optionStringsText).append("\n");
        sb.append("dateValidation: ").append(dateValidation).append("\n");
        sb.append("subForm: ").append(subForm).append("\n");
        sb.append("show_on_minify_form: ").append(isShowOnMiniForm).append("\n");
        sb.append("parent: ").append(parent).append("\n");

        return sb.toString();
    }

    public Field copy() {
        Field newField = new Field();
        newField.setName(name);
        newField.setDisabled(disabled);
        newField.setRequired(required);
        newField.setMultiSelect(multiSelect);
        newField.setDateValidation(dateValidation);
        newField.setType(type);
        newField.setDisplayName(displayName);
        newField.setHelpText(helpText);
        newField.setOptionStringsText(optionStringsText);
        newField.setOptionStringSource(optionStringSource);
        newField.setSubForm(subForm);
        newField.setShowOnMiniForm(isShowOnMiniForm);
        newField.setParent(parent);
        newField.setSectionName(sectionName);

        return newField;

    }

    public enum FieldType {
        SEPARATOR(null),
        TICK_BOX(null),
        NUMERIC_FIELD(NumericDialog.class),
        DATE_FIELD(DateDialog.class),
        TEXTAREA(MultipleTextDialog.class),
        TEXT_FIELD(SingleTextDialog.class),
        RADIO_BUTTON(SingleSelectDialog.class),
        SINGLE_SELECT_BOX(SingleSelectDialog.class),
        MULTI_SELECT_BOX(MultipleSelectDialog.class),
        PHOTO_UPLOAD_BOX(null),
        AUDIO_UPLOAD_BOX(null),
        CUSTOM(null),
        SUBFORM(null),;

        private Class<? extends BaseDialog> clz;

        FieldType(Class<? extends BaseDialog> clz) {
            this.clz = clz;
        }

        public Class<? extends BaseDialog> getClz() {
            return clz;
        }
    }

    public class ValidationKeywords {
        public static final String AGE_KEY = "age";
    }

    public List<String> getSelectedOptions(List<String> results) {
        return GlobalLookupCache.getSelectedOptions(getSelectOptions(), results);
    }

    public String getSingleSelectedOptions(String result) {
        if (hasSelectOptions()) {
            return GlobalLookupCache.getSingleSelectedOptions(getSelectOptions(), result).getDisplayText();
        } else {
            return result;
        }
    }

    public int getSelectOptionIndex(String result) {
        return GlobalLookupCache.getSelectOptionIndex(getSelectOptions(), result);
    }

    public String getTranslatedDate(String date) {
       return Utils.parseDisplayDate(date, PrimeroAppConfiguration.getDefaultLanguage());
    }

    public boolean validate_date_not_future() {
        return this.getDateValidation() != null && this.getDateValidation().equals("not_future_date");
    }
}
