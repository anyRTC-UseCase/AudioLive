package org.ar.ar_audiomic.manager;

import org.ar.ar_audiomic.model.Message;
import org.ar.rtm.ResultCallback;
import org.ar.rtm.RtmMessage;


public interface MessageManager {

    void sendOrder(String userId, String orderType, String content, ResultCallback<Void> callback);

    void sendMessage(String text);

    void sendChannelMessage(String text);

    void processMessage(RtmMessage rtmMessage, String userId);

    void processChannelMessage(RtmMessage rtmMessage, String userId);

    void addMessage(Message message);

}
