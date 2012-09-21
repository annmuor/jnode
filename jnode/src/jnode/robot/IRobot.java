package jnode.robot;

import jnode.ftn.types.FtnMessage;

/**
 * 
 * @author kreon
 * 
 */
public interface IRobot {
	public void execute(FtnMessage fmsg) throws Exception;
}
