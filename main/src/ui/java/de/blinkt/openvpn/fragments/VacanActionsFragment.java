/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.util.Objects;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.ConfigConverter;
import de.blinkt.openvpn.activities.FileSelect;
import de.blinkt.openvpn.activities.MainActivity;

public class VacanActionsFragment extends Fragment implements View.OnClickListener {
    private static final int FILE_PICKER_RESULT_KITKAT = 392;
    private static final int SELECT_PROFILE = 43;
    private static final int IMPORT_PROFILE = 231;

    protected VpnProfile mEditProfile = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vacan_actions, container, false);

        final Button button1 = v.findViewById(R.id.vacanButton1);
        button1.setOnClickListener(new Button1Listener(this));

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {

    }

    private boolean startImportConfigFilePicker() {
        boolean startOldFileDialog = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !Utils.alwaysUseOldFileChooser(getActivity()))
            startOldFileDialog = !startFilePicker();

        if (startOldFileDialog)
            startImportConfig();

        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean startFilePicker() {

        Intent i = Utils.getFilePickerIntent(getActivity(), Utils.FileType.OVPN_CONFIG);
        if (i != null) {
            startActivityForResult(i, FILE_PICKER_RESULT_KITKAT);
            return true;
        } else
            return false;
    }

    private void startImportConfig() {
        Intent intent = new Intent(getActivity(), FileSelect.class);
        intent.putExtra(FileSelect.NO_INLINE_SELECTION, true);
        intent.putExtra(FileSelect.WINDOW_TITLE, R.string.import_configuration_file);
        startActivityForResult(intent, SELECT_PROFILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PROFILE) {
            String fileData = data.getStringExtra(FileSelect.RESULT_DATA);
            Uri uri = new Uri.Builder().path(fileData).scheme("file").build();

            startConfigImport(uri);
        } else if (requestCode == IMPORT_PROFILE) {
            String profileUUID = data.getStringExtra(VpnProfile.EXTRA_PROFILEUUID);
            setVacanSetting(profileUUID);
        }  else if (requestCode == FILE_PICKER_RESULT_KITKAT) {
            if (data != null) {
                Uri uri = data.getData();
                startConfigImport(uri);
            }
        }
    }

    private void startConfigImport(Uri uri) {
        Intent startImport = new Intent(getActivity(), ConfigConverter.class);
        startImport.setAction(ConfigConverter.IMPORT_PROFILE);
        startImport.setData(uri);
        startActivityForResult(startImport, IMPORT_PROFILE);
    }

    private void setVacanSetting(String profileUUID) {
        try {
            Context applicationContext = Objects.requireNonNull(getContext()).getApplicationContext();

            SharedPreferences hoge = PreferenceManager.getDefaultSharedPreferences(applicationContext);
            SharedPreferences.Editor editor = hoge.edit();
            editor.putBoolean("restartvpnonboot", true);
            editor.putString("alwaysOnVpn", profileUUID);
            editor.apply();
        } catch (NullPointerException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    static class Button1Listener implements View.OnClickListener {
        private final WeakReference<VacanActionsFragment> fragmentReference;

        public Button1Listener(VacanActionsFragment vacanActionsFragment) {
            this.fragmentReference = new WeakReference<>(vacanActionsFragment);
        }

        @Override
        public void onClick(View v) {
            VacanActionsFragment fragment = this.fragmentReference.get();
            if (fragment != null) {
                fragment.startImportConfigFilePicker();
            }
        }
    }
}
