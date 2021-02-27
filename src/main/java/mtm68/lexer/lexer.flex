package mtm68.lexer;
import mtm68.util.StringUtils;

%%

%public
%class Lexer
%type Token
%function nextToken

%unicode
%pack
%line
%column
%cup

%{

// TODO: Better lex errors (maybe error token?)

    public enum TokenType {
		// Reserved
		USE,
		IF,
		WHILE,
		ELSE,
		RETURN,
		LENGTH,
		INT_T("int"),
		BOOL_T("bool"),
		TRUE,
		FALSE,

		// Constants
		ID,
		INT("integer"),
		CHARACTER,
		STRING,
		
		// Punctuation
		DOT("."),
		OPEN_SQUARE("["),
		CLOSE_SQUARE("]"),
		OPEN_PAREN("("),
		CLOSE_PAREN(")"),
		OPEN_CURLY("{"),
		CLOSE_CURLY("}"),
		EXCLAMATION("!"),
		COLON(":"),
		SEMICOLON(";"),
		COMMA(","),
        EQ("="),
        UNDERSCORE("_"),
		
		// Operators
		ADD("+"),
		SUB("-"),
		MULT("*"),
		DIV("/"),
		MOD("%"),
		HIGH_MULT("*>>"),
		LT("<"),
		LEQ("<="),
		GT(">"),
		GEQ(">="),
		EQEQ("=="),
		NEQ("!="),
		AND("&"),
		OR("|"),

		// Error
		ERROR;

        private String pp;

        private TokenType(String pp) {
            this.pp = pp;
        }

        private TokenType() {
            this.pp = name().toLowerCase();
        }
		
		@Override
		public String toString() {
			return pp;
		}
    }
    public static class Token {
		private TokenType type;
		private Object attribute;
		private int lineNum;
		private int column;
		public Token(TokenType tt, Object attr, int lineNum, int column) {
			type = tt; attribute = attr;
			this.lineNum = lineNum;
			this.column = column;
		}
		public String toString() {
			return lineNum + ":" + column + " " + type + prettyPrintAttribute();
		}

        private String prettyPrintAttribute(){
            if(attribute == null) return "";
            
            if(type == TokenType.ERROR) return ":" + attribute;
            
            if(attribute instanceof String) {
                String str = (String) attribute;
                return " " + str.replaceAll("[\n]", "\\\\n");
            } else {
            	return " " + attribute;
			}
        }
		
		public int getLineNum() {
			return lineNum;
		}

		public int getColumn() {
			return column;
		}
		
		public TokenType getType() {
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
			result = prime * result + column;
			result = prime * result + lineNum;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (attribute == null) {
				if (other.attribute != null)
					return false;
			} else if (!attribute.equals(other.attribute))
				return false;
			if (column != other.column)
				return false;
			if (lineNum != other.lineNum)
				return false;
			if (type != other.type)
				return false;
			return true;
		}
    }

    StringBuffer string = new StringBuffer();
    int stringCol, stringLine;

	public int yyline() { return yyline + 1; }

	public int yycolumn() { return yycolumn + 1; }

	public Token createToken(TokenType tt, Object attribute, int line, int col) {
		return new Token(tt, attribute, line, col);	
	}
	
	public Token createToken(TokenType tt, Object attribute) {
		return createToken(tt, attribute, yyline(), yycolumn());
	}

	public Token createToken(TokenType tt) {
		return createToken(tt, null);	
	}
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
Comment = "//" {InputCharacter}* {LineTerminator}?
Whitespace = [ \t\f\r\n]
Letter = [a-zA-Z]
Digit = [0-9]
HexDigit = [0-9A-Fa-f]
Hex = "\\x" {HexDigit}{1,4}
Identifier = {Letter}({Digit}|{Letter}|_|"\'")*
Integer = 0 | [1-9]{Digit}* 
IntegerLiteral = "\'" {InputCharacter} "\'" 
HexLiteral = "\'" {Hex} "\'" 

%state STRING

%%


<YYINITIAL> {
    {Whitespace}  { /* ignore */ }
    {Comment}     { /* ignore */ }

    "use"         { return createToken(TokenType.USE); }
    "if"		  { return createToken(TokenType.IF); }
    "while"  	  { return createToken(TokenType.WHILE); }
    "else"		  { return createToken(TokenType.ELSE); }
    "return" 	  { return createToken(TokenType.RETURN); }
    "length"      { return createToken(TokenType.LENGTH); }
    "int"         { return createToken(TokenType.INT_T); }
    "bool"        { return createToken(TokenType.BOOL_T); }
    "true"        { return createToken(TokenType.TRUE); }
    "false"       { return createToken(TokenType.FALSE); }

    "."           { return createToken(TokenType.DOT); }
    "["           { return createToken(TokenType.OPEN_SQUARE); }
    "]"           { return createToken(TokenType.CLOSE_SQUARE); }
    "("           { return createToken(TokenType.OPEN_PAREN); }
    ")"           { return createToken(TokenType.CLOSE_PAREN); }
    "{"           { return createToken(TokenType.OPEN_CURLY); }
    "}"           { return createToken(TokenType.CLOSE_CURLY); }
    "!"           { return createToken(TokenType.EXCLAMATION); }
    ":"           { return createToken(TokenType.COLON); }
    ";"           { return createToken(TokenType.SEMICOLON); }
    ","           { return createToken(TokenType.COMMA); }
    "="           { return createToken(TokenType.EQ); }
    "_"           { return createToken(TokenType.UNDERSCORE); }

    "+"           { return createToken(TokenType.ADD); }
    "-"           { return createToken(TokenType.SUB); }
    "*"           { return createToken(TokenType.MULT); }
    "/"           { return createToken(TokenType.DIV); }
    "%"           { return createToken(TokenType.MOD); }
    "*>>"         { return createToken(TokenType.HIGH_MULT); }
    "<"           { return createToken(TokenType.LT); }
    "<="          { return createToken(TokenType.LEQ); }
    ">"           { return createToken(TokenType.GT); }
    ">="          { return createToken(TokenType.GEQ); }
    "=="          { return createToken(TokenType.EQEQ); }
    "!="          { return createToken(TokenType.NEQ); }
    "&"           { return createToken(TokenType.AND); }
    "|"           { return createToken(TokenType.OR); }

    {Identifier}  { return createToken(TokenType.ID, yytext()); }
    {Integer}     { return createToken(TokenType.INT,
                     Long.parseLong(yytext())); }  // TODO: What to do if integer constant is too big?
    {IntegerLiteral}     { return createToken(TokenType.CHARACTER, yytext().charAt(1)); }  
    {HexLiteral}     { return createToken(TokenType.CHARACTER, StringUtils.convertHexToChar(yytext().replace("'", ""))); }  

    \"            { string.setLength(0); stringLine = yyline(); stringCol = yycolumn(); yybegin(STRING); }
    
    [^]			 { return createToken(TokenType.ERROR, "Invalid character " + yytext()); }
}

<STRING> {

    \"                             { yybegin(YYINITIAL); 
                                    return createToken(TokenType.STRING, string.toString(), stringLine, stringCol); }

    [^\n\'\\\"]+                   { string.append(yytext());}

    \\n                            { string.append('\n'); }
    \\\'                           { string.append('\''); }
    \\                             { string.append('\\'); }
    {Hex}                          { char hexChar = StringUtils.convertHexToChar(yytext());
                                       string.append(hexChar); }
}
