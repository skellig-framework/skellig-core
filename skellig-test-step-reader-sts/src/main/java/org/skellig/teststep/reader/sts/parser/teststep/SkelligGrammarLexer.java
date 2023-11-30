package org.skellig.teststep.reader.sts.parser.teststep;// Generated from SkelligGrammar.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class SkelligGrammarLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, FLOAT=9, 
		INT=10, NAME=11, ID=12, LESSER_EQUAL=13, GREATER_EQUAL=14, EQUAL=15, NOT_EQUAL=16, 
		COMMA=17, KEY_SYMBOLS=18, VALUE_SYMBOLS=19, STRING=20, NEWLINE=21, COMMENT=22, 
		WS=23;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "FLOAT", 
			"INT", "NAME", "ID", "LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", 
			"COMMA", "KEY_SYMBOLS", "VALUE_SYMBOLS", "STRING", "NEWLINE", "COMMENT", 
			"WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'{'", "'}'", "'='", "'['", "']'", "'${'", null, 
			null, "'name'", null, "'<='", "'>='", "'=='", "'!='", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "FLOAT", "INT", 
			"NAME", "ID", "LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", 
			"COMMA", "KEY_SYMBOLS", "VALUE_SYMBOLS", "STRING", "NEWLINE", "COMMENT", 
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


	public SkelligGrammarLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SkelligGrammar.g4"; }

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
		"\u0004\u0000\u0017\u0094\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0004\bB\b\b\u000b\b\f\b"+
		"C\u0001\b\u0001\b\u0004\bH\b\b\u000b\b\f\bI\u0001\t\u0004\tM\b\t\u000b"+
		"\t\f\tN\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0004\u000b"+
		"W\b\u000b\u000b\u000b\f\u000bX\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r"+
		"\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0004\u0011j\b\u0011\u000b"+
		"\u0011\f\u0011k\u0001\u0012\u0004\u0012o\b\u0012\u000b\u0012\f\u0012p"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013w\b\u0013"+
		"\n\u0013\f\u0013z\t\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0003\u0014"+
		"\u007f\b\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0005\u0015\u0087\b\u0015\n\u0015\f\u0015\u008a\t\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0016\u0004\u0016\u008f\b\u0016\u000b\u0016\f"+
		"\u0016\u0090\u0001\u0016\u0001\u0016\u0000\u0000\u0017\u0001\u0001\u0003"+
		"\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011"+
		"\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010"+
		"!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017\u0001\u0000\u0007\u0001"+
		"\u000009\u0005\u0000--09AZ__az\n\u0000!!#\'-.::?@\\\\^`~~\u00a3\u00a3"+
		"\u00ac\u00ac\u0005\u0000*+//<<>>||\u0001\u0000\"\"\u0002\u0000\n\n\r\r"+
		"\u0002\u0000\t\t  \u009e\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003"+
		"\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007"+
		"\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001"+
		"\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000"+
		"\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000"+
		"\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000"+
		"\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000"+
		"\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000"+
		"\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000"+
		"\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000"+
		")\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000-\u0001"+
		"\u0000\u0000\u0000\u0001/\u0001\u0000\u0000\u0000\u00031\u0001\u0000\u0000"+
		"\u0000\u00053\u0001\u0000\u0000\u0000\u00075\u0001\u0000\u0000\u0000\t"+
		"7\u0001\u0000\u0000\u0000\u000b9\u0001\u0000\u0000\u0000\r;\u0001\u0000"+
		"\u0000\u0000\u000f=\u0001\u0000\u0000\u0000\u0011A\u0001\u0000\u0000\u0000"+
		"\u0013L\u0001\u0000\u0000\u0000\u0015P\u0001\u0000\u0000\u0000\u0017V"+
		"\u0001\u0000\u0000\u0000\u0019Z\u0001\u0000\u0000\u0000\u001b]\u0001\u0000"+
		"\u0000\u0000\u001d`\u0001\u0000\u0000\u0000\u001fc\u0001\u0000\u0000\u0000"+
		"!f\u0001\u0000\u0000\u0000#i\u0001\u0000\u0000\u0000%n\u0001\u0000\u0000"+
		"\u0000\'r\u0001\u0000\u0000\u0000)~\u0001\u0000\u0000\u0000+\u0082\u0001"+
		"\u0000\u0000\u0000-\u008e\u0001\u0000\u0000\u0000/0\u0005(\u0000\u0000"+
		"0\u0002\u0001\u0000\u0000\u000012\u0005)\u0000\u00002\u0004\u0001\u0000"+
		"\u0000\u000034\u0005{\u0000\u00004\u0006\u0001\u0000\u0000\u000056\u0005"+
		"}\u0000\u00006\b\u0001\u0000\u0000\u000078\u0005=\u0000\u00008\n\u0001"+
		"\u0000\u0000\u00009:\u0005[\u0000\u0000:\f\u0001\u0000\u0000\u0000;<\u0005"+
		"]\u0000\u0000<\u000e\u0001\u0000\u0000\u0000=>\u0005$\u0000\u0000>?\u0005"+
		"{\u0000\u0000?\u0010\u0001\u0000\u0000\u0000@B\u0007\u0000\u0000\u0000"+
		"A@\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000CA\u0001\u0000\u0000"+
		"\u0000CD\u0001\u0000\u0000\u0000DE\u0001\u0000\u0000\u0000EG\u0005.\u0000"+
		"\u0000FH\u0007\u0000\u0000\u0000GF\u0001\u0000\u0000\u0000HI\u0001\u0000"+
		"\u0000\u0000IG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000J\u0012"+
		"\u0001\u0000\u0000\u0000KM\u0007\u0000\u0000\u0000LK\u0001\u0000\u0000"+
		"\u0000MN\u0001\u0000\u0000\u0000NL\u0001\u0000\u0000\u0000NO\u0001\u0000"+
		"\u0000\u0000O\u0014\u0001\u0000\u0000\u0000PQ\u0005n\u0000\u0000QR\u0005"+
		"a\u0000\u0000RS\u0005m\u0000\u0000ST\u0005e\u0000\u0000T\u0016\u0001\u0000"+
		"\u0000\u0000UW\u0007\u0001\u0000\u0000VU\u0001\u0000\u0000\u0000WX\u0001"+
		"\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XY\u0001\u0000\u0000\u0000"+
		"Y\u0018\u0001\u0000\u0000\u0000Z[\u0005<\u0000\u0000[\\\u0005=\u0000\u0000"+
		"\\\u001a\u0001\u0000\u0000\u0000]^\u0005>\u0000\u0000^_\u0005=\u0000\u0000"+
		"_\u001c\u0001\u0000\u0000\u0000`a\u0005=\u0000\u0000ab\u0005=\u0000\u0000"+
		"b\u001e\u0001\u0000\u0000\u0000cd\u0005!\u0000\u0000de\u0005=\u0000\u0000"+
		"e \u0001\u0000\u0000\u0000fg\u0005,\u0000\u0000g\"\u0001\u0000\u0000\u0000"+
		"hj\u0007\u0002\u0000\u0000ih\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000"+
		"\u0000ki\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000l$\u0001\u0000"+
		"\u0000\u0000mo\u0007\u0003\u0000\u0000nm\u0001\u0000\u0000\u0000op\u0001"+
		"\u0000\u0000\u0000pn\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000"+
		"q&\u0001\u0000\u0000\u0000rx\u0005\"\u0000\u0000sw\b\u0004\u0000\u0000"+
		"tu\u0005\\\u0000\u0000uw\u0005\"\u0000\u0000vs\u0001\u0000\u0000\u0000"+
		"vt\u0001\u0000\u0000\u0000wz\u0001\u0000\u0000\u0000xv\u0001\u0000\u0000"+
		"\u0000xy\u0001\u0000\u0000\u0000y{\u0001\u0000\u0000\u0000zx\u0001\u0000"+
		"\u0000\u0000{|\u0005\"\u0000\u0000|(\u0001\u0000\u0000\u0000}\u007f\u0005"+
		"\r\u0000\u0000~}\u0001\u0000\u0000\u0000~\u007f\u0001\u0000\u0000\u0000"+
		"\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0081\u0005\n\u0000\u0000\u0081"+
		"*\u0001\u0000\u0000\u0000\u0082\u0083\u0005/\u0000\u0000\u0083\u0084\u0005"+
		"/\u0000\u0000\u0084\u0088\u0001\u0000\u0000\u0000\u0085\u0087\b\u0005"+
		"\u0000\u0000\u0086\u0085\u0001\u0000\u0000\u0000\u0087\u008a\u0001\u0000"+
		"\u0000\u0000\u0088\u0086\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000"+
		"\u0000\u0000\u0089\u008b\u0001\u0000\u0000\u0000\u008a\u0088\u0001\u0000"+
		"\u0000\u0000\u008b\u008c\u0006\u0015\u0000\u0000\u008c,\u0001\u0000\u0000"+
		"\u0000\u008d\u008f\u0007\u0006\u0000\u0000\u008e\u008d\u0001\u0000\u0000"+
		"\u0000\u008f\u0090\u0001\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000"+
		"\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u0092\u0001\u0000\u0000"+
		"\u0000\u0092\u0093\u0006\u0016\u0000\u0000\u0093.\u0001\u0000\u0000\u0000"+
		"\f\u0000CINXkpvx~\u0088\u0090\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}