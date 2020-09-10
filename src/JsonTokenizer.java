import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yulei0
 * @date 2020/9/7
 */
public class JsonTokenizer {

    private List<Token> tokenSequence;

    private CharReader charReader;


    public JsonTokenizer(CharReader charReader) {
        this.charReader = charReader;
        tokenSequence = new ArrayList<>();
    }

    public void tokenize() throws IOException {
        while(charReader.hasNext()) {
            char ch = charReader.next();
            TokenType tokenType = TokenType.of(ch);
            switch (tokenType){
                case WHITE_SPACE:
                    // 空白字符忽略
                    break;
                case START_OBJECT:
                case END_OBJECT:
                case START_ARRAY:
                case END_ARRAY:
                case SEP_COMMA:
                case SEP_COLON:
                    tokenSequence.add(new Token(tokenType, String.valueOf(ch)));
                    break;
                case NULL:
                    readNull();
                    break;
                case BOOLEAN:
                    readBoolean(ch);
                    break;
                case STRING:
                    readString();
                    break;
                case NUMBER:
                    readNumber(ch);
                default:
                    throw new JsonException();
            }
        }
        // 結束
        if (charReader.isEof()) {
            tokenSequence.add(Token.endDoc());
        }
    }

    private void readNumber(char ch) throws IOException {
        //TODO
        throw new UnsupportedOperationException();
/*        if (ch == '-') {
            char c = charReader.next();
            if (c == '0') {

            }
        } else if (ch == '0') {

        } else {

        }*/
    }

    private void readBoolean(char ch) throws IOException {
        if (ch =='f') {
            if(charReader.next() == 'a') {
                if (charReader.next() == 'l') {
                    if (charReader.next() == 's') {
                        if (charReader.next() == 'e') {
                            tokenSequence.add(new Token(TokenType.BOOLEAN, "false"));
                            return;
                        }
                    }
                }
            }
        } else if (ch == 't') {
            if(charReader.next() == 'r') {
                if (charReader.next() == 'u') {
                    if (charReader.next() == 'e') {
                        tokenSequence.add(new Token(TokenType.BOOLEAN, "true"));
                        return;
                    }
                }
            }
        }
        throw new JsonException();
    }

    private void readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (charReader.hasNext()) {
            char ch = charReader.next();
            if (ch == '\\') {
                sb.append("\\");
                ch = charReader.next();
                if (!isEscape(ch)) {
                    throw new JsonException();
                }
                sb.append(ch);
                if (ch == 'u') {
                    for (int i = 0; i < 4; i++) {
                        ch = charReader.next();
                        if (isHex(ch)) {
                            sb.append(ch);
                        } else {
                            throw new JsonException();
                        }
                    }
                }
            } else if (ch == '"') {// 結束
                tokenSequence.add(new Token(TokenType.STRING,sb.toString()));
                return;
            } else if (ch == '\r' || ch == '\n') {
                throw new JsonException();
            } else {
                sb.append(ch);
            }
        }
        throw new JsonException();
    }

    private boolean isHex(char ch) {
        return ((ch >= '0' && ch <= '9') || ('a' <= ch && ch <= 'f')
                || ('A' <= ch && ch <= 'F'));
    }

    private boolean isEscape(char ch) {
        return (ch == '"' || ch == '\\' || ch == 'u' || ch == 'r'
                || ch == 'n' || ch == 'b' || ch == 't' || ch == 'f');
    }

    private void readNull() throws IOException {
        if(charReader.next() == 'u') {
            if (charReader.next() == 'l') {
                if (charReader.next() == 'l') {
                    tokenSequence.add(new Token(TokenType.NULL));
                    return;
                }
            }
        }
        throw new JsonException();
    }

    public List<Token> sequence(){
        return new ArrayList<>(tokenSequence);
    }


    static class Token {

        TokenType tokenType;

        private String val;

        public Token(TokenType tokenType) {
            this(tokenType, null);
        }

        public Token(TokenType tokenType, String val) {
            Objects.requireNonNull(tokenType);
            this.tokenType = tokenType;
            this.val = val;
        }

        public static Token endDoc() {
            return new Token(TokenType.END_DOCUMENT);
        }

        public TokenType getTokenType() {
            return tokenType;
        }

        public String getVal() {
            return val;
        }
    }


    enum TokenType {
        START_OBJECT('{'),
        END_OBJECT('}'),
        START_ARRAY('['),
        END_ARRAY(']'),
        NULL('n'),
        NUMBER(new char[]{'-','0','1','2','3','4','5','6','7','8','9'}),
        STRING('"'),
        BOOLEAN(new char[]{'t', 'f'}),
        SEP_COLON(':'),
        SEP_COMMA(','),
        END_DOCUMENT(),
        WHITE_SPACE(' '),
        ;
        // 起始字符
        char[] starts;

        TokenType() {
            starts = new char[0];
        }

        TokenType(char c) {
            this.starts = new char[]{c};
        }
        TokenType(char[] chs) {
            this.starts = chs;
        }


        boolean isMatch(char ch) {
            for(char c : starts) {
                if (c == ch)
                    return true;
            }
            return false;
        }



        public static TokenType of(char ch) {
            TokenType[] values = values();
            for(TokenType tokenType : values) {
                if (tokenType.isMatch(ch)) {
                    return tokenType;
                }
            }
            throw new JsonException();
        }
    }

}
