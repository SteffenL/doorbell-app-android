package com.steffenl.doorbellapp.viewmodels;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.steffenl.doorbellapp.AppContainer;
import com.steffenl.doorbellapp.R;
import com.steffenl.doorbellapp.core.APIClient;
import com.steffenl.doorbellapp.core.service.tasks.TaskCommand;
import com.steffenl.doorbellapp.core.service.tasks.TaskQuery;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Integer> statusText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showRingButton = new MutableLiveData<>();
    private final MutableLiveData<Integer> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<APIClient.DeviceHealthResponseData> deviceHealth = new MutableLiveData<>();

    private final AppContainer appContainer;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public MainViewModel(final AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public LiveData<Integer> getStatusText() {
        return statusText;
    }

    public LiveData<APIClient.DeviceHealthResponseData> getDeviceHealth() {
        return deviceHealth;
    }

    public LiveData<Boolean> getShowRingButton() {
        return showRingButton;
    }

     public LiveData<Integer> getToastMessage() {
        return toastMessage;
     }

    public void setStatusText(@StringRes final int resourceID) {
        statusText.setValue(resourceID);
    }

    public void setShowRingButton(final boolean show) {
        showRingButton.setValue(show);
    }

    public void setToastMessage(@StringRes final int resourceID) {
        toastMessage.setValue(resourceID);
    }

    public void setDeviceHealth(final APIClient.DeviceHealthResponseData deviceHealth) {
        this.deviceHealth.setValue(deviceHealth);
    }

    public void ring() {
        appContainer.getAppService().ring(new TaskCommand.Callback() {
            @Override
            public void completed() {
                // Do nothing
            }

            @Override
            public void errored(final Exception exception) {
                handler.post(() -> setToastMessage(R.string.ring_failed));
            }
        });
    }

    public void refresh() {
        appContainer.getAppService().getDeviceHealth(new TaskQuery.Callback<APIClient.DeviceHealthResponseData>() {
            @Override
            public void completed(final APIClient.DeviceHealthResponseData result) {
                handler.post(() -> setDeviceHealth(result));
            }

            @Override
            public void errored(final Exception exception) {
                handler.post(() -> setToastMessage(R.string.refresh_failed));
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AppContainer appContainer;

        private static Factory instance;

        public Factory(final AppContainer appContainer) {
            this.appContainer = appContainer;
        }

        public static Factory getInstance(final AppContainer appContainer) {
            if (instance == null) {
                instance = new Factory(appContainer);
            }
            return instance;
        }

        @NonNull
        @NotNull
        @Override
        public <T extends ViewModel> T create(@NonNull @NotNull final Class<T> modelClass) {
            try {
                return modelClass.getConstructor(AppContainer.class).newInstance(appContainer);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
