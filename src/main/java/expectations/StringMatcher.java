package com.bt.consumer.localtestserver.expectations;

public class StringMatcher {

    private static final int START = 0;
    private static final int ESCAPE = 1;
    private static final int END = 2;

    private static final char ESCAPE_CHAR = '\\';
    private static final char ANY_CHAR = '*';

    private enum TYPE {
        start, mid, end, any, exact, contains, nulll
    };

    private final TYPE type;
    private final String start;
    private final String end;

    public StringMatcher(String value) {
        if (value == null) {
            this.type = TYPE.nulll;
            this.start = null;
            this.end = null;
        } else {
            int vaLen = value.length();
            if (value.trim().length() == 0) {
                this.type = TYPE.exact;
                this.start = value;
                this.end = null;
            } else {
                if ((value.charAt(0) == ANY_CHAR) && (value.charAt(vaLen - 1) == '*') && (vaLen>1)) {
                    this.type = TYPE.contains;
                    this.start = value.substring(1, vaLen-1);
                    this.end = null;
                } else {
                    StringBuilder st = new StringBuilder();
                    StringBuilder en = new StringBuilder();
                    boolean split = false;
                    int flag = START;
                    for (char c : value.toCharArray()) {
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
                                this.type = (split ? TYPE.start : TYPE.exact);
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
    }

    public boolean match(String with) {
        switch (type) {
            case nulll:
                return with == null;
            case any:
                return true;
            case exact:
                if (with == null) {
                    return start == null;
                }
                return with.equals(start);
            case start:
                return with.startsWith(start);
            case end:
                return with.endsWith(end);
            case contains:
                return (with.contains(start));
            case mid:
                return (with.startsWith(start) && with.endsWith(end));
        }
        return false;
    }


    @Override
    public String toString() {
        return type.name() + "[" + (start + '|' + end) + ']';
    }

}
