package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.ElementAccess;
import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.instructions.ConditionalJump;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.instructions.IncrementCounter;
import r.compiler.ir.tac.operand.CmpGE;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.PrimitiveCall;
import r.compiler.ir.tac.operand.Temp;
import r.compiler.ir.tac.operand.Variable;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Symbol;

import com.google.common.collect.Lists;

public class ForTranslator extends FunctionCallTranslator {
  
  @Override
  public Symbol getName() {
    return Symbol.get("for");
  }

  @Override
  public Operand translateToExpression(TacFactory builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
    
    return new Constant(Null.INSTANCE);
  }


  @Override
  public void addStatement(TacFactory builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
  }
 
  private void addForLoop(TacFactory factory, TranslationContext context, FunctionCall call) {
    
    Symbol symbol = call.getArgument(0);
    Temp counter = factory.newTemp();
    Temp length = factory.newTemp();
    
    Variable elementVariable = new Variable(symbol);
    
    Operand vector = 
        factory.translateSimpleExpression(context, call.getArgument(1));
    
    SEXP body = call.getArgument(2);

    Label bodyLabel = factory.newLabel();
    Label counterLabel = factory.newLabel();
    Label nextLabel = factory.newLabel();
    Label exitLabel = factory.newLabel();
       
    // initialize the counter
    factory.addStatement(new Assignment(counter, new Constant(0)));
    factory.addStatement(new Assignment(length, 
        new PrimitiveCall(Symbol.get("length"), Lists.newArrayList((Operand)vector))));
    factory.addStatement(new GotoStatement(counterLabel));
    
    // start the body here
    factory.addLabel(bodyLabel);
    factory.addStatement(new Assignment(elementVariable, new ElementAccess(vector, counter)));

    LoopContext loopContext = new LoopContext(nextLabel, exitLabel);
    factory.translateStatements(loopContext, body);
    
    // increment the counter
    factory.addLabel(nextLabel);
    factory.addStatement(new IncrementCounter(counter));
    
    // check the counter and potentially loop
    factory.addLabel(counterLabel);
    factory.addStatement(new ConditionalJump(new CmpGE(counter, length), bodyLabel));
    factory.addLabel(exitLabel);
  }  
}