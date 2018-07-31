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
public class StringMatcher {

    private static final int START = 0;
    private static final int ESCAPE = 1;
    private static final int END = 2;

    private static final char ESCAPE_CHAR = '\\';
    private static final char ANY_CHAR = '*';

 
    private enum TYPE {
        start, mid, end, any, exact
    };

    private final TYPE type;
    private final String start;
    private final String end;

   boolean match(String with) {
        switch (type) {
            case any:
                return true;
            case exact:
                return with.equals(start);
            case start:
                return with.startsWith(start);
            case end:
                return with.endsWith(end);
             case mid:
                return (with.startsWith(start) && with.endsWith(end)); 
        }
        return false;
    }

    public StringMatcher(String matchValue) {
        if (matchValue == null) {
            this.type = TYPE.any;
            this.start = null;
            this.end = null;
        } else {
            int vaLen = matchValue.length();
            if (matchValue.trim().length() == 0) {
                this.type = TYPE.exact;
                this.start = matchValue;
                this.end = null;
            } else {
                StringBuilder st = new StringBuilder();
                StringBuilder en = new StringBuilder();
                boolean split = false;
                int flag = START;
                for (char c : matchValue.toCharArray()) {
                    switch (flag) {
                        case START:
                            if (c == ESCAPE_CHAR) {
                                flag = ESCAPE;
                            } else {
                                if (c == ANY_CHAR) {
                                    flag = END;
                                    split = true;
                                } else {
                                    st.append(c);
                                }
                            }
                            break;
                        case ESCAPE:
                            vaLen--;
                            if (c != ESCAPE_CHAR) {
                                flag = START;
                            }
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
                    this.start = st.toString();
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
                            this.type = (split?TYPE.start:TYPE.exact);
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

    @Override
    public String toString() {
        return type.name() + "[" + (start + '|' + end) + ']';
    }

}
