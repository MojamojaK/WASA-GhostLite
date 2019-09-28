package com.example.ikaro.sawa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Conversation service = new Conversation("2018-02-16");
        service.setUsernameAndPassword("993c525c-2373-4c98-abdc-1849b3f5f537","RxVU6X4XN2jl");
        InputData input = new InputData.Builder("Hello").build();
        MessageOptions options = new MessageOptions.Builder("de3e4543-8785-4b0d-b175-39ef3fc44d86").input(input).build();
        //MessageResponse response = service.message(options).execute();
        AsyncWatsonConversation tst = new AsyncWatsonConversation();
        tst.execute(options);
        ((TextView) findViewById(R.id.Test)).setText("test");
    }
}
