package com.tooploox.ussd.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.tooploox.ussd.App;
import com.tooploox.ussd.R;
import com.tooploox.ussd.domain.Ussd;
import com.tooploox.ussd.domain.UssdResultMatcher;
import com.tooploox.ussd.utils.Predicate;
import com.tooploox.ussd.utils.Strings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UssdListActivity extends AppCompatActivity {

    class ActivityViews {

        @BindView(R.id.recycler_view)
        RecyclerView recyclerView;
    }

    class DialogViews {

        @BindView(R.id.dialog_et_ussd_query)
        EditText etUssdQuery;

        @BindView(R.id.dialog_et_regex)
        EditText etRegex;
    }

    class Logic {

        private Predicate<EditText> notEmptyPredicate =
                et -> et.getText() != null && !Strings.isNullOrEmpty(et.getText().toString());

        boolean isInputValid() {
            return notEmptyPredicate.apply(dialogViews.etRegex) && notEmptyPredicate.apply(dialogViews.etUssdQuery);
        }

    }

    public class UiEventsReactor {

        @OnClick(R.id.button_add)
        public void onAddUssdButtonClicked(View button) {
            showAddUssdDialog();
        }

        public void onDialogDoneButtonClicked(DialogInterface dialog, int which) {
            if (!logic.isInputValid())
                return;

            presenter.addUssd(dialogViews.etUssdQuery.getText().toString(), dialogViews.etRegex.getText().toString());
            dialog.dismiss();
        }

        public void onUssdItemClicked(Ussd ussd) {
            presenter.runUssd(ussd);
        }
    }

    private static final int CALL_USSD_PERMISSION = 1;

    private Logic logic = new Logic();
    private ActivityViews activityViews = new ActivityViews();
    private DialogViews dialogViews = new DialogViews();
    private UiEventsReactor eventsReactor = new UiEventsReactor();
    private UssdListAdapter adapter = new UssdListAdapter(view -> activityViews.recyclerView.getChildAdapterPosition(view), eventsReactor);
    private UssdListPresenter presenter = new UssdListPresenter(App.INSTANCE.ussdStorage, adapter, App.INSTANCE.ussdExecutor);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity);

        ButterKnife.bind(activityViews, this);
        ButterKnife.bind(eventsReactor, this);

        activityViews.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityViews.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
    }

    public void showAddUssdDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, eventsReactor::onDialogDoneButtonClicked)
                .create();
        dialog.show();

        ButterKnife.bind(dialogViews, dialog);
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_USSD_PERMISSION);
        }
    }
}