grammar SkelligTestValueGrammar;

// Starting rule
start: logicalExpression EOF | expression EOF;

logicalExpression
    : logicalExpression AND logicalExpression   # andExpr
    | logicalExpression OR logicalExpression    # orExpr
    | '(' logicalExpression ')'                 # parenthesesLogicalExpr
    | NOT (BOOL | functionCall | propertyExpression | callChain | '(' logicalExpression ')')              # notExpr
    | comparison                                # comparisonExpr
    ;

comparison: expression comparator expression;

expression
    : expression MULT expression                # multiplicationExpr
    | expression DIV expression                 # divisionExpr
    | expression ADD expression                 # additionExpr
    | expression SUB expression                 # subtractionExpr
    | arrayValueAccessor                        # arrayValueAccessorExp
    | functionCall                              # functionCallExp
    | propertyExpression                        # propertyExpr
    | '(' expression ')'                        # parenthesesExpr
    | STRING                                    # stringExpr
    | number                                    # numberExpr
    | ID                                        # idExpr
    | BOOL                                      # boolExpr
    | callChain                                 # callChainExp
    ;

callChain: (functionBase | propertyExpression) (DOT functionBase)*;

functionBase
    : functionCall
    | arrayValueAccessor
    | ID
    | STRING;

functionCall: ID '(' (arg (COMMA arg)*)? (')'|'):');

arg: expression | logicalExpression | comparison | lambdaExpression | array;

lambdaExpression: ID LAMBDA (logicalExpression | expression);

propertyExpression: '${' propertyKey (COMMA expression)? '}';

propertyKey
    : propertyKey ADD propertyKey      # additionPropertyKeyExpr
    | propertyExpression               # innerPropertyExpr
    | ID                               # idPropertyKeyExpr
    | STRING                           # stringPropertyKeyExpr
    | INT                              # numberPropertyKeyExpr
    ;

arrayValueAccessor: ID '[' INT ']';

array: '[' expression (COMMA expression)* ']';

number: FLOAT | INT;

comparator: LESSER | GREATER | LESSER_EQUAL | GREATER_EQUAL | EQUAL | NOT_EQUAL;

// Tokens
LESSER: '<';
GREATER: '>';
LESSER_EQUAL: '<=';
GREATER_EQUAL: '>=';
EQUAL: '==';
NOT_EQUAL: '!=';
FLOAT: [0-9]+ '.' [0-9]+;
INT: [0-9]+;
MULT: '*';
DIV: '/';
ADD: '+';
SUB: '-';
AND: '&&';
OR: '||';
NOT: '!';
COMMA: ',';
DOT: '.';
LAMBDA: '->';
BOOL: 'true'|'false';  // Boolean values
ID: [a-zA-Z0-9_:;$]+;  // Identifiers supporting dashes and numbers
STRING: '"' (~["] | '\\"')* '"';  // Strings enclosed in double quotes, excluding newline characters
WS: [ \t\n\r]+ -> skip;  // Whitespace
