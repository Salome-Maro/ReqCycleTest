/*******************************************************************************
 *  Copyright (c) 2013 AtoS
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html *
 *  Contributors:
 *    Tristan Faure (AtoS) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.polarsys.reqcycle.dnd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.polarsys.reqcycle.uri.model.Reachable;

public class DNDReqCycle {
	public static List<Reachable> getReachables(byte[] data) {
		List<Reachable> uris = new ArrayList<Reachable>();
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInput in = new ObjectInputStream(bis);
			Object obj = in.readObject();
			if (obj instanceof Object[]) {
				for (Object child : (Object[]) obj) {
					if (child instanceof Reachable) {
						uris.add((Reachable) child);
					}
				}
			}
			bis.close();
			in.close();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return uris;
	}

}
