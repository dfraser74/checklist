package com.philschatz.checklist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private RecyclerViewEmptySupport mRecyclerView;
    private FloatingActionButton mAddToDoItemFAB;
    private ArrayList<ToDoItem> mToDoItemsArrayList;
    private CoordinatorLayout mCoordLayout;
    public static final String TODOITEM = "com.philschatz.checklist.MainActivity.theToDoItem";
    public static final String TODOITEM_ID = "com.philschatz.checklist.MainActivity.theToDoItemId";
    private ToDoItemAdapter adapter;
    private static final int REQUEST_ID_TODO_ITEM = 100;
    private ToDoItem mJustDeletedToDoItem;
    private String mJustDeletedToDoItemId;
    private int mIndexOfDeletedToDoItem;
    public static final String DATE_TIME_FORMAT_12_HOUR = "MMM d, yyyy  h:mm a";
    public static final String DATE_TIME_FORMAT_24_HOUR = "MMM d, yyyy  k:mm";
    public static final String FILENAME = "todoitems.json";
    private StoreRetrieveData storeRetrieveData;
    public ItemTouchHelper itemTouchHelper;
    private CustomRecyclerScrollViewListener customRecyclerScrollViewListener;
    public static final String SHARED_PREF_DATA_SET_CHANGED = "com.avjindersekhon.datasetchanged";
    public static final String CHANGE_OCCURED = "com.avjinder.changeoccured";
    private int mTheme = -1;
    private String theme = "name_of_the_theme";
    public static final String THEME_PREFERENCES = "com.avjindersekhon.themepref";
    public static final String RECREATE_ACTIVITY = "com.avjindersekhon.recreateactivity";
    public static final String THEME_SAVED = "com.avjindersekhon.savedtheme";
    public static final String DARKTHEME = "com.avjindersekon.darktheme";
    public static final String LIGHTTHEME = "com.avjindersekon.lighttheme";
    private AnalyticsApplication app;
    private String[] testStrings = {"Clean my room",
            "Water the plants",
            "Get car washed",
            "Get my dry cleaning"
    };
    // /Users/[myusername]/Library/Android/sdk/extras/google/google_play_services/docs/reference/ Firebase Javadoc
    private DatabaseReference databaseReference;
    private static final String TAG = "ToDoItemListActivity";



    public static ArrayList<ToDoItem> getLocallyStoredData(StoreRetrieveData storeRetrieveData){
        ArrayList<ToDoItem> items = null;

//        try {
//            items  = storeRetrieveData.loadFromFile();
//
//        } catch (IOException | JSONException e) {
//            e.printStackTrace();
//        }

        if(items == null){
            items = new ArrayList<>();
        }
        return items;

    }

    @Override
    protected void onResume() {
        super.onResume();
        app.send(this);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        if(sharedPreferences.getBoolean(ReminderActivity.EXIT, false)){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ReminderActivity.EXIT,false);
            editor.apply();
            finish();
        }
        /*
        We need to do this, as this activity's onCreate won't be called when coming back from SettingsActivity,
        thus our changes to dark/light mode won't take place, as the setContentView() is not called again.
        So, inside our SettingsFragment, whenever the checkbox's value is changed, in our shared preferences,
        we mark our recreate_activity key as true.

        Note: the recreate_key's value is changed to false before calling recreate(), or we woudl have ended up in an infinite loop,
        as onResume() will be called on recreation, which will again call recreate() and so on....
        and get an ANR

         */
        if(getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE).getBoolean(RECREATE_ACTIVITY, false)){
            SharedPreferences.Editor editor = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(RECREATE_ACTIVITY, false);
            editor.apply();
            recreate();
        }


    }

    @Override
    protected void onStart() {
        app = (AnalyticsApplication)getApplication();
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
//        if(sharedPreferences.getBoolean(CHANGE_OCCURED, false)){
//
//            throw new RuntimeException("adjkhaskjdhs");
////            mToDoItemsArrayList = new ArrayList<ToDoItem>(); // getLocallyStoredData(storeRetrieveData);
////            adapter = new BasicListAdapter(mToDoItemsArrayList);
////            mRecyclerView.setAdapter(adapter);
////            setAlarms();
////
////            SharedPreferences.Editor editor = sharedPreferences.edit();
////            editor.putBoolean(CHANGE_OCCURED, false);
//////            editor.commit();
////            editor.apply();
//
//
//        }
    }

    private void setAlarms(){
        if(mToDoItemsArrayList!=null){
            for(ToDoItem item : mToDoItemsArrayList){
                if(item.getRemindAt()!=null){
                    if(item.getRemindAt().before(new Date())){
                        item.setRemindAt(null);
                        continue;
                    }
                    Intent i = new Intent(this, TodoNotificationService.class);
                    i.putExtra(TodoNotificationService.TODOUUID, item.getIdentifier());
                    i.putExtra(TodoNotificationService.TODOTEXT, item.getTitle());
                    createAlarm(i, item.getIdentifier().hashCode(), item.getRemindAt().getTime());
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        app = (AnalyticsApplication)getApplication();
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/Aller_Regular.tff").setFontAttrId(R.attr.fontPath).build());

        //We recover the theme we've set and setTheme accordingly
        theme = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE).getString(THEME_SAVED, LIGHTTHEME);

        if(theme.equals(LIGHTTHEME)){
            mTheme = R.style.CustomStyle_LightTheme;
        }
        else{
            mTheme = R.style.CustomStyle_DarkTheme;
        }
        this.setTheme(mTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CHANGE_OCCURED, false);
        editor.apply();

        // /Users/[myusername]/Library/Android/sdk/extras/google/google_play_services/docs/reference/ Firebase Javadocs
        databaseReference = FirebaseDatabase.getInstance().getReference().child("lists").child("sandbox").child("items");




//        storeRetrieveData = new StoreRetrieveData(this, FILENAME);
//        mToDoItemsArrayList =  getLocallyStoredData(storeRetrieveData);
        adapter = new ToDoItemAdapter(this, databaseReference);
        setAlarms();


//        adapter.notifyDataSetChanged();
//        storeRetrieveData = new StoreRetrieveData(this, FILENAME);
//
//        try {
//            mToDoItemsArrayList = storeRetrieveData.loadFromFile();
////            Log.d("OskarSchindler", "Arraylist Length: "+mToDoItemsArrayList.size());
//        } catch (IOException | JSONException e) {
////            Log.d("OskarSchindler", "IOException received");
//            e.printStackTrace();
//        }
//
//        if(mToDoItemsArrayList==null){
//            mToDoItemsArrayList = new ArrayList<>();
//        }
//

//        mToDoItemsArrayList = new ArrayList<>();
//        makeUpItems(mToDoItemsArrayList, testStrings.length);

        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        mCoordLayout = (CoordinatorLayout)findViewById(R.id.myCoordinatorLayout);
        mAddToDoItemFAB = (FloatingActionButton)findViewById(R.id.addToDoItemFAB);

        mAddToDoItemFAB.setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                app.send(this, "Action", "FAB pressed");
                Intent newTodo = new Intent(MainActivity.this, AddToDoActivity.class);
                ToDoItem item = new ToDoItem();
                item.setTitle(""); // This way the editor will start up blank
                //noinspection ResourceType
//                String color = getResources().getString(R.color.primary_ligher);
                newTodo.putExtra(TODOITEM, item);
                // new items do not have a Firebase id yet  TODO PHIL Maybe this should be the point when they get an id
                newTodo.putExtra(TODOITEM_ID, (String) null);
//                View decorView = getWindow().getDecorView();
//                View navView= decorView.findViewById(android.R.id.navigationBarBackground);
//                View statusView = decorView.findViewById(android.R.id.statusBarBackground);
//                Pair<View, String> navBar ;
//                if(navView!=null){
//                    navBar = Pair.create(navView, navView.getTransitionName());
//                }
//                else{
//                    navBar = null;
//                }
//                Pair<View, String> statusBar= Pair.create(statusView, statusView.getTransitionName());
//                ActivityOptions options;
//                if(navBar!=null){
//                    options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, navBar, statusBar);
//                }
//                else{
//                    options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, statusBar);
//                }

//                startActivity(new Intent(MainActivity.this, TestLayout.class), options.toBundle());
//                startActivityForResult(newTodo, REQUEST_ID_TODO_ITEM, options.toBundle());

                startActivityForResult(newTodo, REQUEST_ID_TODO_ITEM);
            }
        });


