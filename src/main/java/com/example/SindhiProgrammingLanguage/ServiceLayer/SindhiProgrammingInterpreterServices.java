package com.example.SindhiProgrammingLanguage.ServiceLayer;

import com.example.SindhiProgrammingLanguage.Compiler.SindhiInterpreter;
import com.example.SindhiProgrammingLanguage.Compiler.SindhiLexer;
import com.example.SindhiProgrammingLanguage.Compiler.SindhiToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SindhiProgrammingInterpreterServices {

    public String execute(String sindhiCode) throws Exception {

        SindhiLexer lexer = new SindhiLexer();
        List<SindhiToken> tokens = lexer.tokenize(sindhiCode);
        for (SindhiToken token : tokens) {
            System.out.println(token);
        }
        SindhiInterpreter interpreter = new SindhiInterpreter(tokens);
        return interpreter.interpret();
    }
}
