package nkcs.machinelearning.spicychicken;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.*;
import java.util.*;
import java.util.Map.*;

/**
 * Created by LIUJiayang on 24/05/2017.
 */
public class FeatureWeighting {
    ArrayList<EmailEntity> emailList;
    int emailNumber;
    ArrayList<HashMap<String, Double>> tf_idf_subject_list = new ArrayList<>();
    ArrayList<HashMap<String, Double>> tf_idf_content_list = new ArrayList<>();
    ArrayList<HashMap<String, Double>> tf_idf_participant_list = new ArrayList<>();
    ArrayList<ArrayList<Double>> sim_matrix_subject = new ArrayList<>();
    ArrayList<ArrayList<Double>> sim_matrix_content = new ArrayList<>();
    ArrayList<ArrayList<Double>> sim_matrix_participant = new ArrayList<>();

    public FeatureWeighting(ArrayList<EmailEntity> emailList) {
        this.emailNumber = emailList.size();
        this.emailList = emailList;
    }

    /**
     * @author JINXv
     * @date Thu 25 May 2017
     * @explain calculate tf-idf
     */
    public void calculateTFIDF() throws IOException {
        ArrayList<HashMap<String, Double>> tf_subject_list = new ArrayList<>();
        ArrayList<HashMap<String, Double>> tf_content_list = new ArrayList<>();
        ArrayList<HashMap<String, Double>> tf_participant_list = new ArrayList<>();
        HashMap<String, Double> idf_subject = new HashMap<>();
        HashMap<String, Double> idf_content = new HashMap<>();
        HashMap<String, Double> idf_participant = new HashMap<>();

        for (EmailEntity email : emailList) {
            Analyzer analyzer = new StandardAnalyzer();

            StringReader reader = new StringReader(email.subject);
            TokenStream ts = analyzer.tokenStream("subject", reader);
            CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            HashMap<String, Double> tf_subject = new HashMap<>();
            int term_count = 0;
            while (ts.incrementToken()) {
                term_count++;
                String term = charTermAttribute.toString();
                if (tf_subject.containsKey(term)) {
                    double freq = tf_subject.get(term);
                    tf_subject.put(term, ++freq);
                } else {
                    tf_subject.put(term, 1.0);
                }
            }
            for (Entry<String, Double> entry : tf_subject.entrySet()) {
                entry.setValue(entry.getValue() / term_count);
                String term = entry.getKey();
                if (idf_subject.containsKey(term)) {
                    double freq = idf_subject.get(term);
                    idf_subject.put(term, ++freq);
                } else {
                    idf_subject.put(term, 1.0);
                }
            }
            for (Entry<String, Double> entry : idf_subject.entrySet()) {
                double df = entry.getValue();
                double temp = emailList.size() / (df + 1);
                entry.setValue(temp == 0 ? 0 : Math.log(temp) / Math.log(2));
            }
            tf_subject_list.add(tf_subject);
            ts.close();

            reader = new StringReader(email.content);
            ts = analyzer.tokenStream("content", reader);
            charTermAttribute = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            HashMap<String, Double> tf_content = new HashMap<>();
            term_count = 0;
            while (ts.incrementToken()) {
                term_count++;
                String term = charTermAttribute.toString();
                if (tf_content.containsKey(term)) {
                    double freq = tf_content.get(term);
                    tf_content.put(term, ++freq);
                } else {
                    tf_content.put(term, 1.0);
                }
            }
            for (Entry<String, Double> entry : tf_content.entrySet()) {
                entry.setValue(entry.getValue() / term_count);
                String term = entry.getKey();
                if (idf_content.containsKey(term)) {
                    double freq = idf_content.get(term);
                    idf_content.put(term, ++freq);
                } else {
                    idf_content.put(term, 1.0);
                }
            }
            for (Entry<String, Double> entry : idf_content.entrySet()) {
                double df = entry.getValue();
                double temp = emailList.size() / (df + 1);
                entry.setValue(temp == 0 ? 0 : Math.log(temp) / Math.log(2));
            }
            tf_content_list.add(tf_content);
            ts.close();

            String[] participant = new String[email.participant.size()];
            email.participant.toArray(participant);
            System.out.println(email.participant.toString());
            HashMap<String, Double> tf_participant = new HashMap<>();
            term_count = 0;
            for (String term : participant) {
                term_count++;
                if (tf_participant.containsKey(term)) {
                    double freq = tf_participant.get(term);
                    tf_participant.put(term, ++freq);
                } else {
                    tf_participant.put(term, 1.0);
                }
            }
            for (Entry<String, Double> entry : tf_participant.entrySet()) {
                entry.setValue(entry.getValue() / term_count);
                String term = entry.getKey();
                if (idf_participant.containsKey(term)) {
                    double freq = idf_participant.get(term);
                    idf_participant.put(term, ++freq);
                } else {
                    idf_participant.put(term, 1.0);
                }
            }
            for (Entry<String, Double> entry : idf_participant.entrySet()) {
                double df = entry.getValue();
                double temp = emailList.size() / (df + 1);
                entry.setValue(temp == 0 ? 0 : Math.log(temp) / Math.log(2));
            }
            tf_participant_list.add(tf_participant);
        }

        for (HashMap<String, Double> tf_subject : tf_subject_list) {
            HashMap<String, Double> tf_idf_subject = new HashMap<>();
            for (Entry<String, Double> entry : tf_subject.entrySet()) {
                String term = entry.getKey();
                double tf = entry.getValue();
                double idf = idf_subject.get(term);
                tf_idf_subject.put(term, tf * idf);
            }
            tf_idf_subject_list.add(tf_idf_subject);
        }

        for (HashMap<String, Double> tf_content : tf_content_list) {
            HashMap<String, Double> tf_idf_content = new HashMap<>();
            for (Entry<String, Double> entry : tf_content.entrySet()) {
                String term = entry.getKey();
                double tf = entry.getValue();
                double idf = idf_content.get(term);
                tf_idf_content.put(term, tf * idf);
            }
            tf_idf_content_list.add(tf_idf_content);
        }

        for (HashMap<String, Double> tf_participant : tf_participant_list) {
            HashMap<String, Double> tf_idf_participant = new HashMap<>();
            for (Entry<String, Double> entry : tf_participant.entrySet()) {
                String term = entry.getKey();
                double tf = entry.getValue();
                double idf = idf_participant.get(term);
                tf_idf_participant.put(term, tf * idf);
            }
            tf_idf_participant_list.add(tf_idf_participant);
        }
    }

