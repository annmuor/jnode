package jnode.ndl;

import java.util.Date;

import jnode.ftn.types.FtnAddress;
import junit.framework.TestCase;

import org.junit.Test;

public class NodelistIndexTest {

	@Test
	public void testNodelist() {
		NodelistIndex idx = NodelistScanner.getInstance().createIndex(
				NodelistIndexTest.class.getResourceAsStream("nodelist"),
				new Date().getTime());
		
		FtnAddress a = new FtnAddress("2:5020/848");
		FtnNdlAddress ndl = idx.exists(a);
		int binkpPort = ndl.getBinkpPort();
		String hostname = ndl.getInetHost();
		
		TestCase.assertNotNull(ndl);
		TestCase.assertNotNull(idx);
		TestCase.assertEquals(24554, binkpPort);
		TestCase.assertEquals("fidonode.in", hostname);
		
		a = new FtnAddress("2:5020/2141");
		ndl = idx.exists(a);
		binkpPort = ndl.getBinkpPort();
		hostname = ndl.getInetHost();
		
		TestCase.assertNotNull(ndl);
		TestCase.assertNotNull(idx);
		TestCase.assertEquals(24555, binkpPort);
		TestCase.assertEquals("vp.propush.ru", hostname);
		
	}
}
