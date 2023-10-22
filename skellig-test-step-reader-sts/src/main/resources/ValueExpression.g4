grammar ValueExpression;

// Starting rule
start: logicalExpression EOF | expression EOF;

logicalExpression
    : logicalExpression AND logicalExpression   # andExpr
    | logicalExpression OR logicalExpression    # orExpr
    | NOT logicalExpression                     # notExpr
    | '(' logicalExpression ')'                 # parenthesesLogicalExpr
    | comparison                                # comparisonExpr
    ;

comparison: expression comparator expression;

expression
    : expression MULT expression                # multiplicationExpr
    | expression DIV expression                 # divisionExpr
    | expression ADD expression                 # additionExpr
    | expression SUB expression                 # subtractionExpr
    | propertyInvocation                        # propertyExpr
    | functionInvocation                        # functionExpr
    | '(' expression ')'                        # parenthesesExpr
    | STRING                                    # stringExpr
    | number                                    # numberExpr
    | ID                                        # idExpr
    ;

functionInvocation: functionBase ('.' functionBase)*;

functionBase
    : functionCall
    | ID;

functionCall: ID '(' (arg (',' arg)*)? ')';

arg: expression | logicalExpression | comparison | lambdaExpression;

lambdaExpression: ID '->' logicalExpression | expression;

propertyInvocation: propertyExpression ('.' functionBase)*;

propertyExpression: '${' ID (':' expression)? '}';

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
ID: [a-zA-Z][a-zA-Z0-9-]*;  // Identifiers supporting dashes and numbers
STRING: '"' (~["\r\n])* '"';  // Strings enclosed in double quotes, excluding newline characters
WS: [ \t\n\r]+ -> skip;  // Whitespace
