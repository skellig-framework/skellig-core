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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		FLOAT=10, INT=11, NAME=12, ID=13, LESSER_EQUAL=14, GREATER_EQUAL=15, EQUAL=16, 
		NOT_EQUAL=17, COLON=18, KEY_SYMBOLS=19, VALUE_SYMBOLS=20, STRING=21, NEWLINE=22, 
		COMMENT=23, WS=24;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"FLOAT", "INT", "NAME", "ID", "LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", 
			"NOT_EQUAL", "COLON", "KEY_SYMBOLS", "VALUE_SYMBOLS", "STRING", "NEWLINE", 
			"COMMENT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'{'", "'}'", "'='", "'['", "','", "']'", "'${'", 
			null, null, "'name'", null, "'<='", "'>='", "'=='", "'!='", "':'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "FLOAT", 
			"INT", "NAME", "ID", "LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", 
			"COLON", "KEY_SYMBOLS", "VALUE_SYMBOLS", "STRING", "NEWLINE", "COMMENT", 
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
		"\u0004\u0000\u0018\u0098\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0001\t\u0004\tF\b\t\u000b\t\f\tG\u0001\t\u0001\t\u0004\tL\b\t\u000b"+
		"\t\f\tM\u0001\n\u0004\nQ\b\n\u000b\n\f\nR\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\f\u0004\f[\b\f\u000b\f\f\f\\\u0001"+
		"\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0004\u0012n\b\u0012\u000b\u0012\f\u0012o\u0001\u0013"+
		"\u0004\u0013s\b\u0013\u000b\u0013\f\u0013t\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0005\u0014{\b\u0014\n\u0014\f\u0014~\t\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0015\u0003\u0015\u0083\b\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u008b"+
		"\b\u0016\n\u0016\f\u0016\u008e\t\u0016\u0001\u0016\u0001\u0016\u0001\u0017"+
		"\u0004\u0017\u0093\b\u0017\u000b\u0017\f\u0017\u0094\u0001\u0017\u0001"+
		"\u0017\u0000\u0000\u0018\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004"+
		"\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017"+
		"\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'"+
		"\u0014)\u0015+\u0016-\u0017/\u0018\u0001\u0000\u0007\u0001\u000009\u0005"+
		"\u0000--09AZ__az\t\u0000!!#\'-.?@\\\\^`~~\u00a3\u00a3\u00ac\u00ac\u0005"+
		"\u0000*+//<<>>||\u0001\u0000\"\"\u0002\u0000\n\n\r\r\u0002\u0000\t\t "+
		" \u00a2\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000"+
		"\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000"+
		"\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000"+
		"\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000"+
		"\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000"+
		"\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000"+
		"\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000"+
		"\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000"+
		"!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001"+
		"\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001\u0000"+
		"\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000-\u0001\u0000\u0000\u0000"+
		"\u0000/\u0001\u0000\u0000\u0000\u00011\u0001\u0000\u0000\u0000\u00033"+
		"\u0001\u0000\u0000\u0000\u00055\u0001\u0000\u0000\u0000\u00077\u0001\u0000"+
		"\u0000\u0000\t9\u0001\u0000\u0000\u0000\u000b;\u0001\u0000\u0000\u0000"+
		"\r=\u0001\u0000\u0000\u0000\u000f?\u0001\u0000\u0000\u0000\u0011A\u0001"+
		"\u0000\u0000\u0000\u0013E\u0001\u0000\u0000\u0000\u0015P\u0001\u0000\u0000"+
		"\u0000\u0017T\u0001\u0000\u0000\u0000\u0019Z\u0001\u0000\u0000\u0000\u001b"+
		"^\u0001\u0000\u0000\u0000\u001da\u0001\u0000\u0000\u0000\u001fd\u0001"+
		"\u0000\u0000\u0000!g\u0001\u0000\u0000\u0000#j\u0001\u0000\u0000\u0000"+
		"%m\u0001\u0000\u0000\u0000\'r\u0001\u0000\u0000\u0000)v\u0001\u0000\u0000"+
		"\u0000+\u0082\u0001\u0000\u0000\u0000-\u0086\u0001\u0000\u0000\u0000/"+
		"\u0092\u0001\u0000\u0000\u000012\u0005(\u0000\u00002\u0002\u0001\u0000"+
		"\u0000\u000034\u0005)\u0000\u00004\u0004\u0001\u0000\u0000\u000056\u0005"+
		"{\u0000\u00006\u0006\u0001\u0000\u0000\u000078\u0005}\u0000\u00008\b\u0001"+
		"\u0000\u0000\u00009:\u0005=\u0000\u0000:\n\u0001\u0000\u0000\u0000;<\u0005"+
		"[\u0000\u0000<\f\u0001\u0000\u0000\u0000=>\u0005,\u0000\u0000>\u000e\u0001"+
		"\u0000\u0000\u0000?@\u0005]\u0000\u0000@\u0010\u0001\u0000\u0000\u0000"+
		"AB\u0005$\u0000\u0000BC\u0005{\u0000\u0000C\u0012\u0001\u0000\u0000\u0000"+
		"DF\u0007\u0000\u0000\u0000ED\u0001\u0000\u0000\u0000FG\u0001\u0000\u0000"+
		"\u0000GE\u0001\u0000\u0000\u0000GH\u0001\u0000\u0000\u0000HI\u0001\u0000"+
		"\u0000\u0000IK\u0005.\u0000\u0000JL\u0007\u0000\u0000\u0000KJ\u0001\u0000"+
		"\u0000\u0000LM\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000MN\u0001"+
		"\u0000\u0000\u0000N\u0014\u0001\u0000\u0000\u0000OQ\u0007\u0000\u0000"+
		"\u0000PO\u0001\u0000\u0000\u0000QR\u0001\u0000\u0000\u0000RP\u0001\u0000"+
		"\u0000\u0000RS\u0001\u0000\u0000\u0000S\u0016\u0001\u0000\u0000\u0000"+
		"TU\u0005n\u0000\u0000UV\u0005a\u0000\u0000VW\u0005m\u0000\u0000WX\u0005"+
		"e\u0000\u0000X\u0018\u0001\u0000\u0000\u0000Y[\u0007\u0001\u0000\u0000"+
		"ZY\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\Z\u0001\u0000\u0000"+
		"\u0000\\]\u0001\u0000\u0000\u0000]\u001a\u0001\u0000\u0000\u0000^_\u0005"+
		"<\u0000\u0000_`\u0005=\u0000\u0000`\u001c\u0001\u0000\u0000\u0000ab\u0005"+
		">\u0000\u0000bc\u0005=\u0000\u0000c\u001e\u0001\u0000\u0000\u0000de\u0005"+
		"=\u0000\u0000ef\u0005=\u0000\u0000f \u0001\u0000\u0000\u0000gh\u0005!"+
		"\u0000\u0000hi\u0005=\u0000\u0000i\"\u0001\u0000\u0000\u0000jk\u0005:"+
		"\u0000\u0000k$\u0001\u0000\u0000\u0000ln\u0007\u0002\u0000\u0000ml\u0001"+
		"\u0000\u0000\u0000no\u0001\u0000\u0000\u0000om\u0001\u0000\u0000\u0000"+
		"op\u0001\u0000\u0000\u0000p&\u0001\u0000\u0000\u0000qs\u0007\u0003\u0000"+
		"\u0000rq\u0001\u0000\u0000\u0000st\u0001\u0000\u0000\u0000tr\u0001\u0000"+
		"\u0000\u0000tu\u0001\u0000\u0000\u0000u(\u0001\u0000\u0000\u0000v|\u0005"+
		"\"\u0000\u0000w{\b\u0004\u0000\u0000xy\u0005\\\u0000\u0000y{\u0005\"\u0000"+
		"\u0000zw\u0001\u0000\u0000\u0000zx\u0001\u0000\u0000\u0000{~\u0001\u0000"+
		"\u0000\u0000|z\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000}\u007f"+
		"\u0001\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000\u007f\u0080\u0005\""+
		"\u0000\u0000\u0080*\u0001\u0000\u0000\u0000\u0081\u0083\u0005\r\u0000"+
		"\u0000\u0082\u0081\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000"+
		"\u0000\u0083\u0084\u0001\u0000\u0000\u0000\u0084\u0085\u0005\n\u0000\u0000"+
		"\u0085,\u0001\u0000\u0000\u0000\u0086\u0087\u0005/\u0000\u0000\u0087\u0088"+
		"\u0005/\u0000\u0000\u0088\u008c\u0001\u0000\u0000\u0000\u0089\u008b\b"+
		"\u0005\u0000\u0000\u008a\u0089\u0001\u0000\u0000\u0000\u008b\u008e\u0001"+
		"\u0000\u0000\u0000\u008c\u008a\u0001\u0000\u0000\u0000\u008c\u008d\u0001"+
		"\u0000\u0000\u0000\u008d\u008f\u0001\u0000\u0000\u0000\u008e\u008c\u0001"+
		"\u0000\u0000\u0000\u008f\u0090\u0006\u0016\u0000\u0000\u0090.\u0001\u0000"+
		"\u0000\u0000\u0091\u0093\u0007\u0006\u0000\u0000\u0092\u0091\u0001\u0000"+
		"\u0000\u0000\u0093\u0094\u0001\u0000\u0000\u0000\u0094\u0092\u0001\u0000"+
		"\u0000\u0000\u0094\u0095\u0001\u0000\u0000\u0000\u0095\u0096\u0001\u0000"+
		"\u0000\u0000\u0096\u0097\u0006\u0017\u0000\u0000\u00970\u0001\u0000\u0000"+
		"\u0000\f\u0000GMR\\otz|\u0082\u008c\u0094\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}