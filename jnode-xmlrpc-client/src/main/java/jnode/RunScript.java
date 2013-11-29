package jnode;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class RunScript extends JFrame {

    private JLabel jLabel;
    private JLabel resLabel;
    private JTextField jTextField;
    private JTextArea resText;
    private JButton jButton;

    public RunScript() throws HeadlessException {
        init();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RunScript form = new RunScript();
                form.pack();
                form.setVisible(true);
            }
        }
        );
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);


        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;
        add(getJLabel(), c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        add(getJTextField(), c);
        getJLabel().setLabelFor(getJTextField());
        getJTextField().setPreferredSize(new Dimension(200, 20));

        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;
        add(getResLabel(), c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;

        add(getResText(), c);
        getResLabel().setLabelFor(getResText());

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1.0;
        add(getJButton(), c);
        setTitle("RunScript");
    }

    private JLabel getResLabel() {
        if (resLabel == null) {
            resLabel = new JLabel();
            resLabel.setText("Result");
        }
        return resLabel;
    }

    private JTextArea getResText() {
        if (resText == null) {
            resText = new JTextArea();
            resText.setEditable(false);
        }
        return resText;
    }

    private JLabel getJLabel() {
        if (jLabel == null) {
            jLabel = new JLabel();
            jLabel.setText("Script ID:");
        }
        return jLabel;
    }

    private JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
        }
        return jTextField;
    }

    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("OK");
            jButton.setActionCommand("run");
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    final JButton jButton1 = (JButton) e.getSource();
                    SwingWorker<Void, Void> w = new RunScriptWorker(jButton1);
                    jButton1.setEnabled(false);
                    w.execute();

                    System.out.println(e);
                }
            });
        }
        return jButton;
    }

    private class RunScriptWorker extends SwingWorker<Void, Void> {

        private final JButton button;
        private String result;

        private RunScriptWorker(JButton button) {
            this.button = button;
        }

        @Override
        protected Void doInBackground() throws Exception {
            result = ClientProxy.runScript(getJTextField().getText());
            return null;
        }

        @Override
        protected void done() {
            getResText().setText(result);
            button.setEnabled(true);
        }
    }
}
