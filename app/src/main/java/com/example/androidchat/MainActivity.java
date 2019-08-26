package com.example.androidchat;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {
    private ListView listView;
    private EditText chatText;
    private boolean side = false;
    private JSONObject conversationContext;
    private ChatArrayAdapter chatArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatText = findViewById(R.id.editText);
        configureListView();
        getResponse();
    }

    public void send(View view) {
        String input = chatText.getText().toString();
        // print on the outputTextView what the user types
        chatArrayAdapter.add(new ChatMessage(true, input));
        getResponse();
        chatText.setText("");
    }

    private void configureListView(){
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.my_message);
        listView = findViewById(R.id.messages_view);
        listView.setAdapter(chatArrayAdapter);
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private void getResponse() {
        String workspaceId = "a7436f08-a414-41ab-9906-fa9e6324be46";
        String urlAssistant = "https://gateway.watsonplatform.net/assistant/api/v1/workspaces/" +
                workspaceId +
                "/message?version=2019-02-28";
        String authentication = "YXBpa2V5OlNqOWdOeHdNOF9CS2Naamd1eW1jOE5DeUxseVpUSXU2YmlzdzdteUM4Ukh3";
        AndroidNetworking.post(urlAssistant)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Basic " + authentication)
                .addJSONObjectBody(createJsonObjectBody())
                .setPriority(Priority.HIGH)
                .setTag(R.string.app_name)
                .build()
                .getAsJSONObject(getOutputMessage());
    }

    private JSONObject createJsonObjectBody(){
        JSONObject inputJsonObject = new JSONObject();
        JSONObject jsonBody = new JSONObject();
        try {
            inputJsonObject.put("text", chatText.getText().toString());
            // put the text Json in the main JSONObject
            jsonBody.put("input", inputJsonObject);
            // put the conversation context Json in the main JSONObject
            jsonBody.put("context", conversationContext);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonBody;
    }

    private JSONObjectRequestListener getOutputMessage(){
        return new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray outputJsonObject;
                try {
                    // Get the response text from Watson
                    outputJsonObject = response.getJSONObject("output").getJSONArray("text");
                    // Refresh the conversation context
                    conversationContext = response.getJSONObject("context");
                    /* Sometimes Watson can return more then one string
                     *  These strings are in a JSONArray that is iterated by the for bellow
                     */
                    for(int index=0; index<outputJsonObject.length(); index++){
                        // Print the messages in the outputTextView
                        chatArrayAdapter.add(new ChatMessage(false, outputJsonObject.get(index).toString()));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(ANError anError) {
                // Shows a message of error in the case of the connection fails
                Toast.makeText(getApplicationContext(), "connection error", Toast.LENGTH_SHORT).show();
            }
        };
    }



}
