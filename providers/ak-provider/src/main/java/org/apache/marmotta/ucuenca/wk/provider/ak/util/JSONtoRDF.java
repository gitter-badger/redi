package org.apache.marmotta.ucuenca.wk.provider.ak.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

@Deprecated
public class JSONtoRDF {

    private final JsonArray data;
    private final Model model;
    private final Map<String, String> schema;
    private final ValueFactory factory = ValueFactoryImpl.getInstance();
    private final static String ACADEMICS_URL = "http://localhost:8080/resource/MA/publication/";

    public JSONtoRDF(Map<String, String> schema, JsonArray data, Model model) {
        this.schema = schema;
        this.data = data;
        this.model = model;
    }

    /**
     * @see Parce object json format to RDF
     * @throws Exception
     */
    public void parse() throws Exception {
        Integer i = 0;
        while (i < data.size()) {
            JsonObject json = data.get(i).getAsJsonObject();
            this.mappingProcess(ACADEMICS_URL + Long.toString(json.get("id").getAsLong()), json);
            i++;
        }
    }

    /**
     * @see build publication in RDF format
     * @param resource
     * @param json
     */
    private void mappingProcess(String resource, JsonObject json) {
        for (String key : schema.keySet()) {
            if (key.matches("^entity::type$")) {
                model.add(factory.createStatement(factory.createURI(resource),
                        RDF.TYPE, factory.createURI(schema.get("entity::type"))));
                continue;
            }
            //build properties
            Matcher m = Pattern.compile("^(entity::property:)(.*)$").matcher(key);
            if (m.find()) {
                getGenericAttributes(m, key, json, resource);
            }
            //Build list properties
            Matcher mUri = Pattern.compile("^(entity::list:)(.*)$").matcher(key);
            if (mUri.find()) {
                getGenericAttributesBucle(mUri, key, json, resource);
            }
            //Build manual properties
            getAttributesPartOne(key, json, resource);
            getAttributesPartTwo(key, json, resource);

        }

    }

    /**
     * @see MEthod to build manual properties
     * @param key
     * @param json
     * @param resource
     */
    public void getAttributesPartOne(String key, JsonObject json, String resource) {
        JsonArray aux = new JsonArray();

        switch (key) {
            case "entity::property:text":
                aux = json.get("authors").getAsJsonArray();
                String text = "";
                for (int iterator = 0; iterator < aux.size(); iterator++) {
                    JsonElement element = aux.get(iterator);

                    text = text + (element.getAsJsonObject().get("afiliation") != null ? element.getAsJsonObject().get("afiliation").getAsString() : "") + " -";
                }
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(text)));
                break;

            case "entity::property:fullversionurl":
                aux = json.get("sources").getAsJsonArray();
                for (JsonElement version : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI(version.getAsString())));

                }
                break;
            case "entity::property:references":
                aux = json.get("referencesId").getAsJsonArray();
                for (JsonElement references : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI(ACADEMICS_URL + references.getAsString() + "/")));

                }
                break;
            default:
                break;

        }
    }

    /**
     * @see MEthod to build manual properties
     * @param key
     * @param json
     * @param resource
     */
    public void getAttributesPartTwo(String key, JsonObject json, String resource) {
        JsonArray aux = new JsonArray();

        switch (key) {
            case "entity::property:uri":
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(ACADEMICS_URL + json.get("id").getAsLong())));
                break;

            case "entity::property:creator":
                aux = json.get("authors").getAsJsonArray();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createURI(ACADEMICS_URL + aux.get(0).getAsJsonObject().get("id").getAsString())));
                //author name
                model.add(factory.createStatement(factory.createURI(ACADEMICS_URL + aux.get(0).getAsJsonObject().get("id").getAsString()),
                        factory.createURI(schema.get("entity::property:name")), factory.createLiteral(aux.get(0).getAsJsonObject().get("name").getAsString())));

                for (int iterator = 0; iterator < aux.size(); iterator++) {
                    JsonElement element = aux.get(iterator);
                    model.add(factory.createStatement(factory.createURI(ACADEMICS_URL + element.getAsJsonObject().get("id").getAsString()),
                            RDF.TYPE, factory.createURI(schema.get("entity::property::typePerson"))));
                }

                break;
            case "entity::property:contributor":
                aux = json.get("authors").getAsJsonArray();

                for (int iterator = 1; iterator < aux.size(); iterator++) {
                    JsonElement element = aux.get(iterator);
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI(ACADEMICS_URL + element.getAsJsonObject().get("id").getAsString())));
                    //author name
                    model.add(factory.createStatement(factory.createURI(ACADEMICS_URL + element.getAsJsonObject().get("id").getAsString()),
                            factory.createURI(schema.get("entity::property:name")), factory.createLiteral(element.getAsJsonObject().get("name").getAsString())));

                }
                break;
            default:
                break;

        }

    }

    /**
     * @see Method to build literal properties of publications
     * @param m
     * @param key
     * @param json
     * @param resource
     */
    public void getGenericAttributes(Matcher m, String key, JsonObject json, String resource) {
        if (json.has(m.group(2))) {
            String value = StringUtils.trimToEmpty(json.get(m.group(2)).getAsString());
            if (StringUtils.isNumeric(value)) {
                value = Integer.parseInt(value) > 0 ? value : "";
            }
            if (StringUtils.isNotBlank(value)) {
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(value)));
            }
        }
    }

    /**
     * @see Method to build properties list of publications
     * @param m
     * @param key
     * @param json
     * @param resource
     */
    public void getGenericAttributesBucle(Matcher m, String key, JsonObject json, String resource) {
        if (json.has(m.group(2))) {
            JsonArray aux = json.get(m.group(2)).getAsJsonArray();
            for (JsonElement element : aux) {
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(element.getAsString())));

            }

        }
    }
}
