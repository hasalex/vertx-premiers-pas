package fr.sewatech.vertx;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonExample {
    public static void main(String[] args) {
        String jsonString = "{\"name\":\"Alexis\"}";

        Person person = Json.decodeValue(jsonString, Person.class);
        System.out.println(person);

        String jsonPerson = Json.encode(person);
        System.out.println(jsonPerson);


        JsonObject jsonObject = new JsonObject(jsonString);
        System.out.println("Name:" + jsonObject.getString("name"));

        jsonObject.put("Age", 42);
        System.out.println(jsonObject.encodePrettily());


        String jsonArrayString = "[\"Alexis\", \"Alice\"]";
        JsonArray jsonArray = new JsonArray(jsonArrayString);
        System.out.println("First:" + jsonArray.getString(0));

        jsonArray.add("Bob");
        System.out.println(jsonArray.encode());

    }

    static class Person {
        public String name;

        @Override
        public String toString() {
            return "Person: " + name;
        }
    }
}
