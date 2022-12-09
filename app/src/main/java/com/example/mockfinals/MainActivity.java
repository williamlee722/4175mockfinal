package com.example.mockfinals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity{

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private Button [] buttons = new Button[9];
    private int roundCount;

    boolean playerX;
    String[] button = new String[9];
    int[][] winningPositions ={
            {0,1,2}, {3,4,5}, {6,7,8},
            {0,3,6}, {1,4,7}, {2,5,8},
            {0,4,8}, {2,4,6}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("games");

        initialize();

        databaseReference.child("playerX").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playerX = snapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        for(int i = 0; i < 9; i++) {
            int finalI = i;
            databaseReference.child(String.valueOf(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    button[finalI] = snapshot.getValue(String.class);
                    buttons[finalI].setText(snapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("Xwin").getValue(Boolean.class) == true) {
                    Toast.makeText(getApplicationContext(), "X wins!", Toast.LENGTH_SHORT).show();
                    databaseReference.child("Xwin").setValue(false);

                } if(snapshot.child("Owin").getValue(Boolean.class) == true) {
                    Toast.makeText(getApplicationContext(), "O wins!", Toast.LENGTH_SHORT).show();
                    databaseReference.child("Owin").setValue(false);

                } if(snapshot.child("tie").getValue(Boolean.class) == true) {
                    Toast.makeText(getApplicationContext(), "It's a tie!", Toast.LENGTH_SHORT).show();
                    databaseReference.child("tie").setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        for(int i = 0; i < buttons.length; i++) {
            String buttonID = "btn_" + i;
            int resourceID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = (Button) findViewById(resourceID);
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!((Button)v).getText().toString().equals("")){
                        return;
                    }
                    String buttonID = v.getResources().getResourceName(v.getId());
                    int buttonPointer = Integer.parseInt(buttonID.substring(buttonID.length()-1, buttonID.length()));

                    if(playerX){
                        ((Button)v).setText("X");
                        button[buttonPointer] = "X";
                        databaseReference.child(String.valueOf(buttonPointer)).setValue("X");
                    }else{
                        ((Button)v).setText("O");
                        button[buttonPointer] = "O";
                        databaseReference.child(String.valueOf(buttonPointer)).setValue("O");
                    }
                    roundCount++;
                    databaseReference.child("roundCount").setValue(roundCount);

                    result();
                }
            });
        }
    }

    public boolean checkWinner(){
        boolean Result = false;

        for(int [] winningPositions: winningPositions){
            if(button[winningPositions[0]] == button[winningPositions[1]] &&
                    button[winningPositions[1]] == button[winningPositions[2]] &&
                    button[winningPositions[0]] != ""){
                Result = true;
            }
        }
        return Result;
    }

    public void startNewGame(){
        roundCount = 0;
        playerX = true;
        for(int i=0; i<buttons.length; i++){
            button[i]="";
            buttons[i].setText("");
            buttons[i].setEnabled(true);
            initialize();
        }
    }

    private void result() {
        if(checkWinner()){
            if(playerX){
                databaseReference.child("Xwin").setValue(true);
                startNewGame();
            }else{
                databaseReference.child("Owin").setValue(true);
                startNewGame();
            }
        }else if(roundCount==9){
            databaseReference.child("tie").setValue(true);
            startNewGame();
        }else {
            playerX = !playerX;
            databaseReference.child("playerX").setValue(playerX);
        }
    }

    private void initialize() {
        for(int i = 0; i < buttons.length; i++) {
            databaseReference.child(String.valueOf(i)).setValue("");
        }
        databaseReference.child("playerX").setValue(true);
        databaseReference.child("Xwin").setValue(false);
        databaseReference.child("Owin").setValue(false);
        databaseReference.child("tie").setValue(false);
    }


}