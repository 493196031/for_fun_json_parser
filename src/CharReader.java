import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author yulei0
 * @date 2020/9/7
 */
public class CharReader {

    private static final int MAX_BUFFER_SIZE = 1024;

    private Reader reader;

    private char[] buf;

    private int pos;

    private int n;

    public CharReader(Reader reader) {
        this.reader = reader;
        buf = new char[MAX_BUFFER_SIZE];
        pos = n = 0;
    }

    public boolean hasNext() throws IOException {
        if (isEof()) {
            return false;
        }
        if (pos >= n) {
            flipBuffer();
        }
        return (pos < n);
    }

    public boolean isEof() {
        return n == -1;
    }

    public char next() throws IOException {
        if (!hasNext()) {
            throw new EOFException();
        }
        return buf[pos++];
    }

    private void flipBuffer() throws IOException {
        n = reader.read(buf);
        pos = 0;
    }


}
