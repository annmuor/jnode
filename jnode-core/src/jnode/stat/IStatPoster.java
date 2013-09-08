package jnode.stat;

import jnode.stat.threads.StatPoster;

public interface IStatPoster {
	public void init(StatPoster poster);

	public String getSubject();

	public String getText();
}
