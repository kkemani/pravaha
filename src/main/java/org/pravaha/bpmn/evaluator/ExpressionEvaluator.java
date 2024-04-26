package org.pravaha.bpmn.evaluator;

import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionEvaluator {
	final static Logger logger = LoggerFactory.getLogger("ExpressionEvaluator");

	public static final String EQ_EXPR = "==";
	public static final String GEQ_EXPR = ">=";
	public static final String GRT_EXPR = ">";
	public static final String LEQ_EXPR = "<=";
	public static final String LT_EXPR = "<";
	public static final String NEQ_EXPR = "!=";
	public static final String O_PARENTHESIS = "(";
	public static final String C_PARENTHESIS = ")";
	public static final String NOOP = "NOOP";
	
	public static final String XML_LT_VALUE = "&lt;";
	public static final String XML_GT_VALUE = "&gt;";
	
	public static final String AND_EXPR = "&";
	public static final String OR_EXPR = "|";
	
	public static final String XML_AND_VALUE = "AND";
	public static final String XML_OR_VALUE = "OR";
	public ExpressionEvaluator(){
		
	}
	
	public boolean evaluateComplexExpression(String expression,Map<String, Object> mapVar ){ 
		if(expression.contains(XML_LT_VALUE))
			expression=expression.replaceAll(XML_LT_VALUE, LT_EXPR);
		else if (expression.contains(XML_GT_VALUE))
			expression=expression.replaceAll(XML_GT_VALUE, GRT_EXPR);
		else if(expression.contains(XML_AND_VALUE)){
		logger.debug("Replacing ",XML_AND_VALUE ," with ",AND_EXPR);
			expression=expression.replaceAll(XML_AND_VALUE,AND_EXPR );
		}
		 if(expression.contains(XML_OR_VALUE))
		{
			logger.debug("Replacing ",XML_OR_VALUE ," with ", OR_EXPR);
			expression=expression.replaceAll(XML_OR_VALUE,OR_EXPR );
		}
				
		logger.debug("Inside Complex Validation = {}",expression);
		boolean result =false;
		Stack<String>operatorStack= new Stack<String>();
		Stack<String>operandStack= new Stack<String>();
		int length= expression.length();
		for(int i=0; i < length;i++)
		{
			if(expression.charAt(i)=='(')
			{
				operandStack.push(O_PARENTHESIS);
				logger.debug("Operand Stack size "+operandStack.size());
			}
			else if(expression.charAt(i)== '$')
			{
				logger.debug("Got $ ");
				String subExpression=expression.substring(i, expression.indexOf(")", i));
				boolean innerExpRes=evaluateExpression(subExpression, mapVar);
				logger.debug("SubExpression "+subExpression +" Result : "+innerExpRes);
				if(!operandStack.isEmpty()) {
					operandStack.pop();					
				}
				//logger.debug("Operand Stack size before "+operandStack.size());
				operandStack.push(String.valueOf(innerExpRes));
				//logger.debug("Operand Stack size after"+operandStack.size());
				logger.debug("value of i ="+i);
				i= expression.indexOf(")", i);
				logger.debug("Increased value of i ="+i);
				if(innerExpRes)
					result = true;
			}
			else if(expression.charAt(i)=='&')
			{
				operatorStack.push(AND_EXPR);
				logger.debug("Got AND and Operator Stack  size "+operatorStack.size());
				
				
			}
			else if(expression.charAt(i)== '|')
			{
				operatorStack.push(OR_EXPR);
				logger.debug("Got OR and Operator Stack  size "+operatorStack.size());
			}
			else if(expression.charAt(i)==')')
			{
				String operatorstcVal= null;
				boolean operandExpres1= false;
				boolean operandExpres2= false;
				int count = 0;
				if(i ==length-1)
				{
					while(!(operatorStack.isEmpty()))
					{
						while(!(operandStack.isEmpty()))
						{
							if(operandStack.peek().toString().equals("true")){
								if(count == 0)
									operandExpres1= getbooleanExpressionRes(operandStack.pop());
								else
									operandExpres2= getbooleanExpressionRes(operandStack.pop());
								
								count++;
							}
							
							else if(operandStack.peek().toString().equals("false")){
								if(count == 1)
									operandExpres1= getbooleanExpressionRes(operandStack.pop());
								else
									operandExpres2= getbooleanExpressionRes(operandStack.pop());
								
								count++;
							}
							
							else 
								operandStack.pop();
							
							if(count == 2)
							{
								operatorstcVal =getOperator(operatorStack);
								break;
							}
						}
						
						result= getFinalResult(operandExpres1, operatorstcVal, operandExpres2);
						operandStack.push(String.valueOf(result));
						operatorStack.pop();
					}
				}
				else {
				 operandExpres1= getbooleanExpressionRes(operandStack.pop());
				
				//logger.debug("Opertor Stack Content "+operatorStack.peek());
				//logger.debug("operatorStack Content "+operatorStack.peek());
				
					operatorstcVal= getOperator(operatorStack);
					operatorStack.pop();
					 operandExpres2= getbooleanExpressionRes(operandStack.pop());
				if(operatorstcVal.contains("&&"))
				{
					if(operandExpres1 && operandExpres2)
						result= true;
					else 
						result= false;
				}
				if(operatorstcVal.contains("||"))
				{
					if(operandExpres1 || operandExpres2)
						result= true;
					else 
						result= false;
				}
					

				
				operandStack.push(String.valueOf(result));
				}
				if(i ==length-1 && operatorStack.isEmpty())
				{
					for(int j=0 ;j < operandStack.size();i++)
					{
						operandStack.pop();
					}
				}
				}
				
			
		}
		
		
			return result;
		
		
		
	}
	public boolean getFinalResult(boolean operandExpres1 , String operatorstcVal, boolean operandExpres2)
	{
		boolean result =false;
		if(operatorstcVal.contains("&&"))
		{
			if(operandExpres1 && operandExpres2)
				result= true;
			else 
				result= false;
		}
		if(operatorstcVal.contains("||"))
		{
			if(operandExpres1 || operandExpres2)
				result= true;
			else 
				result= false;
		}
		return result;
	}
	public String getOperator(Stack<String> operatorStack)
	{
		String operatorstcVal=null;
		if(operatorStack.peek().toString().contains("&")){
			operatorstcVal=operatorStack.peek().toString();
			operatorstcVal=operatorstcVal.replaceAll("&","&&");
		}
		else if(operatorStack.peek().toString().contains("|"))
			operatorstcVal=operatorStack.peek().toString().replace("|","||");
			
	return operatorstcVal;
		
	}
	public boolean getbooleanExpressionRes(String exp)
	{
		boolean res= false;
		if(exp.contains("true"))
			res= true;
		
		if(exp.contains("false"))
			res= false;
		
		return res;
		
	}
	public boolean evaluateExpression(String expression, Map<String, Object> mapVar){
		
		if(expression.contains(C_PARENTHESIS)||expression.contains(O_PARENTHESIS) || expression.contains(XML_AND_VALUE)||expression.contains(XML_OR_VALUE))
			return evaluateComplexExpression(expression, mapVar);
		else	
		{
		if(expression.contains(XML_LT_VALUE))
			expression.replace(XML_LT_VALUE, LT_EXPR);
		else if (expression.contains(XML_GT_VALUE))
			expression.replace(XML_GT_VALUE, GRT_EXPR);
		String exprOperator = findOperator(expression);
		if(NOOP.equals(exprOperator))
			return true;
		// write the other operations 
		//"$srVer=='ABC'" : $srVer is a String - do a String.equals
			//	"$srCount==10" -> Int/float/Long -> do the compare as needed
			//	> < (only for numerals- Int, Float, Long)
			//	!= ==  (for all - String, Int, float, long, objects)
		
		return evaluateOperator(expression, exprOperator, mapVar);
		}
	}
	
	// 
	private boolean evaluateOperator(String expression,String exprOperator, 
	 Map<String, Object> mapVar) {
		// TODO Auto-generated method stub
		Object lhopValue = null;
		Object rhopValue = null;
		boolean rhopVar = false;
		String lhop = getLHOp(expression, exprOperator);
		if(lhop.contains("$")){
			lhop = lhop.replace("$", "").trim();
//			lhopValue = delegateExecution.getVariable(lhop);
			lhopValue = mapVar.get(lhop);
		}	
		else if(lhop.contains("'")){
			lhopValue =  lhop.replaceAll("'", "").trim();
		}

		String rhop = getRHOp(expression, exprOperator);
		if(rhop.contains("$")){
			rhop = rhop.replace("$", "").trim();
			rhopVar = true;
//			rhopValue = delegateExecution.getVariable(rhop);
			rhopValue = mapVar.get(rhop);
		}	
		else if(rhop.contains("'")){
			rhopValue =  rhop.replaceAll("'", "").trim();
		}
		
		// if both are null - return true
		if(lhopValue==null && rhop==null)
			return true;
		
		if(lhopValue==null && rhop!=null)
			return false;
		
		// get the numerical values and equate 
		if(lhopValue instanceof Long){
			// compare Long value on LHS with long on RHS
			
			try{
				if(!rhopVar && rhop instanceof String)
					rhopValue = new Long(rhop);

				Long lValue = (Long) lhopValue;
				int compareValue = lValue.compareTo((Long)rhopValue);
				//logger.debug(" compareValue="+ compareValue);
				return applyOperation(compareValue, exprOperator);
			}catch(Exception e){
				return false;
			}
		}
		// do this for Float - add methods in BPOUtil to support comparison
		else if(lhopValue instanceof Float){
			// compare Long value on LHS with long on RHS 
			try{
				if(!rhopVar && rhop instanceof String)
					rhopValue = new Float(rhop);
				
				Float lValue = (Float)lhopValue;
				int compareValue = lValue.compareTo((Float)rhopValue);
				
				return applyOperation(compareValue, exprOperator);
			}catch(Exception e){
				return false;
			}
		}
		
		// do this for Integer - add methods in BPOUtil to support comparison
		else if(lhopValue instanceof Integer){
			// compare Long value on LHS with long on RHS 
			try{
				
				if(!rhopVar && rhop instanceof String) {
					if(rhop.contains("$")) {
						rhop = rhop.replaceAll("$", "").trim();
					} else if(rhop.contains("'")) {
						rhop = rhop.replaceAll("'", "").trim();
					}
					rhopValue = new Integer(rhop);
				}
				
				Integer lValue = (Integer) lhopValue;
				int compareValue = lValue.compareTo((Integer)rhopValue);
				return applyOperation(compareValue, exprOperator);
				
			}catch(Exception e){
				return false;
			}
		}
		

		else if(lhopValue instanceof String){
			// compare String value on LHS with long on RHS 
			try{
				String lValue = (String) lhopValue;
				int compareValue = lValue.compareTo((String)rhopValue);				
				return applyOperation(compareValue, exprOperator);
				
				
			}catch(Exception e){
				return false;
			}
		}

		return false;
	}
	
	protected boolean applyOperation(int comparisonValue, String opType)
	{
		if(GEQ_EXPR.equals(opType)){
			if(comparisonValue>0 || comparisonValue==0)
				return true;
			else
				return false;
		}
		else if(GRT_EXPR.equals(opType)){
			if(comparisonValue>0 )
				return true;
			else
				return false;
		}
		else if(EQ_EXPR.equals(opType)){
			if(comparisonValue==0)
				return true;
			else
				return false;
		}
		else if(NEQ_EXPR.equals(opType)){
			if(comparisonValue!=0)
				return true;
			else
				return false;
		}
		else if(LEQ_EXPR.equals(opType)){
			if(comparisonValue<0 || comparisonValue==0)
				return true;
			else
				return false;
		}
		else if(LT_EXPR.equals(opType)){
			if(comparisonValue<0 )
				return true;
			else
				return false;
		}

		return false;
	}

	private String getRHOp(String expression, String geqExpr){
		return  expression.substring(expression.indexOf(geqExpr)+geqExpr.length()).trim();
	}

	private String getLHOp(String expression, String geqExpr) {
		return expression.substring(0,expression.indexOf(geqExpr)).trim();
	}

	

	public String findOperator(String expr){
		if(expr!=null && expr.contains(EQ_EXPR))
			return EQ_EXPR;
		else if(expr!=null && expr.contains(GEQ_EXPR))
			return GEQ_EXPR;
		else if(expr!=null && expr.contains(GRT_EXPR))
			return GRT_EXPR;
		else if(expr!=null && expr.contains(LEQ_EXPR))
			return LEQ_EXPR;
		else if(expr!=null && expr.contains(LT_EXPR))
			return LT_EXPR;
		else if(expr!=null && expr.contains(NEQ_EXPR))
			return NEQ_EXPR;

		// do the checks for the other types
		return NOOP;
	}
}



