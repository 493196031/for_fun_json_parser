import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;


/**
 * @author yulei0
 * @date 2020/9/7
 */
public class JsonLexicalAnalyzer {

    private JsonTokenizer jsonTokenizer;

    private Iterator<JsonTokenizer.Token> sequence;

    public JsonLexicalAnalyzer(JsonTokenizer jsonTokenizer) {
        this.jsonTokenizer = jsonTokenizer;
    }

    public Object parse() throws IOException {

        jsonTokenizer.tokenize();

        sequence = jsonTokenizer.sequence().iterator();

        JsonTokenizer.Token token = sequence.next();
        if (token.tokenType == JsonTokenizer.TokenType.START_OBJECT) {
            return parseObject();
        }  if (token.tokenType == JsonTokenizer.TokenType.START_ARRAY) {
            //TODO
            throw new JsonException();
        }
        throw new JsonException();
    }

    private HashMap<String,Object> parseObject() {
        // 期望下個token字符類型
        JsonTokenizer.TokenType[] except = {
                JsonTokenizer.TokenType.END_OBJECT,
                JsonTokenizer.TokenType.STRING};
        HashMap<String,Object> obj = new HashMap<>();
        JsonTokenizer.Token prevToken = null;
        String key = null;
        while (sequence.hasNext()) {
            JsonTokenizer.Token token = sequence.next();
            JsonTokenizer.TokenType tokenType = token.tokenType;
            switch (tokenType) {
                case START_OBJECT:
                    ensureExcept(tokenType, except);
                    obj.put(token.getVal(), parseObject());
                    except = new JsonTokenizer.TokenType[]{
                            JsonTokenizer.TokenType.END_OBJECT,
                            JsonTokenizer.TokenType.SEP_COMMA};
                    break;
                case END_OBJECT:
                    ensureExcept(tokenType, except);
                    return obj;
                case SEP_COLON:
                    ensureExcept(tokenType, except);
                    except = new JsonTokenizer.TokenType[]{
                            JsonTokenizer.TokenType.START_OBJECT,
                            JsonTokenizer.TokenType.START_ARRAY,
                            JsonTokenizer.TokenType.NULL,
                            JsonTokenizer.TokenType.BOOLEAN,
                            JsonTokenizer.TokenType.NUMBER,
                            JsonTokenizer.TokenType.STRING
                    };
                    break;
                case SEP_COMMA:
                    ensureExcept(tokenType, except);
                    except = new JsonTokenizer.TokenType[]{
                            JsonTokenizer.TokenType.STRING
                    };
                    break;
                case START_ARRAY:
                    //TODO
                    throw new JsonException();
                case STRING:
                    ensureExcept(tokenType, except);
                    if (prevToken != null && prevToken.tokenType ==
                            JsonTokenizer.TokenType.SEP_COLON) {
                        obj.put(key, token.getVal());
                        except = new JsonTokenizer.TokenType[]{
                                JsonTokenizer.TokenType.SEP_COMMA,
                                JsonTokenizer.TokenType.END_OBJECT
                        };
                    } else {
                        key = token.getVal();
                        except = new JsonTokenizer.TokenType[]{
                                JsonTokenizer.TokenType.SEP_COLON
                        };
                    }
                    break;
                case NUMBER:
                    //TODO
                    throw new JsonException();
                case BOOLEAN:
                    ensureExcept(tokenType, except);
                    obj.put(key, Boolean.valueOf(token.getVal()));
                    except = new JsonTokenizer.TokenType[]{
                            JsonTokenizer.TokenType.SEP_COMMA,
                            JsonTokenizer.TokenType.END_OBJECT
                    };
                    break;
                case NULL:
                    ensureExcept(tokenType, except);
                    obj.put(key, null);
                    except = new JsonTokenizer.TokenType[]{
                            JsonTokenizer.TokenType.SEP_COMMA,
                            JsonTokenizer.TokenType.END_OBJECT
                    };
                    break;
                case END_DOCUMENT:
                    ensureExcept(tokenType, except);
                    break;
            }
            prevToken = token;
        }

        throw new JsonException();
    }

    private void ensureExcept(
            JsonTokenizer.TokenType tokenType,
            JsonTokenizer.TokenType[] except) {
        for(JsonTokenizer.TokenType et : except) {
            if (et == tokenType) {
                return;
            }
        }
        throw new JsonException();
    }

    public static void main(String[] args) throws IOException {
        byte[] bytes = "{\"test\":\"sdfsd\",\"ts2\":null,\"t\":false}".getBytes();
        CharReader charReader = new CharReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        Object parse = new JsonLexicalAnalyzer(new JsonTokenizer(charReader)).parse();
        System.out.println(parse);
    }


}
