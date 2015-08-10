package com.supenta.flitchio.javadocfilter;

import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.Main;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/* Shamelessly stolen from
 * https://gist.github.com/benjchristensen/1410681
 * + Modified to inherit @hide tag in subclasses
 */
public class JavadocFilter {
    public static final String EXCLUDE_TAG = "@hide";

    public static void main(String[] args) {
        String name = JavadocFilter.class.getName();
        Main.execute(name, name, args);
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter) throws java.io.IOException {
        return Standard.validOptions(options, reporter);
    }

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    public static int optionLength(String option) {
        return Standard.optionLength(option);
    }

    public static boolean start(RootDoc root) throws java.io.IOException {
        return Standard.start((RootDoc) process(root, RootDoc.class));
    }

    private static boolean exclude(Doc doc) {
        // if (doc.name().contains("UnitTest")) {
        // return true;
        // }
        if (doc != null) {
            if (doc.tags(EXCLUDE_TAG).length > 0) {
                return true;
            } else if (doc instanceof ProgramElementDoc) {
                if (exclude(((ProgramElementDoc) doc).containingPackage())) {
                    return true;
                } else if (exclude(((ProgramElementDoc) doc).containingClass())) {
                    return true;
                }
            }
        }
        // nothing above found a reason to exclude
        return false;
    }

    private static Object process(Object obj, Class expect) {
        if (obj == null) {
            return null;
        }
        Class cls = obj.getClass();
        if (cls.getName().startsWith("com.sun.")) {
            return Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), new ExcludeHandler(obj));
        } else if (obj instanceof Object[]) {
            Class componentType = expect.getComponentType();
            Object[] array = (Object[]) obj;
            List<Object> list = new ArrayList<>(array.length);
            for (Object entry : array) {
                if ((entry instanceof Doc) && exclude((Doc) entry)) {
                    continue;
                }
                list.add(process(entry, componentType));
            }
            return list.toArray((Object[]) Array.newInstance(componentType, list.size()));
        } else {
            return obj;
        }
    }

    private static class ExcludeHandler implements InvocationHandler {
        private Object target;

        public ExcludeHandler(Object target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args != null) {
                String methodName = method.getName();
                if (methodName.equals("compareTo") || methodName.equals("equals") || methodName.equals("overrides") ||
                        methodName.equals("subclassOf")) {
                    args[0] = unwrap(args[0]);
                }
            }
            try {
                return process(method.invoke(target, args), method.getReturnType());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private Object unwrap(Object proxy) {
            if (proxy instanceof Proxy) {
                return ((ExcludeHandler) Proxy.getInvocationHandler(proxy)).target;
            }
            return proxy;
        }
    }

}