    /**
     * @author JINXv
     * @date Thu 25 May 2017
     * @explain calculate similarity
     */
    public void calculateSimilarity() {
        for (int i = 0; i < emailList.size(); i++) {
            ArrayList<Double> sim_subject = new ArrayList<>();
            ArrayList<Double> sim_content = new ArrayList<>();
            ArrayList<Double> sim_participant = new ArrayList<>();
            for (int j = 0; j < emailList.size(); j++) {
                sim_subject.add(1.0);
                sim_content.add(1.0);
                sim_participant.add(1.0);
            }
            sim_matrix_subject.add(sim_subject);
            sim_matrix_content.add(sim_content);
            sim_matrix_participant.add(sim_participant);
        }

        for (int i = 0; i < emailList.size(); i++) {
            for (int j = 0; j < emailList.size(); j++) {
                if (i == j) {
                    continue;
                }
                if (i > j) {
                    sim_matrix_subject.get(i).set(j, sim_matrix_subject.get(j).get(i));
                    sim_matrix_content.get(i).set(j, sim_matrix_content.get(j).get(i));
                    sim_matrix_participant.get(i).set(j, sim_matrix_participant.get(j).get(i));
                    continue;
                }

                HashMap<String, Double> tf_idf1 = tf_idf_subject_list.get(i);
                HashMap<String, Double> tf_idf2 = tf_idf_subject_list.get(j);
                Set<String> terms1 = tf_idf1.keySet();
                Set<String> terms2 = tf_idf2.keySet();
                Set<String> common_terms = new HashSet<>();
                common_terms.addAll(terms1);
                common_terms.retainAll(terms2);

                double sim = 0;
                for (String term : common_terms) {
                    sim += tf_idf1.get(term) * tf_idf2.get(term);
                }
                double length1 = 0;
                double length2 = 0;
                for (Entry<String, Double> entry : tf_idf1.entrySet()) {
                    length1 += Math.pow(entry.getValue(), 2);
                }
                for (Entry<String, Double> entry : tf_idf2.entrySet()) {
                    length2 += Math.pow(entry.getValue(), 2);
                }
                double temp = Math.sqrt(length1) * Math.sqrt(length2);
                sim = temp == 0 ? 0 : sim / temp;
                sim_matrix_subject.get(i).set(j, sim);

                tf_idf1 = tf_idf_content_list.get(i);
                tf_idf2 = tf_idf_content_list.get(j);
                terms1 = tf_idf1.keySet();
                terms2 = tf_idf2.keySet();
                common_terms = new HashSet<>();
                common_terms.addAll(terms1);
                common_terms.retainAll(terms2);

                sim = 0;
                for (String term : common_terms) {
                    sim += tf_idf1.get(term) * tf_idf2.get(term);
                }
                length1 = 0;
                length2 = 0;
                for (Entry<String, Double> entry : tf_idf1.entrySet()) {
                    length1 += Math.pow(entry.getValue(), 2);
                }
                for (Entry<String, Double> entry : tf_idf2.entrySet()) {
                    length2 += Math.pow(entry.getValue(), 2);
                }
                temp = Math.sqrt(length1) * Math.sqrt(length2);
                sim = temp == 0 ? 0 : sim / temp;
                sim_matrix_content.get(i).set(j, sim);

                tf_idf1 = tf_idf_participant_list.get(i);
                tf_idf2 = tf_idf_participant_list.get(j);
                terms1 = tf_idf1.keySet();
                terms2 = tf_idf2.keySet();
                common_terms = new HashSet<>();
                common_terms.addAll(terms1);
                common_terms.retainAll(terms2);

                sim = 0;
                for (String term : common_terms) {
                    sim += tf_idf1.get(term) * tf_idf2.get(term);
                }
                length1 = 0;
                length2 = 0;
                for (Entry<String, Double> entry : tf_idf1.entrySet()) {
                    length1 += Math.pow(entry.getValue(), 2);
                }
                for (Entry<String, Double> entry : tf_idf2.entrySet()) {
                    length2 += Math.pow(entry.getValue(), 2);
                }
                temp = Math.sqrt(length1) * Math.sqrt(length2);
                sim = temp == 0 ? 0 : sim / temp;
                sim_matrix_participant.get(i).set(j, sim);
            }
        }
    }

