package com.example.launcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    private TextView clockView;
    private Handler handler = new Handler();
    private TextView clockTextView;
    private GestureDetector gestureDetector;
    private CoordinatorLayout coordinatorLayout;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private ListView appListView;
    private int scrollPercentage = 0;
    private PopupMenu popup;
    private Button settingsButton;
    private SearchView searchView;
    private MyCustomAdapter adapter;
    private Filter baseFilter;
    private LinkedList<App> apps;
    private List<ApplicationInfo> packages;
    private LinkedList<String> packagesName;
    private PackageManager pm = null;

    private int dragRange;

    public MainActivity() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        LinearLayout drawer = findViewById(R.id.standard_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(drawer);
        bottomSheetBehavior.setPeekHeight(0);
        View view = this.getCurrentFocus();
        drawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: ");
                return false;
            }
        });

        clockTextView = findViewById(R.id.clockTextView);

        // Start a Handler to update the clock every second
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000);
            }
        });

        gestureDetector = new GestureDetector(this, new MyGestureListener());

        appListView = findViewById(R.id.app_list_view);




        int flags = PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES |
                PackageManager.MATCH_UNINSTALLED_PACKAGES;

        pm = this.getPackageManager();
        //get a list of installed apps.
        refreshApps();

        SharedPreferences sharedPreferences = getSharedPreferences("appDock", MODE_PRIVATE);
        Object[] dockApps =  sharedPreferences.getStringSet("selected", new HashSet<>()).toArray();
        Log.d(TAG, "onCreate: " + dockApps[0].toString());
        Drawable appIcon = null;
        try {
            appIcon = pm.getApplicationIcon(pm.getApplicationInfo(dockApps[0].toString(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ImageView icon1 = findViewById(R.id.icon1);
        icon1.setImageDrawable(appIcon);
        icon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = pm.getLaunchIntentForPackage(dockApps[0].toString());
                startActivity(launchIntent);
            }
        });


        try {
            appIcon = pm.getApplicationIcon(pm.getApplicationInfo(dockApps[1].toString(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ImageView icon2 = findViewById(R.id.icon2);
        icon2.setImageDrawable(appIcon);
        icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = pm.getLaunchIntentForPackage(dockApps[1].toString());
                startActivity(launchIntent);
            }
        });


        try {
            appIcon = pm.getApplicationIcon(pm.getApplicationInfo(dockApps[2].toString(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ImageView icon3 = findViewById(R.id.icon3);
        icon3.setImageDrawable(appIcon);
        icon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = pm.getLaunchIntentForPackage(dockApps[2].toString());
                startActivity(launchIntent);
            }
        });


        try {
            appIcon = pm.getApplicationIcon(pm.getApplicationInfo(dockApps[3].toString(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ImageView icon4 = findViewById(R.id.icon4);
        icon4.setImageDrawable(appIcon);
        icon4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = pm.getLaunchIntentForPackage(dockApps[3].toString());
                startActivity(launchIntent);
            }
        });


        try {
            appIcon = pm.getApplicationIcon(pm.getApplicationInfo(dockApps[4].toString(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ImageView icon5 = findViewById(R.id.icon5);
        icon5.setImageDrawable(appIcon);
        icon5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = pm.getLaunchIntentForPackage(dockApps[4].toString());
                startActivity(launchIntent);
            }
        });


        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.isEmpty()) {
                    refreshApps();
                } else {
                    adapter.getFilter().filter(newText);
                }
                Log.d(TAG, "Filter: " + adapter.getFilter());
                return true;
            }

        });


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

        Log.d(TAG, "refreshApps: " + packagesName.toString());

        Collections.sort(apps, new Comparator<App>() {
            @Override
            public int compare(App o1, App o2) {
                return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
            }
        });

        PackageManager packageManager = this.getPackageManager();
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_view, packagesName);
        adapter = new MyCustomAdapter(this, apps);
        appListView.setAdapter(adapter);
        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String app = apps.get(position).packageName;
                Intent launchIntent = packageManager.getLaunchIntentForPackage(app);
                startActivity(launchIntent);
            }
        });

        appListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Do nothing
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem != 0) {
                    bottomSheetBehavior.setDraggable(false);
                }  else bottomSheetBehavior.setDraggable(true);
            }
        });

        Log.d(TAG, "refreshApps: " + apps);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void updateTime() {
        // Get the current time and format it as a string
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        String formattedTime = dateFormat.format(currentTime);

        // Set the formatted time as the text of the clock TextView
        clockTextView.setText(formattedTime);
    }

    public void launchSettings(View view) {
        finish();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        private static final int SWIPE_MAX_OFF_PATH = 250;

        @Override
        public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
            float diffX = moveEvent.getX() - downEvent.getX();
            float diffY = moveEvent.getY() - downEvent.getY();

            // Swipe from right to left
            if (diffX < -SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && Math.abs(diffY) < SWIPE_MAX_OFF_PATH) {
                Intent launchIntent = pm.getLaunchIntentForPackage("com.google.android.dialer");
                startActivity(launchIntent);
                return true;
            }

            // Swipe from bottom to top
            if (diffY < -SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY && Math.abs(diffX) < SWIPE_MAX_OFF_PATH) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return true;
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_layout, null);
            Drawable myDrawable = AppCompatResources.getDrawable(getBaseContext(),R.drawable.rounded_frame);


            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = (int) e.getX();   //x position
            wmlp.y = (int) e.getY() - 400;   //y position
            dialog.show();
            dialog.getWindow().setLayout(450, 600);
        }
    }

    @Override
    public void onBackPressed() {
        refreshApps();
        Log.d(TAG, "onBackPressed: " + (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED));
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
    }



}

class MyCustomAdapter extends ArrayAdapter<App> {

    private static final String TAG = "MyCustomAdapter";
    private final Context context;
    private final List<App> appList;
    private final List<App> apps;
    private Filter appFilter;

    public MyCustomAdapter(Context context, List<App> apps) {
        super(context, R.layout.list_item, apps);
        this.context = context;
        this.appList = apps;
        this.apps = apps;
        appFilter = new AppFilter();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = rowView.findViewById(R.id.text_view);
        TextView letter = rowView.findViewById(R.id.letter);
        letter.setText(Character.toString(appList.get(position).name.toUpperCase().charAt(0)));
        if (position > 0 && (appList.get(position).name.toUpperCase().charAt(0) == appList.get(position-1).name.toUpperCase().charAt(0))) {
            letter.setVisibility(View.INVISIBLE);
            letter.setHeight(0);
        }
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

class App {
    public String name;
    public String packageName;
    public Drawable appIcon;

    public App(String name, String packageName, Drawable appIcon) {
        this.name = name;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    @Override
    public String toString() {
        return "App{" +
                "name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}


