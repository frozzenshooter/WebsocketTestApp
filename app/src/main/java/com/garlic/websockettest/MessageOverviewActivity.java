package com.garlic.websockettest;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.garlic.websockettest.messages.MessageAdapter;
import com.garlic.websockettest.messages.MessageHandler;

public class MessageOverviewActivity extends AppCompatActivity {

    private MessageHandler msgHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.message_recycler);

        msgHandler = ApplicationContext.getMessageHandler((ApplicationContext) getApplicationContext());
        Double[] messages = msgHandler.getAllMessages();
        MessageAdapter adapter = new MessageAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete_messages:
                this.deleteMessages();
                return true;
            case R.id.action_refresh_messages:
                this.refreshMessages();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }

    private void toast(String text){
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    private void deleteMessages(){
        int amount = msgHandler.resetMessages();
        this.toast("Deleted messages: "+amount);
        this.refreshMessages();
    }

    private void refreshMessages(){
        RecyclerView recyclerView = findViewById(R.id.message_recycler);

        msgHandler = ApplicationContext.getMessageHandler((ApplicationContext) getApplicationContext());
        Double[] messages = msgHandler.getAllMessages();

        MessageAdapter adapter = new MessageAdapter(messages);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the app bar.
        getMenuInflater().inflate(R.menu.menu_messages_overview, menu);
        return super.onCreateOptionsMenu(menu);
    }
}