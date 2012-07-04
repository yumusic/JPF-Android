package gov.nasa.jpf.android;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;

public class IntentEntry {

	String mAction;
	String mType;
	String mComponent;
	HashSet<String> mCategories;
	Bundle mExtras;

	public IntentEntry() {

	}

	public IntentEntry(Context packageContext, Class<?> cls) {
		mComponent = cls.getName();
	}

	public String getComponent() {
		return mComponent;
	}

	public final class ComponentName implements Cloneable,
			Comparable<ComponentName> {
		private final String mPackage;
		private final String mClass;

		/**
		 * Create a new component identifier.
		 * 
		 * @param pkg
		 *            The name of the package that the component exists in. Can
		 *            not be null.
		 * @param cls
		 *            The name of the class inside of <var>pkg</var> that
		 *            implements the component. Can not be null.
		 */
		public ComponentName(String pkg, String cls) {
			if (pkg == null)
				throw new NullPointerException("package name is null");
			if (cls == null)
				throw new NullPointerException("class name is null");
			mPackage = pkg;
			mClass = cls;
		}

		/**
		 * Create a new component identifier from a Context and class name.
		 * 
		 * @param pkg
		 *            A Context for the package implementing the component, from
		 *            which the actual package name will be retrieved.
		 * @param cls
		 *            The name of the class inside of <var>pkg</var> that
		 *            implements the component.
		 */
		public ComponentName(Context pkg, String cls) {
			if (cls == null)
				throw new NullPointerException("class name is null");
			mPackage = pkg.getPackageName();
			mClass = cls;
		}

		/**
		 * Create a new component identifier from a Context and Class object.
		 * 
		 * @param pkg
		 *            A Context for the package implementing the component, from
		 *            which the actual package name will be retrieved.
		 * @param cls
		 *            The Class object of the desired component, from which the
		 *            actual class name will be retrieved.
		 */
		public ComponentName(Context pkg, Class<?> cls) {
			mPackage = pkg.getPackageName();
			mClass = cls.getName();
		}

		public ComponentName clone() {
			return new ComponentName(mPackage, mClass);
		}

		/**
		 * Return the package name of this component.
		 */
		public String getPackageName() {
			return mPackage;
		}

		/**
		 * Return the class name of this component.
		 */
		public String getClassName() {
			return mClass;
		}

		/**
		 * Return the class name, either fully qualified or in a shortened form
		 * (with a leading '.') if it is a suffix of the package.
		 */
		public String getShortClassName() {
			if (mClass.startsWith(mPackage)) {
				int PN = mPackage.length();
				int CN = mClass.length();
				if (CN > PN && mClass.charAt(PN) == '.') {
					return mClass.substring(PN, CN);
				}
			}
			return mClass;
		}

		/**
		 * Return a String that unambiguously describes both the package and
		 * class names contained in the ComponentName. You can later recover the
		 * ComponentName from this string through
		 * {@link #unflattenFromString(String)}.
		 * 
		 * @return Returns a new String holding the package and class names.
		 *         This is represented as the package name, concatenated with a
		 *         '/' and then the class name.
		 * 
		 * @see #unflattenFromString(String)
		 */
		public String flattenToString() {
			return mPackage + "/" + mClass;
		}

		/**
		 * The same as {@link #flattenToString()}, but abbreviates the class
		 * name if it is a suffix of the package. The result can still be used
		 * with {@link #unflattenFromString(String)}.
		 * 
		 * @return Returns a new String holding the package and class names.
		 *         This is represented as the package name, concatenated with a
		 *         '/' and then the class name.
		 * 
		 * @see #unflattenFromString(String)
		 */
		public String flattenToShortString() {
			return mPackage + "/" + getShortClassName();
		}

		/**
		 * Return string representation of this class without the class's name
		 * as a prefix.
		 */
		public String toShortString() {
			return "{" + mPackage + "/" + mClass + "}";
		}

		@Override
		public String toString() {
			return "ComponentInfo{" + mPackage + "/" + mClass + "}";
		}

		@Override
		public boolean equals(Object obj) {
			try {
				if (obj != null) {
					ComponentName other = (ComponentName) obj;
					// Note: no null checks, because mPackage and mClass can
					// never be null.
					return mPackage.equals(other.mPackage)
							&& mClass.equals(other.mClass);
				}
			} catch (ClassCastException e) {
			}
			return false;
		}

		@Override
		public int hashCode() {
			return mPackage.hashCode() + mClass.hashCode();
		}

		public int compareTo(ComponentName that) {
			int v;
			v = this.mPackage.compareTo(that.mPackage);
			if (v != 0) {
				return v;
			}
			return this.mClass.compareTo(that.mClass);
		}

		public int describeContents() {
			return 0;
		}
	}

