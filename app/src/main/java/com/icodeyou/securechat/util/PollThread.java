package com.icodeyou.securechat.util;

import com.icodeyou.securechat.MyApplication;
import com.icodeyou.securechat.model.Contact;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 各种我还不知道轮询什么的轮询
 */
public class PollThread extends Thread {

    private boolean isPoll = true;

    private PollCallBackListener listener;

    public interface PollCallBackListener{
        public void onSuccess(boolean isRefresh);
    }

    public PollThread(PollCallBackListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        while (isPoll) {
            try {
                HttpUtil.getInstance().pollGet(MyApplication.getMyPhone(), new HttpUtil.HttpCallBackListener() {
                    @Override
                    public void onSuccess(String info) {
                        handleJSON(info);
                    }
                    @Override
                    public void onFail(String info) {
                    }
                });
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleJSON(String info) {
        try {
            JSONObject jsonObject = new JSONObject(info);
            JSONArray addContactArray = jsonObject.getJSONArray("addContact");
            JSONArray wantChatArray = jsonObject.getJSONArray("sendMyIP");
            boolean isRefresh = false;
            // 循环addContactArray数组
            for (int i=0;i<addContactArray.length();i++){
                JSONObject object = addContactArray.getJSONObject(i);
                if (object.getInt("addreq") == 0){
                    break;
                }
                String num = object.getString("num");
                String name = object.getString("name");
                NewFriendManager.getInstance().addContact(new Contact(name, num));
                isRefresh = true;
            }

            // 循环wantChatArray数组
            for (int i=0;i<wantChatArray.length();i++){
                JSONObject object = wantChatArray.getJSONObject(i);
                if (object.getInt("conreq") == 0){
                    break;
                }
                String ram = object.getString("ram");
                String num = object.getString("num");
                String name = object.getString("name");
                String ip = object.getString("ip");
                Contact contact = new Contact(name, num);
                contact.setRam(ram);
                WantToChatManager.getInstance().addContact(contact);
                isRefresh = true;
            }
            listener.onSuccess(isRefresh);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
