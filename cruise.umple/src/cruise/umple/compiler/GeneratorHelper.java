/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.12.0.352 modeling language!*/

package cruise.umple.compiler;
import cruise.umple.util.*;
import java.util.*;

public class GeneratorHelper
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public GeneratorHelper()
  {}

  //------------------------
  // INTERFACE
  //------------------------

  public void delete()
  {}

  //------------------------
  // DEVELOPER CODE - PROVIDED AS-IS
  //------------------------
  
  public static String toCode(List<CodeInjection> allCodeInjections)
  {
    String asCode = null;
    if (allCodeInjections != null)
    {
      for (CodeInjection inject : allCodeInjections)
      {
        if (asCode == null)
        {
          asCode = inject.getCode();
        }
        else
        {
          asCode = StringFormatter.format("{0}\n    {1}",asCode,inject.getCode());
        }
      }
    }
    return asCode;
  }
  
  public static void postpare(UmpleModel model)
  {
    for (UmpleClass aClass : model.getUmpleClasses())
    {
      postpare(aClass);
    }  
  }

  public static void prepareTraceItem(TraceItem traceItem, Map<String,String> lookups)
  {
    UmpleClass aClass = traceItem.getUmpleClass();
    Attribute attr = traceItem.getAttribute();
    if (attr != null)
    {
      String setMethod = lookups.get("setMethod");
      String code = lookups.get("attributeCode");
      CodeInjection set = new CodeInjection("after", setMethod, code);
      set.setIsInternal(true);
      aClass.addCodeInjection(set);
    }
  }

  public static void prepareNestedStateMachine(StateMachine sm, int concurrentIndex, Map<String,String> lookups)
  {
    String entryEventName = lookups.get("entryEventName");
    String exitEventName = lookups.get("exitEventName");
    String parentEntryActionCode = lookups.get("parentEntryActionCode");
    String parentExitActionCode = lookups.get("parentExitActionCode");
  
    State parentState = sm.getParentState();
    StateMachine firstSm = parentState.getNestedStateMachine(0);
    State nullState = sm.addState("Null",0);
    nullState.setIsInternal(true);

    if (sm.getStartState() != null)
    {
  
      if (concurrentIndex == 0)
      {
        Action parentExitAction = new Action(parentExitActionCode);
        parentExitAction.setIsInternal(true);
        parentExitAction.setActionType("exit");
        parentState.addAction(parentExitAction,0);
      }

      Event enterEvent = firstSm.findOrCreateEvent(entryEventName);
      enterEvent.setIsInternal(true);
      Transition enterTransition = new Transition(nullState,sm.getStartState());
      enterTransition.setIsInternal(true);
      enterTransition.setEvent(enterEvent);

      Event exitEvent = firstSm.findOrCreateEvent(exitEventName);
      exitEvent.setIsInternal(true);
  
      for (State state : sm.getStates())
      {
        if (state == nullState) { continue; }
        Transition exitTransition = state.addTransition(nullState,0);
        exitTransition.setIsInternal(true);
        exitTransition.setEvent(exitEvent);
      }

      Action parentEntryAction = new Action(parentEntryActionCode);
      parentEntryAction.setActionType("entry");
      parentEntryAction.setIsInternal(true);
      parentState.addAction(parentEntryAction); 
    }  
  }

  private static void postpare(UmpleClass aClass)
  {
    int maxIndex = aClass.numberOfCodeInjections() - 1;
    for (int i=maxIndex; i>=0; i--)
    {
      CodeInjection ci = aClass.getCodeInjection(i);
      if (ci.getIsInternal())
      {
        aClass.removeCodeInjection(ci);
      }
    }
    
    maxIndex = aClass.numberOfDepends() - 1;
    for (int i=maxIndex; i>=0; i--)
    {
      Depend d = aClass.getDepend(i);
      if (d.getIsInternal())
      {
        aClass.removeDepend(d);
      }      
    }  

    List<State> shouldDelete = new ArrayList<State>();
    for (StateMachine sm : aClass.getAllStateMachines())
    {
      postpareInternalStates(sm,shouldDelete);
    }

    // Remove all internally created actions
    for (StateMachine sm : aClass.getAllStateMachines())
    {
      for (State s : sm.getStates())
      {
        for (int i=s.numberOfTransitions()-1; i>=0; i--)
        {
          Transition t = s.getTransition(i);
          if (t.getIsInternal())
          {
            t.delete();
          }
        }

        for (int i=s.numberOfActions()-1; i>=0; i--)
        {
          Action a = s.getAction(i);
          if (a.getIsInternal())
          {
            s.removeAction(a);
          }
        }
      }
    }
    
    for (int i=shouldDelete.size()-1; i>=0; i--)
    {
      State s = shouldDelete.get(i);
      s.delete();
    }
    
  }
  private static void postpareInternalStates(StateMachine sm, List<State> shouldDelete)
  {
    for (int i=sm.numberOfStates() - 1; i >= 0; i--)
    {
      State s = sm.getState(i);
      if (s.getIsInternal())
      {
        shouldDelete.add(s);
      }       
    }
  }
}