    /**
     * @author LIUJiayang
     * @date Thu 25 May 2017
     * @explain export similarity to .arff with compatible format of Weka
     */
    public void exportArff(ArrayList<String> folderList) throws IOException {
        File projectDirectory = new File("");
        String resultPath = projectDirectory.getCanonicalPath() + "/result";
        File resultFolder = new File(resultPath);
        // while, a bug is here within emptyFolder(): when "/result" is empty, there will be NullPointerException at resultFolder.listFiles().length
        // this will never be noticed when old ".arff" exists during project testing
        emptyFolder(resultFolder);

        String fieldName = "";
        ArrayList<ArrayList<Double>> sim_matrix_field = new ArrayList<>();
        for (int field = 0; field < 3; field++) {
            switch (field) {
                case 0:
                    fieldName = "subject";
                    sim_matrix_field = sim_matrix_subject;
                    break;
                case 1:
                    fieldName = "participant";
                    sim_matrix_field = sim_matrix_participant;
                    break;
                case 2:
                    fieldName = "content";
                    sim_matrix_field = sim_matrix_content;
                    break;
                default:
                    break;
            }
            File file = new File(resultPath + "/fw_" + fieldName + ".arff");  // save in result folder gaining the aid of its refresh mode
            // create it if file doesn't exists
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            /* relation area */
            bw.write("@relation sim_" + fieldName + "\n\n");
            /* attribute area */
            for (int i = 0; i < emailNumber; i++) {
                bw.write("@attribute &E_" + i + " numeric\n");
            }
            bw.write("@attribute Folder {");
            for (int i = 0; i < folderList.size(); i++) {
                bw.write(folderList.get(i) + ",");
            }
            bw.write("}\n\n");
            /* data area */
            bw.write("@data\n");
            for (int i = 0; i < emailNumber; i++) {         // line
                for (int j = 0; j < emailNumber; j++) {     // column
                    bw.write(sim_matrix_field.get(i).get(j) + ",");
                }
                bw.write(emailList.get(i).folderName + "\n");
            }
            bw.close();
        }
        System.out.println("Export to .arff done!");
    }

    /**
     * @author LIUJiayang
     * @date Wed 24 May 2017
     * @explain empty folder before result created, otherwise the buffer for each doc will accumulate while project testing
     */
    private void emptyFolder(File file) {
        if (file.isFile() || file.listFiles().length == 0) {    // single file or empty folder
            file.delete();
        } else {                                                // folder
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                emptyFolder(files[i]);
                files[i].delete();
            }
        }
    }
}
