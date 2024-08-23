package com.crypto.analysis.main.view;

import com.crypto.analysis.main.core.regression.RegressionPanel;
import com.crypto.analysis.main.core.strategies.StrategyPanel;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame implements Runnable {
    private JTabbedPane mainTabbedPane = new JTabbedPane();
    private RegressionPanel regPanel;
    private StrategyPanel strategyPanel;
    private TabLogPanel logPanel;

    public GUI() {
        setTitle("Charts prediction");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.LIGHT_GRAY);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        setLayout(new BorderLayout());
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("icon.png")));

        init();
    }
    private void init() {
        regPanel = new RegressionPanel();
        strategyPanel = new StrategyPanel();
        logPanel = new TabLogPanel();

        run();
        setVisible(true);
    }

    @Override
    public void run() {
        mainTabbedPane.add("Charts", regPanel);
        mainTabbedPane.add("Strategies", strategyPanel);
        mainTabbedPane.add("Logs", logPanel);
        mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 15));

        mainTabbedPane.setBackground(Color.LIGHT_GRAY);


        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(new Font("Arial", Font.PLAIN, 15));
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setFont(new Font("Arial", Font.PLAIN, 15));
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Version stable-0.0.1\n 24.05.2024", "About", JOptionPane.INFORMATION_MESSAGE));
        settingsMenu.add(aboutMenuItem);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        menuBar.setBackground(Color.LIGHT_GRAY);
        settingsMenu.setBackground(Color.LIGHT_GRAY);
        aboutMenuItem.setBackground(Color.LIGHT_GRAY);

        settingsMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        aboutMenuItem.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        add(mainTabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI app = new GUI();
            app.setVisible(true);
        });
    }
}