//        mRecyclerView = (RecyclerView)findViewById(R.id.toDoRecyclerView);
        mRecyclerView = (RecyclerViewEmptySupport)findViewById(R.id.toDoRecyclerView);
        if(theme.equals(LIGHTTHEME)){
            mRecyclerView.setBackgroundColor(getResources().getColor(R.color.primary_lightest));
        }
        mRecyclerView.setEmptyView(findViewById(R.id.toDoEmptyView));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        customRecyclerScrollViewListener = new CustomRecyclerScrollViewListener() {
            @Override
            public void show() {

                mAddToDoItemFAB.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
//                mAddToDoItemFAB.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }

            @Override
            public void hide() {

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)mAddToDoItemFAB.getLayoutParams();
                int fabMargin = lp.bottomMargin;
                mAddToDoItemFAB.animate().translationY(mAddToDoItemFAB.getHeight()+fabMargin).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }
        };
        mRecyclerView.addOnScrollListener(customRecyclerScrollViewListener);


        ItemTouchHelper.Callback callback = new ItemTouchHelperClass(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);


        mRecyclerView.setAdapter(adapter);
//        setUpTransitions();



    }

    public void addThemeToSharedPreferences(String theme){
        SharedPreferences sharedPreferences = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(THEME_SAVED, theme);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.aboutMeMenuItem:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);
                return true;
