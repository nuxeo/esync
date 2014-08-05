package checker;

import org.reflections.Reflections;

import java.util.Set;

/**
 * Use reflection to get the list of checkers
 */
public class Discovery {

    static public Set<Class<? extends AbstractChecker>> getCheckersClass() {
        Reflections reflections = new Reflections("checker");
        Set<Class<? extends AbstractChecker>> checkers =
                reflections.getSubTypesOf(AbstractChecker.class);
        return checkers;
    }

}
