grammar vgdl;

tokens {INDENT, DEDENT}



@parser::members {
 //This set holds the valid sprite tokens
  private java.util.Set<String> sprites = new java.util.HashSet<String>();
  
}
@lexer::members {  
  // A queue where extra tokens are pushed on (see the NEWLINE lexer rule).
  private java.util.LinkedList<Token> tokens = new java.util.LinkedList<>();
  // The stack that keeps track of the indentation level.
  java.util.Stack<Integer> indents = new java.util.Stack<>();

  // The most recently produced token.
  private Token lastToken = null;
  @Override
  public void emit(Token t) {
    super.setToken(t);
    tokens.offer(t);
  }

  @Override
  public Token nextToken() {
    // Check if the end-of-file is ahead and there are still some DEDENTS expected.
    if (_input.LA(1) == EOF && !this.indents.isEmpty()) {
      // Remove any trailing EOF tokens from our buffer.
      for (int i = tokens.size() - 1; i >= 0; i--) {
        if (tokens.get(i).getType() == EOF) {
          tokens.remove(i);
        }
      }

      // First emit an extra line break that serves as the end of the statement.
      this.emit(commonToken(vgdlParser.NEWLINE, "\n"));

      // Now emit as much DEDENT tokens as needed.
      while (!indents.isEmpty()) {
        this.emit(createDedent());
        indents.pop();
      }

      // Put the EOF back on the token stream.
      this.emit(commonToken(vgdlParser.EOF, "<EOF>"));
    }

    Token next = super.nextToken();

    if (next.getChannel() == Token.DEFAULT_CHANNEL) {
      // Keep track of the last token on the default channel.
      this.lastToken = next;
    }

    return tokens.isEmpty() ? next : tokens.poll();
  }

  private Token createDedent() {
    CommonToken dedent = commonToken(vgdlParser.DEDENT, "");
    dedent.setLine(this.lastToken.getLine());
    return dedent;
  }

  private CommonToken commonToken(int type, String text) {
    int stop = this.getCharIndex() - 1;
    int start = text.isEmpty() ? stop : stop - text.length() + 1;
    return new CommonToken(this._tokenFactorySourcePair, type, DEFAULT_TOKEN_CHANNEL, start, stop);
  }

  // Calculates the indentation of the provided spaces, taking the
  // following rules into account:
  //
  // "Tabs are replaced (from left to right) by one to eight spaces
  //  such that the total number of characters up to and including
  //  the replacement is a multiple of eight [...]"
  //
  //  -- https://docs.python.org/3.1/reference/lexical_analysis.html#indentation
  static int getIndentationCount(String spaces) {
    int count = 0;
    for (char ch : spaces.toCharArray()) {
      switch (ch) {
        case '\t':
          count += 8 - (count % 8);
          break;
        default:
          // A normal space char.
          count++;
      }
    }

    return count;
  }

  boolean atStartOfInput() {
    return super.getCharPositionInLine() == 0 && super.getLine() == 1;
  }
}  

game
 : game_class NEWLINE
    INDENT sprite_set DEDENT*
    INDENT* level_mappings DEDENT*
    INDENT* interaction_set DEDENT*
    INDENT* termination_set DEDENT*
;

game_class
 : name=Identifier 
 options=option*
 ;



sprite_set
locals [java.util.Stack<String> lastSpriteClass = new java.util.Stack();]
 :  SPRITE_SET NEWLINE INDENT
 	sprite+		
 ;
 
sprite returns [String parentClass]
	@init{
		String parentClass;
	}
  :
  name=Identifier '>'  
  ( 
  	{$sprite_set::lastSpriteClass.isEmpty()}? sprite_class=Identifier options=option*  {$parentClass=$sprite_class.getText();}
  | 
  	{!$sprite_set::lastSpriteClass.isEmpty()}? options=option* {$parentClass=$sprite_set::lastSpriteClass.peek();}  	
  ) 
  {sprites.add($name.text);}
  NEWLINE 
  ((INDENT {$sprite_set::lastSpriteClass.push($name.text);})*
  |
  (DEDENT {$sprite_set::lastSpriteClass.pop();})*
  )
 ;

level_mappings
	: 
	LEVEL_MAPPING NEWLINE INDENT
	mappings=level_mapping+ 
	DEDENT
	;
 
level_mapping
 : 
 	symbol=level_symbol '>' sprite_class=known_sprite_name+ NEWLINE 
 ;
 
 level_symbol
 	:
 	(Character | SpecialCharacter | ASSIGN) 
 ;
 
known_sprite_name
 : 
 Identifier
;
 
interaction_set
 : 
 INTERACTION_SET NEWLINE INDENT
 interactions=interaction+
 DEDENT
 ;
 

 interaction
 	: 
	actors=known_sprite_name+
 	'>' 
 	action=Identifier 
 	options=option* NEWLINE 
 	;

termination_set
 :
 	TERMINATION_SET NEWLINE INDENT
 	terminations=termination+
 	DEDENT
 ;

termination
	: termination_class=Identifier options=option* NEWLINE
	;

option
 : option_key ASSIGN option_value

 ;
 
 option_key
 	: Identifier
 	;
option_value
	:
	Word|Identifier|SpecialCharacter|Character
	;




/*
 * lexer rules
 */
ASSIGN : '=';
TRUE : 'true';
FALSE : 'false';
SPRITE_SET : 'SpriteSet';
TERMINATION_SET : 'TerminationSet';
INTERACTION_SET : 'InteractionSet';
LEVEL_MAPPING : 'LevelMapping';





Character
	: Letter
	;    

Identifier
    :   Character LetterOrDigitOrSpecialChar*
    ;
SpecialCharacter
	: LetterOrDigitOrCommaOrSpecialChar     
	;
Word
	: LetterOrDigitOrCommaOrSpecialChar+ 
	;




NEWLINE
 	: 
 	( {atStartOfInput()}?   SPACES
   | ( '\r'? '\n' | '\r' ) SPACES?
   )
   {
     String newLine = getText().replaceAll("[^\r\n]+", "");
     String spaces = getText().replaceAll("[\r\n]+", "");
     int next = _input.LA(1);
     if (next == '\r' || next == '\n') {
       // If we're on a blank line, ignore all indents, 
       // dedents and line breaks.
       skip();
     }
     else {
       emit(commonToken(NEWLINE, newLine));
       int indent = getIndentationCount(spaces);
       int previous = indents.isEmpty() ? 0 : indents.peek();
       if (indent == previous) {
         // skip indents of the same size as the present indent-size
         skip();
       }
       else if (indent > previous) {
         indents.push(indent);
         emit(commonToken(vgdlParser.INDENT, spaces));
       }
       else {
         // Possibly emit more than 1 DEDENT token.
         while(!indents.isEmpty() && indents.peek() > indent) {
           this.emit(createDedent());
           indents.pop();
         }
       }
     }
   }
;



WS 
	: ( SPACES | COMMENT ) -> skip
;




fragment SPACES
 : [ \t]+
;


fragment
Letter
    :   [a-zA-Z]    
    ;

fragment
LetterOrDigit
    :   [0-9] | Letter
    ; 
fragment
LetterOrDigitOrSpecialChar
 :
 [_.-] | LetterOrDigit
;
fragment LetterOrDigitOrCommaOrSpecialChar
	: ',' | LetterOrDigitOrSpecialChar
	; 
	
fragment COMMENT
 : '#' ~[\r\n]*
;	