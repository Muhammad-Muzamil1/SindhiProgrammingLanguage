package com.example.SindhiProgrammingLanguage.ServiceLayer;

import com.example.SindhiProgrammingLanguage.Compiler.SindhiInterpreter;
import com.example.SindhiProgrammingLanguage.Compiler.SindhiLexer;
import com.example.SindhiProgrammingLanguage.Compiler.SindhiToken;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.*;

@Service
public class SindhiProgrammingInterpreterServices {

    private static final ExecutorService executor = Executors.newCachedThreadPool(); // allows concurrent users

    public String execute(String code) throws Exception {
        Future<String> future = executor.submit(() -> runInterpreter(code));

        try {
            // Wait max 3 seconds for code to finish
            return future.get(8, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // Interrupt the thread if stuck
            return "⚠️ غلطي: ڪوڊ جي عمل کي وقت ختم ٿي ويو. ممڪن آهي ته انفي نائيٽ لوپ يا تمام ڊگهو ڪوڊ هجي.";
        } catch (ExecutionException e) {
            return "❌ رن ٽائيم غلطي: " + e.getCause().getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "❌ عمل غير متوقع طور تي روڪيو ويو.";
        }
    }

    private String runInterpreter(String code) throws Exception {
        code = code.replace("\r\n", "\n").replace('\r', '\n');
        SindhiLexer lexer = new SindhiLexer();
        List<SindhiToken> tokens = lexer.tokenize(code);
        SindhiInterpreter interpreter = new SindhiInterpreter(tokens);
        return interpreter.interpret();
    }
}
