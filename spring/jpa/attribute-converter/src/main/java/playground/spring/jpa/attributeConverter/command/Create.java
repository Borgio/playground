package playground.spring.jpa.attributeConverter.command;

import playground.spring.jpa.attributeConverter.model.A;
import playground.spring.jpa.attributeConverter.repository.ARepository;

/**
 * Creates a record with the given name. The ID will be automatically generated.
 *
 * create Menlo Park
 */
public class Create implements Command {

    private String name;

    public Create(String commandLineWithoutInsert) {

        name = commandLineWithoutInsert.trim();

        if (name.isEmpty()) {

            throw new IllegalArgumentException("empty name");
        }
    }

    @Override
    public void execute(ARepository aRepository) {

        A a = new A();

        a.setName(name);

        aRepository.save(a);

        System.out.println("created " + a);
    }
}
