package com.bbinnick.gamestack.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.bbinnick.gamestack.model.Game;

@Component
public class GameValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Game.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Game game = (Game) target;

        if (game.getTitle() == null || game.getTitle().isEmpty()) {
            errors.rejectValue("title", "NotEmpty.game.title");
        }

        // Add additional validation for other fields as needed
        /*
        if (game.getReleaseDate() == null) {
            errors.rejectValue("releaseDate", "NotNull.game.releaseDate");
        }
		*/
        // Any additional custom validation can go here
    }
}

