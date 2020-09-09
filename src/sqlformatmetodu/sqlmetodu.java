package sqlformatmetodu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Stack;

import sqlformatmetodu.sqlbelirtec;


public class sqlmetodu {
	private final sqlparser fParser = new sqlparser();

	private sqlrule fRule = null;

	private Stack<Boolean> functionBracket = new Stack<Boolean>();

	public sqlmetodu(final sqlrule argRule) {
		fRule = argRule;
	}

	public String format(final String argSql) throws sqlistisna {
		functionBracket.clear();
		try {
			boolean isSqlEndsWithNewLine = false;
			if (argSql.endsWith("\n")) {
				isSqlEndsWithNewLine = true;
			}

			List<sqlbelirtec> list = fParser.parse(argSql);

			list = format(list);

			String after = "";
			for (int index = 0; index < list.size(); index++) {
				sqlbelirtec token = list.get(index);
				after += token.getString();
			}

			if (isSqlEndsWithNewLine) {
				after += "\n";
			}

			return after;
		} catch (Exception ex) {
			final sqlistisna sqlException = new sqlistisna(ex.toString());
			sqlException.initCause(ex);
			throw sqlException;
		}
	}

	private List<sqlbelirtec> format(final List<sqlbelirtec> argList) {

		sqlbelirtec token = argList.get(0);
		if (token.getType() == sqltokensabitleri.SPACE) {
			argList.remove(0);
		}

		token = argList.get(argList.size() - 1);
		if (token.getType() == sqltokensabitleri.SPACE) {
			argList.remove(argList.size() - 1);
		}

		for (int index = 0; index < argList.size(); index++) {
			token = argList.get(index);
			if (token.getType() == sqltokensabitleri.KEYWORD) {
				switch (fRule.keyword) {
				case sqlrule.KEYWORD_NONE:
					break;
				case sqlrule.KEYWORD_UPPER_CASE:
					token.setString(token.getString().toUpperCase());
					break;
				case sqlrule.KEYWORD_LOWER_CASE:
					token.setString(token.getString().toLowerCase());
					break;
				}
			}
		}

		for (int index = argList.size() - 1; index >= 1; index--) {
			token = argList.get(index);
			sqlbelirtec prevToken = argList.get(index - 1);
			if (token.getType() == sqltokensabitleri.SPACE && (prevToken.getType() == sqltokensabitleri.SYMBOL
					|| prevToken.getType() == sqltokensabitleri.COMMENT)) {
				argList.remove(index);
			} else if ((token.getType() == sqltokensabitleri.SYMBOL || token.getType() == sqltokensabitleri.COMMENT)
					&& prevToken.getType() == sqltokensabitleri.SPACE) {
				argList.remove(index - 1);
			} else if (token.getType() == sqltokensabitleri.SPACE) {
				token.setString(" ");
			}
		}

		for (int index = 0; index < argList.size() - 2; index++) {
			sqlbelirtec t0 = argList.get(index);
			sqlbelirtec t1 = argList.get(index + 1);
			sqlbelirtec t2 = argList.get(index + 2);

			if (t0.getType() == sqltokensabitleri.KEYWORD && t1.getType() == sqltokensabitleri.SPACE
					&& t2.getType() == sqltokensabitleri.KEYWORD) {
				if (((t0.getString().equalsIgnoreCase("ORDER") || t0.getString().equalsIgnoreCase("GROUP"))
						&& t2.getString().equalsIgnoreCase("BY"))) {
					t0.setString(t0.getString() + " " + t2.getString());
					argList.remove(index + 1);
					argList.remove(index + 1);
				}

				if ((t0.getString().equalsIgnoreCase("INNER") || t0.getString().equalsIgnoreCase("LEFT")
						|| t0.getString().equalsIgnoreCase("RIGHT")) && t2.getString().equalsIgnoreCase("JOIN")) {
					t0.setString(t0.getString() + " " + t2.getString());
					argList.remove(index + 1);
					argList.remove(index + 1);
				}

			}

			if (t0.getString().equals("(") && t1.getString().equals("+") && t2.getString().equals(")")) {
				t0.setString("(+)");
				argList.remove(index + 1);
				argList.remove(index + 1);
			}

		}

		for (int index = 0; index < argList.size() - 4; index++) {
			sqlbelirtec t0 = argList.get(index);
			sqlbelirtec t1 = argList.get(index + 1);
			sqlbelirtec t2 = argList.get(index + 2);
			sqlbelirtec t3 = argList.get(index + 3);
			sqlbelirtec t4 = argList.get(index + 4);

			if (t0.getType() == sqltokensabitleri.KEYWORD && t1.getType() == sqltokensabitleri.SPACE
					&& t2.getType() == sqltokensabitleri.KEYWORD && t3.getType() == sqltokensabitleri.SPACE
					&& t4.getType() == sqltokensabitleri.KEYWORD) {
				if ((t0.getString().equalsIgnoreCase("LEFT") || t0.getString().equalsIgnoreCase("RIGHT"))
						&& t2.getString().equalsIgnoreCase("OUTER") && t4.getString().equalsIgnoreCase("JOIN")) {
					t0.setString(t0.getString() + " " + t2.getString() + " " + t4.getString());
					argList.remove(index + 1);
					argList.remove(index + 1);
					argList.remove(index + 1);
					argList.remove(index + 1);
				}
			}
		}

		int indent = 0;

		final Stack<Integer> bracketIndent = new Stack<Integer>();
		sqlbelirtec prev = new sqlbelirtec(sqltokensabitleri.SPACE, " ");
		boolean encounterBetween = false;
		for (int index = 0; index < argList.size(); index++) {
			token = argList.get(index);
			if (token.getType() == sqltokensabitleri.SYMBOL) {

				if (token.getString().equals("(")) {
					functionBracket.push(fRule.isFunction(prev.getString()) ? Boolean.TRUE : Boolean.FALSE);
					bracketIndent.push(new Integer(indent));
					indent++;
					index += insertReturnAndIndent(argList, index + 1, indent);
				}

				else if (token.getString().equals(")")) {
					indent = bracketIndent.pop().intValue();
					index += insertReturnAndIndent(argList, index, indent);
					functionBracket.pop();
				}

				else if (token.getString().equals(",")) {
					index += insertReturnAndIndent(argList, index, indent);
				} else if (token.getString().equals(";")) {
					indent = 0;
					index += insertReturnAndIndent(argList, index, indent);
				}
			} else if (token.getType() == sqltokensabitleri.KEYWORD) {

				if (token.getString().equalsIgnoreCase("DELETE") || token.getString().equalsIgnoreCase("SELECT")
						|| token.getString().equalsIgnoreCase("UPDATE")) {
					indent += 2;
					index += insertReturnAndIndent(argList, index + 1, indent);
				}

				if (token.getString().equalsIgnoreCase("INSERT") || token.getString().equalsIgnoreCase("INTO")
						|| token.getString().equalsIgnoreCase("CREATE") || token.getString().equalsIgnoreCase("DROP")
						|| token.getString().equalsIgnoreCase("TRUNCATE") || token.getString().equalsIgnoreCase("TABLE")
						|| token.getString().equalsIgnoreCase("CASE")) {
					indent++;
					index += insertReturnAndIndent(argList, index + 1, indent);
				}

				if (token.getString().equalsIgnoreCase("FROM") || token.getString().equalsIgnoreCase("WHERE")
						|| token.getString().equalsIgnoreCase("SET") || token.getString().equalsIgnoreCase("ORDER BY")
						|| token.getString().equalsIgnoreCase("GROUP BY")
						|| token.getString().equalsIgnoreCase("HAVING")) {
					index += insertReturnAndIndent(argList, index, indent - 1);
					index += insertReturnAndIndent(argList, index + 1, indent);
				}

				if (token.getString().equalsIgnoreCase("INNER JOIN") || token.getString().equalsIgnoreCase("LEFT JOIN")
						|| token.getString().equalsIgnoreCase("RIGHT JOIN")
						|| token.getString().equalsIgnoreCase("LEFT OUTER JOIN")
						|| token.getString().equalsIgnoreCase("RIGHT OUTER JOIN")) {
					index += insertReturnAndIndent(argList, index, indent - 1);
				}

				if (token.getString().equalsIgnoreCase("VALUES")) {
					indent--;
					index += insertReturnAndIndent(argList, index, indent);
				}

				if (token.getString().equalsIgnoreCase("END")) {
					indent--;
					index += insertReturnAndIndent(argList, index, indent);
				}

				if (token.getString().equalsIgnoreCase("OR") || token.getString().equalsIgnoreCase("THEN")
						|| token.getString().equalsIgnoreCase("ELSE")) {
					index += insertReturnAndIndent(argList, index, indent);
				}

				if (token.getString().equalsIgnoreCase("USING")) {
					index += insertReturnAndIndent(argList, index, indent + 1);
				}
				if (token.getString().equalsIgnoreCase("ON")) {
					index += insertReturnAndIndent(argList, index, indent);
				}

				if (token.getString().equalsIgnoreCase("UNION") || token.getString().equalsIgnoreCase("INTERSECT")
						|| token.getString().equalsIgnoreCase("EXCEPT")) {
					indent -= 2;
					index += insertReturnAndIndent(argList, index, indent);
					index += insertReturnAndIndent(argList, index + 1, indent);
				}
				if (token.getString().equalsIgnoreCase("BETWEEN")) {
					encounterBetween = true;
				}
				if (token.getString().equalsIgnoreCase("AND")) {

					if (!encounterBetween) {
						index += insertReturnAndIndent(argList, index, indent);
					}
					encounterBetween = false;
				}
			} else if (token.getType() == sqltokensabitleri.COMMENT) {
				if (token.getString().startsWith("/*")) {

					index += insertReturnAndIndent(argList, index + 1, indent);
				}
			}
			prev = token;
		}

		for (int index = argList.size() - 1; index >= 4; index--) {
			if (index >= argList.size()) {
				continue;
			}

			sqlbelirtec t0 = argList.get(index);
			sqlbelirtec t1 = argList.get(index - 1);
			sqlbelirtec t2 = argList.get(index - 2);
			sqlbelirtec t3 = argList.get(index - 3);
			sqlbelirtec t4 = argList.get(index - 4);

			if (t4.getString().equalsIgnoreCase("(") && t3.getString().trim().equalsIgnoreCase("")
					&& t1.getString().trim().equalsIgnoreCase("") && t0.getString().equalsIgnoreCase(")")) {
				t4.setString(t4.getString() + t2.getString() + t0.getString());
				argList.remove(index);
				argList.remove(index - 1);
				argList.remove(index - 2);
				argList.remove(index - 3);
			}
		}

		for (int index = 1; index < argList.size(); index++) {
			prev = argList.get(index - 1);
			token = argList.get(index);

			if (prev.getType() != sqltokensabitleri.SPACE && token.getType() != sqltokensabitleri.SPACE) {

				if (prev.getString().equals(",")) {
					continue;
				}

				if (fRule.isFunction(prev.getString()) && token.getString().equals("(")) {
					continue;
				}
				argList.add(index, new sqlbelirtec(sqltokensabitleri.SPACE, " "));
			}
		}

		return argList;
	}

