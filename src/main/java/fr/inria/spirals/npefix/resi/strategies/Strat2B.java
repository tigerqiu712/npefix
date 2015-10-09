package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.ExceptionStack;
import fr.inria.spirals.npefix.resi.ForceReturn;
import fr.inria.spirals.npefix.resi.Strategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * a=new A
 * @author bcornu
 *
 */
public class Strat2B extends Strategy{

	public <T> T isCalled(T o, Class<?> clazz) {
		if (o == null) {
			if (ExceptionStack.isStoppable(NullPointerException.class)) {
				return null;
			}
			if(clazz.isPrimitive()){
				o = initPrimitive(clazz);
				return o;
			}
			if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) ) {
				clazz = getImplForInterface(clazz);
				if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) ) {
					throw new AbnormalExecutionError("missing interface " + clazz);
				}
			}
			try {
				o = (T) clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				try{
					for (Constructor constructor : clazz.getConstructors()) {
						try{
							Class[] types = constructor.getParameterTypes();
							Object[] params = new Object[types.length];
							for (int i = 0; i < types.length; i++) {
								try{
									if(types[i].equals(clazz))
										throw new ForceReturn();
									else
										params[i] = init(types[i]);
								}catch (Throwable t){
									t.printStackTrace();
								}
							}
							o = (T) constructor.newInstance(params);
							return (T) o;
						}catch (Throwable t){
							t.printStackTrace();
						}
					}
				} catch (Throwable t){
					t.printStackTrace();
				}
				System.err.println("cannot new instance "+clazz);
			}
		}
		return (T) o;
	}

	public boolean beforeDeref(Object called) {
		return true;
	}


	@Override
	public <T> T returned(Class<?> clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
