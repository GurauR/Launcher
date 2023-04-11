package com.example.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LeftAppActivity extends AppCompatActivity {

    private static final String TAG = "Dock";
    private AppsAdapter adapter;
    private LinkedList<App> apps;
    private ListView appListView;
    private List<ApplicationInfo> packages;
    private LinkedList<String> packagesName;
    private PackageManager pm = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_left_app);
        appListView = findViewById(R.id.app_list_view);
        pm = this.getPackageManager();
        sharedPreferences = getSharedPreferences("leftApp", MODE_PRIVATE);
        refreshApps();
        editor = sharedPreferences.edit();
        selected= sharedPreferences.getString("selected", "");
        Log.d(TAG, "onCreate: " + selected);

    }

    private void refreshApps() {

        apps = new LinkedList<App>();
        packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        packagesName = new LinkedList<>();

        for (ApplicationInfo appInfo : packages) {
            if (pm.getLaunchIntentForPackage(appInfo.packageName) == null) {
                // System application
            } else {
                CharSequence appName = pm.getApplicationLabel(appInfo);
                Drawable appIcon = pm.getApplicationIcon(appInfo);
                apps.add(new App(appName.toString(), appInfo.packageName, appIcon));
                packagesName.add((String) pm.getApplicationLabel(appInfo));
            }
        }

        Collections.sort(packagesName, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });

        Collections.sort(apps, new Comparator<App>() {
            @Override
            public int compare(App o1, App o2) {
                return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
            }
        });

        PackageManager packageManager = this.getPackageManager();
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_view, packagesName);
        adapter = new AppsAdapter(this, apps);
        appListView.setAdapter(adapter);
        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String app = apps.get(position).packageName;
                selected = app;
                Log.d(TAG, "onItemClick: " + selected);
                editor.putString("selected", selected);
                editor.commit();
                Log.d(TAG, "onItemClick: " + sharedPreferences.getString("selected", ""));
                Log.d(TAG, "onItemClick: " + position);
                view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.orange));
                finish();
            }
        });

        Log.d(TAG, "refreshApps: " + apps);
    }
}

class AppsAdapter extends ArrayAdapter<App> {

    private static final String TAG = "MyCustomAdapter";
    private final Context context;
    private final List<App> appList;
    private final List<App> apps;
    private Filter appFilter;

    public AppsAdapter(Context context, List<App> apps) {
        super(context, R.layout.list_item, apps);
        this.context = context;
        this.appList = apps;
        this.apps = apps;
        appFilter = new AppFilter();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_dock, parent, false);
        //SharedPreferences sharedPreferences = view.getSharedPreferences("appDock", MODE_PRIVATE);
        //Log.d(TAG, "getView: " + SharedPreferences.);
        TextView textView = rowView.findViewById(R.id.text_view);
        ImageView imageView = rowView.findViewById(R.id.icon);
        textView.setText(appList.get(position).name);
        imageView.setImageDrawable(appList.get(position).appIcon);
        return rowView;
    }

    @Override
    public Filter getFilter() {
        return appFilter;
    }

    class AppFilter extends Filter {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<App> filteredList = new ArrayList<>();
                for (App app : appList) {
                    if (app.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(app);
                    }
                }
                filterResults.count = filteredList.size();
                filterResults.values = filteredList;
            } else {
                filterResults.count = appList.size();
                filterResults.values = appList;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.d(TAG, "publishResults: " + apps);
            clear();
            addAll((List<App>) results.values);
            notifyDataSetChanged();
        }
    }

}