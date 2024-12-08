package com.giveawaychess;

import javax.swing.SwingUtilities;

public class AntichessGame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AntichessUI::new);

    }
}