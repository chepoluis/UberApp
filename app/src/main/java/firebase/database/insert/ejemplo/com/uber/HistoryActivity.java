package firebase.database.insert.ejemplo.com.uber;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import firebase.database.insert.ejemplo.com.uber.historyRecyclerView.HistoryAdapter;
import firebase.database.insert.ejemplo.com.uber.historyRecyclerView.HistoryObject;

public class HistoryActivity extends AppCompatActivity {
    private String customerOrDriver, userId;

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    private TextView mBalance;

    private Double Balance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mBalance = findViewById(R.id.balance);

        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        // setNestedScrollingEnabled: make the scroll more natural
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        customerOrDriver = getIntent().getExtras().getString("customerOrDriver");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

        if(customerOrDriver.equals("Drivers")) {
            mBalance.setVisibility(View.VISIBLE);
        }
    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrDriver).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot history : dataSnapshot.getChildren()) {
                        FetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    String distance = "";
                    Double ridePrice = 0.0;

                    /*
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getKey().equals("timestamp")) {
                            timestamp = Long.valueOf(child.getValue().toString());
                        }
                    }
                    */

                    if(dataSnapshot.child("timestamp").getValue() != null) {
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }

                    if(dataSnapshot.child("customerPaid").getValue() != null && dataSnapshot.child("driverPaidOut").getValue() == null) {
                        if(dataSnapshot.child("distance").getValue() != null) {
                            distance = dataSnapshot.child("distance").getValue().toString();
                            ridePrice = (Double.valueOf(distance) * 9);
                            Balance += ridePrice;
                            mBalance.setText("Balance: $ " + String.valueOf(Balance) + " MXN");
                        }
                    }


                    HistoryObject obj = new HistoryObject(rideId, getDate(timestamp));
                    resultsHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
        return date;
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory() { return resultsHistory; }
}
