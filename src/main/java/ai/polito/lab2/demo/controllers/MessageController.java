package ai.polito.lab2.demo.controllers;

import ai.polito.lab2.demo.Entity.*;
import ai.polito.lab2.demo.Repositories.UserRepo;
import ai.polito.lab2.demo.Service.*;
import ai.polito.lab2.demo.viewmodels.MessageVM;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.protocol.HTTP;
import org.aspectj.weaver.patterns.HasMemberTypePatternForPerThisMatching;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MessageController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;


    @ApiOperation("Endpoint per tutti i messaggi per uno user")
    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Richiesta mal formata"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Richiesta non permessa")
    })
    public ResponseEntity<ArrayList<MessageVM>> getMessages(@RequestParam (required = true) String username){
        if (username == null || username.equals("") || userService.getUserByUsername(username) == null)
            return new ResponseEntity("Richiesta malformata", HttpStatus.BAD_REQUEST);

        ArrayList<MessageVM> messages = new ArrayList<>();
        messages = messageService.getMessages(username);

        return new ResponseEntity(messages, HttpStatus.OK);
    }

    @ApiOperation("Endpoint per modificare lo stato di un messaggio")
    @RequestMapping(value = "/messages/{messageID}/edit/{read}", method = RequestMethod.PUT)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "No content"),
            @ApiResponse(code = 400, message = "Richiesta mal formata"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Richiesta non permessa")
    })
    public ResponseEntity readedMessage(@PathVariable String messageID, @PathVariable Boolean read){
            if (messageID == null || messageID.equals(""))
                return new ResponseEntity("Richiesta malformata", HttpStatus.BAD_REQUEST);

            if (messageService.findMessageByMessageID(new ObjectId(messageID)) == null)
                return new ResponseEntity("Messaggio non trovato", HttpStatus.NOT_FOUND);

            messageService.readedUpdated(new ObjectId(messageID), read);;
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN", "ROLE_MULE"})
    @ApiOperation("Endpoint per modificare lo stato di un turno")
    @RequestMapping(value = "/messages/{messageID}/{status}", method = RequestMethod.PUT)
    public ResponseEntity editStatus(@PathVariable final String messageID, @PathVariable String status) {

        if (messageID == null || messageID.equals(""))
            return  new ResponseEntity("Richiesta malformata", HttpStatus.BAD_REQUEST);

        if (messageService.findMessageByMessageID(new ObjectId(messageID)) == null)
            return new ResponseEntity("Messaggio non trovato", HttpStatus.NOT_FOUND);

        Message m = messageService.editStatus(new ObjectId(messageID), status);

        if ( m == null)
            return new ResponseEntity("Richiesta Malformata", HttpStatus.BAD_REQUEST);
        else
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @ApiOperation("Endpoint per cancellare un messaggio")
    @RequestMapping(value = "/messages/{messageID}", method = RequestMethod.DELETE)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "No content"),
            @ApiResponse(code = 400, message = "Richiesta mal formata"),
            @ApiResponse(code = 401, message = "Non autorizzato"),
            @ApiResponse(code = 403, message = "Richiesta non permessa"),
            @ApiResponse(code = 404, message = "Messaggio non trovato")
    })
    public ResponseEntity deleteChild(@PathVariable ObjectId messageID ) {

        if ( messageID == null)
            return new ResponseEntity("Richiesta mal formata", HttpStatus.BAD_REQUEST);

        if (messageService.findMessageByMessageID(messageID) == null)
            return new ResponseEntity("Messaggio non trovato", HttpStatus.NOT_FOUND);


        if (messageService.deleteByMessageID(messageID) == -1)
            return new ResponseEntity("Il messaggio non può essere cancellato, devi prima fornire una risposta", HttpStatus.BAD_REQUEST);
        else
            return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
