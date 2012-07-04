package gov.nasa.jpf.android;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.DirectCallStackFrame;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.ThreadInfo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;

import android.content.ComponentName;
import android.content.Intent;

/**
 * Models the Android ActivityManagerService. This Service is traditionally run
 * in the system process and is not part of the application process. That is why
 * it is modelled in native code. This class is not scheduling relevant. It is
 * necessary to model the communication between the system and the application.
 * For testing purposes this class only supports this one application and
 * assumes that for now no other applications are install
 * 
 * @see com.android.server.amn.ActivityManagerService
 * @see android.app.ActivityManagerProxy
 * @see android.app.ActivityManagerNative
 * 
 * @author Heila van der Merwe
 * 
 */
public class JPF_android_app_ActivityManagerProxy {
	static Logger log = JPF.getLogger("gov.nasa.jpf.android");

	/** ID for stack frames pushed by direct calls from the native code */
	private static final String UIACTION = "[UIAction]";

	/** Stores details of Intent objects variables used in the scripting file */
	private static HashMap<String, IntentEntry> intentMap = new HashMap<String, IntentEntry>();

	/**
	 * Retrieves the reference to an intent in the intentMap using its name
	 * specified by action.target. If no such intent exists, an Intent object is
	 * created and stored in the intentMap. It then sets the field of the intent
	 * (specified by action.getAction()) with value action.arguments[0] which is
	 * the value of the field to set..
	 * 
	 * @param env
	 * @param action
	 *            - action as read from the script file
	 */
	public static void setIntent(MJIEnv env, UIAction action) {
		log.fine("Setting " + action.toString());
		IntentEntry intent = intentMap.get(action.getTarget());

		if (intent == null) {
			intent = new IntentEntry();
			intentMap.put(action.target, intent);
		}

		// Use reflection to call the setter method on the Intent object
		@SuppressWarnings("unchecked")
		Class<IntentEntry> intentClass = (Class<IntentEntry>) intent.getClass();
		try {
			Method m = intentClass.getMethod(action.getAction(), String.class);
			m.invoke(intent, action.getArguments());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles actions related to application component such as starting and
	 * stopping activities and sending broadcasts etc.
	 * 
	 * @param env
	 * @param action
	 *            - the component action to handle
	 */
	static void handleComponentAction(MJIEnv env, UIAction action) {
		log.fine("Handling component action: " + action.toString());

		if (action.action.equals("startActivity")) {
			String intentName = (String) action.arguments[0];
			int intentref = getJPFIntent(env, intentName);
			startActivity(env, 0, intentref);

		} else if (action.action.equals("destroy")) {
			String intentName = (String) action.arguments[0];
			int intentref = getJPFIntent(env, intentName);
			startActivity(env, 0, intentref);

		}
	}

	/**
	 * Returns the reference to an the Intent object
	 * 
	 * @param env
	 * @param intent
	 * 
	 * @return
	 */
	public static int getJPFIntent(MJIEnv env, String intentName) {
		IntentEntry intent = intentMap.get(intentName);

		int intentRef = env.newObject("android.content.Intent");
		ElementInfo ei = env.getElementInfo(intentRef);
		int componentRef = env.newString(intent.getComponent());
		ei.setReferenceField("mComponent", componentRef);
		return intentRef;

	}

	/**
	 * Used by Activity to start an activity
	 * 
	 * @param env
	 * @param clsRef
	 * @param intentRef
	 */
	public static void startActivityProxy(MJIEnv env, int clsRef, int intentRef) {
		ThreadInfo ti = env.getThreadInfo();

		// so that the method is not called twice on return from making direct
		// call
		if (!ti.hasReturnedFromDirectCall(UIACTION)) {
			startActivity(env, clsRef, intentRef);
		}
	}

	/**
	 * This is always called to start an Activity
	 * 
	 * @param env
	 * @param intentRef
	 *            the reference to the intent starting the activity
	 */
	public static void startActivity(MJIEnv env, int clsRef, int intentRef) {
		// TODO pause activity currently active etc
		// Lookup the name of the activity to launch
		int activityNameRef = getActivity(env, intentRef);
		String activityName = env.getStringObject(activityNameRef);
		log.fine("Start activity " + activityName);

		// schedule launch of activity
		int appRef = JPF_android_app_ActivityThread.getApplicationRef();
		String methodName = "scheduleLaunchActivity(Ljava/lang/String;Landroid/content/Intent;)V";
		int[] args = { activityNameRef, intentRef };

		callMethod(env, appRef, methodName, args);

	}

	private static int getActivity(MJIEnv env, int intentRef) {
		// TODO lookup other fields through intent filters
		return env.getReferenceField(intentRef, "mComponent");
	}

	public static void stopActivity() {
		// TODO
		@SuppressWarnings("unused")
		String methodName = "scheduleDestroyActivity(Ljava/lang/String;)V";
	}

	/**
	 * Link to thew Application. Uses a direct call to call a method on
	 * ApplicationThread scheduling certain events to be handled by the
	 * application's main thread.
	 * 
	 * @param env
	 * @param methodName
	 *            the method signature of the method to call directly
	 * @param args
	 *            the arguments of the method
	 */
	private static void callMethod(MJIEnv env, int classRef, String methodName,
			int[] argsRefs) {

		ThreadInfo ti = env.getThreadInfo();
		MethodInfo mi = env.getClassInfo(classRef).getMethod(methodName, true);

		// Create direct call stub with identifier [UIAction]
		MethodInfo stub = mi.createDirectCallStub(UIACTION);
		DirectCallStackFrame frame = new DirectCallStackFrame(stub);

		// if the method is not static the reference to the object is pushed to
		// allow access to fields
		if (!mi.isStatic()) {
			frame.push(classRef, true);
		}

		// arguments for the method is pushed on the frame
		if (argsRefs != null) {
			for (int i = 0; i < argsRefs.length; i++) {
				frame.push(argsRefs[i], true);
			}
		}
		// frame is pushed to the execution thread
		ti.pushFrame(frame);
	}

}