//            case R.id.switch_themes:
//                if(mTheme == R.style.CustomStyle_DarkTheme){
//                    addThemeToSharedPreferences(LIGHTTHEME);
//                }
//                else{
//                    addThemeToSharedPreferences(DARKTHEME);
//                }
//
////                if(mTheme == R.style.CustomStyle_DarkTheme){
////                    mTheme = R.style.CustomStyle_LightTheme;
////                }
////                else{
////                    mTheme = R.style.CustomStyle_DarkTheme;
////                }
//                this.recreate();
//                return true;
            case R.id.preferences:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= RESULT_CANCELED && requestCode == REQUEST_ID_TODO_ITEM){
            ToDoItem item =(ToDoItem) data.getSerializableExtra(TODOITEM);
            String itemId = data.getStringExtra(TODOITEM_ID);

            if(item.getTitle().length()<=0){
                return;
            }
            boolean existed = false;

            if(item.getRemindAt()!=null){
                Intent i = new Intent(this, TodoNotificationService.class);
                i.putExtra(TodoNotificationService.TODOTEXT, item.getTitle());
                i.putExtra(TodoNotificationService.TODOUUID, item.getIdentifier());
                createAlarm(i, item.getIdentifier().hashCode(), item.getRemindAt().getTime());
//                Log.d("OskarSchindler", "Alarm Created: "+item.getTitle()+" at "+item.getRemindAt());
            }

//            for(int i = 0; i<mToDoItemsArrayList.size();i++){
//                if(item.getIdentifier().equals(mToDoItemsArrayList.get(i).getIdentifier())){
//
////                    mToDoItemsArrayList.set(i, item);
////                    existed = true;
////                    adapter.notifyDataSetChanged();
////                    DatabaseReference child = databaseReference.child((item.firebaseKey));
////                    child.setValue(item);
//                    break;
//                }
//            }
////            if(!existed) {
////                addToDataStore(item);
////            }
            addToDataStore(item, itemId);

        }
    }

    private AlarmManager getAlarmManager(){
        return (AlarmManager)getSystemService(ALARM_SERVICE);
    }

    private boolean doesPendingIntentExist(Intent i, int requestCode){
        PendingIntent pi = PendingIntent.getService(this,requestCode, i, PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }

    private void createAlarm(Intent i, int requestCode, long timeInMillis){
        AlarmManager am = getAlarmManager();
        PendingIntent pi = PendingIntent.getService(this,requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
//        Log.d("OskarSchindler", "createAlarm "+requestCode+" time: "+timeInMillis+" PI "+pi.toString());
    }
    private void deleteAlarm(Intent i, int requestCode){
        if(doesPendingIntentExist(i, requestCode)){
            PendingIntent pi = PendingIntent.getService(this, requestCode,i, PendingIntent.FLAG_NO_CREATE);
            pi.cancel();
            getAlarmManager().cancel(pi);
            Log.d("OskarSchindler", "PI Cancelled " + doesPendingIntentExist(i, requestCode));
        }
    }

    private void addToDataStore(ToDoItem item, String itemId){
        // append a new item or edit an existing item
        if (itemId != null) {
            databaseReference.child(itemId).setValue(item);
        } else {
            databaseReference.push().setValue(item);
        }

//        mToDoItemsArrayList.add(item)
//        adapter.notifyItemInserted(mToDoItemsArrayList.size() - 1);

    }



    // TODO FIXME Use this instead: https://github.com/firebase/FirebaseUI-Android/blob/master/database/src/main/java/com/firebase/ui/database/FirebaseRecyclerAdapter.java
    private class ToDoItemAdapter extends RecyclerView.Adapter<ToDoItemAdapter.ViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mToDoItemIds = new ArrayList<>();
        private List<ToDoItem> mToDoItems = new ArrayList<>();

        public ToDoItemAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new item has been added, add it to the displayed list
                    ToDoItem item = dataSnapshot.getValue(ToDoItem.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mToDoItemIds.add(dataSnapshot.getKey());
                    mToDoItems.add(item);
                    notifyItemInserted(mToDoItems.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    ToDoItem newToDoItem = dataSnapshot.getValue(ToDoItem.class);
                    String itemKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int itemIndex = mToDoItemIds.indexOf(itemKey);
                    if (itemIndex > -1) {
                        // Replace with the new data
                        mToDoItems.set(itemIndex, newToDoItem);

                        // Update the RecyclerView
                        notifyItemChanged(itemIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + itemKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String itemKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int itemIndex = mToDoItemIds.indexOf(itemKey);
                    if (itemIndex > -1) {
                        // Remove data from the list
                        mToDoItemIds.remove(itemIndex);
                        mToDoItems.remove(itemIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(itemIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + itemKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    ToDoItem movedToDoItem = dataSnapshot.getValue(ToDoItem.class);
                    String itemKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postToDoItems:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load items.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

//        @Override
//        public ToDoItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
////            Context mContext = parent.getContext();
//            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//            View view = inflater.inflate(R.layout.item_comment, parent, false);
//            return new ToDoItemAdapter.ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(ToDoItemAdapter.ViewHolder holder, int position) {
//            ToDoItem comment = mToDoItems.get(position);
//            holder.authorView.setText(comment.author);
//            holder.bodyView.setText(comment.text);
//        }


        // ItemTouchHelperAdapter methods
        @Override
        public void onItemMoved(int fromPosition, int toPosition) {
           if(fromPosition<toPosition){
               for(int i=fromPosition; i<toPosition; i++){
                   Collections.swap(mToDoItems, i, i+1);
                   Collections.swap(mToDoItemIds, i, i+1);
               }
           }
            else{
               for(int i=fromPosition; i > toPosition; i--){
                   Collections.swap(mToDoItems, i, i-1);
                   Collections.swap(mToDoItemIds, i, i-1);
               }
           }
            notifyItemMoved(fromPosition, toPosition);
        }
        @Override
        public void onItemRemoved(final int position) {
            //Remove this line if not using Google Analytics
            app.send(this, "Action", "Swiped Todo Away");

//            mJustDeletedToDoItem =  items.remove(position);
//            mIndexOfDeletedToDoItem = position;
            mJustDeletedToDoItem = mToDoItems.get(position);
            mJustDeletedToDoItemId = mToDoItemIds.get(position);
            mIndexOfDeletedToDoItem = position;
            DatabaseReference child = databaseReference.child(mJustDeletedToDoItemId);
            child.removeValue();

            Intent i = new Intent(MainActivity.this,TodoNotificationService.class);
            deleteAlarm(i, mJustDeletedToDoItem.getIdentifier().hashCode());
            notifyItemRemoved(position);

//            String toShow = (mJustDeletedToDoItem.getTitle().length()>20)?mJustDeletedToDoItem.getTitle().substring(0, 20)+"...":mJustDeletedToDoItem.getTitle();
            String toShow = "Todo";
            Snackbar.make(mCoordLayout, "Deleted "+toShow,Snackbar.LENGTH_SHORT)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //Comment the line below if not using Google Analytics
                            app.send(this, "Action", "UNDO Pressed");
//                            items.add(mIndexOfDeletedToDoItem, mJustDeletedToDoItem);
                            if(mJustDeletedToDoItem.getRemindAt()!=null){
                                Intent i = new Intent(MainActivity.this, TodoNotificationService.class);
                                i.putExtra(TodoNotificationService.TODOTEXT, mJustDeletedToDoItem.getTitle());
                                i.putExtra(TodoNotificationService.TODOUUID, mJustDeletedToDoItem.getIdentifier());
                                createAlarm(i, mJustDeletedToDoItem.getIdentifier().hashCode(), mJustDeletedToDoItem.getRemindAt().getTime());
                            }
                            // TODO: PHIL Insertion order should be a float so we can always insert between 2 items
                            databaseReference.push().setValue(mJustDeletedToDoItem);
//                            notifyItemInserted(mIndexOfDeletedToDoItem);
                        }
                    }).show();
        }



        @Override
        public ToDoItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.list_circle_try, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ToDoItemAdapter.ViewHolder holder, final int position) {
            ToDoItem item = mToDoItems.get(position);
//            if(item.getRemindAt()!=null && item.getRemindAt().before(new Date())){
//                item.setRemindAt(null);
//            }
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
            //Background color for each to-do item. Necessary for night/day mode
            int bgColor;
            //color of title text in our to-do item. White for night mode, dark gray for day mode
            int todoTextColor;
            if(sharedPreferences.getString(THEME_SAVED, LIGHTTHEME).equals(LIGHTTHEME)){
                bgColor = Color.WHITE;
                todoTextColor = mContext.getResources().getColor(R.color.secondary_text);
            }
            else{
                bgColor = Color.DKGRAY;
                todoTextColor = Color.WHITE;
            }
            holder.linearLayout.setBackgroundColor(bgColor);

            if(item.getRemindAt()!=null){
                holder.mToDoTextview.setMaxLines(1);
                holder.mTimeTextView.setVisibility(View.VISIBLE);
//                holder.mToDoTextview.setVisibility(View.GONE);
            }
            else{
                holder.mTimeTextView.setVisibility(View.GONE);
                holder.mToDoTextview.setMaxLines(2);
            }
            holder.mToDoTextview.setText(item.getTitle());
            holder.mToDoTextview.setTextColor(todoTextColor);
//            holder.mColorTextView.setBackgroundColor(Color.parseColor(item.getTodoColor()));

//            TextDrawable myDrawable = TextDrawable.builder().buildRoundRect(item.getTitle().substring(0,1),Color.RED, 10);
            //We check if holder.color is set or not
//            if(item.getTodoColor() == null){
//                ColorGenerator generator = ColorGenerator.MATERIAL;
//                int color = generator.getRandomColor();
//                item.setTodoColor(color+"");
//            }
//            Log.d("OskarSchindler", "Color: "+item.getTodoColor());

            String firstLetter = item.getTitle().substring(0,1);
            // Use the first letter as the hash for the color
            int color = ColorGenerator.MATERIAL.getColor(firstLetter);
            TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                    .textColor(Color.WHITE)
                    .useFont(Typeface.DEFAULT)
                    .toUpperCase()
                    .endConfig()
                    .buildRound(firstLetter, color);

//            TextDrawable myDrawable = TextDrawable.builder().buildRound(item.getTitle().substring(0,1),holder.color);
            holder.mColorImageView.setImageDrawable(myDrawable);
            if(item.getRemindAt()!=null){
                String timeToShow;
                if(android.text.format.DateFormat.is24HourFormat(MainActivity.this)){
                    timeToShow = AddToDoActivity.formatDate(MainActivity.DATE_TIME_FORMAT_24_HOUR, item.getRemindAt());
                }
                else{
                    timeToShow = AddToDoActivity.formatDate(MainActivity.DATE_TIME_FORMAT_12_HOUR, item.getRemindAt());
                }
                holder.mTimeTextView.setText(timeToShow);
            }


        }

    @Override
        public int getItemCount() {
            return mToDoItems.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

        @SuppressWarnings("deprecation")
        public class ViewHolder extends RecyclerView.ViewHolder{

            View mView;
            LinearLayout linearLayout;
            TextView mToDoTextview;
//            TextView mColorTextView;
            ImageView mColorImageView;
            TextView mTimeTextView;
//            int color = -1;

            public ViewHolder(View v){
                super(v);
                mView = v;
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = ViewHolder.this.getAdapterPosition();
                        ToDoItem item = mToDoItems.get(position);
                        String itemId = mToDoItemIds.get(position);

                        Intent i = new Intent(MainActivity.this, AddToDoActivity.class);
                        i.putExtra(TODOITEM, item);
                        i.putExtra(TODOITEM_ID, itemId);
                        startActivityForResult(i, REQUEST_ID_TODO_ITEM);
                    }
                });
                mToDoTextview = (TextView)v.findViewById(R.id.toDoListItemTextview);
                mTimeTextView = (TextView)v.findViewById(R.id.todoListItemTimeTextView);
//                mColorTextView = (TextView)v.findViewById(R.id.toDoColorTextView);
                mColorImageView = (ImageView)v.findViewById(R.id.toDoListItemColorImageView);
                linearLayout = (LinearLayout)v.findViewById(R.id.listItemLinearLayout);
            }


        }

}


    //Used when using custom fonts
//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

//    private void saveDate(){
//        try {
//            storeRetrieveData.saveToFile(mToDoItemsArrayList);
//        } catch (JSONException | IOException e) {
//            e.printStackTrace();
//        }
//
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        try {
//            storeRetrieveData.saveToFile(mToDoItemsArrayList);
//        } catch (JSONException | IOException e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        mRecyclerView.removeOnScrollListener(customRecyclerScrollViewListener);
    }


//    public void setUpTransitions(){
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            Transition enterT = new Slide(Gravity.RIGHT);
//            enterT.setDuration(500);
//
//            Transition exitT = new Slide(Gravity.LEFT);
//            exitT.setDuration(300);
//
//            Fade fade = new Fade();
//            fade.setDuration(500);
//
//            getWindow().setExitTransition(fade);
//            getWindow().setReenterTransition(fade);
//
//        }
//    }

}
