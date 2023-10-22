package org.skellig.teststep.reader.sts.parser;// Generated from NumberComparison.g4 by ANTLR 4.13.1

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class NumberComparisonLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, LESSER=9, 
		GREATER=10, LESSER_EQUAL=11, GREATER_EQUAL=12, EQUAL=13, NOT_EQUAL=14, 
		FLOAT=15, INT=16, MULT=17, DIV=18, ADD=19, SUB=20, AND=21, OR=22, NOT=23, 
		ID=24, STRING=25, WS=26;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "LESSER", 
			"GREATER", "LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", "FLOAT", 
			"INT", "MULT", "DIV", "ADD", "SUB", "AND", "OR", "NOT", "ID", "STRING", 
			"WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'.'", "','", "'->'", "'${'", "':'", "'}'", "'<'", 
			"'>'", "'<='", "'>='", "'=='", "'!='", null, null, "'*'", "'/'", "'+'", 
			"'-'", "'&&'", "'||'", "'!'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "LESSER", "GREATER", 
			"LESSER_EQUAL", "GREATER_EQUAL", "EQUAL", "NOT_EQUAL", "FLOAT", "INT", 
			"MULT", "DIV", "ADD", "SUB", "AND", "OR", "NOT", "ID", "STRING", "WS"
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


	public NumberComparisonLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NumberComparison.g4"; }

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
		"\u0004\u0000\u001a\u008e\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\u000e\u0004\u000e"+
		"Y\b\u000e\u000b\u000e\f\u000eZ\u0001\u000e\u0001\u000e\u0004\u000e_\b"+
		"\u000e\u000b\u000e\f\u000e`\u0001\u000f\u0004\u000fd\b\u000f\u000b\u000f"+
		"\f\u000fe\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0017"+
		"\u0001\u0017\u0005\u0017z\b\u0017\n\u0017\f\u0017}\t\u0017\u0001\u0018"+
		"\u0001\u0018\u0005\u0018\u0081\b\u0018\n\u0018\f\u0018\u0084\t\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0019\u0004\u0019\u0089\b\u0019\u000b\u0019\f"+
		"\u0019\u008a\u0001\u0019\u0001\u0019\u0000\u0000\u001a\u0001\u0001\u0003"+
		"\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011"+
		"\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010"+
		"!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017/\u00181\u00193\u001a"+
		"\u0001\u0000\u0005\u0001\u000009\u0002\u0000AZaz\u0004\u0000--09AZaz\u0003"+
		"\u0000\n\n\r\r\"\"\u0003\u0000\t\n\r\r  \u0093\u0000\u0001\u0001\u0000"+
		"\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000"+
		"\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000"+
		"\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000"+
		"\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000"+
		"\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000"+
		"\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000"+
		"\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000"+
		"\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000"+
		"#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001"+
		"\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000"+
		"\u0000\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000\u0000"+
		"1\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00015\u0001"+
		"\u0000\u0000\u0000\u00037\u0001\u0000\u0000\u0000\u00059\u0001\u0000\u0000"+
		"\u0000\u0007;\u0001\u0000\u0000\u0000\t=\u0001\u0000\u0000\u0000\u000b"+
		"@\u0001\u0000\u0000\u0000\rC\u0001\u0000\u0000\u0000\u000fE\u0001\u0000"+
		"\u0000\u0000\u0011G\u0001\u0000\u0000\u0000\u0013I\u0001\u0000\u0000\u0000"+
		"\u0015K\u0001\u0000\u0000\u0000\u0017N\u0001\u0000\u0000\u0000\u0019Q"+
		"\u0001\u0000\u0000\u0000\u001bT\u0001\u0000\u0000\u0000\u001dX\u0001\u0000"+
		"\u0000\u0000\u001fc\u0001\u0000\u0000\u0000!g\u0001\u0000\u0000\u0000"+
		"#i\u0001\u0000\u0000\u0000%k\u0001\u0000\u0000\u0000\'m\u0001\u0000\u0000"+
		"\u0000)o\u0001\u0000\u0000\u0000+r\u0001\u0000\u0000\u0000-u\u0001\u0000"+
		"\u0000\u0000/w\u0001\u0000\u0000\u00001~\u0001\u0000\u0000\u00003\u0088"+
		"\u0001\u0000\u0000\u000056\u0005(\u0000\u00006\u0002\u0001\u0000\u0000"+
		"\u000078\u0005)\u0000\u00008\u0004\u0001\u0000\u0000\u00009:\u0005.\u0000"+
		"\u0000:\u0006\u0001\u0000\u0000\u0000;<\u0005,\u0000\u0000<\b\u0001\u0000"+
		"\u0000\u0000=>\u0005-\u0000\u0000>?\u0005>\u0000\u0000?\n\u0001\u0000"+
		"\u0000\u0000@A\u0005$\u0000\u0000AB\u0005{\u0000\u0000B\f\u0001\u0000"+
		"\u0000\u0000CD\u0005:\u0000\u0000D\u000e\u0001\u0000\u0000\u0000EF\u0005"+
		"}\u0000\u0000F\u0010\u0001\u0000\u0000\u0000GH\u0005<\u0000\u0000H\u0012"+
		"\u0001\u0000\u0000\u0000IJ\u0005>\u0000\u0000J\u0014\u0001\u0000\u0000"+
		"\u0000KL\u0005<\u0000\u0000LM\u0005=\u0000\u0000M\u0016\u0001\u0000\u0000"+
		"\u0000NO\u0005>\u0000\u0000OP\u0005=\u0000\u0000P\u0018\u0001\u0000\u0000"+
		"\u0000QR\u0005=\u0000\u0000RS\u0005=\u0000\u0000S\u001a\u0001\u0000\u0000"+
		"\u0000TU\u0005!\u0000\u0000UV\u0005=\u0000\u0000V\u001c\u0001\u0000\u0000"+
		"\u0000WY\u0007\u0000\u0000\u0000XW\u0001\u0000\u0000\u0000YZ\u0001\u0000"+
		"\u0000\u0000ZX\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[\\\u0001"+
		"\u0000\u0000\u0000\\^\u0005.\u0000\u0000]_\u0007\u0000\u0000\u0000^]\u0001"+
		"\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000"+
		"`a\u0001\u0000\u0000\u0000a\u001e\u0001\u0000\u0000\u0000bd\u0007\u0000"+
		"\u0000\u0000cb\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000ec\u0001"+
		"\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000f \u0001\u0000\u0000\u0000"+
		"gh\u0005*\u0000\u0000h\"\u0001\u0000\u0000\u0000ij\u0005/\u0000\u0000"+
		"j$\u0001\u0000\u0000\u0000kl\u0005+\u0000\u0000l&\u0001\u0000\u0000\u0000"+
		"mn\u0005-\u0000\u0000n(\u0001\u0000\u0000\u0000op\u0005&\u0000\u0000p"+
		"q\u0005&\u0000\u0000q*\u0001\u0000\u0000\u0000rs\u0005|\u0000\u0000st"+
		"\u0005|\u0000\u0000t,\u0001\u0000\u0000\u0000uv\u0005!\u0000\u0000v.\u0001"+
		"\u0000\u0000\u0000w{\u0007\u0001\u0000\u0000xz\u0007\u0002\u0000\u0000"+
		"yx\u0001\u0000\u0000\u0000z}\u0001\u0000\u0000\u0000{y\u0001\u0000\u0000"+
		"\u0000{|\u0001\u0000\u0000\u0000|0\u0001\u0000\u0000\u0000}{\u0001\u0000"+
		"\u0000\u0000~\u0082\u0005\"\u0000\u0000\u007f\u0081\b\u0003\u0000\u0000"+
		"\u0080\u007f\u0001\u0000\u0000\u0000\u0081\u0084\u0001\u0000\u0000\u0000"+
		"\u0082\u0080\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000"+
		"\u0083\u0085\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000\u0000\u0000"+
		"\u0085\u0086\u0005\"\u0000\u0000\u00862\u0001\u0000\u0000\u0000\u0087"+
		"\u0089\u0007\u0004\u0000\u0000\u0088\u0087\u0001\u0000\u0000\u0000\u0089"+
		"\u008a\u0001\u0000\u0000\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008a"+
		"\u008b\u0001\u0000\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c"+
		"\u008d\u0006\u0019\u0000\u0000\u008d4\u0001\u0000\u0000\u0000\u0007\u0000"+
		"Z`e{\u0082\u008a\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}