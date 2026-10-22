package org.max2ba.usernotificationservcei.utils;

import static org.jeasy.random.FieldPredicates.named;

import lombok.experimental.UtilityClass;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.validation.BeanValidationRandomizerRegistry;

@UtilityClass
public class RandomClassGenerator {

    private final EasyRandomParameters parameters = new EasyRandomParameters()
            .randomizerRegistry(new BeanValidationRandomizerRegistry())
            .randomize(named("id"), () -> null);

    private final EasyRandom easyRandom = new EasyRandom(parameters);

    public <T> T create(Class<T> type) {
        return easyRandom.nextObject(type);
    }
}
