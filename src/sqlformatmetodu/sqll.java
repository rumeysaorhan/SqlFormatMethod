package sqlformatmetodu;

public class sqll {
public static String replace(final String argTargetString,final String argFrom, final String argTo) {
		
        String newStr = "";
        int lastpos = 0;

        for (;;) {
            final int pos = argTargetString.indexOf(argFrom, lastpos);
            if (pos == -1) {
                break;
            }

            newStr += argTargetString.substring(lastpos, pos);
            newStr += argTo;
            lastpos = pos + argFrom.length();
        }

        return newStr + argTargetString.substring(lastpos);
    }

}
