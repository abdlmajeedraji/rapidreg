package org.unicef.rapidreg.widgets.dialog;

import android.app.Dialog;
import android.content.Context;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.lookups.Option;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchAbleMultiSelectDialog extends Dialog {
    private static final String TAG = SearchAbleMultiSelectDialog.class.getSimpleName();

    @BindView(R.id.dialog_title)
    TextView dialogTitleTextView;
    @BindView(R.id.dialog_list_content)
    ListView list;
    @BindView(R.id.EditBox)
    EditText filterText;
    @BindView(R.id.okButton)
    Button okButton;
    @BindView(R.id.cancelButton)
    Button cancelButton;
    @BindView(R.id.clearButton)
    Button clearButton;
    @BindView(R.id.usernameWrapper)
    TextInputLayout filterLayout;

    private SearchAbleMultiSelectDialog.MyAdapter adapter = null;

    private Context context;
    private List<String> results;

    public SearchAbleMultiSelectDialog(Context context, String title, List<Option> items, List<String> selectedItems) {
        super(context);
        this.context = context;
        results = selectedItems;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_with_multi_select_list);
        ButterKnife.bind(this);

        dialogTitleTextView.setText(title);
        filterText.addTextChangedListener(filterTextWatcher);

        adapter = new SearchAbleMultiSelectDialog.MyAdapter(context, items);

        list.setAdapter(adapter);
        list.setOnItemClickListener((adapter, view, position, id) -> Log.d(TAG, "Selected Item is = " + list
                .getItemAtPosition(position)));

        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.clearButton)
    void onClickCleanButton() {
        results.clear();
        adapter.notifyDataSetChanged();
    }

    public void setOkButton(final View.OnClickListener listener) {
        okButton.setOnClickListener(listener);
    }

    public void setCancelButton(final View.OnClickListener listener) {

        cancelButton.setOnClickListener(listener);
    }

    public void setOnClick(SearchAbleMultiSelectDialog.SearchAbleMultiSelectDialogOnClickListener listener) {
        adapter.listener = listener;
    }

    public void disableDialogFilter(boolean isDisable) {
        if (isDisable) {
            filterLayout.setVisibility(View.GONE);
        } else {
            filterLayout.setVisibility(View.VISIBLE);
        }
    }

    public void disableClearButton(boolean isDisable) {
        if (isDisable) {
            clearButton.setVisibility(View.GONE);
        } else {
            cancelButton.setVisibility(View.VISIBLE);
        }
    }

    public interface SearchAbleMultiSelectDialogOnClickListener {
        void onClick(List<String> results);
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            adapter.getFilter().filter(s);
        }
    };

    @Override
    public void onStop() {
        filterText.removeTextChangedListener(filterTextWatcher);
    }

    public class MyAdapter extends BaseAdapter implements Filterable {

        List<Option> arrayList;
        List<Option> mOriginalValues; // Original Values
        LayoutInflater inflater;
        private SearchAbleMultiSelectDialog.MyAdapter.ViewHolder holder;

        SearchAbleMultiSelectDialog.SearchAbleMultiSelectDialogOnClickListener listener = null;

        public MyAdapter(Context context, List<Option> arrayList) {
            this.arrayList = arrayList;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView textView, line;
            CheckBox checkBox;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            holder = new SearchAbleMultiSelectDialog.MyAdapter.ViewHolder();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.form_check_box, null);
                holder.textView = (TextView) convertView.findViewById(R.id.label);
                holder.textView.setClickable(true);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.value);
                holder.line = (TextView) convertView.findViewById(R.id.checkbox_line);
                holder.line.setVisibility(View.VISIBLE);
                convertView.setTag(holder);
            } else {
                holder = (SearchAbleMultiSelectDialog.MyAdapter.ViewHolder) convertView.getTag();
            }

            holder.textView.setText(arrayList.get(position).getDisplayText());
            holder.textView.setOnClickListener(view -> {
                if (!results.contains(arrayList.get(position).getId())) {
                    results.add(arrayList.get(position).getId());
                } else {
                    results.remove(arrayList.get(position).getId());
                }
                listener.onClick(results);
                notifyDataSetChanged();
            });

            holder.checkBox.setOnClickListener(view -> {
                if (!results.contains(arrayList.get(position).getId())) {
                    results.add(arrayList.get(position).getId());
                } else {
                    results.remove(arrayList.get(position).getId());
                }
                listener.onClick(results);
                notifyDataSetChanged();
            });

            if (results.contains(arrayList.get(position).getId())) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    arrayList = (List<Option>) results.values; // has the filtered values
                    notifyDataSetChanged();  // notifies the data with new filtered values
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation
                    // in values
                    List<String> FilteredArrList = new ArrayList<>();

                    if (mOriginalValues == null) {
                        mOriginalValues = new ArrayList<>(arrayList); // saves the original data in mOriginalValues
                    }

                    if (constraint == null || constraint.length() == 0) {

                        // set the Original result to return
                        results.count = mOriginalValues.size();
                        results.values = mOriginalValues;
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        for (int i = 0; i < mOriginalValues.size(); i++) {
                            String data = mOriginalValues.get(i).getDisplayText();
                            if (data.toLowerCase().contains(constraint.toString())) {
                                FilteredArrList.add(data);
                            }
                        }
                        // set the Filtered result to return
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }
                    return results;
                }
            };
            return filter;
        }
    }
}
