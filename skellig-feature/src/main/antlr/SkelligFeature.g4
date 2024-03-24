grammar SkelligFeature;

// Parser rules
featureFile : feature beforeFeature? beforeTestScenario? scenario* afterFeature? afterTestScenario? EOF ;
beforeFeature  : NEWLINE+ BEFORE_FEATURE step+;
beforeTestScenario  :  NEWLINE+ BEFORE_TEST_SCENARIO step+;
afterFeature  :  NEWLINE+ AFTER_FEATURE step+;
afterTestScenario  :  NEWLINE+ AFTER_TEST_SCENARIO step+;
feature     :  tagList* FEATURE title NEWLINE*;
scenario    :  NEWLINE+ tagList* SCENARIO title step* examples*;

step      : NEWLINE+ (givenStep | whenStep | thenStep | andStep | starStep) NEWLINE*;
givenStep : GIVEN stepText parametersTable?;
whenStep  : WHEN stepText parametersTable?;
thenStep  : THEN stepText parametersTable?;
andStep   : AND stepText parametersTable?;
starStep  : STAR stepText parametersTable?;

examples        : NEWLINE+ tagList* EXAMPLES parametersTable;
parametersTable : NEWLINE parametersRow+;
parametersRow   : PIPE (TEXT+ PIPE)+ NEWLINE?;

tagList : tag+ NEWLINE+;
tag     : TAG;

title    : TEXT+;
stepText : TEXT+;

// Lexer rules
FEATURE  : 'Feature:' ;
SCENARIO : 'Scenario:' ;
GIVEN    : 'Given' ;
WHEN     : 'When' ;
THEN     : 'Then' ;
AND      : 'And' ;
STAR      : '*' ;
EXAMPLES  : 'Examples:' ;
BEFORE_FEATURE  : 'Before Feature:' ;
BEFORE_TEST_SCENARIO  : 'Before Test Scenario:' ;
AFTER_FEATURE  : 'After Feature:' ;
AFTER_TEST_SCENARIO  : 'After Test Scenario:' ;
PIPE : '|' ;
TAG  : '@' ~[\r\n\t ]+ ;

TEXT     : ~[ \t\r\n|]+ ;
NEWLINE : '\r'? '\n' ;
COMMENT : '//' ~[\r\n]* [\n\r] -> skip;  // skip comments
WS       : [ \t]+ -> skip ; // skip whitespace
