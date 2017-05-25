package nkcs.machinelearning.spicychicken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static nkcs.machinelearning.spicychicken.Main.emailNumber;

/**
 * Created by LIUJiayang on 25/05/2017.
 */
public class NewEmailUI {
    private static JFrame frame;
    private static JTextField subjectText, fromText, toText, ccText, bccText;
    private static JTextArea contentText;

    /**
     * @author LIUJiayang
     * @date Thu 25 May 2017
     * @explain create and show GUI
     * @notice highly suggest this method been called in event thread for the sake of thread safe
     */
    public static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);    // for a better look
        frame = new JFrame("New Email Foldering");   // create and set frame
        // let GUI appear at the very middle of the screen
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int windowsWedth = 500;
        int windowsHeight = 600;
        frame.setBounds((screenWidth - windowsWedth) / 2, (screenHeight - windowsHeight) / 2, windowsWedth, windowsHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);
        frame.setVisible(true);
    }

    /**
     * @author LIUJiayang
     * @date Thu 25 May 2017
     * @explain place components on panel
     */
    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);
        /* Subject */
        JLabel subjectLabel = new JLabel("Subject:");
        Font labelfont = new Font(subjectLabel.getName(), Font.PLAIN, 20);
        subjectLabel.setBounds(30, 30, 100, 30);
        subjectLabel.setFont(labelfont);
        panel.add(subjectLabel);
        subjectText = new JTextField(20);
        subjectText.setBounds(120, 30, 350, 30);
        subjectText.setFont(labelfont);
        panel.add(subjectText);

        /* Participant */
        JLabel fromLabel = new JLabel("From:");
        fromLabel.setBounds(30, 80, 80, 30);
        fromLabel.setFont(labelfont);
        panel.add(fromLabel);
        fromText = new JTextField(20);
        fromText.setBounds(120, 80, 350, 30);
        fromText.setFont(labelfont);
        panel.add(fromText);

        JLabel toLabel = new JLabel("To:");
        toLabel.setBounds(30, 120, 80, 30);
        toLabel.setFont(labelfont);
        panel.add(toLabel);
        toText = new JTextField(20);
        toText.setBounds(120, 120, 350, 30);
        toText.setFont(labelfont);
        panel.add(toText);

        JLabel ccLabel = new JLabel("Cc:");
        ccLabel.setBounds(30, 160, 80, 30);
        ccLabel.setFont(labelfont);
        panel.add(ccLabel);
        ccText = new JTextField(20);
        ccText.setBounds(120, 160, 350, 30);
        ccText.setFont(labelfont);
        panel.add(ccText);

        JLabel bccLabel = new JLabel("Bcc:");
        bccLabel.setBounds(30, 200, 80, 30);
        bccLabel.setFont(labelfont);
        panel.add(bccLabel);
        bccText = new JTextField(20);
        bccText.setBounds(120, 200, 350, 30);
        bccText.setFont(labelfont);
        panel.add(bccText);

        /* Content */
        JLabel contentLabel = new JLabel("Content:");
        contentLabel.setBounds(30, 240, 100, 30);
        contentLabel.setFont(labelfont);
        panel.add(contentLabel);
        contentText = new JTextArea();
        contentText.setBounds(30, 270, 435, 200);
        contentText.setLineWrap(true);
        contentText.setFont(labelfont);
        panel.add(contentText);

        /* Button */
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(80, 500, 140, 50);
        cancelButton.setFont(labelfont);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
                // frame.setVisible(false);     // wait for
            }
        });
        panel.add(cancelButton);

        JButton sendButton = new JButton("Send");
        sendButton.setBounds(280, 500, 140, 50);
        sendButton.setFont(labelfont);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportEmail();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String folderingResult = "call";
                JOptionPane.showMessageDialog(null, folderingResult, "Foldering Result", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panel.add(sendButton);
    }

    /**
     * @author LIUJiayang
     * @date Thu 25 May 2017
     * @explain export user input to newemail.txt
     */
    private static void exportEmail() throws IOException {
        File projectDirectory = new File("");
        String resultPath = projectDirectory.getCanonicalPath() + "/result";
        File file = new File(resultPath + "/newemail.txt");  // save in result folder gaining the aid of its refresh mode
        // create it if file doesn't exists
        if (!file.exists())
            file.createNewFile();
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        /* subject area */
        bw.write("Subject: " + subjectText.getText() + "\n");
        /* participant area */
        bw.write("From: " + fromText.getText() + "\n");
        bw.write("To: " + toText.getText() + "\n");
        bw.write("Cc: " + ccText.getText() + "\n");
        bw.write("Bcc: " + bccText.getText() + "\n");
        /* content area */
        bw.write("\n" + contentText.getText());
        bw.close();
        System.out.println("Export to newemail.txt done!");
    }
}
