package com.example.ikaro.sawa;

import android.os.AsyncTask;

import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

/**
 * Created by ikaro on 2018/03/22.
 */

public class AsyncWatsonConversation extends AsyncTask<MessageOptions,Void,Void>
{
    String result = "";
    @Override
    protected Void doInBackground(MessageOptions...params)
    {
        Conversation service = new Conversation("2018-02-16");
        service.setUsernameAndPassword("993c525c-2373-4c98-abdc-1849b3f5f537","RxVU6X4XN2jl");
        MessageResponse response = service.message(params[0]).execute();
        result=response.toString();
        return null;
    }
}
