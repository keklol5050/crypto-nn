package com.crypto.analysis.main.view;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class TabLogPanel extends JPanel {
    private final JTextArea textLogArea;
    public TabLogPanel() {
        this.setLayout(new BorderLayout());

        textLogArea = new JTextArea();
        textLogArea.setBackground(Color.LIGHT_GRAY);
        textLogArea.setEditable(false);
        textLogArea.setFont(new Font("Arial", Font.PLAIN, 15));
        TextLogAppender.setTextArea(textLogArea);

        JScrollPane areaScrollPane = new JScrollPane(textLogArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.add(areaScrollPane);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuCopy = new JMenuItem("Copy");
        popup.add(menuCopy);
        menuCopy.addActionListener(e -> {
            textLogArea.selectAll();
            String textAll = textLogArea.getText();

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(textAll);
            clipboard.setContents(stringSelection, null);

        });

        JMenuItem menuClear = new JMenuItem("Clear");
        popup.add(menuClear);
        menuClear.addActionListener(e -> {
            textLogArea.setText(null);
        });

        popup.addSeparator();
        JMenuItem wordWrapMenu = new JMenuItem("Word wrap");
        popup.add(wordWrapMenu);
        wordWrapMenu.addActionListener(e -> {
            boolean wordWrap = !textLogArea.getLineWrap();
            textLogArea.setLineWrap(wordWrap);
            wordWrapMenu.setText(wordWrap ? "Word unwrap" : "Word wrap");
        });

        textLogArea.setComponentPopupMenu(popup);
    }

    public void clear() {
        textLogArea.setText("");
    }
}
