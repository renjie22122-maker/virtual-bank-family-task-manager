import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

public class JsonUtil {

    /**
     * Reads a JSON file and returns a JSONArray.
     *
     * @param filename The name of the JSON file to read.
     * @return A JSONArray containing the data from the JSON file.
     */
    public static JSONArray readJsonArrayFromFile(String filename) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(filename)) {
            Object obj = parser.parse(reader);
            return (JSONArray) obj;
        } catch (FileNotFoundException e) {
            return new JSONArray();
        } catch (IOException | ParseException | ClassCastException e) {
            throw new IllegalStateException("Could not read JSON array from " + filename, e);
        }
    }

    /**
     * Writes a JSONArray to a JSON file.
     *
     * @param jsonArray The JSONArray to write.
     * @param filename  The name of the JSON file to write to.
     */
    public static void writeJsonArrayToFile(JSONArray jsonArray, String filename) {
        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Could not write JSON array to " + filename, e);
        }
    }

    /**
     * Reads a JSON file and returns a JSONObject.
     *
     * @param filename The name of the JSON file to read.
     * @return A JSONObject containing the data from the JSON file.
     */
    public static JSONObject readJsonObjectFromFile(String filename) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(filename)) {
            Object obj = parser.parse(reader);
            return (JSONObject) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return new JSONObject(); // Return an empty object if there is an error
        }
    }

    /**
     * Writes a JSONObject to a JSON file.
     *
     * @param jsonObject The JSONObject to write.
     * @param filename   The name of the JSON file to write to.
     */
    public static void writeJsonObjectToFile(JSONObject jsonObject, String filename) {
        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Backs up a JSON file by copying its contents to a backup file.
     *
     * @param sourceFilename The name of the source JSON file.
     * @param backupFilename The name of the backup JSON file.
     */
    public static void backupJsonFile(String sourceFilename, String backupFilename) {
        JSONArray jsonArray = readJsonArrayFromFile(sourceFilename);
        writeJsonArrayToFile(jsonArray, backupFilename);
    }
}
