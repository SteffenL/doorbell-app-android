package com.steffenl.doorbellapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import com.steffenl.doorbellapp.App;
import com.steffenl.doorbellapp.AppContainer;
import com.steffenl.doorbellapp.R;
import com.steffenl.doorbellapp.viewmodels.MainViewModel;

public class MainFragment extends Fragment {
    private AppContainer appContainer;
    private SharedPreferences preferences;

    private Button ringButton;
    private TextView statusTextView;

    public MainFragment() {
        super(R.layout.fragment_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FragmentActivity activity = requireActivity();
        final MainViewModel viewModel = new ViewModelProvider(activity, MainViewModel.Factory.getInstance(appContainer)).get(MainViewModel.class);

        appContainer = ((App) activity.getApplication()).getAppContainer();
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        ringButton = view.findViewById(R.id.ring_button);
        statusTextView = view.findViewById(R.id.status_text_view);

        statusTextView.setText(R.string.status_idle);
        ringButton.setOnClickListener(v -> viewModel.ring());

        viewModel.getShowRingButton().observe(getViewLifecycleOwner(), show -> {
            ringButton.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        viewModel.getStatusText().observe(getViewLifecycleOwner(), resourceID -> {
            statusTextView.setText(resourceID);
        });
    }
}