	/**
	 * Add a new category to the intent. Categories provide additional detail
	 * about the action the intent is perform. When resolving an intent, only
	 * activities that provide <em>all</em> of the requested categories will be
	 * used.
	 * 
	 * @param category
	 *            The desired category. This can be either one of the predefined
	 *            IntentEntry categories, or a custom category in your own
	 *            namespace.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #hasCategory
	 * @see #removeCategory
	 */
	public IntentEntry addCategory(String category) {
		if (mCategories == null) {
			mCategories = new HashSet<String>();
		}
		mCategories.add(category.intern());
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The boolean data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getBooleanExtra(String, boolean)
	 */
	public IntentEntry putExtra(String name, boolean value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putBoolean(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The byte data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getByteExtra(String, byte)
	 */
	public IntentEntry putExtra(String name, byte value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putByte(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The char data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getCharExtra(String, char)
	 */
	public IntentEntry putExtra(String name, char value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putChar(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The short data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getShortExtra(String, short)
	 */
	public IntentEntry putExtra(String name, short value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putShort(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The integer data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getIntExtra(String, int)
	 */
	public IntentEntry putExtra(String name, int value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putInt(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The long data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getLongExtra(String, long)
	 */
	public IntentEntry putExtra(String name, long value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putLong(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The float data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getFloatExtra(String, float)
	 */
	public IntentEntry putExtra(String name, float value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putFloat(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The double data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getDoubleExtra(String, double)
	 */
	public IntentEntry putExtra(String name, double value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putDouble(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The String data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getStringExtra(String)
	 */
	public IntentEntry putExtra(String name, String value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putString(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The CharSequence data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getCharSequenceExtra(String)
	 */
	public IntentEntry putExtra(String name, CharSequence value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putCharSequence(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The ArrayList<Integer> data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getIntegerArrayListExtra(String)
	 */
	public IntentEntry putIntegerArrayListExtra(String name,
			ArrayList<Integer> value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putIntegerArrayList(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The ArrayList<String> data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getStringArrayListExtra(String)
	 */
	public IntentEntry putStringArrayListExtra(String name,
			ArrayList<String> value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putStringArrayList(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The boolean array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getBooleanArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, boolean[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putBooleanArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The byte array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getByteArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, byte[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putByteArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The short array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getShortArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, short[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putShortArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The char array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getCharArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, char[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putCharArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The int array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getIntArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, int[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putIntArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The byte array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getLongArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, long[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putLongArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The float array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getFloatArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, float[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putFloatArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The double array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getDoubleArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, double[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putDoubleArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The String array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getStringArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, String[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putStringArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The CharSequence array data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getCharSequenceArrayExtra(String)
	 */
	public IntentEntry putExtra(String name, CharSequence[] value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putCharSequenceArray(name, value);
		return this;
	}

	/**
	 * Add extended data to the intent. The name must include a package prefix,
	 * for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param name
	 *            The name of the extra data, with package prefix.
	 * @param value
	 *            The Bundle data value.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #putExtras
	 * @see #removeExtra
	 * @see #getBundleExtra(String)
	 */
	public IntentEntry putExtra(String name, Bundle value) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putBundle(name, value);
		return this;
	}

	/**
	 * Copy all extras in 'src' in to this intent.
	 * 
	 * @param src
	 *            Contains the extras to copy.
	 * 
	 * @see #putExtra
	 */
	public IntentEntry putExtras(IntentEntry src) {
		if (src.mExtras != null) {
			if (mExtras == null) {
				mExtras = new Bundle(src.mExtras);
			} else {
				mExtras.putAll(src.mExtras);
			}
		}
		return this;
	}

	/**
	 * Add a set of extended data to the intent. The keys must include a package
	 * prefix, for example the app com.android.contacts would use names like
	 * "com.android.contacts.ShowAll".
	 * 
	 * @param extras
	 *            The Bundle of extras to add to this intent.
	 * 
	 * @see #putExtra
	 * @see #removeExtra
	 */
	public IntentEntry putExtras(Bundle extras) {
		if (mExtras == null) {
			mExtras = new Bundle();
		}
		mExtras.putAll(extras);
		return this;
	}

	/**
	 * Completely replace the extras in the IntentEntry with the extras in the
	 * given IntentEntry.
	 * 
	 * @param src
	 *            The exact extras contained in this IntentEntry are copied into
	 *            the target intent, replacing any that were previously there.
	 */
	public IntentEntry replaceExtras(IntentEntry src) {
		mExtras = src.mExtras != null ? new Bundle(src.mExtras) : null;
		return this;
	}

	/**
	 * Completely replace the extras in the IntentEntry with the given Bundle of
	 * extras.
	 * 
	 * @param extras
	 *            The new set of extras in the IntentEntry, or null to erase all
	 *            extras.
	 */
	public IntentEntry replaceExtras(Bundle extras) {
		mExtras = extras != null ? new Bundle(extras) : null;
		return this;
	}

	/**
	 * Remove extended data from the intent.
	 * 
	 * @see #putExtra
	 */
	public void removeExtra(String name) {
		if (mExtras != null) {
			mExtras.remove(name);
			if (mExtras.size() == 0) {
				mExtras = null;
			}
		}
	}

	/**
	 * (Usually optional) Explicitly set the component to handle the intent. If
	 * left with the default value of null, the system will determine the
	 * appropriate class to use based on the other fields (action, data, type,
	 * categories) in the IntentEntry. If this class is defined, the specified
	 * class will always be used regardless of the other fields. You should only
	 * set this value when you know you absolutely want a specific class to be
	 * used; otherwise it is better to let the system find the appropriate class
	 * so that you will respect the installed applications and user preferences.
	 * 
	 * @param component
	 *            The name of the application component to handle the intent, or
	 *            null to let the system find one for you.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #setClass
	 * @see #setClassName(Context, String)
	 * @see #setClassName(String, String)
	 * @see #getComponent
	 * @see #resolveActivity
	 */
	public void setComponent(String component) {
		System.out.println("setting component to: " + component);
		mComponent = component;
	}

	/**
	 * Set the general action to be performed.
	 * 
	 * @param action
	 *            An action name, such as ACTION_VIEW. Application-specific
	 *            actions should be prefixed with the vendor's package name.
	 * 
	 * @return Returns the same IntentEntry object, for chaining multiple calls
	 *         into a single statement.
	 * 
	 * @see #getAction
	 */
	public IntentEntry setAction(String action) {
		mAction = action != null ? action.intern() : null;
		return this;
	}

	/**
	 * Retrieve the general action to be performed, such as {@link #ACTION_VIEW}
	 * . The action describes the general way the rest of the information in the
	 * intent should be interpreted -- most importantly, what to do with the
	 * data returned by {@link #getData}.
	 * 
	 * @return The action of this intent or null if none is specified.
	 * 
	 * @see #setAction
	 */
	public String getAction() {
		return mAction;
	}

	/**
	 * Return the set of all categories in the intent. If there are no
	 * categories, returns NULL.
	 * 
	 * @return The set of categories you can examine. Do not modify!
	 * 
	 * @see #hasCategory
	 * @see #addCategory
	 */
	public Set<String> getCategories() {
		return mCategories;
	}

}
