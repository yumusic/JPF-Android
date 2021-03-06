//
// Copyright (C) 2014 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//

package gov.nasa.jpf.vm;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.event.CheckEvent;
import gov.nasa.jpf.util.event.Event;
import gov.nasa.jpf.util.event.EventChoiceGenerator;
import gov.nasa.jpf.util.event.EventTree;
import gov.nasa.jpf.util.event.NoEvent;

/**
 * native peer for EventProducer
 */
public class JPF_gov_nasa_jpf_EventProducer extends NativePeer {

  static final String CG_NAME = "processNextEvent";
  
  public static JPFLogger log = JPF.getLogger("event");
  
  protected EventTree eventTree;
  protected Event event;
  
  public JPF_gov_nasa_jpf_EventProducer (Config config){
    eventTree = config.getEssentialInstance(EventTree.CONFIG_KEY, EventTree.class);
    logger.info("event tree generated by: ", eventTree.getClass().getName());
  }
  
  //--- those can be used by subclasses to add additional processing steps during processNextEvent
  
  /**
   * override this if the event processing results in direct calls, i.e. there is no change in the model method calling
   * processNextEvent
   */
  protected boolean hasReturnedFormDirectCall (MJIEnv env,  int objRef){
    return false;
  }
  
  /**
   * nothing here, to be overridden in case processing has to happen in the native peer
   * this is only called from within generateNextEvent()
   */
  protected void processEvent (MJIEnv env, int objRef){
  }
  
  /**
   * evaluate a pseudo event that checks properties 
   */
  protected boolean checkEvent (MJIEnv env, int objRef){
    return ((CheckEvent)event).check(env);
  }
  
  /**
   * nothing here, to be overridden by subclasses that have to force states, modify the CG on the fly etc.
   */
  protected EventChoiceGenerator processNextCG (MJIEnv env, int objRef, EventChoiceGenerator cg){
    return cg;
  }
  
  /**
   * this is our main purpose in life - processing events
   * 
   * @returns true if there was another event, false if there isn't any event left on this path
   */
  @MJI
  public boolean processNextEvent____Z (MJIEnv env, int objRef){
    ThreadInfo ti = env.getThreadInfo();
    
    if (hasReturnedFormDirectCall( env, objRef)){
      return true;
    }
    
    SystemState ss = env.getSystemState();
    EventChoiceGenerator cg;

    event = null;
    
    if (!ti.isFirstStepInsn()){      
      EventChoiceGenerator cgPrev = ss.getLastChoiceGeneratorOfType(EventChoiceGenerator.class);
      if (cgPrev != null){
        cg = cgPrev.getSuccessor(CG_NAME);        
      } else {
        cg = new EventChoiceGenerator( CG_NAME, eventTree.getRoot());
      }
      
      if ((cg = processNextCG(env, objRef, cg)) != null){
        if (log.isInfoLogged()){
          log.info("next event generator: ", cg.toString());
        }
        ss.setNextChoiceGenerator(cg);
        env.repeatInvocation();
        return true; // does not matter
        
      } else {
        log.info("no more events");        
        return false;
      }
      
    } else { // re-execution
      cg = ss.getCurrentChoiceGenerator(CG_NAME, EventChoiceGenerator.class);
      event = cg.getNextChoice();
      
      if (event != null) {
        if (!(event instanceof NoEvent)) {
          if (event instanceof CheckEvent) {
            CheckEvent ce = (CheckEvent) event;
            if (log.isInfoLogged()) {
              log.info("checking: ", ce.getExpression());
            }
            if (!checkEvent(env, objRef)) {
              env.throwAssertion("checking " + ce.getExpression() + " failed");
            }

          } else {
            if (log.isInfoLogged()) {
              log.info("processing event: ", event.toString());
            }
            processEvent(env, objRef);
          }
        }

        return true;
        
      } else {
        return false;
      }
    }
  }
  
  @MJI(noOrphanWarning=true)
  public int getEventName____Ljava_lang_String_2 (MJIEnv env, int objRef){
    if (event != null){
      return env.newString( event.getName());
    } else {
      return MJIEnv.NULL;
    }
  }

  //--- for testing and debugging purposes (requires special EventTree implementations e.g. derived from TestEventTree)
  // <2do> should be moved to subclass
  
  @MJI(noOrphanWarning=true)
  public boolean checkPath____Z (MJIEnv env, int objRef){
    SystemState ss = env.getSystemState();
    EventChoiceGenerator cg = ss.getLastChoiceGeneratorOfType(EventChoiceGenerator.class);

    if (cg != null){
      Event lastEvent = cg.getNextChoice();      
      if (eventTree.checkPath(lastEvent)){
        return true;
      } else {
        log.warning("trace check for event ", lastEvent.toString(), " failed");
        return false;
      }
      
    } else {
      return false; // there should have been one
    }
  }
  
  @MJI(noOrphanWarning=true)
  public boolean isCompletelyCovered____Z (MJIEnv env, int objRef){
    return eventTree.isCompletelyCovered();
  }
}
