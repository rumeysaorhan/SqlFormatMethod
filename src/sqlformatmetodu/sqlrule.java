package sqlformatmetodu;

public class sqlrule {
	
	int keyword = KEYWORD_UPPER_CASE;

    public static final int KEYWORD_NONE = 0;

    public static final int KEYWORD_UPPER_CASE = 1;

    public static final int KEYWORD_LOWER_CASE = 2;

    
    String indentString = "    ";

    
    private String[] fFunctionNames = null;

    public void setKeywordCase(int keyword) {
        this.keyword = keyword;
    }

    
    boolean isFunction(String name) {
        if (fFunctionNames == null)
            return false;
        for (int i = 0; i < fFunctionNames.length; i++) {
            if (fFunctionNames[i].equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

   
    public void setFunctionNames(String[] names) {
        fFunctionNames = names;
    }
}
