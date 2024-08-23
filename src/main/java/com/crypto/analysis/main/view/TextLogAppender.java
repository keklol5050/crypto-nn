package com.crypto.analysis.main.view;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.crypto.analysis.main.core.data_utils.select.StaticUtils;

import javax.swing.*;
import java.util.Date;

public class TextLogAppender extends AppenderBase<ILoggingEvent> {
    private static JTextArea textArea;

    public static void setTextArea(JTextArea textArea) {
        TextLogAppender.textArea = textArea;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (textArea != null) {
            SwingUtilities.invokeLater(() -> textArea.append(String.format("%s %s %s %s %n",
                    StaticUtils.sdfFullISO.format(new Date(eventObject.getTimeStamp())), eventObject.getLevel(), eventObject.getLoggerName(), eventObject.getFormattedMessage())));
        }
    }
}