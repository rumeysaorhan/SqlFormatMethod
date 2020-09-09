package sqlformatmetodu;

import sqlformatmetodu.sqlsoyutbelirtec;

public class sqlbelirtec extends sqlsoyutbelirtec {
	
    
    public sqlbelirtec(final int argType, final String argString,final int argPos) {
    	
        setType(argType);
        setString(argString);
        setPos(argPos);
    }

  
    public sqlbelirtec(final int argType, final String argString) {
        this(argType, argString, -1);
    }
}