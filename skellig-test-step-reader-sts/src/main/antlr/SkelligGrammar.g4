grammar SkelligGrammar;

// Entry point
file            : testStepName* EOF;
testStepName   : NEWLINE* NAME '(' STRING ')' NEWLINE* '{' NEWLINE* (NEWLINE* pair NEWLINE*)* NEWLINE*'}' NEWLINE*;

pair : key ('=' value | map | array) ;

key : expression+;
value : expression*;
values: value | map | array;
array : NEWLINE* '[' NEWLINE* values (COMMA NEWLINE* values)* NEWLINE* ']' NEWLINE*;
map   : NEWLINE* '{' NEWLINE* (pair NEWLINE*)* '}' NEWLINE*;

expression
    : propertyExpression   # propertyExpr
    | functionExpression   # functionExpr
    | '(' expression+ ')'  # parenthesesExpr
    | VALUE_SYMBOLS        # symbols
    | LESSER_EQUAL         # lessThanEquals
    | GREATER_EQUAL        # moreThanEquals
    | EQUAL                # equals
    | NOT_EQUAL            # notEquals
    | KEY_SYMBOLS          # keySymbols
    | STRING               # stringExpr
    | ID                   # idExpr
    | NAME                 # nameValue
    | number               # numberExpr
    ;

functionExpression: ID '(' (arg (COMMA arg)*) ')';

arg: expression* | array | map;

propertyExpression: '${' expression* (COMMA expression*)? '}';

number: FLOAT | INT;


// Tokens
FLOAT: [0-9]+ '.' [0-9]+;
INT: [0-9]+;
NAME: 'name';
ID: [a-zA-Z0-9_]+;  // Identifiers supporting dashes and numbers
LESSER_EQUAL: '<=';
GREATER_EQUAL: '>=';
EQUAL: '==';
NOT_EQUAL: '!=';
COMMA: ',';
KEY_SYMBOLS: [._\-&'%$£!?`¬#~@^\\:]+;
VALUE_SYMBOLS: [><|+*/]+;
STRING: '"' (~["] | '\\"')* '"';  // Strings enclosed in double quotes, excluding newline characters
NEWLINE : '\r'? '\n' ;
COMMENT : '//' ~[\r\n]* [\n\r] -> skip;  // skip comments
WS : [ \t]+ -> skip;  // skip whitespace