package jnode.robot;

import jnode.ftn.FtnMessage;

/**
 * 
 * @author kreon
 * 
 */
public interface IRobot {
	public void execute(FtnMessage fmsg) throws Exception;
}
