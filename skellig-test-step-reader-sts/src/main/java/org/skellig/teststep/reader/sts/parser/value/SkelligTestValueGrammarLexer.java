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
		ADD=17, SUB=18, AND=19, OR=20, NOT=21, COMMA=22, DOT=23, LAMBDA=24, BOOL=25, 
		ID=26, STRING=27, WS=28;
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
			"BOOL", "ID", "STRING", "WS"
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
			"ADD", "SUB", "AND", "OR", "NOT", "COMMA", "DOT", "LAMBDA", "BOOL", "ID", 
			"STRING", "WS"
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
		"\u0004\u0000\u001c\u009f\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0002\u001b\u0007\u001b\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0004\fX\b\f\u000b"+
		"\f\f\fY\u0001\f\u0001\f\u0004\f^\b\f\u000b\f\f\f_\u0001\r\u0004\rc\b\r"+
		"\u000b\r\f\rd\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u0087\b\u0018\u0001\u0019"+
		"\u0004\u0019\u008a\b\u0019\u000b\u0019\f\u0019\u008b\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u0092\b\u001a\n\u001a\f\u001a"+
		"\u0095\t\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0004\u001b\u009a\b"+
		"\u001b\u000b\u001b\f\u001b\u009b\u0001\u001b\u0001\u001b\u0000\u0000\u001c"+
		"\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r"+
		"\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e"+
		"\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017"+
		"/\u00181\u00193\u001a5\u001b7\u001c\u0001\u0000\u0004\u0001\u000009\u0005"+
		"\u0000$$0;AZ__az\u0001\u0000\"\"\u0003\u0000\t\n\r\r  \u00a6\u0000\u0001"+
		"\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005"+
		"\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001"+
		"\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000"+
		"\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000"+
		"\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000"+
		"\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000"+
		"\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000"+
		"\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000"+
		"\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000"+
		"\'\u0001\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001"+
		"\u0000\u0000\u0000\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000"+
		"\u0000\u00001\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u0000"+
		"5\u0001\u0000\u0000\u0000\u00007\u0001\u0000\u0000\u0000\u00019\u0001"+
		"\u0000\u0000\u0000\u0003;\u0001\u0000\u0000\u0000\u0005=\u0001\u0000\u0000"+
		"\u0000\u0007@\u0001\u0000\u0000\u0000\tB\u0001\u0000\u0000\u0000\u000b"+
		"D\u0001\u0000\u0000\u0000\rF\u0001\u0000\u0000\u0000\u000fH\u0001\u0000"+
		"\u0000\u0000\u0011J\u0001\u0000\u0000\u0000\u0013M\u0001\u0000\u0000\u0000"+
		"\u0015P\u0001\u0000\u0000\u0000\u0017S\u0001\u0000\u0000\u0000\u0019W"+
		"\u0001\u0000\u0000\u0000\u001bb\u0001\u0000\u0000\u0000\u001df\u0001\u0000"+
		"\u0000\u0000\u001fh\u0001\u0000\u0000\u0000!j\u0001\u0000\u0000\u0000"+
		"#l\u0001\u0000\u0000\u0000%n\u0001\u0000\u0000\u0000\'q\u0001\u0000\u0000"+
		"\u0000)t\u0001\u0000\u0000\u0000+v\u0001\u0000\u0000\u0000-x\u0001\u0000"+
		"\u0000\u0000/z\u0001\u0000\u0000\u00001\u0086\u0001\u0000\u0000\u0000"+
		"3\u0089\u0001\u0000\u0000\u00005\u008d\u0001\u0000\u0000\u00007\u0099"+
		"\u0001\u0000\u0000\u00009:\u0005(\u0000\u0000:\u0002\u0001\u0000\u0000"+
		"\u0000;<\u0005)\u0000\u0000<\u0004\u0001\u0000\u0000\u0000=>\u0005$\u0000"+
		"\u0000>?\u0005{\u0000\u0000?\u0006\u0001\u0000\u0000\u0000@A\u0005}\u0000"+
		"\u0000A\b\u0001\u0000\u0000\u0000BC\u0005[\u0000\u0000C\n\u0001\u0000"+
		"\u0000\u0000DE\u0005]\u0000\u0000E\f\u0001\u0000\u0000\u0000FG\u0005<"+
		"\u0000\u0000G\u000e\u0001\u0000\u0000\u0000HI\u0005>\u0000\u0000I\u0010"+
		"\u0001\u0000\u0000\u0000JK\u0005<\u0000\u0000KL\u0005=\u0000\u0000L\u0012"+
		"\u0001\u0000\u0000\u0000MN\u0005>\u0000\u0000NO\u0005=\u0000\u0000O\u0014"+
		"\u0001\u0000\u0000\u0000PQ\u0005=\u0000\u0000QR\u0005=\u0000\u0000R\u0016"+
		"\u0001\u0000\u0000\u0000ST\u0005!\u0000\u0000TU\u0005=\u0000\u0000U\u0018"+
		"\u0001\u0000\u0000\u0000VX\u0007\u0000\u0000\u0000WV\u0001\u0000\u0000"+
		"\u0000XY\u0001\u0000\u0000\u0000YW\u0001\u0000\u0000\u0000YZ\u0001\u0000"+
		"\u0000\u0000Z[\u0001\u0000\u0000\u0000[]\u0005.\u0000\u0000\\^\u0007\u0000"+
		"\u0000\u0000]\\\u0001\u0000\u0000\u0000^_\u0001\u0000\u0000\u0000_]\u0001"+
		"\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`\u001a\u0001\u0000\u0000"+
		"\u0000ac\u0007\u0000\u0000\u0000ba\u0001\u0000\u0000\u0000cd\u0001\u0000"+
		"\u0000\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000e\u001c"+
		"\u0001\u0000\u0000\u0000fg\u0005*\u0000\u0000g\u001e\u0001\u0000\u0000"+
		"\u0000hi\u0005/\u0000\u0000i \u0001\u0000\u0000\u0000jk\u0005+\u0000\u0000"+
		"k\"\u0001\u0000\u0000\u0000lm\u0005-\u0000\u0000m$\u0001\u0000\u0000\u0000"+
		"no\u0005&\u0000\u0000op\u0005&\u0000\u0000p&\u0001\u0000\u0000\u0000q"+
		"r\u0005|\u0000\u0000rs\u0005|\u0000\u0000s(\u0001\u0000\u0000\u0000tu"+
		"\u0005!\u0000\u0000u*\u0001\u0000\u0000\u0000vw\u0005,\u0000\u0000w,\u0001"+
		"\u0000\u0000\u0000xy\u0005.\u0000\u0000y.\u0001\u0000\u0000\u0000z{\u0005"+
		"-\u0000\u0000{|\u0005>\u0000\u0000|0\u0001\u0000\u0000\u0000}~\u0005t"+
		"\u0000\u0000~\u007f\u0005r\u0000\u0000\u007f\u0080\u0005u\u0000\u0000"+
		"\u0080\u0087\u0005e\u0000\u0000\u0081\u0082\u0005f\u0000\u0000\u0082\u0083"+
		"\u0005a\u0000\u0000\u0083\u0084\u0005l\u0000\u0000\u0084\u0085\u0005s"+
		"\u0000\u0000\u0085\u0087\u0005e\u0000\u0000\u0086}\u0001\u0000\u0000\u0000"+
		"\u0086\u0081\u0001\u0000\u0000\u0000\u00872\u0001\u0000\u0000\u0000\u0088"+
		"\u008a\u0007\u0001\u0000\u0000\u0089\u0088\u0001\u0000\u0000\u0000\u008a"+
		"\u008b\u0001\u0000\u0000\u0000\u008b\u0089\u0001\u0000\u0000\u0000\u008b"+
		"\u008c\u0001\u0000\u0000\u0000\u008c4\u0001\u0000\u0000\u0000\u008d\u0093"+
		"\u0005\"\u0000\u0000\u008e\u0092\b\u0002\u0000\u0000\u008f\u0090\u0005"+
		"\\\u0000\u0000\u0090\u0092\u0005\"\u0000\u0000\u0091\u008e\u0001\u0000"+
		"\u0000\u0000\u0091\u008f\u0001\u0000\u0000\u0000\u0092\u0095\u0001\u0000"+
		"\u0000\u0000\u0093\u0091\u0001\u0000\u0000\u0000\u0093\u0094\u0001\u0000"+
		"\u0000\u0000\u0094\u0096\u0001\u0000\u0000\u0000\u0095\u0093\u0001\u0000"+
		"\u0000\u0000\u0096\u0097\u0005\"\u0000\u0000\u00976\u0001\u0000\u0000"+
		"\u0000\u0098\u009a\u0007\u0003\u0000\u0000\u0099\u0098\u0001\u0000\u0000"+
		"\u0000\u009a\u009b\u0001\u0000\u0000\u0000\u009b\u0099\u0001\u0000\u0000"+
		"\u0000\u009b\u009c\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000"+
		"\u0000\u009d\u009e\u0006\u001b\u0000\u0000\u009e8\u0001\u0000\u0000\u0000"+
		"\t\u0000Y_d\u0086\u008b\u0091\u0093\u009b\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}