/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.ftn.types;

import java.text.MessageFormat;

/**
 * 
 * @author kreon
 * 
 */
public class Ftn2D {
	private int net;
	private int node;

	public int getNet() {
		return net;
	}

	public void setNet(int net) {
		this.net = net;
	}

	public int getNode() {
		return node;
	}

	public void setNode(int node) {
		this.node = node;
	}

    public static Ftn2D fromString(String net, String node){
        int netInt;
        int nodeInt;
        try
        {
            netInt = Integer.parseInt(net);
            nodeInt = Integer.parseInt(node);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException(MessageFormat.format("fail create Ftn2D from string args [{0}, {1}]", net, node));
        }

        return new Ftn2D(netInt, nodeInt);
    }

	public Ftn2D(int net, int node) {
		super();
		this.net = net;
		this.node = node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + net;
		result = prime * result + node;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ftn2D other = (Ftn2D) obj;
		if (net != other.net)
			return false;
		if (node != other.node)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d/%d", net, node);
	}

}