	private int insertReturnAndIndent(final List<sqlbelirtec> argList, final int argIndex, final int argIndent) {

		if (functionBracket.contains(Boolean.TRUE))
			return 0;
		try {

			String s = "\n";

			final sqlbelirtec prevToken = argList.get(argIndex - 1);
			if (prevToken.getType() == sqltokensabitleri.COMMENT && prevToken.getString().startsWith("--")) {
				s = "";
			}

			for (int index = 0; index < argIndent; index++) {
				s += fRule.indentString;
			}

			sqlbelirtec token = argList.get(argIndex);
			if (token.getType() == sqltokensabitleri.SPACE) {
				token.setString(s);
				return 0;
			}

			token = argList.get(argIndex - 1);
			if (token.getType() == sqltokensabitleri.SPACE) {
				token.setString(s);
				return 0;
			}

			argList.add(argIndex, new sqlbelirtec(sqltokensabitleri.SPACE, s));
			return 1;
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
	}
	

	public static void main(final String[] args) throws Exception {

		final sqlrule rule = new sqlrule();
		rule.keyword = sqlrule.KEYWORD_UPPER_CASE;
		rule.indentString = "    ";
		final String[] SqlFuncs = {

				// getNumericFunctions
				"ABS", "ACOS", "ASIN", "ATAN", "ATAN2", "BIT_COUNT", "CEILING", "COS", "COT", "DEGREES", "EXP", "FLOOR",
				"LOG", "LOG10", "MAX", "MIN", "MOD", "PI", "POW", "POWER", "RADIANS", "RAND", "ROUND", "TAN",
				"TRUNCATE",

				// getStringFunctions
				"ASCII", "BIN", "BIT_LENGTH", "CHAR", "CHARACTER_LENGTH", "CHAR_LENGTH", "CONCAT", "CONCAT_WS", "CONV",
				"ELT", "EXPORT_SET", "FIELD", "FIND_IN_SET", "HEX,INSERT", "INSTR", "LCASE", "LEFT", "LENGTH",
				"LOAD_FILE", "LOCATE", "LOCATE", "LOWER", "LPAD", "LTRIM", "MAKE_SET", "MATCH", "MID", "OCT",
				"OCTET_LENGTH", "ORD", "POSITION", "QUOTE", "REPEAT", "REPLACE", "REVERSE", "RIGHT", "RPAD", "RTRIM",
				"SOUNDEX", "SPACE", "STRCMP", "SUBSTRING", "SUBSTRING", "SUBSTRING", "SUBSTRING", "SUBSTRING_INDEX",
				"TRIM", "UCASE", "UPPER",

				// getSystemFunctions
				"DATABASE", "USER", "SYSTEM_USER", "SESSION_USER", "PASSWORD", "ENCRYPT", "LAST_INSERT_ID", "VERSION",

				// getTimeDateFunctions
				"DAYOFWEEK", "WEEKDAY", "DAYOFMONTH", "DAYOFYEAR", "MONTH", "DAYNAME", "MONTHNAME", "QUARTER", "WEEK",
				"YEAR", "HOUR", "MINUTE", "SECOND", "PERIOD_ADD", "PERIOD_DIFF", "TO_DAYS", "FROM_DAYS", "DATE_FORMAT",
				"TIME_FORMAT", "CURDATE", "CURRENT_DATE", "CURTIME", "CURRENT_TIME", "NOW", "SYSDATE",
				"CURRENT_TIMESTAMP", "UNIX_TIMESTAMP", "FROM_UNIXTIME", "SEC_TO_TIME", "TIME_TO_SEC" };

		rule.setFunctionNames(SqlFuncs);
		final sqlmetodu formatter = new sqlmetodu(rule);
		
		
		final File[] files = new File("C:\\Users\\fatma.rumeysa.orhan\\eclipse-workspace\\ffg").listFiles();

		for (int i = 0; i < files.length; i++) {
			System.out.println("" + files[i].getName());

			final BufferedReader reader = new BufferedReader(new FileReader(files[i]));
			String before = "";
			while (reader.ready()) {
				String line = reader.readLine();
				if (line == null)
					break;

				before += line + "\n";
			}
			reader.close();

			System.out.println("[before]\n" + before);
			String after = formatter.format(before);
			System.out.println("[after]\n" + after);
		
	}
	}}
    