package com.example.SindhiProgrammingLanguage.PresentationLayer;

import com.example.SindhiProgrammingLanguage.ServiceLayer.SindhiProgrammingInterpreterServices;
import com.example.SindhiProgrammingLanguage.SindhiCodeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/SindhiLanguage")
public class SindhiProgrammingController {

    @Autowired
    SindhiProgrammingInterpreterServices sindhiProgrammingInterpreterServices;

    @PostMapping("/v1")
    public ResponseEntity<String> SindhiController(@RequestBody SindhiCodeRequest Code){
        try {
            String result = sindhiProgrammingInterpreterServices.execute(Code.getSindhiCode());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

}

