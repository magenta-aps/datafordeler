package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;

/**
 * A type-specific ObjectInputStream, that should only contain objects of
 * the specific type.
 * Contains static methods for deserializing an InputStream of JSON data into
 * an ItemInputStream.
 */
public class ItemInputStream<T> extends ObjectInputStream {

    private static final Logger log = LogManager.getLogger(ItemInputStream.class.getCanonicalName());

    public ItemInputStream(InputStream in) throws IOException {
        super(in);
    }

    public T next() throws IOException {
        try {
            // Return a Checksum object from the stream; only Checksum objects
            // should be present there
            return (T) this.readObject();
        } catch (ClassNotFoundException e) {
            // The next object in the stream is of a class we can't find
            // This would mean that there's a non-Checksum object in there.
            // Ignore it and get the next one instead
            return this.next();
        } catch (EOFException e) {
            // Apparently we always get this at the end of the stream
            return null;
        }
    }

    /**
     * Parses a stream of JSON data into a stream of instances of type T
     * The json data should be in the form of a wrapped array, like '{"items":[{object1},{object2},...]}
     *
     * @param jsonStream     A stream of text data containing JSON
     * @param itemClass      The class we expect objects to parse to
     * @param objectListName The fieldname denoting the array; "items" in the example above
     * @param objectMapper   A Jackson object mapper
     * @param <T>            A JSON-deserializable class instance
     * @return
     */
    public static <T> ItemInputStream<T> parseJsonStream(InputStream jsonStream, Class<T> itemClass, final String objectListName, ObjectMapper objectMapper) {
        PipedInputStream inputStream = new PipedInputStream();
        try {
            final PipedOutputStream outputStream = new PipedOutputStream(inputStream);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JsonParser jsonParser = new JsonFactory().createParser(jsonStream);
                        JsonToken previousToken = null;

                        int level = 0;
                        int listItemLevel = -1;
                        for (JsonToken nextToken = jsonParser.nextToken(); nextToken != null && level < 1000; nextToken = jsonParser.nextToken()) {
                            if (nextToken == JsonToken.START_OBJECT) {
                                level++;
                                if (level == listItemLevel) {
                                    T item = objectMapper.readValue(jsonParser, itemClass);
                                    objectOutputStream.writeObject(item);
                                    level--;
                                }
                            } else if (nextToken == JsonToken.END_OBJECT) {
                                level--;

                            } else if (nextToken == JsonToken.START_ARRAY && previousToken == JsonToken.FIELD_NAME && objectListName.equals(jsonParser.getCurrentName())) {
                                listItemLevel = level + 1;
                            }
                            if (level == 0) {
                                break;
                            }
                            previousToken = nextToken;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            objectOutputStream.close();
                        } catch (IOException e) {
                        }
                        try {
                            jsonStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            });
            t.start();

            return new ItemInputStream<T>(new BufferedInputStream(inputStream));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parses a stream of JSON data into a stream of Object instances, their class according to the given classMap
     * The json data should be in the form of a wrapped array, like '{"items":[{"type":"foo",more object data},{"type":"bar",more object data},...]}
     *
     * @param jsonStream     A stream of text data containing JSON
     * @param classMap       A map converting type fields to classes. In the above example: {"foo":com.example.Foo, "bar":com.example.Bar}. Any objects found in the input that are not found in this map will be ignored.
     * @param objectListName The fieldname denoting the array; "items" in the example above
     * @param schemaKey      The fieldname denoting the object type; "type" in the example above
     * @param objectMapper   A Jackson object mapper
     * @param <T>            A JSON-deserializable class instance
     * @return
     */
    public static <T> ItemInputStream<T> parseJsonStream(InputStream jsonStream, Map<String, Class<? extends T>> classMap, final String objectListName, final String schemaKey, ObjectMapper objectMapper) {
        PipedInputStream inputStream = new PipedInputStream();
        try {
            final PipedOutputStream outputStream = new PipedOutputStream(inputStream);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JsonParser jsonParser = new JsonFactory().createParser(jsonStream);
                        JsonToken previousToken = null;

                        int level = 0;
                        int listItemLevel = -1;
                        for (JsonToken nextToken = jsonParser.nextToken(); nextToken != null && level < 1000; nextToken = jsonParser.nextToken()) {
                            if (nextToken == JsonToken.START_OBJECT) {
                                level++;
                                if (level == listItemLevel) {
                                    JsonNode node = objectMapper.readTree(jsonParser);
                                    level--;
                                    String type = node.get(schemaKey).asText();
                                    if (classMap.containsKey(type)) {
                                        T item = objectMapper.convertValue(node, classMap.get(type));
                                        objectOutputStream.writeObject(item);
                                    } else {
                                        log.debug("Found an item with unrecognized schemaKey (" + schemaKey + "): " + type + ". This item will be ignored");
                                    }
                                }
                            } else if (nextToken == JsonToken.END_OBJECT) {
                                level--;

                            } else if (nextToken == JsonToken.START_ARRAY && previousToken == JsonToken.FIELD_NAME && objectListName.equals(jsonParser.getCurrentName())) {
                                listItemLevel = level + 1;
                            }
                            if (level == 0) {
                                break;
                            }
                            previousToken = nextToken;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            objectOutputStream.close();
                        } catch (IOException e) {
                        }
                        try {
                            jsonStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            });
            t.start();

            return new ItemInputStream<T>(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
