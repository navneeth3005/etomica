package etomica.virial.cluster2.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Virial2TestSuite {

  public static Test suite() {

    TestSuite suite = new TestSuite("Test for etomica.virial.cluster2.test");
    // $JUnit-BEGIN$
    // good 09-06-11 (fails for non-implemented tests)
    suite.addTestSuite(TestBitmapOfLongVector.class);
    // good 09-06-11 (fails for non-implemented tests)
    suite.addTestSuite(TestBitmapOfLong.class);
    // good 09-06-11
    suite.addTestSuite(TestSimpleProcessWrapper.class);
    // good 09-06-11
    suite.addTestSuite(TestNautyEdgesGenerator.class);
    // good 09-06-11
    suite.addTestSuite(TestNaiveEdgesGenerator.class);
    // $JUnit-END$
    return suite;
  }
}