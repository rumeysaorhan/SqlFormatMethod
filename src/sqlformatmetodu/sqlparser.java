package sqlformatmetodu;

import java.util.ArrayList;
import java.util.List;

import sqlformatmetodu.sqlbelirtec;



public class sqlparser {

  
    private String fBefore;

   
    private char fChar;

    
    private int fPos;

  
    private static final String[] twoCharacterSymbol = { "<>", "<=", ">=", "||" };

  
   
    public static boolean isSpace(final char argChar) {
       
        return argChar == ' ' || argChar == '\t' || argChar == '\n' || argChar == '\r' || argChar == 65535;
    }

  
    public static boolean isLetter(final char argChar) {
        
        if (isSpace(argChar)) {
            return false;
        }
        if (isDigit(argChar)) {
            return false;
        }
        if (isSymbol(argChar)) {
            return false;
        }
        return true;
    }

   
    public static boolean isDigit(final char argChar) {
        return '0' <= argChar && argChar <= '9';
    }

   
    public static boolean isSymbol(final char argChar) {
        switch (argChar) {
        case '"': 
        case '?':
        case '%': 
        case '&': 
        case '(': 
        case ')': 
        case '|': 
        case '*': 
        case '+': 
        case ',': 
        case '-': 
        case '.': 
        case '/': 
        case ':': 
        case ';': 
        case '<': 
        case '=':
        case '>': 
        case '\'':

           
            return true;
        default:
            return false;
        }
    }

   
    sqlbelirtec nextToken() {
    	
        int start_pos = fPos;
        if (fPos >= fBefore.length()) {
            fPos++;
            return new sqlbelirtec(sqltokensabitleri.END, "", start_pos);
        }

        fChar = fBefore.charAt(fPos);

        if (isSpace(fChar)) {
            String workString = "";
            for (;;) {
                workString += fChar;
                fChar = fBefore.charAt(fPos);
                if (!isSpace(fChar)) {
                    return new sqlbelirtec(sqltokensabitleri.SPACE,workString, start_pos);
                }
                fPos++;
                if (fPos >= fBefore.length()) {
                    return new sqlbelirtec(sqltokensabitleri.SPACE, workString, start_pos);
                }
            }
        }  
        
            else if (isDigit(fChar)) {
            String s = "";
            
            while (isDigit(fChar) || fChar == '.') {
               
                s += fChar;
                fPos++;


                if (fPos >= fBefore.length()) {
                   
                    break;
                }

                fChar = fBefore.charAt(fPos);
            }
            return new sqlbelirtec(sqltokensabitleri.VALUE, s,start_pos);
        } else if (isLetter(fChar)) {
            String s = "";
            
            while (isLetter(fChar) || isDigit(fChar) || fChar == '.') {
                s += fChar;
                fPos++;
                if (fPos >= fBefore.length()) {
                    break;
                }

                fChar = fBefore.charAt(fPos);
            }
            for (int i = 0; i < sqlsabitleri.SQL_RESERVED_WORDS.length; i++) {
                if (s.compareToIgnoreCase(sqlsabitleri.SQL_RESERVED_WORDS[i]) == 0) {
                    return new sqlbelirtec(sqltokensabitleri.KEYWORD,s, start_pos);
                }
            }
            return new sqlbelirtec(sqltokensabitleri.NAME, s,start_pos);
        }
        
        else if (fChar == '-') {
            fPos++;
            char ch2 = fBefore.charAt(fPos);
            
            if (ch2 != '-') {
                return new sqlbelirtec(sqltokensabitleri.SYMBOL, "-",start_pos);
            }
            fPos++;
            String s = "--";
            for (;;) {
                fChar = fBefore.charAt(fPos);
                s += fChar;
                fPos++;
                if (fChar == '\n' || fPos >= fBefore.length()) {
                    return new sqlbelirtec(sqltokensabitleri.COMMENT,s, start_pos);
                }
            }
        }
       
        else if (fChar == '/') {
            fPos++;
            char ch2 = fBefore.charAt(fPos);
           
            if (ch2 != '*') {
                return new sqlbelirtec(sqltokensabitleri.SYMBOL, "/", start_pos);
            }

            String s = "/*";
            fPos++;
            int ch0 ;
            for (;;) {
                ch0 = fChar;
                fChar = fBefore.charAt(fPos);
                s += fChar;
                fPos++;
                if (ch0 == '*' && fChar == '/') {
                    return new sqlbelirtec(sqltokensabitleri.COMMENT,s, start_pos);
                }
            }
        } else if (fChar == '\'') {
            fPos++;
            String s = "'";
            for (;;) {
                fChar = fBefore.charAt(fPos);
                s += fChar;
                fPos++;
                if (fChar == '\'') {
                    return new sqlbelirtec(sqltokensabitleri.VALUE, s,start_pos);
                }
            }
        } else if (fChar == '\"') {
            fPos++;
            String s = "\"";
            for (;;) {
                fChar = fBefore.charAt(fPos);
                s += fChar;
                fPos++;
                if (fChar == '\"') {
                    return new sqlbelirtec(sqltokensabitleri.NAME, s, start_pos);
                }
            }
        }

        else if (isSymbol(fChar)) {
            
            String s = "" + fChar;
            fPos++;
            if (fPos >= fBefore.length()) {
                return new sqlbelirtec(sqltokensabitleri.SYMBOL, s,start_pos);
            }
            
            char ch2 = fBefore.charAt(fPos);
            for (int i = 0; i < twoCharacterSymbol.length; i++) {
                if (twoCharacterSymbol[i].charAt(0) == fChar && twoCharacterSymbol[i].charAt(1) == ch2) {
                    fPos++;
                    s += ch2;
                    break;
                }
            }
            return new sqlbelirtec(sqltokensabitleri.SYMBOL, s,start_pos);
        } else {
            fPos++;
            return new sqlbelirtec(sqltokensabitleri.UNKNOWN, "" + fChar, start_pos);
        }
    }

  
    public List<sqlbelirtec> parse(final String argSql) {
        fPos = 0;
        fBefore = argSql;

        final List<sqlbelirtec> list = new ArrayList<sqlbelirtec>();
        for (;;) {
            final sqlbelirtec token = nextToken();
            if (token.getType() == sqltokensabitleri.END) {
                break;
            }

            list.add(token);
        }
        return list;
    }
   
    
    
}