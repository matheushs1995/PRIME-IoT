package module2.similarity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import module2.dao.IoTTopicsMoreContextDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Similarity {

    static String URLBase = "https://www.abbreviations.com/services/v2/";
    static String chooseAPI;

    static String uid = "uid=7342";
    static String token = "tokenid=T8omXUHa7NnjEd3p";

    static String word = "word=";
    static String format = "format=json";

    static String projectDir = "C:\\Users\\mathe\\Documents\\Dropbox\\Projetos\\PragmaticIoT-V2";

    static List<String[]> descsAndSimilarity;

    public static String getDescriptionsTermsAndSynonymsWithHTMLTag(String w) throws JSONException, IOException {

        chooseAPI = "syno.php";

        Client client = ClientBuilder.newClient();

        File file = new File(projectDir + "\\files\\" + w + ".json");

        String response;

        if (!file.exists()) {

            response = client.target(URLBase + chooseAPI + "?" + uid + "&" + token + "&" + word + w + "&" + format)
                    .request(MediaType.APPLICATION_JSON).get(String.class);

            if (!response.isBlank()) {
                FileWriter writer = new FileWriter(projectDir + "\\files\\" + w + ".json");
                writer.write(response);
                writer.close();
            }

        } else {
            Scanner reader = new Scanner(file);
            response = reader.nextLine();
        }

        JSONObject obj = new JSONObject(response);

        try {

            JSONArray result = obj.getJSONArray("result");

            String tempS = "";

            for (int i = 0; i < result.length(); ++i) {
                final JSONObject item = result.getJSONObject(i);
                tempS += "<div class='checkboxDiv input1 inputFormat2'>"
                        + "<input type='radio' name='desc' value='" + i + "' style='margin-right: 20px' id='cb" + i + "'>";

                try {
                    tempS += "<strong class='thick space1'>Term:</strong>"
                            + "<strong id='cb" + 0 + "-t'>"
                            + item.getString("term") + "</strong>;";
                } catch (Exception e) {
                }

                try {
                    tempS += "<strong class='thick space1'>Definition:</strong>"
                            + "<strong id='cb" + 0 + "-d' >"
                            + item.getString("definition") + "</strong>;";
                } catch (Exception e) {
                }

                try {
                    tempS += "<strong class='thick space1'>Synonyms:</strong>"
                            + "<strong id='cb" + 0 + "-s' >"
                            + item.getString("synonyms") + "</strong>";
                } catch (Exception e) {
                }

                tempS += "<br></div>";
            }

            tempS = "<div class='checkboxDiv input1 inputFormat2'><input type='radio' name='desc' value='od' style='margin-right: 20px' id='cbOther'><input id='inDesc' class='input11 inputFormat' placeholder='Another description...'/><br></div>"+tempS;

            return tempS;

        } catch (Exception ex) {
            return "";
        }

    }

    public static String getSimilarTopicsOne(String topic, String id, String keywords) throws FileNotFoundException, JSONException, OWLOntologyCreationException {
        File file = new File(projectDir + "\\files\\" + topic + ".json");

        String response;

        if (!file.exists()) {
            return "";
        }

        Scanner reader = new Scanner(file);
        response = reader.nextLine();

        JSONObject obj = new JSONObject(response);
        JSONArray result = obj.getJSONArray("result");
        JSONObject item = result.getJSONObject(Integer.parseInt(id));

        descsAndSimilarity = IoTTopicsMoreContextDAO.getSimilarityWithFixDefinition(topic, item.getString("term"), item.getString("definition"), item.getString("synonyms"), keywords);

        if (descsAndSimilarity.isEmpty()) {
            return "e";
        }

        if (descsAndSimilarity.get(0)[0].compareTo("ed") == 0) {
            return descsAndSimilarity.get(0)[0] + "," + IoTTopicsMoreContextDAO.getFirstAltenatveTopicOfGT(descsAndSimilarity.get(0)[1]);
        }

        quickSort(descsAndSimilarity, 0, descsAndSimilarity.size() - 1);

        String tempS = "";
        List<String> gts = new ArrayList<>();

        for (int i = 0; i < descsAndSimilarity.size(); ++i) {
            String gt = IoTTopicsMoreContextDAO.getGTofDescription(descsAndSimilarity.get(i)[0]);
            if (gt.isBlank()) {
                return "e";
            }

            if (!ifExists(gts,gt)) {

                String ats = parseListToString(IoTTopicsMoreContextDAO.getAllAlternativeTopicsOfGT(gt));
                if (ats.isBlank()) {
                    return "e";
                }

                String def = IoTTopicsMoreContextDAO.getDefinition(descsAndSimilarity.get(i)[0]);
                if (def.isBlank()) {
                    return "e";
                }

                tempS += "<div class='checkboxDiv input1 inputFormat2'>"
                        + "<input type='radio' name='gt' value='" + gt + "' style='margin-right: 20px'>"
                        + "<strong class='thick space1'>Alternative Topics:</strong>"
                        + "<strong>" + ats + "</strong>;"
                        + "<strong class='thick space1'>Definition:</strong>"
                        + "<strong>" + def + "</strong>;"
                        + "<br></div>";
                
                gts.add(gt);
                
            }
        }

        if(!tempS.isBlank()){
            tempS ="<div class='checkboxDiv input1 inputFormat2'>"
                        + "<input type='radio' name='gt' value='none' style='margin-right: 20px'>"
                        + "<strong>None</strong>"
                        + "<br></div>"
                        + tempS;
        }
        
        return tempS;
    }
    
    public static String getSimilarTopicsTwo(String topic, String desc, String keywords) throws FileNotFoundException, JSONException, OWLOntologyCreationException {
        
        descsAndSimilarity = IoTTopicsMoreContextDAO.getSimilarityWithFixDefinition(topic, desc, keywords);

        if (descsAndSimilarity.isEmpty()) {
            return "e";
        }

        if (descsAndSimilarity.get(0)[0].compareTo("ed") == 0) {
            return descsAndSimilarity.get(0)[0] + "," + descsAndSimilarity.get(0)[1];
        }

        quickSort(descsAndSimilarity, 0, descsAndSimilarity.size() - 1);

        String tempS = "";
        List<String> gts = new ArrayList<>();
        
        for (int i = 0; i < descsAndSimilarity.size(); ++i) {
            String gt = IoTTopicsMoreContextDAO.getGTofDescription(descsAndSimilarity.get(i)[0]);
            if (gt.isBlank()) {
                return "e";
            }

            if (!ifExists(gts,gt)) {

                String ats = parseListToString(IoTTopicsMoreContextDAO.getAllAlternativeTopicsOfGT(gt));
                if (ats.isBlank()) {
                    return "e";
                }

                String def = IoTTopicsMoreContextDAO.getDefinition(descsAndSimilarity.get(i)[0]);
                if (def.isBlank()) {
                    return "e";
                }

                tempS += "<div class='checkboxDiv input1 inputFormat2'>"
                        + "<input type='radio' name='gt' value='" + gt + "' style='margin-right: 20px'>"
                        + "<strong class='thick space1'>Alternative Topics:</strong>"
                        + "<strong>" + ats + "</strong>;"
                        + "<strong class='thick space1'>Definition:</strong>"
                        + "<strong>" + def + "</strong>;"
                        + "<br></div>";
                
                gts.add(gt);
                
            }
        }
        
        if(!tempS.isBlank()){
            tempS ="<div class='checkboxDiv input1 inputFormat2'>"
                        + "<input type='radio' name='gt' value='none' style='margin-right: 20px'>"
                        + "<strong>None</strong>"
                        + "<br></div>"
                        + tempS;
        }
        
        return tempS;
    }
    
    
    
    public static boolean ifExists(List<String> gts, String gt){
        for(String v: gts){
            if(v.compareTo(gt)==0){
                return true;
            }
        }
        
        return false;
    }

    public static void quickSort(List<String[]> v, int left, int right) {
        int l = left;
        int r = right;
        double pivo = Double.parseDouble(v.get((l + r) / 2)[1]);
        String[] troca;

        while (l <= r) {
            while (Double.parseDouble(v.get(l)[1]) < pivo) {
                l = l + 1;
            }
            while (Double.parseDouble(v.get(r)[1]) > pivo) {
                r = r - 1;
            }
            if (l <= r) {
                troca = v.get(l);
                v.set(l, v.get(r));
                v.set(r, troca);
                l = l + 1;
                r = r - 1;
            }
        }
        if (r > left) {
            quickSort(v, left, r);
        }

        if (l < right) {
            quickSort(v, l, right);
        }

    }

    public static String parseListToString(List<String> list) {

        if (list == null || list.isEmpty()) {
            return "";
        }

        String tempS = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            tempS += "," + list.get(i);
        }

        return tempS;

    }

}