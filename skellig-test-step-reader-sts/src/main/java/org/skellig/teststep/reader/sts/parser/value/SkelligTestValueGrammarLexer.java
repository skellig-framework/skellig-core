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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, LESSER=8, GREATER=9, 
		LESSER_EQUAL=10, GREATER_EQUAL=11, EQUAL=12, NOT_EQUAL=13, FLOAT=14, INT=15, 
		MULT=16, DIV=17, ADD=18, SUB=19, AND=20, OR=21, NOT=22, COMMA=23, DOT=24, 
		LAMBDA=25, BOOL=26, ID=27, STRING=28, WS=29;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "LESSER", "GREATER", 
			"LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", "FLOAT", "INT", 
			"MULT", "DIV", "ADD", "SUB", "AND", "OR", "NOT", "COMMA", "DOT", "LAMBDA", 
			"BOOL", "ID", "STRING", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'):'", "'${'", "'}'", "'['", "']'", "'<'", "'>'", 
			"'<='", "'>='", "'=='", "'!='", null, null, "'*'", "'/'", "'+'", "'-'", 
			"'&&'", "'||'", "'!'", "','", "'.'", "'->'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, "LESSER", "GREATER", 
			"LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", "FLOAT", "INT", 
			"MULT", "DIV", "ADD", "SUB", "AND", "OR", "NOT", "COMMA", "DOT", "LAMBDA", 
			"BOOL", "ID", "STRING", "WS"
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
		"\u0004\u0000\u001d\u00a4\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\f\u0001\f\u0001\f\u0001\r\u0004\r]\b\r\u000b\r\f\r^\u0001"+
		"\r\u0001\r\u0004\rc\b\r\u000b\r\f\rd\u0001\u000e\u0004\u000eh\b\u000e"+
		"\u000b\u000e\f\u000ei\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u008c\b\u0019"+
		"\u0001\u001a\u0004\u001a\u008f\b\u001a\u000b\u001a\f\u001a\u0090\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001b\u0097\b\u001b\n"+
		"\u001b\f\u001b\u009a\t\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0004"+
		"\u001c\u009f\b\u001c\u000b\u001c\f\u001c\u00a0\u0001\u001c\u0001\u001c"+
		"\u0000\u0000\u001d\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005"+
		"\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019"+
		"\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015"+
		"+\u0016-\u0017/\u00181\u00193\u001a5\u001b7\u001c9\u001d\u0001\u0000\u0004"+
		"\u0001\u000009\u0005\u0000$$0;AZ__az\u0001\u0000\"\"\u0003\u0000\t\n\r"+
		"\r  \u00ab\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000"+
		"\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000"+
		"\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000"+
		"\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000"+
		"\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000"+
		"\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000"+
		"\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000"+
		"\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000"+
		"\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%"+
		"\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001"+
		"\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000-\u0001\u0000\u0000"+
		"\u0000\u0000/\u0001\u0000\u0000\u0000\u00001\u0001\u0000\u0000\u0000\u0000"+
		"3\u0001\u0000\u0000\u0000\u00005\u0001\u0000\u0000\u0000\u00007\u0001"+
		"\u0000\u0000\u0000\u00009\u0001\u0000\u0000\u0000\u0001;\u0001\u0000\u0000"+
		"\u0000\u0003=\u0001\u0000\u0000\u0000\u0005?\u0001\u0000\u0000\u0000\u0007"+
		"B\u0001\u0000\u0000\u0000\tE\u0001\u0000\u0000\u0000\u000bG\u0001\u0000"+
		"\u0000\u0000\rI\u0001\u0000\u0000\u0000\u000fK\u0001\u0000\u0000\u0000"+
		"\u0011M\u0001\u0000\u0000\u0000\u0013O\u0001\u0000\u0000\u0000\u0015R"+
		"\u0001\u0000\u0000\u0000\u0017U\u0001\u0000\u0000\u0000\u0019X\u0001\u0000"+
		"\u0000\u0000\u001b\\\u0001\u0000\u0000\u0000\u001dg\u0001\u0000\u0000"+
		"\u0000\u001fk\u0001\u0000\u0000\u0000!m\u0001\u0000\u0000\u0000#o\u0001"+
		"\u0000\u0000\u0000%q\u0001\u0000\u0000\u0000\'s\u0001\u0000\u0000\u0000"+
		")v\u0001\u0000\u0000\u0000+y\u0001\u0000\u0000\u0000-{\u0001\u0000\u0000"+
		"\u0000/}\u0001\u0000\u0000\u00001\u007f\u0001\u0000\u0000\u00003\u008b"+
		"\u0001\u0000\u0000\u00005\u008e\u0001\u0000\u0000\u00007\u0092\u0001\u0000"+
		"\u0000\u00009\u009e\u0001\u0000\u0000\u0000;<\u0005(\u0000\u0000<\u0002"+
		"\u0001\u0000\u0000\u0000=>\u0005)\u0000\u0000>\u0004\u0001\u0000\u0000"+
		"\u0000?@\u0005)\u0000\u0000@A\u0005:\u0000\u0000A\u0006\u0001\u0000\u0000"+
		"\u0000BC\u0005$\u0000\u0000CD\u0005{\u0000\u0000D\b\u0001\u0000\u0000"+
		"\u0000EF\u0005}\u0000\u0000F\n\u0001\u0000\u0000\u0000GH\u0005[\u0000"+
		"\u0000H\f\u0001\u0000\u0000\u0000IJ\u0005]\u0000\u0000J\u000e\u0001\u0000"+
		"\u0000\u0000KL\u0005<\u0000\u0000L\u0010\u0001\u0000\u0000\u0000MN\u0005"+
		">\u0000\u0000N\u0012\u0001\u0000\u0000\u0000OP\u0005<\u0000\u0000PQ\u0005"+
		"=\u0000\u0000Q\u0014\u0001\u0000\u0000\u0000RS\u0005>\u0000\u0000ST\u0005"+
		"=\u0000\u0000T\u0016\u0001\u0000\u0000\u0000UV\u0005=\u0000\u0000VW\u0005"+
		"=\u0000\u0000W\u0018\u0001\u0000\u0000\u0000XY\u0005!\u0000\u0000YZ\u0005"+
		"=\u0000\u0000Z\u001a\u0001\u0000\u0000\u0000[]\u0007\u0000\u0000\u0000"+
		"\\[\u0001\u0000\u0000\u0000]^\u0001\u0000\u0000\u0000^\\\u0001\u0000\u0000"+
		"\u0000^_\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`b\u0005.\u0000"+
		"\u0000ac\u0007\u0000\u0000\u0000ba\u0001\u0000\u0000\u0000cd\u0001\u0000"+
		"\u0000\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000e\u001c"+
		"\u0001\u0000\u0000\u0000fh\u0007\u0000\u0000\u0000gf\u0001\u0000\u0000"+
		"\u0000hi\u0001\u0000\u0000\u0000ig\u0001\u0000\u0000\u0000ij\u0001\u0000"+
		"\u0000\u0000j\u001e\u0001\u0000\u0000\u0000kl\u0005*\u0000\u0000l \u0001"+
		"\u0000\u0000\u0000mn\u0005/\u0000\u0000n\"\u0001\u0000\u0000\u0000op\u0005"+
		"+\u0000\u0000p$\u0001\u0000\u0000\u0000qr\u0005-\u0000\u0000r&\u0001\u0000"+
		"\u0000\u0000st\u0005&\u0000\u0000tu\u0005&\u0000\u0000u(\u0001\u0000\u0000"+
		"\u0000vw\u0005|\u0000\u0000wx\u0005|\u0000\u0000x*\u0001\u0000\u0000\u0000"+
		"yz\u0005!\u0000\u0000z,\u0001\u0000\u0000\u0000{|\u0005,\u0000\u0000|"+
		".\u0001\u0000\u0000\u0000}~\u0005.\u0000\u0000~0\u0001\u0000\u0000\u0000"+
		"\u007f\u0080\u0005-\u0000\u0000\u0080\u0081\u0005>\u0000\u0000\u00812"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0005t\u0000\u0000\u0083\u0084\u0005"+
		"r\u0000\u0000\u0084\u0085\u0005u\u0000\u0000\u0085\u008c\u0005e\u0000"+
		"\u0000\u0086\u0087\u0005f\u0000\u0000\u0087\u0088\u0005a\u0000\u0000\u0088"+
		"\u0089\u0005l\u0000\u0000\u0089\u008a\u0005s\u0000\u0000\u008a\u008c\u0005"+
		"e\u0000\u0000\u008b\u0082\u0001\u0000\u0000\u0000\u008b\u0086\u0001\u0000"+
		"\u0000\u0000\u008c4\u0001\u0000\u0000\u0000\u008d\u008f\u0007\u0001\u0000"+
		"\u0000\u008e\u008d\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000"+
		"\u0000\u0090\u008e\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000"+
		"\u0000\u00916\u0001\u0000\u0000\u0000\u0092\u0098\u0005\"\u0000\u0000"+
		"\u0093\u0097\b\u0002\u0000\u0000\u0094\u0095\u0005\\\u0000\u0000\u0095"+
		"\u0097\u0005\"\u0000\u0000\u0096\u0093\u0001\u0000\u0000\u0000\u0096\u0094"+
		"\u0001\u0000\u0000\u0000\u0097\u009a\u0001\u0000\u0000\u0000\u0098\u0096"+
		"\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u009b"+
		"\u0001\u0000\u0000\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u009b\u009c"+
		"\u0005\"\u0000\u0000\u009c8\u0001\u0000\u0000\u0000\u009d\u009f\u0007"+
		"\u0003\u0000\u0000\u009e\u009d\u0001\u0000\u0000\u0000\u009f\u00a0\u0001"+
		"\u0000\u0000\u0000\u00a0\u009e\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001"+
		"\u0000\u0000\u0000\u00a1\u00a2\u0001\u0000\u0000\u0000\u00a2\u00a3\u0006"+
		"\u001c\u0000\u0000\u00a3:\u0001\u0000\u0000\u0000\t\u0000^di\u008b\u0090"+
		"\u0096\u0098\u00a0\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}