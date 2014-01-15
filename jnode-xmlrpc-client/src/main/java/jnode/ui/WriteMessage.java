package jnode.ui;

import jnode.impl.EchomailToolsProxy;

import javax.swing.*;
import java.awt.event.*;

public class WriteMessage extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tfArea;
    private JTextField tfSubject;
    private JTextArea taBody;
    private JTextField tfFrom;
    private JTextField tfTo;

    public WriteMessage() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void clear() {
        tfArea.setText("");
        tfSubject.setText("");
        taBody.setText("");
    }

    private boolean isEmptyStr(String value){
        return value == null || value.length() == 0;
    }

    private boolean isCustomFromTo(){
        return !isEmptyStr(tfFrom.getText()) && !isEmptyStr(tfTo.getText());
    }

    private void onOK() {

        String result;

        if (isCustomFromTo()){
            result = EchomailToolsProxy.writeEchomail(tfArea.getText(), tfSubject.getText(), taBody.getText(), tfFrom.getText(), tfTo.getText());
        } else{
            result = EchomailToolsProxy.writeEchomail(tfArea.getText(), tfSubject.getText(), taBody.getText());
        }

        if (isEmptyStr(result)) {
            final String message = tfArea.getText() + " " + tfSubject.getText() + " sent successfully";
            clear();
            JOptionPane.showMessageDialog(this, message,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        WriteMessage dialog = new WriteMessage();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
