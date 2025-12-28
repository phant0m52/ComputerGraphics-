package app;

import ui.MainFrame;

import javax.swing.*;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
