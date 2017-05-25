package nkcs.machinelearning.spicychicken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by LIUJiayang on 23/05/2017.
 */
public class Main {
    static String userFolder;
    static ArrayList<EmailEntity> emailList;
    static ArrayList<String> folderList;
    static int emailNumber = 0;

    static private void init() throws IOException {
        File directory = new File("");
        userFolder = directory.getCanonicalPath() + "/usermailbox";
        emailList = new ArrayList<>();
        folderList = new ArrayList<>();
    }

    /**
     * @author LIUJiayang & JINXv
     * @date Tue 23 May 2017
     * @explain training set loading in
     */
    static private void loadTrainingSet() throws Exception {
        int emailId = -1;
        int classLabel = -1;
        File folder = new File(userFolder);

        for (File subfolder : folder.listFiles()) {
            String path = subfolder.getAbsolutePath();
            // [notice] handle hiding file like ".DS_Store"
            String folderName = path.substring(path.lastIndexOf('/') + 1);
            System.out.println("folderName: " + folderName);
            if (folderName.contains("."))
                continue;
            classLabel++;
            folderList.add(folderName);

            for (File file : subfolder.listFiles()) {
                // [notice] handle hiding file like ".DS_Store"
                System.out.println("fileName: " + file.getName());
                if (file.getName().contains("_"))
                    continue;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                EmailEntity email = new EmailEntity();
                email.emailId = ++emailId;
                email.classLabel = classLabel;
                email.folderName = folderName;
                /* load Subject */
                email.subject = reader.readLine().substring(8).trim();  // it is for the special case like "Subject: Re: ..." that I give up using "split(":")[1]"
                /* load Participant */
                email.participant = new ArrayList<>();  // critical
                int fieldCount = 0;
                String line;
                while (fieldCount < 4) {
                    line = reader.readLine();
                    if (line.contains(":")) {
                        fieldCount++;
                        // handle the line with ":" first
                        if (line.split(":").length > 1) {     // thus have at least one participant
                            line = line.split(":")[1].trim();
                        } else {    // when field have no address
                            line = "EmptyField";
                        }
                    }
                    if (!line.equals("EmptyField")) {
                        String[] splitList = line.split(",");
                        for (int i = 0; i < splitList.length; i++) {
                            email.participant.add(splitList[i].trim());
                        }
                    }
                }
                /* load Content */
                while ((line = reader.readLine()) != null) {
                    email.content += line + " ";
                }
                emailList.add(email);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        init();
        // Step 1: training set loading in
        loadTrainingSet();
        // test
//        System.out.println("\n---------------------------------------------\n");
//        for (int i = 0; i < emailList.size(); i++) {
//            System.out.println("emailId: " + emailList.get(i).emailId);
//            System.out.println("classLabel: " + emailList.get(i).classLabel);
//            System.out.println("subject: " + emailList.get(i).subject);
//            System.out.println("participant: " + emailList.get(i).participant.toString());
//            System.out.println("content: " + emailList.get(i).content);
//            System.out.println("\n*********************************************\n");
//        }

        // Step 2: feature weighting
        FeatureWeighting fw = new FeatureWeighting(emailList);
        fw.calculateTFIDF();
        fw.calculateSimilarity();
        fw.exportArff(folderList);

        // Step 3: feature selection
        FeatureSelection fs = new FeatureSelection();
        fs.filtrateAttribute("subject", 300);
        fs.filtrateAttribute("participant", 300);
        fs.filtrateAttribute("content", 300);

        // Step 4: classifier training

        // Step n: new email foldering interaction
//        NewEmailUI ui = new NewEmailUI();
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                ui.createAndShowGUI();
//            }
//        });
    }
}
