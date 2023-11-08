package org.skellig.teststep.reader.sts.parser.value;// Generated from SkelligTestValueGrammar.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class SkelligTestValueGrammarParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, LESSER=7, GREATER=8, LESSER_EQUAL=9, 
		GREATER_EQUAL=10, EQUAL=11, NOT_EQUAL=12, FLOAT=13, INT=14, MULT=15, DIV=16, 
		ADD=17, SUB=18, AND=19, OR=20, NOT=21, COMMA=22, DOT=23, LAMBDA=24, ID=25, 
		STRING=26, WS=27;
	public static final int
		RULE_start = 0, RULE_logicalExpression = 1, RULE_comparison = 2, RULE_expression = 3, 
		RULE_callChain = 4, RULE_functionBase = 5, RULE_functionCall = 6, RULE_arg = 7, 
		RULE_lambdaExpression = 8, RULE_propertyExpression = 9, RULE_propertyKey = 10, 
		RULE_arrayValueAccessor = 11, RULE_number = 12, RULE_comparator = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "logicalExpression", "comparison", "expression", "callChain", 
			"functionBase", "functionCall", "arg", "lambdaExpression", "propertyExpression", 
			"propertyKey", "arrayValueAccessor", "number", "comparator"
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

	@Override
	public String getGrammarFileName() { return "SkelligTestValueGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SkelligTestValueGrammarParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StartContext extends ParserRuleContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SkelligTestValueGrammarParser.EOF, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitStart(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			setState(34);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(28);
				logicalExpression(0);
				setState(29);
				match(EOF);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(31);
				expression(0);
				setState(32);
				match(EOF);
				}
				break;
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
	public static class LogicalExpressionContext extends ParserRuleContext {
		public LogicalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalExpression; }
	 
		public LogicalExpressionContext() { }
		public void copyFrom(LogicalExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotExprContext extends LogicalExpressionContext {
		public TerminalNode NOT() { return getToken(SkelligTestValueGrammarParser.NOT, 0); }
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public NotExprContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterNotExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitNotExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OrExprContext extends LogicalExpressionContext {
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public TerminalNode OR() { return getToken(SkelligTestValueGrammarParser.OR, 0); }
		public OrExprContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterOrExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitOrExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonExprContext extends LogicalExpressionContext {
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public ComparisonExprContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterComparisonExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitComparisonExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenthesesLogicalExprContext extends LogicalExpressionContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public ParenthesesLogicalExprContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterParenthesesLogicalExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitParenthesesLogicalExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AndExprContext extends LogicalExpressionContext {
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(SkelligTestValueGrammarParser.AND, 0); }
		public AndExprContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterAndExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitAndExpr(this);
		}
	}

	public final LogicalExpressionContext logicalExpression() throws RecognitionException {
		return logicalExpression(0);
	}

	private LogicalExpressionContext logicalExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		LogicalExpressionContext _localctx = new LogicalExpressionContext(_ctx, _parentState);
		LogicalExpressionContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_logicalExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				_localctx = new NotExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(37);
				match(NOT);
				setState(38);
				logicalExpression(3);
				}
				break;
			case 2:
				{
				_localctx = new ParenthesesLogicalExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(39);
				match(T__0);
				setState(40);
				logicalExpression(0);
				setState(41);
				match(T__1);
				}
				break;
			case 3:
				{
				_localctx = new ComparisonExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(43);
				comparison();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(54);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(52);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						_localctx = new AndExprContext(new LogicalExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_logicalExpression);
						setState(46);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(47);
						match(AND);
						setState(48);
						logicalExpression(6);
						}
						break;
					case 2:
						{
						_localctx = new OrExprContext(new LogicalExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_logicalExpression);
						setState(49);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(50);
						match(OR);
						setState(51);
						logicalExpression(5);
						}
						break;
					}
					} 
				}
				setState(56);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ComparatorContext comparator() {
			return getRuleContext(ComparatorContext.class,0);
		}
		public ComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitComparison(this);
		}
	}

	public final ComparisonContext comparison() throws RecognitionException {
		ComparisonContext _localctx = new ComparisonContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_comparison);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			expression(0);
			setState(58);
			comparator();
			setState(59);
			expression(0);
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
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringExprContext extends ExpressionContext {
		public TerminalNode STRING() { return getToken(SkelligTestValueGrammarParser.STRING, 0); }
		public StringExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterStringExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitStringExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallExpContext extends ExpressionContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public FunctionCallExpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterFunctionCallExp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitFunctionCallExp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CallChainExpContext extends ExpressionContext {
		public CallChainContext callChain() {
			return getRuleContext(CallChainContext.class,0);
		}
		public CallChainExpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterCallChainExp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitCallChainExp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AdditionExprContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ADD() { return getToken(SkelligTestValueGrammarParser.ADD, 0); }
		public AdditionExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterAdditionExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitAdditionExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberExprContext extends ExpressionContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public NumberExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterNumberExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitNumberExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PropertyExprContext extends ExpressionContext {
		public PropertyExpressionContext propertyExpression() {
			return getRuleContext(PropertyExpressionContext.class,0);
		}
		public PropertyExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterPropertyExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitPropertyExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicationExprContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode MULT() { return getToken(SkelligTestValueGrammarParser.MULT, 0); }
		public MultiplicationExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterMultiplicationExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitMultiplicationExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArrayValueAccessorExpContext extends ExpressionContext {
		public ArrayValueAccessorContext arrayValueAccessor() {
			return getRuleContext(ArrayValueAccessorContext.class,0);
		}
		public ArrayValueAccessorExpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterArrayValueAccessorExp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitArrayValueAccessorExp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenthesesExprContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ParenthesesExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterParenthesesExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitParenthesesExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DivisionExprContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode DIV() { return getToken(SkelligTestValueGrammarParser.DIV, 0); }
		public DivisionExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterDivisionExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitDivisionExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SubtractionExprContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode SUB() { return getToken(SkelligTestValueGrammarParser.SUB, 0); }
		public SubtractionExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterSubtractionExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitSubtractionExpr(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IdExprContext extends ExpressionContext {
		public TerminalNode ID() { return getToken(SkelligTestValueGrammarParser.ID, 0); }
		public IdExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterIdExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitIdExpr(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 6;
		enterRecursionRule(_localctx, 6, RULE_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				_localctx = new ArrayValueAccessorExpContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(62);
				arrayValueAccessor();
				}
				break;
			case 2:
				{
				_localctx = new FunctionCallExpContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(63);
				functionCall();
				}
				break;
			case 3:
				{
				_localctx = new PropertyExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(64);
				propertyExpression();
				}
				break;
			case 4:
				{
				_localctx = new ParenthesesExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(65);
				match(T__0);
				setState(66);
				expression(0);
				setState(67);
				match(T__1);
				}
				break;
			case 5:
				{
				_localctx = new StringExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(69);
				match(STRING);
				}
				break;
			case 6:
				{
				_localctx = new NumberExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(70);
				number();
				}
				break;
			case 7:
				{
				_localctx = new IdExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(71);
				match(ID);
				}
				break;
			case 8:
				{
				_localctx = new CallChainExpContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(72);
				callChain();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(89);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(87);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
					case 1:
						{
						_localctx = new MultiplicationExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(75);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(76);
						match(MULT);
						setState(77);
						expression(13);
						}
						break;
					case 2:
						{
						_localctx = new DivisionExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(78);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(79);
						match(DIV);
						setState(80);
						expression(12);
						}
						break;
					case 3:
						{
						_localctx = new AdditionExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(81);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(82);
						match(ADD);
						setState(83);
						expression(11);
						}
						break;
					case 4:
						{
						_localctx = new SubtractionExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(84);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(85);
						match(SUB);
						setState(86);
						expression(10);
						}
						break;
					}
					} 
				}
				setState(91);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallChainContext extends ParserRuleContext {
		public List<FunctionBaseContext> functionBase() {
			return getRuleContexts(FunctionBaseContext.class);
		}
		public FunctionBaseContext functionBase(int i) {
			return getRuleContext(FunctionBaseContext.class,i);
		}
		public PropertyExpressionContext propertyExpression() {
			return getRuleContext(PropertyExpressionContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(SkelligTestValueGrammarParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SkelligTestValueGrammarParser.DOT, i);
		}
		public CallChainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callChain; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterCallChain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitCallChain(this);
		}
	}

	public final CallChainContext callChain() throws RecognitionException {
		CallChainContext _localctx = new CallChainContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_callChain);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case STRING:
				{
				setState(92);
				functionBase();
				}
				break;
			case T__2:
				{
				setState(93);
				propertyExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(100);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(96);
					match(DOT);
					setState(97);
					functionBase();
					}
					} 
				}
				setState(102);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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
	public static class FunctionBaseContext extends ParserRuleContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public ArrayValueAccessorContext arrayValueAccessor() {
			return getRuleContext(ArrayValueAccessorContext.class,0);
		}
		public TerminalNode ID() { return getToken(SkelligTestValueGrammarParser.ID, 0); }
		public TerminalNode STRING() { return getToken(SkelligTestValueGrammarParser.STRING, 0); }
		public FunctionBaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionBase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterFunctionBase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitFunctionBase(this);
		}
	}

	public final FunctionBaseContext functionBase() throws RecognitionException {
		FunctionBaseContext _localctx = new FunctionBaseContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_functionBase);
		try {
			setState(107);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(103);
				functionCall();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(104);
				arrayValueAccessor();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(105);
				match(ID);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(106);
				match(STRING);
				}
				break;
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
	public static class FunctionCallContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SkelligTestValueGrammarParser.ID, 0); }
		public List<ArgContext> arg() {
			return getRuleContexts(ArgContext.class);
		}
		public ArgContext arg(int i) {
			return getRuleContext(ArgContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SkelligTestValueGrammarParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SkelligTestValueGrammarParser.COMMA, i);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitFunctionCall(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			match(ID);
			setState(110);
			match(T__0);
			setState(119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 102785034L) != 0)) {
				{
				setState(111);
				arg();
				setState(116);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(112);
					match(COMMA);
					setState(113);
					arg();
					}
					}
					setState(118);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(121);
			match(T__1);
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
	public static class ArgContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public LambdaExpressionContext lambdaExpression() {
			return getRuleContext(LambdaExpressionContext.class,0);
		}
		public ArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitArg(this);
		}
	}

	public final ArgContext arg() throws RecognitionException {
		ArgContext _localctx = new ArgContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_arg);
		try {
			setState(127);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(123);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(124);
				logicalExpression(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(125);
				comparison();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(126);
				lambdaExpression();
				}
				break;
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
	public static class LambdaExpressionContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SkelligTestValueGrammarParser.ID, 0); }
		public TerminalNode LAMBDA() { return getToken(SkelligTestValueGrammarParser.LAMBDA, 0); }
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LambdaExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterLambdaExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitLambdaExpression(this);
		}
	}

	public final LambdaExpressionContext lambdaExpression() throws RecognitionException {
		LambdaExpressionContext _localctx = new LambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(ID);
			setState(130);
			match(LAMBDA);
			setState(133);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(131);
				logicalExpression(0);
				}
				break;
			case 2:
				{
				setState(132);
				expression(0);
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
	public static class PropertyExpressionContext extends ParserRuleContext {
		public PropertyKeyContext propertyKey() {
			return getRuleContext(PropertyKeyContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(SkelligTestValueGrammarParser.COMMA, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public PropertyExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterPropertyExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitPropertyExpression(this);
		}
	}

	public final PropertyExpressionContext propertyExpression() throws RecognitionException {
		PropertyExpressionContext _localctx = new PropertyExpressionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_propertyExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			match(T__2);
			setState(136);
			propertyKey();
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(137);
				match(COMMA);
				setState(138);
				expression(0);
				}
			}

			setState(141);
			match(T__3);
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
	public static class PropertyKeyContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SkelligTestValueGrammarParser.ID, 0); }
		public TerminalNode INT() { return getToken(SkelligTestValueGrammarParser.INT, 0); }
		public TerminalNode STRING() { return getToken(SkelligTestValueGrammarParser.STRING, 0); }
		public PropertyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterPropertyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitPropertyKey(this);
		}
	}

	public final PropertyKeyContext propertyKey() throws RecognitionException {
		PropertyKeyContext _localctx = new PropertyKeyContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_propertyKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 100679680L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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
	public static class ArrayValueAccessorContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SkelligTestValueGrammarParser.ID, 0); }
		public TerminalNode INT() { return getToken(SkelligTestValueGrammarParser.INT, 0); }
		public ArrayValueAccessorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayValueAccessor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterArrayValueAccessor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitArrayValueAccessor(this);
		}
	}

	public final ArrayValueAccessorContext arrayValueAccessor() throws RecognitionException {
		ArrayValueAccessorContext _localctx = new ArrayValueAccessorContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_arrayValueAccessor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			match(ID);
			setState(146);
			match(T__4);
			setState(147);
			match(INT);
			setState(148);
			match(T__5);
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
	public static class NumberContext extends ParserRuleContext {
		public TerminalNode FLOAT() { return getToken(SkelligTestValueGrammarParser.FLOAT, 0); }
		public TerminalNode INT() { return getToken(SkelligTestValueGrammarParser.INT, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitNumber(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			_la = _input.LA(1);
			if ( !(_la==FLOAT || _la==INT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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
	public static class ComparatorContext extends ParserRuleContext {
		public TerminalNode LESSER() { return getToken(SkelligTestValueGrammarParser.LESSER, 0); }
		public TerminalNode GREATER() { return getToken(SkelligTestValueGrammarParser.GREATER, 0); }
		public TerminalNode LESSER_EQUAL() { return getToken(SkelligTestValueGrammarParser.LESSER_EQUAL, 0); }
		public TerminalNode GREATER_EQUAL() { return getToken(SkelligTestValueGrammarParser.GREATER_EQUAL, 0); }
		public TerminalNode EQUAL() { return getToken(SkelligTestValueGrammarParser.EQUAL, 0); }
		public TerminalNode NOT_EQUAL() { return getToken(SkelligTestValueGrammarParser.NOT_EQUAL, 0); }
		public ComparatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).enterComparator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SkelligTestValueGrammarListener ) ((SkelligTestValueGrammarListener)listener).exitComparator(this);
		}
	}

	public final ComparatorContext comparator() throws RecognitionException {
		ComparatorContext _localctx = new ComparatorContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_comparator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 8064L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return logicalExpression_sempred((LogicalExpressionContext)_localctx, predIndex);
		case 3:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean logicalExpression_sempred(LogicalExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 12);
		case 3:
			return precpred(_ctx, 11);
		case 4:
			return precpred(_ctx, 10);
		case 5:
			return precpred(_ctx, 9);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u001b\u009b\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0003\u0000#\b\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001-\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0005\u00015\b\u0001\n\u0001\f\u00018\t"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003J\b"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0005\u0003X\b\u0003\n\u0003\f\u0003[\t\u0003\u0001\u0004\u0001"+
		"\u0004\u0003\u0004_\b\u0004\u0001\u0004\u0001\u0004\u0005\u0004c\b\u0004"+
		"\n\u0004\f\u0004f\t\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0003\u0005l\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0005\u0006s\b\u0006\n\u0006\f\u0006v\t\u0006\u0003\u0006"+
		"x\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u0080\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0003\b\u0086\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u008c\b\t\u0001"+
		"\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0000\u0002\u0002"+
		"\u0006\u000e\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u0000\u0003\u0002\u0000\u000e\u000e\u0019\u001a\u0001\u0000"+
		"\r\u000e\u0001\u0000\u0007\f\u00a8\u0000\"\u0001\u0000\u0000\u0000\u0002"+
		",\u0001\u0000\u0000\u0000\u00049\u0001\u0000\u0000\u0000\u0006I\u0001"+
		"\u0000\u0000\u0000\b^\u0001\u0000\u0000\u0000\nk\u0001\u0000\u0000\u0000"+
		"\fm\u0001\u0000\u0000\u0000\u000e\u007f\u0001\u0000\u0000\u0000\u0010"+
		"\u0081\u0001\u0000\u0000\u0000\u0012\u0087\u0001\u0000\u0000\u0000\u0014"+
		"\u008f\u0001\u0000\u0000\u0000\u0016\u0091\u0001\u0000\u0000\u0000\u0018"+
		"\u0096\u0001\u0000\u0000\u0000\u001a\u0098\u0001\u0000\u0000\u0000\u001c"+
		"\u001d\u0003\u0002\u0001\u0000\u001d\u001e\u0005\u0000\u0000\u0001\u001e"+
		"#\u0001\u0000\u0000\u0000\u001f \u0003\u0006\u0003\u0000 !\u0005\u0000"+
		"\u0000\u0001!#\u0001\u0000\u0000\u0000\"\u001c\u0001\u0000\u0000\u0000"+
		"\"\u001f\u0001\u0000\u0000\u0000#\u0001\u0001\u0000\u0000\u0000$%\u0006"+
		"\u0001\uffff\uffff\u0000%&\u0005\u0015\u0000\u0000&-\u0003\u0002\u0001"+
		"\u0003\'(\u0005\u0001\u0000\u0000()\u0003\u0002\u0001\u0000)*\u0005\u0002"+
		"\u0000\u0000*-\u0001\u0000\u0000\u0000+-\u0003\u0004\u0002\u0000,$\u0001"+
		"\u0000\u0000\u0000,\'\u0001\u0000\u0000\u0000,+\u0001\u0000\u0000\u0000"+
		"-6\u0001\u0000\u0000\u0000./\n\u0005\u0000\u0000/0\u0005\u0013\u0000\u0000"+
		"05\u0003\u0002\u0001\u000612\n\u0004\u0000\u000023\u0005\u0014\u0000\u0000"+
		"35\u0003\u0002\u0001\u00054.\u0001\u0000\u0000\u000041\u0001\u0000\u0000"+
		"\u000058\u0001\u0000\u0000\u000064\u0001\u0000\u0000\u000067\u0001\u0000"+
		"\u0000\u00007\u0003\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u0000"+
		"9:\u0003\u0006\u0003\u0000:;\u0003\u001a\r\u0000;<\u0003\u0006\u0003\u0000"+
		"<\u0005\u0001\u0000\u0000\u0000=>\u0006\u0003\uffff\uffff\u0000>J\u0003"+
		"\u0016\u000b\u0000?J\u0003\f\u0006\u0000@J\u0003\u0012\t\u0000AB\u0005"+
		"\u0001\u0000\u0000BC\u0003\u0006\u0003\u0000CD\u0005\u0002\u0000\u0000"+
		"DJ\u0001\u0000\u0000\u0000EJ\u0005\u001a\u0000\u0000FJ\u0003\u0018\f\u0000"+
		"GJ\u0005\u0019\u0000\u0000HJ\u0003\b\u0004\u0000I=\u0001\u0000\u0000\u0000"+
		"I?\u0001\u0000\u0000\u0000I@\u0001\u0000\u0000\u0000IA\u0001\u0000\u0000"+
		"\u0000IE\u0001\u0000\u0000\u0000IF\u0001\u0000\u0000\u0000IG\u0001\u0000"+
		"\u0000\u0000IH\u0001\u0000\u0000\u0000JY\u0001\u0000\u0000\u0000KL\n\f"+
		"\u0000\u0000LM\u0005\u000f\u0000\u0000MX\u0003\u0006\u0003\rNO\n\u000b"+
		"\u0000\u0000OP\u0005\u0010\u0000\u0000PX\u0003\u0006\u0003\fQR\n\n\u0000"+
		"\u0000RS\u0005\u0011\u0000\u0000SX\u0003\u0006\u0003\u000bTU\n\t\u0000"+
		"\u0000UV\u0005\u0012\u0000\u0000VX\u0003\u0006\u0003\nWK\u0001\u0000\u0000"+
		"\u0000WN\u0001\u0000\u0000\u0000WQ\u0001\u0000\u0000\u0000WT\u0001\u0000"+
		"\u0000\u0000X[\u0001\u0000\u0000\u0000YW\u0001\u0000\u0000\u0000YZ\u0001"+
		"\u0000\u0000\u0000Z\u0007\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000"+
		"\u0000\\_\u0003\n\u0005\u0000]_\u0003\u0012\t\u0000^\\\u0001\u0000\u0000"+
		"\u0000^]\u0001\u0000\u0000\u0000_d\u0001\u0000\u0000\u0000`a\u0005\u0017"+
		"\u0000\u0000ac\u0003\n\u0005\u0000b`\u0001\u0000\u0000\u0000cf\u0001\u0000"+
		"\u0000\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000e\t\u0001"+
		"\u0000\u0000\u0000fd\u0001\u0000\u0000\u0000gl\u0003\f\u0006\u0000hl\u0003"+
		"\u0016\u000b\u0000il\u0005\u0019\u0000\u0000jl\u0005\u001a\u0000\u0000"+
		"kg\u0001\u0000\u0000\u0000kh\u0001\u0000\u0000\u0000ki\u0001\u0000\u0000"+
		"\u0000kj\u0001\u0000\u0000\u0000l\u000b\u0001\u0000\u0000\u0000mn\u0005"+
		"\u0019\u0000\u0000nw\u0005\u0001\u0000\u0000ot\u0003\u000e\u0007\u0000"+
		"pq\u0005\u0016\u0000\u0000qs\u0003\u000e\u0007\u0000rp\u0001\u0000\u0000"+
		"\u0000sv\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000\u0000tu\u0001\u0000"+
		"\u0000\u0000ux\u0001\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000wo\u0001"+
		"\u0000\u0000\u0000wx\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000"+
		"yz\u0005\u0002\u0000\u0000z\r\u0001\u0000\u0000\u0000{\u0080\u0003\u0006"+
		"\u0003\u0000|\u0080\u0003\u0002\u0001\u0000}\u0080\u0003\u0004\u0002\u0000"+
		"~\u0080\u0003\u0010\b\u0000\u007f{\u0001\u0000\u0000\u0000\u007f|\u0001"+
		"\u0000\u0000\u0000\u007f}\u0001\u0000\u0000\u0000\u007f~\u0001\u0000\u0000"+
		"\u0000\u0080\u000f\u0001\u0000\u0000\u0000\u0081\u0082\u0005\u0019\u0000"+
		"\u0000\u0082\u0085\u0005\u0018\u0000\u0000\u0083\u0086\u0003\u0002\u0001"+
		"\u0000\u0084\u0086\u0003\u0006\u0003\u0000\u0085\u0083\u0001\u0000\u0000"+
		"\u0000\u0085\u0084\u0001\u0000\u0000\u0000\u0086\u0011\u0001\u0000\u0000"+
		"\u0000\u0087\u0088\u0005\u0003\u0000\u0000\u0088\u008b\u0003\u0014\n\u0000"+
		"\u0089\u008a\u0005\u0016\u0000\u0000\u008a\u008c\u0003\u0006\u0003\u0000"+
		"\u008b\u0089\u0001\u0000\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000"+
		"\u008c\u008d\u0001\u0000\u0000\u0000\u008d\u008e\u0005\u0004\u0000\u0000"+
		"\u008e\u0013\u0001\u0000\u0000\u0000\u008f\u0090\u0007\u0000\u0000\u0000"+
		"\u0090\u0015\u0001\u0000\u0000\u0000\u0091\u0092\u0005\u0019\u0000\u0000"+
		"\u0092\u0093\u0005\u0005\u0000\u0000\u0093\u0094\u0005\u000e\u0000\u0000"+
		"\u0094\u0095\u0005\u0006\u0000\u0000\u0095\u0017\u0001\u0000\u0000\u0000"+
		"\u0096\u0097\u0007\u0001\u0000\u0000\u0097\u0019\u0001\u0000\u0000\u0000"+
		"\u0098\u0099\u0007\u0002\u0000\u0000\u0099\u001b\u0001\u0000\u0000\u0000"+
		"\u000f\",46IWY^dktw\u007f\u0085\u008b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}