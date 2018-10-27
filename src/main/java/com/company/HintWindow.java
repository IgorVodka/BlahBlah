package com.company;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HintWindow {
    private JButton btn;
    private String fullAnswer;
    private JDialog dialog;

    enum HintWindowStatus { STARTED, WAITING, FIRST_TAKEN, SECOND_TAKEN, RESULT }

    public HintWindow() {
        this.btn = new JButton("!");
    }

    public void setStatus(HintWindowStatus status) {
        switch (status) {
            case STARTED:
                this.btn.setBackground(Color.BLUE);
                return;
            case WAITING:
                this.btn.setBackground(Color.WHITE);
                return;
            case FIRST_TAKEN:
                this.btn.setBackground(Color.GRAY);
                return;
            case SECOND_TAKEN:
                this.btn.setBackground(Color.GRAY);
                return;
            case RESULT:
                this.btn.setBackground(Color.BLACK);
                this.btn.grabFocus();
                return;
            default:
                this.btn.setBackground(Color.BLACK);
        }
    }

    public void setHint(String hint, String fullAnswer) {
        this.btn.setToolTipText(hint);
        this.fullAnswer = fullAnswer;
    }

    public void createWindow() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.dialog = new JDialog();
        dialog.setSize(4, 4);
        dialog.setAlwaysOnTop(true);
        dialog.setUndecorated(true);
        dialog.setVisible(true);

        ((JPanel)dialog.getContentPane()).setBorder(new EmptyBorder(0, 0, 0, 0));

        this.btn.setLocation(0, 0);
        this.btn.setSize(4, 4);
        dialog.add(this.btn);

        ToolTipManager.sharedInstance().setInitialDelay(0);

        this.btn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        System.out.println("Hiding...");
                        dialog.setVisible(false);
                        Thread.sleep(30000);
                        dialog.setVisible(true);
                    } else if (e.getButton() == MouseEvent.BUTTON2) {
                        System.out.println("Full answer.");
                        btn.setToolTipText(fullAnswer);
                    }
                } catch (Exception exc) {
                    // todo: also if second button clicked, show full info
                }
            }
        });
    }
}
