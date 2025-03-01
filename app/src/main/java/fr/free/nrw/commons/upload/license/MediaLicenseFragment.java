package fr.free.nrw.commons.upload.license;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.free.nrw.commons.upload.UploadActivity;
import fr.free.nrw.commons.utils.DialogUtil;
import java.util.List;

import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.free.nrw.commons.R;
import fr.free.nrw.commons.Utils;
import fr.free.nrw.commons.upload.UploadBaseFragment;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class MediaLicenseFragment extends UploadBaseFragment implements MediaLicenseContract.View {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_subtitle)
    TextView tvSubTitle;
    @BindView(R.id.spinner_license_list)
    Spinner spinnerLicenseList;
    @BindView(R.id.tv_share_license_summary)
    TextView tvShareLicenseSummary;
    @BindView(R.id.tooltip)
    ImageView tooltip;
    @BindView(R.id.ll_info_monument_upload)
    LinearLayout llInfoMonumentUpload;

    @Inject
    MediaLicenseContract.UserActionListener presenter;

    private ArrayAdapter<String> adapter;
    private List<String> licenses;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_license, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        init();
    }

    private void init() {
        tvTitle.setText(getString(R.string.step_count, callback.getIndexInViewFlipper(this) + 1,
            callback.getTotalNumberOfSteps(), getString(R.string.license_step_title)));
        setTvSubTitle();
        tooltip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showAlertDialog(getActivity(), getString(R.string.license_step_title), getString(R.string.license_tooltip), getString(android.R.string.ok), null, true);
            }
        });
        initPresenter();
        initLicenseSpinner();
        presenter.getLicenses();

        /**
         * Show the wlm info message if the upload is a WLM upload
         */
        if(callback.isWLMUpload()){
            //TODO : Update the info message logo
            llInfoMonumentUpload.setVisibility(View.VISIBLE);
        }else{
            llInfoMonumentUpload.setVisibility(View.GONE);
        }
    }

    /**
     * Removes the tv Subtitle If the activity is the instance of [UploadActivity] and
     * if multiple files aren't selected.
     */
    private void setTvSubTitle() {
        final Activity activity = getActivity();
        if (activity instanceof  UploadActivity) {
            final boolean isMultipleFileSelected = ((UploadActivity) activity).getIsMultipleFilesSelected();
            if (!isMultipleFileSelected) {
                tvSubTitle.setVisibility(View.GONE);
            }
        }
    }

    private void initPresenter() {
        presenter.onAttachView(this);
    }

    /**
     * Initialise the license spinner
     */
    private void initLicenseSpinner() {
        if (getActivity() == null) {
            return;
        }
        adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item);
        spinnerLicenseList.setAdapter(adapter);
        spinnerLicenseList.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                long l) {
                String licenseName = adapterView.getItemAtPosition(position).toString();
                presenter.selectLicense(licenseName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                presenter.selectLicense(null);
            }
        });
    }

    @Override
    public void setLicenses(List<String> licenses) {
        adapter.clear();
        this.licenses = licenses;
        adapter.addAll(this.licenses);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setSelectedLicense(String license) {
        int position = licenses.indexOf(getString(Utils.licenseNameFor(license)));
        // Check if position is valid
        if (position < 0) {
            Timber.d("Invalid position: %d. Using default licenses", position);
            position = licenses.size() - 1;
        } else {
            Timber.d("Position: %d %s", position, getString(Utils.licenseNameFor(license)));
        }
        spinnerLicenseList.setSelection(position);
    }

    @Override
    public void updateLicenseSummary(String licenseSummary, int numberOfItems) {
        String licenseHyperLink = "<a href='" + Utils.licenseUrlFor(licenseSummary) + "'>" +
                getString(Utils.licenseNameFor(licenseSummary)) + "</a><br>";

        setTextViewHTML(tvShareLicenseSummary, getResources()
                .getQuantityString(R.plurals.share_license_summary, numberOfItems,
                        licenseHyperLink));
    }

    private void setTextViewHTML(TextView textView, String text) {
        CharSequence sequence = Html.fromHtml(text);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        textView.setText(strBuilder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Handle hyperlink click
                String hyperLink = span.getURL();
                launchBrowser(hyperLink);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void launchBrowser(String hyperLink) {
        Utils.handleWebUrl(getContext(), Uri.parse(hyperLink));
    }

    @Override
    public void onDestroyView() {
        presenter.onDetachView();
        //Free the adapter to avoid memory leaks
        adapter = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.btn_previous)
    public void onPreviousButtonClicked() {
        callback.onPreviousButtonClicked(callback.getIndexInViewFlipper(this));
    }

    @OnClick(R.id.btn_submit)
    public void onSubmitButtonClicked() {
        callback.onNextButtonClicked(callback.getIndexInViewFlipper(this));
    }

}
