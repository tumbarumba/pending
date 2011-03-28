package net.ttsui.junit.rules.pending;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class PendingRuleSemantics {
    private Mockery context = new Mockery() {{ setImposteriser(ClassImposteriser.INSTANCE); }};
    private final Statement base = context.mock(Statement.class);
    private final FrameworkMethod frameworkMethod = context.mock(FrameworkMethod.class);
    
    @Test public void
    failingTestAnnotatedWithPendingImplemenationShouldPass() throws Throwable {
        final PendingImplementation annotation = context.mock(PendingImplementation.class);
        
        context.checking(new Expectations() {{
            oneOf(base).evaluate(); will(throwException(new AssertionError()));
            
            allowing(frameworkMethod).getAnnotation(PendingImplementation.class); will(returnValue(annotation));
        }});
        
        PendingRule rule = new PendingRule();
        rule.apply(base, frameworkMethod, null);
        
        context.assertIsSatisfied();
    }
    
    @Test(expected=AssertionError.class) public void
    passingTestAnnotatedWithPendingImplementationShouldFail() throws Throwable {
        final PendingImplementation annotation = context.mock(PendingImplementation.class);
        
        context.checking(new Expectations() {{
            allowing(base).evaluate();
            allowing(frameworkMethod).getAnnotation(PendingImplementation.class); will(returnValue(annotation));
        }});
        
        PendingRule rule = new PendingRule();
        rule.apply(base, frameworkMethod, null);
    }
    
    @Test public void
    returnsOriginalStatementForTestsNotAnnotatedWithPendingImplementation() {
        
        context.checking(new Expectations() {{
            oneOf(frameworkMethod).getAnnotation(PendingImplementation.class); will(returnValue(null));
            oneOf(frameworkMethod).getMethod(); will(returnValue(TestClassWithoutAnnotation.class.getDeclaredMethods()[0]));
        }});
        
        PendingRule rule = new PendingRule();
        Statement returnedStatement = rule.apply(base, frameworkMethod, null);
        
        assertThat(returnedStatement, is(base));
    }
    
    private class TestClassWithoutAnnotation {
        @SuppressWarnings("unused") public void testMethodWithoutAnnotation() { }
    }
}