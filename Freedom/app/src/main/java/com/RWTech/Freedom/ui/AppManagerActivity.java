/* Copyright (c) 2009, Nathan Freitas, Freedom / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package com.RWTech.Freedom.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.RWTech.Freedom.R;
import com.RWTech.Freedom.service.OrbotConstants;
import com.RWTech.Freedom.service.util.Prefs;
import com.RWTech.Freedom.service.vpn.TorifiedApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import static com.RWTech.Freedom.service.vpn.VpnPrefs.PREFS_KEY_TORIFIED;

public class AppManagerActivity extends AppCompatActivity implements OnClickListener, OrbotConstants {

    private GridView listApps;
    private ListAdapter adapterApps;
    private ProgressBar progressBar;
    PackageManager pMgr = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pMgr = getPackageManager();

        this.setContentView(R.layout.layout_apps);
        setTitle(R.string.apps_mode);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listApps = findViewById(R.id.applistview);
        progressBar = findViewById(R.id.progressBar);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPrefs = Prefs.getSharedPrefs(getApplicationContext());
        reloadApps();
    }


    /*
     * Create the UI Options Menu (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_refresh_apps)
        {
            mApps = null;
            reloadApps();
        }
        else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadApps () {
        new AsyncTask<Void, Void, Void>() {
            protected void onPreExecute() {
                // Pre Code
                progressBar.setVisibility(View.VISIBLE);
            }
            protected Void doInBackground(Void... unused) {
                loadApps(mPrefs);
                return null;
            }
            protected void onPostExecute(Void unused) {
                listApps.setAdapter(adapterApps);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();


    }

    SharedPreferences mPrefs = null;
    static ArrayList<TorifiedApp> mApps = null;

    private void loadApps (SharedPreferences prefs) {
        if (mApps == null)
            mApps = getApps(prefs);

        Collections.sort(mApps,new Comparator<TorifiedApp>() {
            public int compare(TorifiedApp o1, TorifiedApp o2) {
                /* Some apps start with lowercase letters and without the sorting being case
                   insensitive they'd appear at the end of the grid of apps, a position where users
                   would likely not expect to find them.
                 */
                if (o1.isTorified() == o2.isTorified())
                    return o1.getName().compareToIgnoreCase(o2.getName());
                if (o1.isTorified()) return -1;
                return 1;
            }
        });

        final LayoutInflater inflater = getLayoutInflater();

        adapterApps = new ArrayAdapter<TorifiedApp>(this, R.layout.layout_apps_item, R.id.itemtext,mApps) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ListEntry entry = null;

                if (convertView == null)
                    convertView = inflater.inflate(R.layout.layout_apps_item, parent, false);
                else
                    entry = (ListEntry) convertView.getTag();

                if (entry == null) {
                    // Inflate a new view
                    entry = new ListEntry();
                    entry.icon = convertView.findViewById(R.id.itemicon);
                    entry.box = convertView.findViewById(R.id.itemcheck);
                    entry.text = convertView.findViewById(R.id.itemtext);
                    convertView.setTag(entry);
                }

                final TorifiedApp app = mApps.get(position);

                if (entry.icon != null) {

                    try {
                        entry.icon.setImageDrawable(pMgr.getApplicationIcon(app.getPackageName()));
                        entry.icon.setOnClickListener(AppManagerActivity.this);
                        entry.icon.setTag(entry.box);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                if (entry.text != null) {
                    entry.text.setText(app.getName());
                    entry.text.setOnClickListener(AppManagerActivity.this);
                    entry.text.setTag(entry.box);
                }

                if (entry.box != null) {
                    entry.box.setOnClickListener(AppManagerActivity.this);
                    entry.box.setChecked(app.isTorified());
                    entry.box.setTag(app);
                }

                return convertView;
            }
        };


    }

    private static class ListEntry {
        private CheckBox box;
        private TextView text;
        private ImageView icon;
    }

    public ArrayList<TorifiedApp> getApps(SharedPreferences prefs) {

        String tordAppString = prefs.getString(PREFS_KEY_TORIFIED, "");

        String[] tordApps;

        StringTokenizer st = new StringTokenizer(tordAppString,"|");
        tordApps = new String[st.countTokens()];
        int tordIdx = 0;
        while (st.hasMoreTokens())
        {
            tordApps[tordIdx++] = st.nextToken();
        }
        Arrays.sort(tordApps);

        List<ApplicationInfo> lAppInfo = pMgr.getInstalledApplications(0);

        Iterator<ApplicationInfo> itAppInfo = lAppInfo.iterator();

        ArrayList<TorifiedApp> apps = new ArrayList<>();

        ApplicationInfo aInfo;

        TorifiedApp app;

        while (itAppInfo.hasNext())
        {
            aInfo = itAppInfo.next();
            // don't include apps user has disabled, often these ship with the device
            if (!aInfo.enabled) continue;
            app = new TorifiedApp();


            try {
                PackageInfo pInfo = pMgr.getPackageInfo(aInfo.packageName, PackageManager.GET_PERMISSIONS);

                if (pInfo != null && pInfo.requestedPermissions != null)
                {
                    for (String permInfo:pInfo.requestedPermissions)
                    {
                        if (permInfo.equals(Manifest.permission.INTERNET))
                        {
                            app.setUsesInternet(true);
                        }
                    }

                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try
            {
                app.setName(pMgr.getApplicationLabel(aInfo).toString());
            }
            catch (Exception e)
            {
                // no name
                continue; //we only show apps with names
            }


            if (!app.usesInternet())
                continue;
            else
            {
                apps.add(app);
            }

            app.setEnabled(aInfo.enabled);
            app.setUid(aInfo.uid);
            app.setUsername(pMgr.getNameForUid(app.getUid()));
            app.setProcname(aInfo.processName);
            app.setPackageName(aInfo.packageName);


            // check if this application is allowed
            if (Arrays.binarySearch(tordApps, app.getUsername()) >= 0) {
                app.setTorified(true);
            }
            else
            {
                app.setTorified(false);
            }

        }

        Collections.sort(apps);

        return apps;
    }


    public void saveAppSettings() {

        StringBuilder tordApps = new StringBuilder();
        Intent response = new Intent();

        for (TorifiedApp tApp:mApps)
        {
            if (tApp.isTorified())
            {
                tordApps.append(tApp.getUsername());
                tordApps.append("|");
                response.putExtra(tApp.getUsername(),true);
            }
        }

        Editor edit = mPrefs.edit();
        edit.putString(PREFS_KEY_TORIFIED, tordApps.toString());
        edit.commit();

        setResult(RESULT_OK,response);
    }


    public void onClick(View v) {

        CheckBox cbox = null;

        if (v instanceof CheckBox)
            cbox = (CheckBox)v;
        else if (v.getTag() instanceof CheckBox)
            cbox = (CheckBox)v.getTag();

        if (cbox != null) {
            final TorifiedApp app = (TorifiedApp) cbox.getTag();
            if (app != null) {
                app.setTorified(!app.isTorified());
                cbox.setChecked(app.isTorified());
            }

            saveAppSettings();
        }
    }
}
