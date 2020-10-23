package com.ibm.cohort.engine.test;
import com.ibm.cohort.engine.CqlEngineWrapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class TestWrapper {
	private CqlEngineWrapper engine;
	private ByteArrayOutputStream byteStream;
	private PrintStream ps;
	public TestWrapper()
    {   
        engine = new CqlEngineWrapper();
    }
	/**
	 * @param d 'config/remote-hapi-fhir.json'
	 * @param t 'config/remote-hapi-fhir.json'
	 * @param f 'src/test/resources/cql/basic'
	 * @param l 'Test'
	 * @param c '1235008
	 * @return
	 * @throws Exception
	 */
    public String warm(String d, String t, String f, String l, String c) throws Exception
    {
        String[] args = new String[] {"-d", d, "-t", t, "-f", f, "-l", l, "-c", c};
        byteStream = new ByteArrayOutputStream();
        ps = new PrintStream(byteStream, true, StandardCharsets.UTF_8.name());
        engine.runWithArgs(args, ps);
        String out =  byteStream.toString(StandardCharsets.UTF_8.name());
        byteStream.reset(); // Wipe the stream so the next run will have clean output.
        return out;
    }
    public String execute(String library, String contextId) throws Exception
    {
    	List<String> contextIds = new LinkedList<String>();
    	contextIds.add(contextId);
    	engine.evaluate(library, null, null, null, contextIds, (contextI, expression, result) -> {
			ps.println(String.format("Expression: %s, Context: %s, Result: %s", expression, contextI,
					(result != null) ? String.format("%s", result.toString()) : "null"));
		});
    	String out =  byteStream.toString(StandardCharsets.UTF_8.name());
        byteStream.reset(); // Wipe the stream so the next run will have clean output.
        return out;
    }
}
