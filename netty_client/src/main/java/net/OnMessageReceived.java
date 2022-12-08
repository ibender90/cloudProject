package net;

import model.AbstractMessage;

import java.io.IOException;

public interface OnMessageReceived {
    void onReceive(AbstractMessage msg) throws IOException;
}
