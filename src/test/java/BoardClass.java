import org.json.simple.parser.JSONParser;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;

public class BoardClass extends GeneralClass {

    String boardUrl;
    String newBoardName = "Board Test";
    String updatedName = "Updated Board Test";
    private String boardID;
    private String firstListID;
    private String secondListID;
    private String firstListFirstCardID;
    private String secondListFirstCardID;
    JSONArray jsonList = new JSONArray();
    JSONObject jsonObject = new JSONObject();
    String filePathBoard = "C:\\Users\\ASUS\\IdeaProjects\\TrelloApiProject\\src\\main\\resources\\board.json";
    String filePathList = "C:\\Users\\ASUS\\IdeaProjects\\TrelloApiProject\\src\\main\\resources\\lists.json";
    String filePathCard = "C:\\Users\\ASUS\\IdeaProjects\\TrelloApiProject\\src\\main\\resources\\card.json";

    //Methods for writing and reading files

    public void writeToFile(String filePath) {
        String fileName = filePath;
        jsonList.add(jsonObject);

        try (FileWriter file = new FileWriter(fileName)) {

            file.write(jsonList.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromFile(String filePath, String key){
        String fileName = filePath;
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(fileName)){

            JSONArray jsonArray = (JSONArray) parser.parse(reader);
            JSONObject jsonObjectRead = (JSONObject) jsonArray.get(0);
            System.out.println(jsonObjectRead.get(key));

            if (key.equals("boardID")){
                boardID = (String) jsonObjectRead.get(key);
            }else if (key.equals("firstListID")){
                firstListID = (String) jsonObjectRead.get(key);
            }else if (key.equals("secondListID")) {
                secondListID = (String) jsonObjectRead.get(key);
            }else if (key.equals("firstListFirstCardID")) {
                firstListFirstCardID = (String) jsonObjectRead.get(key);
            }else if (key.equals("secondListFirstCardID")){
                secondListFirstCardID = (String) jsonObjectRead.get(key);
            }else{
                System.err.println("Illegal input for key!");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ParseException e){
            e.printStackTrace();
        }
    }

    @Test
    public void createBoard(){

        boardUrl = "/1/boards/?key=" + trelloKey + "&token=" + trelloToken +  "&name=" + newBoardName;

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.headers("Content-type","application/json");

        Response response = request.post(boardUrl);

        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.toString());

        boardID = response.jsonPath().get("id");
        System.out.println("Created board id is: " + boardID);
        jsonObject.put("boardID", boardID);
        writeToFile(filePathBoard);
    }

    @Test
    public void getTwoListNameFromBoard(){
        readFromFile(filePathBoard,"boardID");
        boardUrl = "/1/boards/"+ boardID + "/lists?key=" + trelloKey + "&token=" + trelloToken;

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type","application/json");

        Response response = request.get(boardUrl);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.asString());

        ArrayList nameOfArray = response.jsonPath().get("name");

        System.out.println("First List Name: " + nameOfArray.get(0));
        System.out.println("Second List Name: " + nameOfArray.get(1));

        ArrayList idOfArray = response.jsonPath().get("id");

        firstListID = (String) idOfArray.get(0);
        System.out.println("First List ID: " + firstListID);

        secondListID = (String) idOfArray.get(1);
        System.out.println("Second List ID: " + secondListID);

        jsonObject.put("firstListID", firstListID);
        jsonObject.put("secondListID", secondListID);
        writeToFile(filePathList);
    }

    //adding cards for two list in one test
    @Test
    public void addFirstCardToFirstList(){
        String cardName = "Card 1";
        readFromFile(filePathList,"firstListID");
        boardUrl = "/1/cards?key=" + trelloKey + "&token=" + trelloToken + "&idList=" + firstListID + "&name=" + cardName;

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");

        Response response = request.post(boardUrl);

        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.asString());

        Assert.assertEquals(cardName, response.jsonPath().get("name"));
        System.out.println("Created Card Name: " + response.jsonPath().get("name"));
        System.out.println(response.jsonPath().get("id").toString());
        firstListFirstCardID = response.jsonPath().get("id").toString();
        jsonObject.put("firstListFirstCardID", firstListFirstCardID);
        addFirstCardToSecondList();
        writeToFile(filePathCard);
    }

    public void addFirstCardToSecondList(){
        String cardName = "Card 1";
        readFromFile(filePathList, "secondListID");
        boardUrl = "/1/cards?key=" + trelloKey + "&token=" + trelloToken + "&idList=" + secondListID + "&name=" + cardName;

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");

        Response response = request.post(boardUrl);

        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.asString());

        Assert.assertEquals(cardName, response.jsonPath().get("name"));
        System.out.println("Created Card Name is: " + response.jsonPath().get("name"));
        secondListFirstCardID = response.jsonPath().get("id");
        jsonObject.put("secondListFirstCardID", secondListFirstCardID);
    }

    @Test
    public void updateCardNameRandomly(){
        String updateCardName = "updated card name";
        String[] str = {"firstListFirstCardID","secondListFirstCardID"};
        Random random = new Random();
        int index = random.nextInt(str.length);
        readFromFile(filePathCard,str[index]);
        if (str[index].equals("firstListFirstCardID"))
        {
            boardUrl = "/1/cards/" + firstListFirstCardID + "?key=" + trelloKey + "&token=" + trelloToken + "&name=" + updateCardName;
        }
        else
        {
            boardUrl =  "/1/cards/" + secondListFirstCardID + "?key=" + trelloKey + "&token=" + trelloToken + "&name=" + updateCardName;
        }

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");

        Response response = request.put(boardUrl);

        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.toString());

        Assert.assertEquals(updateCardName, response.jsonPath().get("name"));
        System.out.println("Updated Card Name: " + response.jsonPath().get("name"));
    }

    @Test
    public void deleteAddedFirstCard(){

        readFromFile(filePathCard,"firstListFirstCardID");
        boardUrl = "/1/cards/" + firstListFirstCardID + "?key=" + trelloKey + "&token=" + trelloToken;

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");

        Response response = request.delete(boardUrl);

        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.asString());
    }

    @Test
    public void deleteAddedSecondCard(){
        readFromFile(filePathCard, "secondListFirstCardID");
        boardUrl = "/1/cards/" + secondListFirstCardID + "?key=" + trelloKey + "&token=" + trelloToken;

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");

        Response response = request.delete(boardUrl);

        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.asString());
    }

    @Test
    public void deleteCreatedBoard(){
        readFromFile(filePathBoard, "boardID");
        boardUrl = "/1/boards/" + boardID + "?key=" + trelloKey + "&token=" + trelloToken;

        RestAssured.baseURI = trelloAPIURL;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");

        Response response = request.delete(boardUrl);

        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.asString());
    }
}
