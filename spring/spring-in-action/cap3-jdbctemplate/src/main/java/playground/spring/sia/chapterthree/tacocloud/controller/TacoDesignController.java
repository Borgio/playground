package playground.spring.sia.chapterthree.tacocloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import playground.spring.sia.chapterthree.tacocloud.model.Ingredient;
import playground.spring.sia.chapterthree.tacocloud.model.Taco;
import playground.spring.sia.chapterthree.tacocloud.persistence.IngredientRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/design")
public class TacoDesignController {

    private final IngredientRepository ingredientRepository;

    @Autowired
    public TacoDesignController(IngredientRepository ingredientRepository) {

        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping
    public String showDesignForm(Model model) {

        List<Ingredient> ingredients = new ArrayList<>();

        ingredientRepository.findAll().forEach(ingredients::add);

        Ingredient.Type[] types = Ingredient.Type.values();

        for(Ingredient.Type type: types) {

            model.addAttribute(type.toString().toLowerCase(), filterByType(ingredients, type));
        }

        model.addAttribute("taco", new Taco());

        return "design";
    }

    @PostMapping
    public String processTacoDesign(@Valid Taco taco, Errors errors) {

        if (errors.hasErrors()) {

            return "design";
        }

        // save the taco taco

        System.out.println("processing taco taco " + taco);

        return "redirect:/orders/current";
    }

    private List<Ingredient> filterByType(List<Ingredient> ingredients, Ingredient.Type type) {

        List<Ingredient> result = new ArrayList<>();

        for(Ingredient i: ingredients) {

            if (type.equals(i.getType())) {

                result.add(i);
            }
        }
        return result;
    }
}
