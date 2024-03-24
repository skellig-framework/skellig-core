package org.skellig.feature.parser;// Generated from SkelligFeature.g4 by ANTLR 4.13.1

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class SkelligFeatureLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		FEATURE=1, SCENARIO=2, EXAMPLES=3, BEFORE_FEATURE=4, BEFORE_TEST_SCENARIO=5, 
		AFTER_FEATURE=6, AFTER_TEST_SCENARIO=7, PIPE=8, TAG=9, TEXT=10, NEWLINE=11, 
		COMMENT=12, WS=13;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"FEATURE", "SCENARIO", "EXAMPLES", "BEFORE_FEATURE", "BEFORE_TEST_SCENARIO", 
			"AFTER_FEATURE", "AFTER_TEST_SCENARIO", "PIPE", "TAG", "TEXT", "NEWLINE", 
			"COMMENT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'Feature:'", "'Scenario:'", "'Examples:'", "'Before Feature:'", 
			"'Before Test Scenario:'", "'After Feature:'", "'After Test Scenario:'", 
			"'|'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "FEATURE", "SCENARIO", "EXAMPLES", "BEFORE_FEATURE", "BEFORE_TEST_SCENARIO", 
			"AFTER_FEATURE", "AFTER_TEST_SCENARIO", "PIPE", "TAG", "TEXT", "NEWLINE", 
			"COMMENT", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public SkelligFeatureLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SkelligFeature.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\r\u00a8\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\b\u0001\b\u0004\b\u0087\b\b\u000b\b\f\b\u0088\u0001\t\u0004\t\u008c\b"+
		"\t\u000b\t\f\t\u008d\u0001\n\u0003\n\u0091\b\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u0099\b\u000b\n\u000b"+
		"\f\u000b\u009c\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\f\u0004\f\u00a3\b\f\u000b\f\f\f\u00a4\u0001\f\u0001\f\u0000\u0000"+
		"\r\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006"+
		"\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u0001\u0000"+
		"\u0004\u0003\u0000\t\n\r\r  \u0004\u0000\t\n\r\r  ||\u0002\u0000\n\n\r"+
		"\r\u0002\u0000\t\t  \u00ac\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003"+
		"\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007"+
		"\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001"+
		"\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000"+
		"\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000"+
		"\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000"+
		"\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0001\u001b\u0001\u0000"+
		"\u0000\u0000\u0003$\u0001\u0000\u0000\u0000\u0005.\u0001\u0000\u0000\u0000"+
		"\u00078\u0001\u0000\u0000\u0000\tH\u0001\u0000\u0000\u0000\u000b^\u0001"+
		"\u0000\u0000\u0000\rm\u0001\u0000\u0000\u0000\u000f\u0082\u0001\u0000"+
		"\u0000\u0000\u0011\u0084\u0001\u0000\u0000\u0000\u0013\u008b\u0001\u0000"+
		"\u0000\u0000\u0015\u0090\u0001\u0000\u0000\u0000\u0017\u0094\u0001\u0000"+
		"\u0000\u0000\u0019\u00a2\u0001\u0000\u0000\u0000\u001b\u001c\u0005F\u0000"+
		"\u0000\u001c\u001d\u0005e\u0000\u0000\u001d\u001e\u0005a\u0000\u0000\u001e"+
		"\u001f\u0005t\u0000\u0000\u001f \u0005u\u0000\u0000 !\u0005r\u0000\u0000"+
		"!\"\u0005e\u0000\u0000\"#\u0005:\u0000\u0000#\u0002\u0001\u0000\u0000"+
		"\u0000$%\u0005S\u0000\u0000%&\u0005c\u0000\u0000&\'\u0005e\u0000\u0000"+
		"\'(\u0005n\u0000\u0000()\u0005a\u0000\u0000)*\u0005r\u0000\u0000*+\u0005"+
		"i\u0000\u0000+,\u0005o\u0000\u0000,-\u0005:\u0000\u0000-\u0004\u0001\u0000"+
		"\u0000\u0000./\u0005E\u0000\u0000/0\u0005x\u0000\u000001\u0005a\u0000"+
		"\u000012\u0005m\u0000\u000023\u0005p\u0000\u000034\u0005l\u0000\u0000"+
		"45\u0005e\u0000\u000056\u0005s\u0000\u000067\u0005:\u0000\u00007\u0006"+
		"\u0001\u0000\u0000\u000089\u0005B\u0000\u00009:\u0005e\u0000\u0000:;\u0005"+
		"f\u0000\u0000;<\u0005o\u0000\u0000<=\u0005r\u0000\u0000=>\u0005e\u0000"+
		"\u0000>?\u0005 \u0000\u0000?@\u0005F\u0000\u0000@A\u0005e\u0000\u0000"+
		"AB\u0005a\u0000\u0000BC\u0005t\u0000\u0000CD\u0005u\u0000\u0000DE\u0005"+
		"r\u0000\u0000EF\u0005e\u0000\u0000FG\u0005:\u0000\u0000G\b\u0001\u0000"+
		"\u0000\u0000HI\u0005B\u0000\u0000IJ\u0005e\u0000\u0000JK\u0005f\u0000"+
		"\u0000KL\u0005o\u0000\u0000LM\u0005r\u0000\u0000MN\u0005e\u0000\u0000"+
		"NO\u0005 \u0000\u0000OP\u0005T\u0000\u0000PQ\u0005e\u0000\u0000QR\u0005"+
		"s\u0000\u0000RS\u0005t\u0000\u0000ST\u0005 \u0000\u0000TU\u0005S\u0000"+
		"\u0000UV\u0005c\u0000\u0000VW\u0005e\u0000\u0000WX\u0005n\u0000\u0000"+
		"XY\u0005a\u0000\u0000YZ\u0005r\u0000\u0000Z[\u0005i\u0000\u0000[\\\u0005"+
		"o\u0000\u0000\\]\u0005:\u0000\u0000]\n\u0001\u0000\u0000\u0000^_\u0005"+
		"A\u0000\u0000_`\u0005f\u0000\u0000`a\u0005t\u0000\u0000ab\u0005e\u0000"+
		"\u0000bc\u0005r\u0000\u0000cd\u0005 \u0000\u0000de\u0005F\u0000\u0000"+
		"ef\u0005e\u0000\u0000fg\u0005a\u0000\u0000gh\u0005t\u0000\u0000hi\u0005"+
		"u\u0000\u0000ij\u0005r\u0000\u0000jk\u0005e\u0000\u0000kl\u0005:\u0000"+
		"\u0000l\f\u0001\u0000\u0000\u0000mn\u0005A\u0000\u0000no\u0005f\u0000"+
		"\u0000op\u0005t\u0000\u0000pq\u0005e\u0000\u0000qr\u0005r\u0000\u0000"+
		"rs\u0005 \u0000\u0000st\u0005T\u0000\u0000tu\u0005e\u0000\u0000uv\u0005"+
		"s\u0000\u0000vw\u0005t\u0000\u0000wx\u0005 \u0000\u0000xy\u0005S\u0000"+
		"\u0000yz\u0005c\u0000\u0000z{\u0005e\u0000\u0000{|\u0005n\u0000\u0000"+
		"|}\u0005a\u0000\u0000}~\u0005r\u0000\u0000~\u007f\u0005i\u0000\u0000\u007f"+
		"\u0080\u0005o\u0000\u0000\u0080\u0081\u0005:\u0000\u0000\u0081\u000e\u0001"+
		"\u0000\u0000\u0000\u0082\u0083\u0005|\u0000\u0000\u0083\u0010\u0001\u0000"+
		"\u0000\u0000\u0084\u0086\u0005@\u0000\u0000\u0085\u0087\b\u0000\u0000"+
		"\u0000\u0086\u0085\u0001\u0000\u0000\u0000\u0087\u0088\u0001\u0000\u0000"+
		"\u0000\u0088\u0086\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000\u0000"+
		"\u0000\u0089\u0012\u0001\u0000\u0000\u0000\u008a\u008c\b\u0001\u0000\u0000"+
		"\u008b\u008a\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000\u0000"+
		"\u008d\u008b\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000\u0000\u0000"+
		"\u008e\u0014\u0001\u0000\u0000\u0000\u008f\u0091\u0005\r\u0000\u0000\u0090"+
		"\u008f\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091"+
		"\u0092\u0001\u0000\u0000\u0000\u0092\u0093\u0005\n\u0000\u0000\u0093\u0016"+
		"\u0001\u0000\u0000\u0000\u0094\u0095\u0005/\u0000\u0000\u0095\u0096\u0005"+
		"/\u0000\u0000\u0096\u009a\u0001\u0000\u0000\u0000\u0097\u0099\b\u0002"+
		"\u0000\u0000\u0098\u0097\u0001\u0000\u0000\u0000\u0099\u009c\u0001\u0000"+
		"\u0000\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u009a\u009b\u0001\u0000"+
		"\u0000\u0000\u009b\u009d\u0001\u0000\u0000\u0000\u009c\u009a\u0001\u0000"+
		"\u0000\u0000\u009d\u009e\u0007\u0002\u0000\u0000\u009e\u009f\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u0006\u000b\u0000\u0000\u00a0\u0018\u0001\u0000"+
		"\u0000\u0000\u00a1\u00a3\u0007\u0003\u0000\u0000\u00a2\u00a1\u0001\u0000"+
		"\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4\u00a2\u0001\u0000"+
		"\u0000\u0000\u00a4\u00a5\u0001\u0000\u0000\u0000\u00a5\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a6\u00a7\u0006\f\u0000\u0000\u00a7\u001a\u0001\u0000\u0000"+
		"\u0000\u0006\u0000\u0088\u008d\u0090\u009a\u00a4\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}