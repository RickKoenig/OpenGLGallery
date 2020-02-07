package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by cyberrickers on 11/5/2016.
 */

public class Script {
    private static final String TAG = "Script";
    private ArrayList<String> data;
    private String fnamea;
    private int inputCharSize;

    private enum state {GETCHAR,SKIPWS,HASHMODE,QUOTEMODE,SLASH1,STAR1,STAR2,BAIL,BACKSLASH1,BACKSLASHQ};
    private enum chartype {CHARS,WS,CRLF,ISEOF,HASH ,QUOTE,SLASH,STAR,BACKSLASH};
    private final char EOF = 0;

    chartype getchartype(char c) {
        if (c == ' ' || c == '\t')
            return chartype.WS;
        if (c == '\n' || c == '\r')
            return chartype.CRLF;
        if (c == '#')
            return chartype.HASH;
        if (c == '\\')
            return chartype.BACKSLASH;
        if (c == '\"')
            return chartype.QUOTE;
        if (c == EOF)
            return chartype.ISEOF;
        if (c == '/')
            return chartype.SLASH;
        if (c == '*')
            return chartype.STAR;
        return chartype.CHARS;
    }

    public Script(String fname) {
        fnamea = fname;
        data = new ArrayList<>();
        String parse = Utils.getStringFromAsset(fname,true);
        if (parse == null)
            return;
        inputCharSize = parse.length();
        //data.add(parse);
        String s = "";
        int cp = 0;
        state curstate = state.GETCHAR;
        while(curstate != state.BAIL) {
            char c;
            int ret;
            if (cp < parse.length())
                c = parse.charAt(cp++);
            else
                c = EOF;
            chartype ct = getchartype(c);
            switch(curstate) {
//// state: normal get chars
                case BAIL:
                    break;
                case GETCHAR:
                    switch(ct) {
// look out for a / in /*
                        case SLASH:
                            curstate = state.SLASH1;
                            break;
// accumulate normal C8 into string
                        case STAR:
                        case CHARS:
                            s+=c;
                            break;
// start a quote
                        case QUOTE:
                            if (s.length() > 0)
                                data.add(s);
                            s = "";
                            curstate = state.QUOTEMODE;
                            break;
// found a whitespace crlf, add string to script (if necc.), look for more ws
                        case WS:
                        case CRLF:
                            if (s.length() > 0)
                                data.add(s);
                            s = "";
                            curstate = state.SKIPWS;
                            break;
// found a comment starter, add string to script (if necc.) goto hash state
                        case HASH:
                            if (s.length() > 0)
                                data.add(s);
                            s = "";
                            curstate = state.HASHMODE;
                            break;
// end of file, add string to script (if necc.), done
                        case ISEOF:
                            if (s.length() > 0)
                                data.add(s);
                            curstate = state.BAIL;
                            break;
                        case BACKSLASH:
                            curstate = state.BACKSLASH1;
                            break;
                    }
                    break;
//// state: slash looking for * in  /*
                case SLASH1:
                    switch(ct) {
// another to EOL comment
                        case SLASH:
                            if (s.length() > 0)
                                data.add(s);
                            s = "";
                            curstate = state.HASHMODE;
                            break;
// start a comment
                        case STAR:
                            curstate = state.STAR1;
                            break;
// accumulate normal C8 into string
                        case CHARS:
                            s+='/';
                            s+=c;
                            curstate = state.GETCHAR;
                            break;
// start a quote
                        case QUOTE:
                            s+='/';
                            data.add(s);
                            s = "";
                            curstate = state.QUOTEMODE;
                            break;
// found a whitespace crlf, add string to script (if necc.), look for more ws
                        case WS:
                        case CRLF:
                            s+='/';
                            data.add(s);
                            s = "";
                            curstate = state.SKIPWS;
                            break;
// found a comment starter, add string to script (if necc.) goto hash state
                        case HASH:
                            s+='/';
                            data.add(s);
                            s = "";
                            curstate = state.HASHMODE;
                            break;
// end o file, add string to script (if necc.), done
                        case ISEOF:
                            s+='/';
                            data.add(s);
                            curstate = state.BAIL;
                            break;
                        case BACKSLASH:
                            s+='/';
                            curstate = state.BACKSLASH1;
                            break;
                    }
                    break;
//// state: comment looking for * in */
                case STAR1:
                    switch(ct) {
// ignore
                        case SLASH:
                            break;
// get to star2 state
                        case STAR:
                            curstate = state.STAR2;
                            break;
// ignore
                        case CHARS:
                            break;
// found a quote, ignore
                        case QUOTE:
                            break;
// found a whitespace, ignore
                        case WS:
                            break;
// found a crlf, ignore
                        case CRLF:
                            break;
// found a comment starter, ignore
                        case HASH:
                            break;
// end o file, done
                        case ISEOF:
                            Utils.alert("1missing */ (end comment) for '" + fnamea + "'");
                            break;
                        case BACKSLASH:
                            break;
                    }
                    break;
//// state: looking for / in  */
                case STAR2:
                    switch(ct) {
// found / in */ go back to normal
                        case SLASH:
                            curstate = state.GETCHAR;
                            break;
// ignore, stay in star2 mode, (looking for / in */)
                        case STAR:
                            break;
// back to star1
                        case CHARS:
                        case QUOTE:
                        case WS:
                        case CRLF:
                        case HASH:
                            curstate = state.STAR1;
                            break;
// end o file, complain
                        case ISEOF:
                            Utils.alert("2missing */ (end comment) for '" + fnamea + "'");
                            break;
                        case BACKSLASH:
                            curstate = state.STAR1;
                            break;
                    }
                    break;
//// state: skip over whitepsace
                case SKIPWS:
                    switch(ct) {
// start accumulate chars into string again
                        case SLASH:
                            curstate = state.SLASH1;
                            break;
                        case STAR:
                        case CHARS:
                            s+=c;
                            curstate = state.GETCHAR;
                            break;
// start a quote
                        case QUOTE:
                            curstate = state.QUOTEMODE;
                            break;
// found more whitespace crlf, do nothing
                        case WS:
                        case CRLF:
                            break;
// found a comment starter, enter hasmode
                        case HASH:
                            curstate = state.HASHMODE;
                            break;
// end of file, done
                        case ISEOF:
                            curstate = state.BAIL;
                            break;
                        case BACKSLASH:
                            curstate = state.BACKSLASH1;
                            break;
                    }
                    break;
//// state: found a hash, skip over comment until crlf
                case HASHMODE:
                    switch(ct) {
// ignore
                        case SLASH:
                        case STAR:
                        case CHARS:
                            break;
// found a quote, ignore
                        case QUOTE:
                            break;
// found a whitespace, ignore
                        case WS:
                            break;
// found a crlf, look for chars again
                        case CRLF:
                            curstate = state.GETCHAR;
                            break;
// found a comment starter, ignore
                        case HASH:
                            break;
// end o file, done
                        case ISEOF:
                            curstate = state.BAIL;
                            break;
                        case BACKSLASH:
                            break;
                    }
                    break;
//// state: found a quote, find other quote
                case QUOTEMODE:
                    switch(ct) {
// accumulate normal C8 into string, stay in quotemode
                        case SLASH:
                        case STAR:
                        case CHARS:
                            s+=c;
                            break;
// end a quote, write out string to script (if necc.) and get chars again
                        case QUOTE:
//				if (s.length() > 0)
                            data.add(s);
                            s = "";
                            curstate = state.GETCHAR;
                            break;
// found crlf, ignore, but stay in quotemode
                        case CRLF:
                            break;
// found a whitespace, add ws to string
                        case WS:
// found a comment starter, since it's quoted just add to string
                        case HASH:
                            s+=c;
                            break;
// end o file, complain
                        case ISEOF:
                            Utils.alert("missing close quote for '" + fnamea + "'");
                            break;
                        case BACKSLASH:
                            curstate = state.BACKSLASHQ;
                            break;
                    }
                    break;
/// state: backslash
                case BACKSLASH1:
                    switch(ct) {
// accumulate normal C8 into string, stay in quotemode
                        case SLASH:
                        case STAR:
                        case CHARS:
                        case CRLF:
                        case WS:
                        case HASH:
                            s+='\\';
                            s+=c;
                            curstate = state.GETCHAR;
                            break;
                        case ISEOF:
                            Utils.alert("backslash at eof '" + fnamea + "'");
                            break;
                        case BACKSLASH:
                        case QUOTE:
                            s+=c;
                            curstate = state.GETCHAR;
                    }
                    break;
/// state: backslash inside quotes
                case BACKSLASHQ:
                    switch(ct) {
// accumulate normal C8 into string, stay in quotemode
                        case SLASH:
                        case STAR:
                        case CHARS:
                        case CRLF:
                        case WS:
                        case HASH:
                            s +='\\';
                            s += c;
                            curstate = state.QUOTEMODE;
                            break;
                        case ISEOF:
                            Utils.alert("backslash at eof '" + fnamea + "'");
                            break;
                        case BACKSLASH:
                        case QUOTE:
                            s += c;
                            curstate = state.QUOTEMODE;
                            break;
                    }
                    break;
            }
        }
    }



    public ArrayList<String> getData() {
        int numchars = 0;
        for (String s : data)
            numchars += s.length();
        Log.e(TAG,"Script " + fnamea + " inchars " + inputCharSize + " tokens " + data.size() + " characters " + numchars);
        return data;
    }

}
