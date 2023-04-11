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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    // UI elements
    private TextView clockView, clockTextView;
    private ListView appListView;
    private Button settingsButton;
    private SearchView searchView;
    private CoordinatorLayout coordinatorLayout;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private ImageView icon1, icon2, icon3, icon4, icon5;
    private GestureDetector gestureDetector;

    // Other variables
    private final Handler handler = new Handler();
    private PopupMenu popup;
    private Filter baseFilter;
    private PackageManager pm = null;
    private LinkedList<App> apps;
    private LinkedList<App> finalApps;
    private List<ApplicationInfo> packages;
    private LinkedList<String> packagesName;
    private String appLeft;
    private MyCustomAdapter adapter;
    private int scrollPercentage = 0;
    private String oldText = "";

    public MainActivity() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up display metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Set up bottom sheet behavior
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

        // Set up clock text view and handler to update clock every second
        clockTextView = findViewById(R.id.clockTextView);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000);
            }
        });

        // Set up gesture detector and app list view
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        appListView = findViewById(R.id.app_list_view);

        // Get a list of installed apps
        int flags = PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES |
                PackageManager.MATCH_UNINSTALLED_PACKAGES;
        pm = this.getPackageManager();
        refreshApps();

        finalApps = apps;

        // Get selected dock apps and set up dock icons
        SharedPreferences sharedPreferences = getSharedPreferences("appDock", MODE_PRIVATE);
        Object[] dockApps = loadLinkedHashSet("selected", this).toArray();
        Log.d(TAG, "dockApps: " + Arrays.toString(dockApps));
        sharedPreferences = getSharedPreferences("leftApp", MODE_PRIVATE);
        appLeft = sharedPreferences.getString("selected", null);
        Log.d(TAG, "appLeft: " + appLeft);
        Drawable appIcon = null;
        ImageView[] icons = {findViewById(R.id.icon1), findViewById(R.id.icon2), findViewById(R.id.icon3), findViewById(R.id.icon4), findViewById(R.id.icon5)};
        for (int i = 0; i < dockApps.length; i++) {
            if (dockApps[i] != null) {
                try {
                    appIcon = pm.getApplicationIcon(pm.getApplicationInfo(dockApps[i].toString(), 0));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                ImageView icon = icons[i];
                icon.setVisibility(View.VISIBLE);
                icon.setImageDrawable(appIcon);
                int finalI = i;
                icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchIntent = pm.getLaunchIntentForPackage(dockApps[finalI].toString());
                        startActivity(launchIntent);
                    }
                });
            }
        }

        // Set up coordinator layout and search view
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        searchView = findViewById(R.id.searchView);

        // Listen for changes in the search field
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
                return true;
            }
        });
    }

    private void refreshApps() {
        // Check if the list of installed apps needs to be refreshed
        boolean needsRefresh = true;
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        //if (newPackages.size() != packages.size()) {
        //    needsRefresh = true;
        //} else {
        //    for (int i = 0; i < newPackages.size(); i++) {
        //        if (!newPackages.get(i).packageName.equals(packages.get(i).packageName)) {
        //            needsRefresh = true;
        //            break;
        //        }
        //    }
        //}

        if (!needsRefresh) {
            // The list of installed apps hasn't changed, so we don't need to refresh it
            return;
        }

        // Retrieve the list of installed apps and their labels and icons in parallel
        List<CharSequence> labels = new ArrayList<>();
        List<Drawable> icons = new ArrayList<>();
        List<String> newPackages = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> future1 = executor.submit(() -> {
            for (ApplicationInfo appInfo : packages) {
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    labels.add(pm.getApplicationLabel(appInfo));
                }
            }
        });
        Future<?> future2 = executor.submit(() -> {
            for (ApplicationInfo appInfo : packages) {
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    icons.add(pm.getApplicationIcon(appInfo));
                }
            }
        });
        Future<?> future3 = executor.submit(() -> {
            for (ApplicationInfo appInfo : packages) {
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    newPackages.add(appInfo.packageName);
                }
            }
        });
        try {
            future1.get();
            future2.get();
            future3.get();
        } catch (InterruptedException | ExecutionException e) {
            // Handle the exception
        }
        executor.shutdown();

        // Combine the lists of labels and icons into a list of apps
        List<App> newApps = new ArrayList<>(labels.size());
        for (int i = 0; i < labels.size(); i++) {
            newApps.add(new App(labels.get(i).toString(), newPackages.get(i), icons.get(i)));
        }

        // Sort the list of app names using insertion sort (assuming it's nearly sorted)
        for (int i = 1; i < labels.size(); i++) {
            String key = labels.get(i).toString();
            App keyApp = newApps.get(i);
            int j = i - 1;
            while (j >= 0 && labels.get(j).toString().compareToIgnoreCase(key) > 0) {
                labels.set(j + 1, labels.get(j));
                newApps.set(j + 1, newApps.get(j));
                j--;
            }
            labels.set(j + 1, key);
            newApps.set(j + 1, keyApp);
        }

        // Update the adapter and listeners
        runOnUiThread(() -> {
            adapter = new MyCustomAdapter(this, newApps);
            appListView.setAdapter(adapter);
            appListView.setOnItemClickListener((parent, view, position, id) -> {
                Log.d(TAG, "refreshApps: " + newApps.get(position).packageName);
                String app = newApps.get(position).packageName;
                Intent launchIntent = pm.getLaunchIntentForPackage(app);
                startActivity(launchIntent);
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
                    } else {
                        bottomSheetBehavior.setDraggable(true);
                    }
                }
            });
        });
    }




    // Update the time displayed on the clock
    private void updateTime() {
        // Get the current time and format it as a string
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        String formattedTime = dateFormat.format(currentTime);

        // Set the formatted time as the text of the clock TextView
        clockTextView.setText(formattedTime);
    }

    // Launch the settings activity
    public void launchSettings(View view) {
        finish(); // Close the main activity
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // Bottom sheet behavior callback
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    // Gesture detector
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        private static final int SWIPE_MAX_OFF_PATH = 250;

        @Override
        public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
            // Get the difference between the coordinates of the down and move events
            float diffX = moveEvent.getX() - downEvent.getX();
            float diffY = moveEvent.getY() - downEvent.getY();

            // Swipe from left to right
            if (diffX < -SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && Math.abs(diffY) < SWIPE_MAX_OFF_PATH) {
                Intent launchIntent = pm.getLaunchIntentForPackage("com.google.android.dialer"); // Dialer is the default app
                startActivity(launchIntent);
                return true;
            }
            // Swipe from right to left
            else if (diffX > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && Math.abs(diffY) < SWIPE_MAX_OFF_PATH) {
                Intent launchIntent = pm.getLaunchIntentForPackage(appLeft); // appLeft is the package name of the app that was selected in the settings
                startActivity(launchIntent);
                return true;
            }
            // Swipe from bottom to top
            else if (diffY < -SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY && Math.abs(diffX) < SWIPE_MAX_OFF_PATH) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Expand the bottom sheet
                return true;
            }

            return false;
        }



        // Long press to show the dialog
        @Override
        public void onLongPress(MotionEvent e) {

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_layout, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Set the background of the dialog to transparent

            // Set the position of the dialog
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            wmlp.x = (int) e.getX();   // x position
            wmlp.y = (int) e.getY() - 400;   // y position
            dialog.show(); // Show the dialog
            dialog.getWindow().setLayout(450, 600); // Set the size of the dialog
        }

    }



    @Override
    public void onBackPressed() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        // start the intent with an animation inside anim folder
        super.startActivity(intent);

    }

    public LinkedHashSet<String> loadLinkedHashSet(String setName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("appDock", 0);
        int size = prefs.getInt(setName + "_size", 0);
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (int i = 0; i < size; i++)
            set.add(prefs.getString(setName + "_" + i, null));
        return set;
    }

    class MyCustomAdapter extends ArrayAdapter<App> {

        private static final String TAG = "MyCustomAdapter";
        private final Context context;
        private final List<App> appList;
        private List<App> apps;
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
                // Check if the constraint length is smaller than the oldText
                if (constraint.length() < oldText.length()) {
                    clear();
                    apps = finalApps;
                    performFiltering(constraint);
                } else {
                    Log.d(TAG, "publishResults: " + constraint + " " + oldText);
                    oldText = constraint.toString();
                    clear();
                    addAll((List<App>) results.values);
                    notifyDataSetChanged();
                }
            }
        }

    }


}

// Custom adapter for the ListView


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


