package org.skellig.teststep.reader.sts.parser.value;// Generated from SkelligTestValueGrammar.g4 by ANTLR 4.13.1

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class SkelligTestValueGrammarLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, LESSER=7, GREATER=8, LESSER_EQUAL=9, 
		GREATER_EQUAL=10, EQUAL=11, NOT_EQUAL=12, FLOAT=13, INT=14, MULT=15, DIV=16, 
		ADD=17, SUB=18, AND=19, OR=20, NOT=21, COMMA=22, DOT=23, LAMBDA=24, ID=25, 
		STRING=26, WS=27;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "LESSER", "GREATER", 
			"LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", "FLOAT", "INT", 
			"MULT", "DIV", "ADD", "SUB", "AND", "OR", "NOT", "COMMA", "DOT", "LAMBDA", 
			"ID", "STRING", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'${'", "'}'", "'['", "']'", "'<'", "'>'", "'<='", 
			"'>='", "'=='", "'!='", null, null, "'*'", "'/'", "'+'", "'-'", "'&&'", 
			"'||'", "'!'", "','", "'.'", "'->'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, "LESSER", "GREATER", "LESSER_EQUAL", 
			"GREATER_EQUAL", "EQUAL", "NOT_EQUAL", "FLOAT", "INT", "MULT", "DIV", 
			"ADD", "SUB", "AND", "OR", "NOT", "COMMA", "DOT", "LAMBDA", "ID", "STRING", 
			"WS"
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


	public SkelligTestValueGrammarLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SkelligTestValueGrammar.g4"; }

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
		"\u0004\u0000\u001b\u0092\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0004\fV\b\f\u000b\f\f\fW\u0001\f\u0001"+
		"\f\u0004\f\\\b\f\u000b\f\f\f]\u0001\r\u0004\ra\b\r\u000b\r\f\rb\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001"+
		"\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0004"+
		"\u0018}\b\u0018\u000b\u0018\f\u0018~\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0005\u0019\u0085\b\u0019\n\u0019\f\u0019\u0088\t\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u001a\u0004\u001a\u008d\b\u001a\u000b\u001a\f"+
		"\u001a\u008e\u0001\u001a\u0001\u001a\u0000\u0000\u001b\u0001\u0001\u0003"+
		"\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011"+
		"\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010"+
		"!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017/\u00181\u00193\u001a"+
		"5\u001b\u0001\u0000\u0004\u0001\u000009\u0005\u0000--0;AZ__az\u0003\u0000"+
		"\n\n\r\r\"\"\u0003\u0000\t\n\r\r  \u0098\u0000\u0001\u0001\u0000\u0000"+
		"\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000"+
		"\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000"+
		"\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000"+
		"\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000"+
		"\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000"+
		"\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000"+
		"\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000"+
		"\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001"+
		"\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000"+
		"\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000"+
		"\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000\u00001"+
		"\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00005\u0001\u0000"+
		"\u0000\u0000\u00017\u0001\u0000\u0000\u0000\u00039\u0001\u0000\u0000\u0000"+
		"\u0005;\u0001\u0000\u0000\u0000\u0007>\u0001\u0000\u0000\u0000\t@\u0001"+
		"\u0000\u0000\u0000\u000bB\u0001\u0000\u0000\u0000\rD\u0001\u0000\u0000"+
		"\u0000\u000fF\u0001\u0000\u0000\u0000\u0011H\u0001\u0000\u0000\u0000\u0013"+
		"K\u0001\u0000\u0000\u0000\u0015N\u0001\u0000\u0000\u0000\u0017Q\u0001"+
		"\u0000\u0000\u0000\u0019U\u0001\u0000\u0000\u0000\u001b`\u0001\u0000\u0000"+
		"\u0000\u001dd\u0001\u0000\u0000\u0000\u001ff\u0001\u0000\u0000\u0000!"+
		"h\u0001\u0000\u0000\u0000#j\u0001\u0000\u0000\u0000%l\u0001\u0000\u0000"+
		"\u0000\'o\u0001\u0000\u0000\u0000)r\u0001\u0000\u0000\u0000+t\u0001\u0000"+
		"\u0000\u0000-v\u0001\u0000\u0000\u0000/x\u0001\u0000\u0000\u00001|\u0001"+
		"\u0000\u0000\u00003\u0080\u0001\u0000\u0000\u00005\u008c\u0001\u0000\u0000"+
		"\u000078\u0005(\u0000\u00008\u0002\u0001\u0000\u0000\u00009:\u0005)\u0000"+
		"\u0000:\u0004\u0001\u0000\u0000\u0000;<\u0005$\u0000\u0000<=\u0005{\u0000"+
		"\u0000=\u0006\u0001\u0000\u0000\u0000>?\u0005}\u0000\u0000?\b\u0001\u0000"+
		"\u0000\u0000@A\u0005[\u0000\u0000A\n\u0001\u0000\u0000\u0000BC\u0005]"+
		"\u0000\u0000C\f\u0001\u0000\u0000\u0000DE\u0005<\u0000\u0000E\u000e\u0001"+
		"\u0000\u0000\u0000FG\u0005>\u0000\u0000G\u0010\u0001\u0000\u0000\u0000"+
		"HI\u0005<\u0000\u0000IJ\u0005=\u0000\u0000J\u0012\u0001\u0000\u0000\u0000"+
		"KL\u0005>\u0000\u0000LM\u0005=\u0000\u0000M\u0014\u0001\u0000\u0000\u0000"+
		"NO\u0005=\u0000\u0000OP\u0005=\u0000\u0000P\u0016\u0001\u0000\u0000\u0000"+
		"QR\u0005!\u0000\u0000RS\u0005=\u0000\u0000S\u0018\u0001\u0000\u0000\u0000"+
		"TV\u0007\u0000\u0000\u0000UT\u0001\u0000\u0000\u0000VW\u0001\u0000\u0000"+
		"\u0000WU\u0001\u0000\u0000\u0000WX\u0001\u0000\u0000\u0000XY\u0001\u0000"+
		"\u0000\u0000Y[\u0005.\u0000\u0000Z\\\u0007\u0000\u0000\u0000[Z\u0001\u0000"+
		"\u0000\u0000\\]\u0001\u0000\u0000\u0000][\u0001\u0000\u0000\u0000]^\u0001"+
		"\u0000\u0000\u0000^\u001a\u0001\u0000\u0000\u0000_a\u0007\u0000\u0000"+
		"\u0000`_\u0001\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000b`\u0001\u0000"+
		"\u0000\u0000bc\u0001\u0000\u0000\u0000c\u001c\u0001\u0000\u0000\u0000"+
		"de\u0005*\u0000\u0000e\u001e\u0001\u0000\u0000\u0000fg\u0005/\u0000\u0000"+
		"g \u0001\u0000\u0000\u0000hi\u0005+\u0000\u0000i\"\u0001\u0000\u0000\u0000"+
		"jk\u0005-\u0000\u0000k$\u0001\u0000\u0000\u0000lm\u0005&\u0000\u0000m"+
		"n\u0005&\u0000\u0000n&\u0001\u0000\u0000\u0000op\u0005|\u0000\u0000pq"+
		"\u0005|\u0000\u0000q(\u0001\u0000\u0000\u0000rs\u0005!\u0000\u0000s*\u0001"+
		"\u0000\u0000\u0000tu\u0005,\u0000\u0000u,\u0001\u0000\u0000\u0000vw\u0005"+
		".\u0000\u0000w.\u0001\u0000\u0000\u0000xy\u0005-\u0000\u0000yz\u0005>"+
		"\u0000\u0000z0\u0001\u0000\u0000\u0000{}\u0007\u0001\u0000\u0000|{\u0001"+
		"\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000"+
		"~\u007f\u0001\u0000\u0000\u0000\u007f2\u0001\u0000\u0000\u0000\u0080\u0086"+
		"\u0005\"\u0000\u0000\u0081\u0085\b\u0002\u0000\u0000\u0082\u0083\u0005"+
		"\\\u0000\u0000\u0083\u0085\u0005\"\u0000\u0000\u0084\u0081\u0001\u0000"+
		"\u0000\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0085\u0088\u0001\u0000"+
		"\u0000\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0086\u0087\u0001\u0000"+
		"\u0000\u0000\u0087\u0089\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000"+
		"\u0000\u0000\u0089\u008a\u0005\"\u0000\u0000\u008a4\u0001\u0000\u0000"+
		"\u0000\u008b\u008d\u0007\u0003\u0000\u0000\u008c\u008b\u0001\u0000\u0000"+
		"\u0000\u008d\u008e\u0001\u0000\u0000\u0000\u008e\u008c\u0001\u0000\u0000"+
		"\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000"+
		"\u0000\u0090\u0091\u0006\u001a\u0000\u0000\u00916\u0001\u0000\u0000\u0000"+
		"\b\u0000W]b~\u0084\u0086\u008e\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}