/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

/**
 *
 * @author stuart
 */
public class MatcherString {

    private static final int START = 0;
    private static final int ESCAPE = 1;
    private static final int END = 2;

    private static char ESCAPE_CHAR = '\\';
    private static char ANY_CHAR = '*';
    private enum TYPE {
        start, mid, end, any, exact
    };

    private final TYPE type;
    private final String start;
    private final String end;

    public MatcherString(String value) {
        if (value == null) {
            this.type = TYPE.any;
            this.start = null;
            this.end = null;
        } else {
            int vaLen = value.length();
            if (value.trim().length() == 0) {
                this.type = TYPE.exact;
                this.start = value;
                this.end = null;
            } else {
                StringBuilder st = new StringBuilder();
                StringBuilder en = new StringBuilder();
                int flag = START;
                for (char c : value.toCharArray()) {
                    switch (flag) {
                        case START:
                            if (c == ESCAPE_CHAR) {
                                flag = ESCAPE;
                            } else {
                                if (c == ANY_CHAR) {
                                    flag = END;
                                } else {
                                    st.append(c);
                                }
                            }
                            break;
                        case ESCAPE:
                            vaLen--;
                            flag = START;
                            st.append(c);
                            break;
                        default:
                            en.append(c);
                    }
                }
                int stLen = st.length();
                int enLen = en.length();

                if (stLen == vaLen) {
                    this.type = TYPE.exact;
                    this.start = value;
                    this.end = null;
                } else {
                    if (stLen == 0) {
                        if (enLen == 0) {
                            this.type = TYPE.any;
                            this.start = null;
                            this.end = null;
                        } else {
                            this.type = TYPE.end;
                            this.start = null;
                            this.end = en.toString();
                        }
                    } else {
                        if (enLen == 0) {
                            this.type = TYPE.start;
                            this.start = st.toString();
                            this.end = null;
                        } else {
                            this.type = TYPE.mid;
                            this.start = st.toString();
                            this.end = en.toString();
                        }
                    }
                }
            }
        }
    }

    public static void setEscapeChar(char newChar) {
        ESCAPE_CHAR = newChar;
    }
    
    @Override
    public String toString() {
        return type.name() + "[" + (start + '|' + end) + ']';
    }

}
