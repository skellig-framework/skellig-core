package org.skellig.feature.parser;// Generated from SkelligFeature.g4 by ANTLR 4.13.1

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class SkelligFeatureParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		FEATURE=1, SCENARIO=2, GIVEN=3, WHEN=4, THEN=5, AND=6, STAR=7, EXAMPLES=8, 
		BEFORE_FEATURE=9, BEFORE_TEST_SCENARIO=10, AFTER_FEATURE=11, AFTER_TEST_SCENARIO=12, 
		PIPE=13, TAG=14, TEXT=15, NEWLINE=16, COMMENT=17, WS=18;
	public static final int
		RULE_featureFile = 0, RULE_beforeFeature = 1, RULE_beforeTestScenario = 2, 
		RULE_afterFeature = 3, RULE_afterTestScenario = 4, RULE_feature = 5, RULE_scenario = 6, 
		RULE_step = 7, RULE_examples = 8, RULE_parametersTable = 9, RULE_parametersRow = 10, 
		RULE_tagList = 11, RULE_tag = 12, RULE_title = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"featureFile", "beforeFeature", "beforeTestScenario", "afterFeature", 
			"afterTestScenario", "feature", "scenario", "step", "examples", "parametersTable", 
			"parametersRow", "tagList", "tag", "title"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'Feature:'", "'Scenario:'", "'Given'", "'When'", "'Then'", "'And'", 
			"'*'", "'Examples:'", "'Before Feature:'", "'Before Test Scenario:'", 
			"'After Feature:'", "'After Test Scenario:'", "'|'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "FEATURE", "SCENARIO", "GIVEN", "WHEN", "THEN", "AND", "STAR", 
			"EXAMPLES", "BEFORE_FEATURE", "BEFORE_TEST_SCENARIO", "AFTER_FEATURE", 
			"AFTER_TEST_SCENARIO", "PIPE", "TAG", "TEXT", "NEWLINE", "COMMENT", "WS"
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

	@Override
	public String getGrammarFileName() { return "SkelligFeature.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SkelligFeatureParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FeatureFileContext extends ParserRuleContext {
		public FeatureContext feature() {
			return getRuleContext(FeatureContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SkelligFeatureParser.EOF, 0); }
		public BeforeFeatureContext beforeFeature() {
			return getRuleContext(BeforeFeatureContext.class,0);
		}
		public BeforeTestScenarioContext beforeTestScenario() {
			return getRuleContext(BeforeTestScenarioContext.class,0);
		}
		public List<ScenarioContext> scenario() {
			return getRuleContexts(ScenarioContext.class);
		}
		public ScenarioContext scenario(int i) {
			return getRuleContext(ScenarioContext.class,i);
		}
		public AfterFeatureContext afterFeature() {
			return getRuleContext(AfterFeatureContext.class,0);
		}
		public AfterTestScenarioContext afterTestScenario() {
			return getRuleContext(AfterTestScenarioContext.class,0);
		}
		public FeatureFileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_featureFile; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterFeatureFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitFeatureFile(this);
		}
	}

	public final FeatureFileContext featureFile() throws RecognitionException {
		FeatureFileContext _localctx = new FeatureFileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_featureFile);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			feature();
			setState(30);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(29);
				beforeFeature();
				}
				break;
			}
			setState(33);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(32);
				beforeTestScenario();
				}
				break;
			}
			setState(38);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(35);
					scenario();
					}
					} 
				}
				setState(40);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			setState(42);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(41);
				afterFeature();
				}
				break;
			}
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(44);
				afterTestScenario();
				}
			}

			setState(47);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BeforeFeatureContext extends ParserRuleContext {
		public TerminalNode BEFORE_FEATURE() { return getToken(SkelligFeatureParser.BEFORE_FEATURE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public List<StepContext> step() {
			return getRuleContexts(StepContext.class);
		}
		public StepContext step(int i) {
			return getRuleContext(StepContext.class,i);
		}
		public BeforeFeatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_beforeFeature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterBeforeFeature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitBeforeFeature(this);
		}
	}

	public final BeforeFeatureContext beforeFeature() throws RecognitionException {
		BeforeFeatureContext _localctx = new BeforeFeatureContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_beforeFeature);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(50); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(49);
				match(NEWLINE);
				}
				}
				setState(52); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(54);
			match(BEFORE_FEATURE);
			setState(56); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(55);
					step();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(58); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BeforeTestScenarioContext extends ParserRuleContext {
		public TerminalNode BEFORE_TEST_SCENARIO() { return getToken(SkelligFeatureParser.BEFORE_TEST_SCENARIO, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public List<StepContext> step() {
			return getRuleContexts(StepContext.class);
		}
		public StepContext step(int i) {
			return getRuleContext(StepContext.class,i);
		}
		public BeforeTestScenarioContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_beforeTestScenario; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterBeforeTestScenario(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitBeforeTestScenario(this);
		}
	}

	public final BeforeTestScenarioContext beforeTestScenario() throws RecognitionException {
		BeforeTestScenarioContext _localctx = new BeforeTestScenarioContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_beforeTestScenario);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(61); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(60);
				match(NEWLINE);
				}
				}
				setState(63); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(65);
			match(BEFORE_TEST_SCENARIO);
			setState(67); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(66);
					step();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(69); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AfterFeatureContext extends ParserRuleContext {
		public TerminalNode AFTER_FEATURE() { return getToken(SkelligFeatureParser.AFTER_FEATURE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public List<StepContext> step() {
			return getRuleContexts(StepContext.class);
		}
		public StepContext step(int i) {
			return getRuleContext(StepContext.class,i);
		}
		public AfterFeatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_afterFeature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterAfterFeature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitAfterFeature(this);
		}
	}

	public final AfterFeatureContext afterFeature() throws RecognitionException {
		AfterFeatureContext _localctx = new AfterFeatureContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_afterFeature);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(72); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(71);
				match(NEWLINE);
				}
				}
				setState(74); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(76);
			match(AFTER_FEATURE);
			setState(78); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(77);
					step();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(80); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AfterTestScenarioContext extends ParserRuleContext {
		public TerminalNode AFTER_TEST_SCENARIO() { return getToken(SkelligFeatureParser.AFTER_TEST_SCENARIO, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public List<StepContext> step() {
			return getRuleContexts(StepContext.class);
		}
		public StepContext step(int i) {
			return getRuleContext(StepContext.class,i);
		}
		public AfterTestScenarioContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_afterTestScenario; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterAfterTestScenario(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitAfterTestScenario(this);
		}
	}

	public final AfterTestScenarioContext afterTestScenario() throws RecognitionException {
		AfterTestScenarioContext _localctx = new AfterTestScenarioContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_afterTestScenario);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(82);
				match(NEWLINE);
				}
				}
				setState(85); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(87);
			match(AFTER_TEST_SCENARIO);
			setState(89); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(88);
				step();
				}
				}
				setState(91); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FeatureContext extends ParserRuleContext {
		public TerminalNode FEATURE() { return getToken(SkelligFeatureParser.FEATURE, 0); }
		public TitleContext title() {
			return getRuleContext(TitleContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public List<TagListContext> tagList() {
			return getRuleContexts(TagListContext.class);
		}
		public TagListContext tagList(int i) {
			return getRuleContext(TagListContext.class,i);
		}
		public FeatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_feature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterFeature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitFeature(this);
		}
	}

	public final FeatureContext feature() throws RecognitionException {
		FeatureContext _localctx = new FeatureContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_feature);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(93);
				match(NEWLINE);
				}
				}
				setState(98);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG) {
				{
				{
				setState(99);
				tagList();
				}
				}
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(105);
			match(FEATURE);
			setState(106);
			title();
			setState(110);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(107);
					match(NEWLINE);
					}
					} 
				}
				setState(112);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ScenarioContext extends ParserRuleContext {
		public TerminalNode SCENARIO() { return getToken(SkelligFeatureParser.SCENARIO, 0); }
		public TitleContext title() {
			return getRuleContext(TitleContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public List<TagListContext> tagList() {
			return getRuleContexts(TagListContext.class);
		}
		public TagListContext tagList(int i) {
			return getRuleContext(TagListContext.class,i);
		}
		public List<StepContext> step() {
			return getRuleContexts(StepContext.class);
		}
		public StepContext step(int i) {
			return getRuleContext(StepContext.class,i);
		}
		public List<ExamplesContext> examples() {
			return getRuleContexts(ExamplesContext.class);
		}
		public ExamplesContext examples(int i) {
			return getRuleContext(ExamplesContext.class,i);
		}
		public ScenarioContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scenario; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterScenario(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitScenario(this);
		}
	}

	public final ScenarioContext scenario() throws RecognitionException {
		ScenarioContext _localctx = new ScenarioContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_scenario);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(114); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(113);
				match(NEWLINE);
				}
				}
				setState(116); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG) {
				{
				{
				setState(118);
				tagList();
				}
				}
				setState(123);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(124);
			match(SCENARIO);
			setState(125);
			title();
			setState(129);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(126);
					step();
					}
					} 
				}
				setState(131);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			setState(135);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(132);
					examples();
					}
					} 
				}
				setState(137);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StepContext extends ParserRuleContext {
		public TitleContext title() {
			return getRuleContext(TitleContext.class,0);
		}
		public TerminalNode GIVEN() { return getToken(SkelligFeatureParser.GIVEN, 0); }
		public TerminalNode WHEN() { return getToken(SkelligFeatureParser.WHEN, 0); }
		public TerminalNode THEN() { return getToken(SkelligFeatureParser.THEN, 0); }
		public TerminalNode AND() { return getToken(SkelligFeatureParser.AND, 0); }
		public TerminalNode STAR() { return getToken(SkelligFeatureParser.STAR, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public ParametersTableContext parametersTable() {
			return getRuleContext(ParametersTableContext.class,0);
		}
		public StepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_step; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterStep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitStep(this);
		}
	}

	public final StepContext step() throws RecognitionException {
		StepContext _localctx = new StepContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_step);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(139); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(138);
				match(NEWLINE);
				}
				}
				setState(141); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(143);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 248L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(144);
			title();
			setState(146);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(145);
				parametersTable();
				}
				break;
			}
			setState(151);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(148);
					match(NEWLINE);
					}
					} 
				}
				setState(153);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExamplesContext extends ParserRuleContext {
		public TerminalNode EXAMPLES() { return getToken(SkelligFeatureParser.EXAMPLES, 0); }
		public ParametersTableContext parametersTable() {
			return getRuleContext(ParametersTableContext.class,0);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public List<TagListContext> tagList() {
			return getRuleContexts(TagListContext.class);
		}
		public TagListContext tagList(int i) {
			return getRuleContext(TagListContext.class,i);
		}
		public ExamplesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_examples; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterExamples(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitExamples(this);
		}
	}

	public final ExamplesContext examples() throws RecognitionException {
		ExamplesContext _localctx = new ExamplesContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_examples);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(154);
				match(NEWLINE);
				}
				}
				setState(157); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			setState(162);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG) {
				{
				{
				setState(159);
				tagList();
				}
				}
				setState(164);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(165);
			match(EXAMPLES);
			setState(166);
			parametersTable();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParametersTableContext extends ParserRuleContext {
		public TerminalNode NEWLINE() { return getToken(SkelligFeatureParser.NEWLINE, 0); }
		public List<ParametersRowContext> parametersRow() {
			return getRuleContexts(ParametersRowContext.class);
		}
		public ParametersRowContext parametersRow(int i) {
			return getRuleContext(ParametersRowContext.class,i);
		}
		public ParametersTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parametersTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterParametersTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitParametersTable(this);
		}
	}

	public final ParametersTableContext parametersTable() throws RecognitionException {
		ParametersTableContext _localctx = new ParametersTableContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_parametersTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			match(NEWLINE);
			setState(170); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(169);
				parametersRow();
				}
				}
				setState(172); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==PIPE );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParametersRowContext extends ParserRuleContext {
		public List<TerminalNode> PIPE() { return getTokens(SkelligFeatureParser.PIPE); }
		public TerminalNode PIPE(int i) {
			return getToken(SkelligFeatureParser.PIPE, i);
		}
		public TerminalNode NEWLINE() { return getToken(SkelligFeatureParser.NEWLINE, 0); }
		public List<TerminalNode> TEXT() { return getTokens(SkelligFeatureParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(SkelligFeatureParser.TEXT, i);
		}
		public ParametersRowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parametersRow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterParametersRow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitParametersRow(this);
		}
	}

	public final ParametersRowContext parametersRow() throws RecognitionException {
		ParametersRowContext _localctx = new ParametersRowContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_parametersRow);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(174);
			match(PIPE);
			setState(181); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(176); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(175);
					match(TEXT);
					}
					}
					setState(178); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==TEXT );
				setState(180);
				match(PIPE);
				}
				}
				setState(183); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==TEXT );
			setState(186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				setState(185);
				match(NEWLINE);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TagListContext extends ParserRuleContext {
		public List<TagContext> tag() {
			return getRuleContexts(TagContext.class);
		}
		public TagContext tag(int i) {
			return getRuleContext(TagContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(SkelligFeatureParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(SkelligFeatureParser.NEWLINE, i);
		}
		public TagListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterTagList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitTagList(this);
		}
	}

	public final TagListContext tagList() throws RecognitionException {
		TagListContext _localctx = new TagListContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_tagList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(188);
				tag();
				}
				}
				setState(191); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==TAG );
			setState(194); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(193);
				match(NEWLINE);
				}
				}
				setState(196); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEWLINE );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TagContext extends ParserRuleContext {
		public TerminalNode TAG() { return getToken(SkelligFeatureParser.TAG, 0); }
		public TagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitTag(this);
		}
	}

	public final TagContext tag() throws RecognitionException {
		TagContext _localctx = new TagContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_tag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			match(TAG);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TitleContext extends ParserRuleContext {
		public List<TerminalNode> TEXT() { return getTokens(SkelligFeatureParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(SkelligFeatureParser.TEXT, i);
		}
		public TitleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_title; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).enterTitle(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligFeatureListener) ((SkelligFeatureListener)listener).exitTitle(this);
		}
	}

	public final TitleContext title() throws RecognitionException {
		TitleContext _localctx = new TitleContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_title);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(200);
				match(TEXT);
				}
				}
				setState(203); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==TEXT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0012\u00ce\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0001\u0000\u0001\u0000\u0003\u0000\u001f"+
		"\b\u0000\u0001\u0000\u0003\u0000\"\b\u0000\u0001\u0000\u0005\u0000%\b"+
		"\u0000\n\u0000\f\u0000(\t\u0000\u0001\u0000\u0003\u0000+\b\u0000\u0001"+
		"\u0000\u0003\u0000.\b\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0004"+
		"\u00013\b\u0001\u000b\u0001\f\u00014\u0001\u0001\u0001\u0001\u0004\u0001"+
		"9\b\u0001\u000b\u0001\f\u0001:\u0001\u0002\u0004\u0002>\b\u0002\u000b"+
		"\u0002\f\u0002?\u0001\u0002\u0001\u0002\u0004\u0002D\b\u0002\u000b\u0002"+
		"\f\u0002E\u0001\u0003\u0004\u0003I\b\u0003\u000b\u0003\f\u0003J\u0001"+
		"\u0003\u0001\u0003\u0004\u0003O\b\u0003\u000b\u0003\f\u0003P\u0001\u0004"+
		"\u0004\u0004T\b\u0004\u000b\u0004\f\u0004U\u0001\u0004\u0001\u0004\u0004"+
		"\u0004Z\b\u0004\u000b\u0004\f\u0004[\u0001\u0005\u0005\u0005_\b\u0005"+
		"\n\u0005\f\u0005b\t\u0005\u0001\u0005\u0005\u0005e\b\u0005\n\u0005\f\u0005"+
		"h\t\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005m\b\u0005\n\u0005"+
		"\f\u0005p\t\u0005\u0001\u0006\u0004\u0006s\b\u0006\u000b\u0006\f\u0006"+
		"t\u0001\u0006\u0005\u0006x\b\u0006\n\u0006\f\u0006{\t\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0005\u0006\u0080\b\u0006\n\u0006\f\u0006\u0083"+
		"\t\u0006\u0001\u0006\u0005\u0006\u0086\b\u0006\n\u0006\f\u0006\u0089\t"+
		"\u0006\u0001\u0007\u0004\u0007\u008c\b\u0007\u000b\u0007\f\u0007\u008d"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0093\b\u0007\u0001\u0007"+
		"\u0005\u0007\u0096\b\u0007\n\u0007\f\u0007\u0099\t\u0007\u0001\b\u0004"+
		"\b\u009c\b\b\u000b\b\f\b\u009d\u0001\b\u0005\b\u00a1\b\b\n\b\f\b\u00a4"+
		"\t\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0004\t\u00ab\b\t\u000b\t"+
		"\f\t\u00ac\u0001\n\u0001\n\u0004\n\u00b1\b\n\u000b\n\f\n\u00b2\u0001\n"+
		"\u0004\n\u00b6\b\n\u000b\n\f\n\u00b7\u0001\n\u0003\n\u00bb\b\n\u0001\u000b"+
		"\u0004\u000b\u00be\b\u000b\u000b\u000b\f\u000b\u00bf\u0001\u000b\u0004"+
		"\u000b\u00c3\b\u000b\u000b\u000b\f\u000b\u00c4\u0001\f\u0001\f\u0001\r"+
		"\u0004\r\u00ca\b\r\u000b\r\f\r\u00cb\u0001\r\u0000\u0000\u000e\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u0000\u0001"+
		"\u0001\u0000\u0003\u0007\u00df\u0000\u001c\u0001\u0000\u0000\u0000\u0002"+
		"2\u0001\u0000\u0000\u0000\u0004=\u0001\u0000\u0000\u0000\u0006H\u0001"+
		"\u0000\u0000\u0000\bS\u0001\u0000\u0000\u0000\n`\u0001\u0000\u0000\u0000"+
		"\fr\u0001\u0000\u0000\u0000\u000e\u008b\u0001\u0000\u0000\u0000\u0010"+
		"\u009b\u0001\u0000\u0000\u0000\u0012\u00a8\u0001\u0000\u0000\u0000\u0014"+
		"\u00ae\u0001\u0000\u0000\u0000\u0016\u00bd\u0001\u0000\u0000\u0000\u0018"+
		"\u00c6\u0001\u0000\u0000\u0000\u001a\u00c9\u0001\u0000\u0000\u0000\u001c"+
		"\u001e\u0003\n\u0005\u0000\u001d\u001f\u0003\u0002\u0001\u0000\u001e\u001d"+
		"\u0001\u0000\u0000\u0000\u001e\u001f\u0001\u0000\u0000\u0000\u001f!\u0001"+
		"\u0000\u0000\u0000 \"\u0003\u0004\u0002\u0000! \u0001\u0000\u0000\u0000"+
		"!\"\u0001\u0000\u0000\u0000\"&\u0001\u0000\u0000\u0000#%\u0003\f\u0006"+
		"\u0000$#\u0001\u0000\u0000\u0000%(\u0001\u0000\u0000\u0000&$\u0001\u0000"+
		"\u0000\u0000&\'\u0001\u0000\u0000\u0000\'*\u0001\u0000\u0000\u0000(&\u0001"+
		"\u0000\u0000\u0000)+\u0003\u0006\u0003\u0000*)\u0001\u0000\u0000\u0000"+
		"*+\u0001\u0000\u0000\u0000+-\u0001\u0000\u0000\u0000,.\u0003\b\u0004\u0000"+
		"-,\u0001\u0000\u0000\u0000-.\u0001\u0000\u0000\u0000./\u0001\u0000\u0000"+
		"\u0000/0\u0005\u0000\u0000\u00010\u0001\u0001\u0000\u0000\u000013\u0005"+
		"\u0010\u0000\u000021\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u0000"+
		"42\u0001\u0000\u0000\u000045\u0001\u0000\u0000\u000056\u0001\u0000\u0000"+
		"\u000068\u0005\t\u0000\u000079\u0003\u000e\u0007\u000087\u0001\u0000\u0000"+
		"\u00009:\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000:;\u0001\u0000"+
		"\u0000\u0000;\u0003\u0001\u0000\u0000\u0000<>\u0005\u0010\u0000\u0000"+
		"=<\u0001\u0000\u0000\u0000>?\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000"+
		"\u0000?@\u0001\u0000\u0000\u0000@A\u0001\u0000\u0000\u0000AC\u0005\n\u0000"+
		"\u0000BD\u0003\u000e\u0007\u0000CB\u0001\u0000\u0000\u0000DE\u0001\u0000"+
		"\u0000\u0000EC\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000F\u0005"+
		"\u0001\u0000\u0000\u0000GI\u0005\u0010\u0000\u0000HG\u0001\u0000\u0000"+
		"\u0000IJ\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000JK\u0001\u0000"+
		"\u0000\u0000KL\u0001\u0000\u0000\u0000LN\u0005\u000b\u0000\u0000MO\u0003"+
		"\u000e\u0007\u0000NM\u0001\u0000\u0000\u0000OP\u0001\u0000\u0000\u0000"+
		"PN\u0001\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000Q\u0007\u0001\u0000"+
		"\u0000\u0000RT\u0005\u0010\u0000\u0000SR\u0001\u0000\u0000\u0000TU\u0001"+
		"\u0000\u0000\u0000US\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000"+
		"VW\u0001\u0000\u0000\u0000WY\u0005\f\u0000\u0000XZ\u0003\u000e\u0007\u0000"+
		"YX\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000"+
		"\u0000[\\\u0001\u0000\u0000\u0000\\\t\u0001\u0000\u0000\u0000]_\u0005"+
		"\u0010\u0000\u0000^]\u0001\u0000\u0000\u0000_b\u0001\u0000\u0000\u0000"+
		"`^\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000af\u0001\u0000\u0000"+
		"\u0000b`\u0001\u0000\u0000\u0000ce\u0003\u0016\u000b\u0000dc\u0001\u0000"+
		"\u0000\u0000eh\u0001\u0000\u0000\u0000fd\u0001\u0000\u0000\u0000fg\u0001"+
		"\u0000\u0000\u0000gi\u0001\u0000\u0000\u0000hf\u0001\u0000\u0000\u0000"+
		"ij\u0005\u0001\u0000\u0000jn\u0003\u001a\r\u0000km\u0005\u0010\u0000\u0000"+
		"lk\u0001\u0000\u0000\u0000mp\u0001\u0000\u0000\u0000nl\u0001\u0000\u0000"+
		"\u0000no\u0001\u0000\u0000\u0000o\u000b\u0001\u0000\u0000\u0000pn\u0001"+
		"\u0000\u0000\u0000qs\u0005\u0010\u0000\u0000rq\u0001\u0000\u0000\u0000"+
		"st\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000\u0000tu\u0001\u0000\u0000"+
		"\u0000uy\u0001\u0000\u0000\u0000vx\u0003\u0016\u000b\u0000wv\u0001\u0000"+
		"\u0000\u0000x{\u0001\u0000\u0000\u0000yw\u0001\u0000\u0000\u0000yz\u0001"+
		"\u0000\u0000\u0000z|\u0001\u0000\u0000\u0000{y\u0001\u0000\u0000\u0000"+
		"|}\u0005\u0002\u0000\u0000}\u0081\u0003\u001a\r\u0000~\u0080\u0003\u000e"+
		"\u0007\u0000\u007f~\u0001\u0000\u0000\u0000\u0080\u0083\u0001\u0000\u0000"+
		"\u0000\u0081\u007f\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000"+
		"\u0000\u0082\u0087\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000"+
		"\u0000\u0084\u0086\u0003\u0010\b\u0000\u0085\u0084\u0001\u0000\u0000\u0000"+
		"\u0086\u0089\u0001\u0000\u0000\u0000\u0087\u0085\u0001\u0000\u0000\u0000"+
		"\u0087\u0088\u0001\u0000\u0000\u0000\u0088\r\u0001\u0000\u0000\u0000\u0089"+
		"\u0087\u0001\u0000\u0000\u0000\u008a\u008c\u0005\u0010\u0000\u0000\u008b"+
		"\u008a\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000\u0000\u008d"+
		"\u008b\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000\u0000\u0000\u008e"+
		"\u008f\u0001\u0000\u0000\u0000\u008f\u0090\u0007\u0000\u0000\u0000\u0090"+
		"\u0092\u0003\u001a\r\u0000\u0091\u0093\u0003\u0012\t\u0000\u0092\u0091"+
		"\u0001\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0093\u0097"+
		"\u0001\u0000\u0000\u0000\u0094\u0096\u0005\u0010\u0000\u0000\u0095\u0094"+
		"\u0001\u0000\u0000\u0000\u0096\u0099\u0001\u0000\u0000\u0000\u0097\u0095"+
		"\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000\u0000\u0098\u000f"+
		"\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u009a\u009c"+
		"\u0005\u0010\u0000\u0000\u009b\u009a\u0001\u0000\u0000\u0000\u009c\u009d"+
		"\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e"+
		"\u0001\u0000\u0000\u0000\u009e\u00a2\u0001\u0000\u0000\u0000\u009f\u00a1"+
		"\u0003\u0016\u000b\u0000\u00a0\u009f\u0001\u0000\u0000\u0000\u00a1\u00a4"+
		"\u0001\u0000\u0000\u0000\u00a2\u00a0\u0001\u0000\u0000\u0000\u00a2\u00a3"+
		"\u0001\u0000\u0000\u0000\u00a3\u00a5\u0001\u0000\u0000\u0000\u00a4\u00a2"+
		"\u0001\u0000\u0000\u0000\u00a5\u00a6\u0005\b\u0000\u0000\u00a6\u00a7\u0003"+
		"\u0012\t\u0000\u00a7\u0011\u0001\u0000\u0000\u0000\u00a8\u00aa\u0005\u0010"+
		"\u0000\u0000\u00a9\u00ab\u0003\u0014\n\u0000\u00aa\u00a9\u0001\u0000\u0000"+
		"\u0000\u00ab\u00ac\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000"+
		"\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad\u0013\u0001\u0000\u0000"+
		"\u0000\u00ae\u00b5\u0005\r\u0000\u0000\u00af\u00b1\u0005\u000f\u0000\u0000"+
		"\u00b0\u00af\u0001\u0000\u0000\u0000\u00b1\u00b2\u0001\u0000\u0000\u0000"+
		"\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000"+
		"\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b6\u0005\r\u0000\u0000\u00b5"+
		"\u00b0\u0001\u0000\u0000\u0000\u00b6\u00b7\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b5\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8"+
		"\u00ba\u0001\u0000\u0000\u0000\u00b9\u00bb\u0005\u0010\u0000\u0000\u00ba"+
		"\u00b9\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb"+
		"\u0015\u0001\u0000\u0000\u0000\u00bc\u00be\u0003\u0018\f\u0000\u00bd\u00bc"+
		"\u0001\u0000\u0000\u0000\u00be\u00bf\u0001\u0000\u0000\u0000\u00bf\u00bd"+
		"\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c2"+
		"\u0001\u0000\u0000\u0000\u00c1\u00c3\u0005\u0010\u0000\u0000\u00c2\u00c1"+
		"\u0001\u0000\u0000\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4\u00c2"+
		"\u0001\u0000\u0000\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000\u00c5\u0017"+
		"\u0001\u0000\u0000\u0000\u00c6\u00c7\u0005\u000e\u0000\u0000\u00c7\u0019"+
		"\u0001\u0000\u0000\u0000\u00c8\u00ca\u0005\u000f\u0000\u0000\u00c9\u00c8"+
		"\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001\u0000\u0000\u0000\u00cb\u00c9"+
		"\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc\u001b"+
		"\u0001\u0000\u0000\u0000 \u001e!&*-4:?EJPU[`fnty\u0081\u0087\u008d\u0092"+
		"\u0097\u009d\u00a2\u00ac\u00b2\u00b7\u00ba\u00bf\u00c4\u00cb";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}